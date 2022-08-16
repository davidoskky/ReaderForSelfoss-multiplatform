package bou.amine.apps.readerforselfossv2.repository

import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import bou.amine.apps.readerforselfossv2.utils.DateUtils
import bou.amine.apps.readerforselfossv2.utils.ItemType
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Repository(private val api: SelfossApi, private val apiDetails: ApiDetailsService) {
    val settings = Settings()

    var items = ArrayList<SelfossModel.Item>()

    var baseUrl = apiDetails.getBaseUrl()
    lateinit var dateUtils: DateUtils

    var displayedItems = ItemType.UNREAD

    var tagFilter: SelfossModel.Tag? = null
    var sourceFilter: SelfossModel.Source? = null
    var searchFilter: String? = null

    var itemsCaching = settings.getBoolean("items_caching", false)

    var apiMajorVersion = 0
    var badgeUnread = 0
    set(value) {field = if (value < 0) { 0 } else { value } }
    var badgeAll = 0
    set(value) {field = if (value < 0) { 0 } else { value } }
    var badgeStarred = 0
    set(value) {field = if (value < 0) { 0 } else { value } }

    init {
        // TODO: Dispatchers.IO not available in KMM, an alternative solution should be found
        CoroutineScope(Dispatchers.Main).launch {
            updateApiVersion()
            dateUtils = DateUtils(apiMajorVersion)
            reloadBadges()
        }
    }

    suspend fun getNewerItems(): ArrayList<SelfossModel.Item> {
        // TODO: Check connectivity, use the updatedSince parameter
        val fetchedItems = api.getItems(displayedItems.type,
            settings.getString("prefer_api_items_number", "200").toInt(),
            offset = 0,
            tagFilter?.tag,
            sourceFilter?.id?.toLong(),
            searchFilter,
            null)

        if (fetchedItems != null) {
            items = ArrayList(fetchedItems)
        }
        return items
    }

    suspend fun getOlderItems(): ArrayList<SelfossModel.Item> {
        // TODO: Check connectivity
        val offset = items.size
        val fetchedItems = api.getItems(displayedItems.type,
            settings.getString("prefer_api_items_number", "200").toInt(),
            offset,
            tagFilter?.tag,
            sourceFilter?.id?.toLong(),
            searchFilter,
            null)

        if (fetchedItems != null) {
            appendItems(fetchedItems)
        }
        return items
    }

    suspend fun allItems(itemType: ItemType): List<SelfossModel.Item>? =
        api.getItems(itemType.type, 200, 0, tagFilter?.tag, sourceFilter?.id?.toLong(), searchFilter, null)

    private fun appendItems(fetchedItems: List<SelfossModel.Item>) {
        // TODO: Store in DB if enabled by user
        val fetchedIDS = fetchedItems.map { it.id }
        val tmpItems = ArrayList(items.filterNot { it.id in fetchedIDS })
        tmpItems.addAll(fetchedItems)
        sortItems(tmpItems)
        items = tmpItems
    }

    private fun sortItems(items: ArrayList<SelfossModel.Item>) {
        items.sortByDescending { dateUtils.parseDate(it.datetime) }
    }

    suspend fun reloadBadges(): Boolean {
        // TODO: Check connectivity, calculate from DB
        var success = false
        val response = api.stats()
        if (response != null) {
            badgeUnread = response.unread
            badgeAll = response.total
            badgeStarred = response.starred
            success = true
        }
        return success
    }

    suspend fun getTags(): List<SelfossModel.Tag>? {
        // TODO: Check success, store in DB
        return api.tags()
    }

    suspend fun getSpouts(): Map<String, SelfossModel.Spout>? {
        // TODO: Check success, store in DB
        return api.spouts()
    }

    suspend fun getSources(): ArrayList<SelfossModel.Source>? {
        // TODO: Check success
        return api.sources()
    }

    suspend fun markAsRead(id: Int): Boolean {
        // TODO: Check internet connection
        val success = api.markAsRead(id.toString())?.isSuccess == true

        if (success) {
            markAsReadLocally(items.first {it.id == id})
        }
        return success
    }

    suspend fun unmarkAsRead(id: Int): Boolean {
        // TODO: Check internet connection
        val success = api.unmarkAsRead(id.toString())?.isSuccess == true

        if (success) {
            unmarkAsReadLocally(items.first {it.id == id})
        }
        return success
    }

    suspend fun starr(id: Int): Boolean {
        // TODO: Check success, store in DB
        val success = api.starr(id.toString())?.isSuccess == true

        if (success) {
            starrLocally(items.first {it.id == id})
        }
        return success
    }

    suspend fun unstarr(id: Int): Boolean {
        // TODO: Check internet connection
        val success = api.unstarr(id.toString())?.isSuccess == true

        if (success) {
            unstarrLocally(items.first {it.id == id})
        }
        return success
    }

    suspend fun markAllAsRead(ids: List<Int>): Boolean {
        // TODO: Check Internet connectivity, store in DB

        val success = api.markAllAsRead(ids.map { it.toString() })?.isSuccess == true

        if (success) {
            val itemsToMark = items.filter { it.id in ids }
            for (item in itemsToMark) {
                markAsReadLocally(item)
            }
        }
        return success
    }

    private fun markAsReadLocally(item: SelfossModel.Item) {
        // TODO: Mark also in the database
        if (item.unread) {
            item.unread = false
            badgeUnread -= 1
        }
    }

    private fun unmarkAsReadLocally(item: SelfossModel.Item) {
        // TODO: Mark also in the database
        if (!item.unread) {
            item.unread = true
            badgeUnread += 1
        }
    }

    private fun starrLocally(item: SelfossModel.Item) {
        // TODO: Mark also in the database
        if (!item.starred) {
            item.starred = true
            badgeStarred += 1
        }
    }

    private fun unstarrLocally(item: SelfossModel.Item) {
        // TODO: Mark also in the database
        if (item.starred) {
            item.starred = false
            badgeStarred -= 1
        }
    }

    suspend fun createSource(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String
    ): Boolean {
        // TODO: Check connectivity
        val response = api.createSourceForVersion(
            title,
            url,
            spout,
            tags,
            filter,
            apiMajorVersion
        )

        return response != null
    }

    suspend fun deleteSource(id: Int): Boolean {
        // TODO: Check connectivity, store in DB
        var success = false
        val response = api.deleteSource(id)
        if (response != null) {
            success = response.isSuccess
        }

        return success
    }

    suspend fun updateRemote(): Boolean {
        // TODO: Handle connectivity issues
        val response = api.update()
        return response?.isSuccess ?: false
    }

    suspend fun login(): Boolean {
        var result = false
        try {
            val response = api.login()
            if (response != null && response.isSuccess) {
                result = true
            }
        } catch (cause: Throwable) {
            Napier.e(cause.stackTraceToString(),tag = "RepositoryImpl.updateRemote")
        }
        return result
    }

    fun refreshLoginInformation(url: String, login: String, password: String,
                                         httpLogin: String, httpPassword: String,
                                         isSelfSignedCert: Boolean) {
        settings.putString("url", url)
        settings.putString("login", login)
        settings.putString("httpUserName", httpLogin)
        settings.putString("password", password)
        settings.putString("httpPassword", httpPassword)
        settings.putBoolean("isSelfSignedCert", isSelfSignedCert)
        baseUrl = url
        api.refreshLoginInformation()
    }

    private suspend fun updateApiVersion() {
        // TODO: Handle connectivity issues
        val fetchedVersion = api.version()
        if (fetchedVersion != null) {
            apiMajorVersion = fetchedVersion.getApiMajorVersion()
            settings.putInt("apiVersionMajor", apiMajorVersion)
        } else {
            apiMajorVersion = settings.getInt("apiVersionMajor", 0)
        }
    }

    // TODO: Handle offline actions
}