package bou.amine.apps.readerforselfossv2.repository

import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import bou.amine.apps.readerforselfossv2.utils.DateUtils
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RepositoryImpl(private val api: SelfossApi, private val apiDetails: ApiDetailsService) : Repository {
    val settings = Settings()

    override var items = ArrayList<SelfossModel.Item>()
    get() { return ArrayList(field) }
    set(value) { field = ArrayList(value) }

    override var baseUrl = apiDetails.getBaseUrl()

    override var displayedItems = "unread"
        set(value) {
            field = when (value) {
                "all" -> "all"
                "unread" -> "unread"
                "read" -> "read"
                "starred" -> "starred"
                else -> "all"
            }
        }

    override var tagFilter: SelfossModel.Tag? = null
    override var sourceFilter: SelfossModel.Source? = null
    override var searchFilter: String? = null

    override var itemsCaching = settings.getBoolean("items_caching", false)

    override var apiMajorVersion = 0
    override var badgeUnread = 0
    set(value) {field = if (value < 0) { 0 } else { value } }
    override var badgeAll = 0
    set(value) {field = if (value < 0) { 0 } else { value } }
    override var badgeStarred = 0
    set(value) {field = if (value < 0) { 0 } else { value } }

    init {
        // TODO: Dispatchers.IO not available in KMM, an alternative solution should be found
        CoroutineScope(Dispatchers.Main).launch {
            updateApiVersion()
            reloadBadges()
        }
    }

    override suspend fun getNewerItems(): ArrayList<SelfossModel.Item> {
        // TODO: Check connectivity
        val fetchedItems = api.getItems(displayedItems,
            settings.getString("prefer_api_items_number", "200").toInt(),
            offset = 0,
            tagFilter?.tag,
            sourceFilter?.id?.toLong(),
            searchFilter)

        if (fetchedItems != null) {
            items = ArrayList(fetchedItems)
        }
        return items
    }

    override suspend fun getOlderItems(): ArrayList<SelfossModel.Item> {
        // TODO: Check connectivity
        val offset = items.size
        val fetchedItems = api.getItems(displayedItems,
            settings.getString("prefer_api_items_number", "200").toInt(),
            offset,
            tagFilter?.tag,
            sourceFilter?.id?.toLong(),
            searchFilter)

        if (fetchedItems != null) {
            items = ArrayList(fetchedItems)
        }
        return items
    }

    private fun storeItems(fetchedItems: List<SelfossModel.Item>) {
        // TODO: Store in DB
        val fetchedIDS = fetchedItems.map { it.id }
        val tmpItems = ArrayList(items)
        tmpItems.removeAll{ it.id in fetchedIDS }
        tmpItems.addAll(fetchedItems)
        sortItems(tmpItems)
        items = tmpItems
    }

    private fun sortItems(items: ArrayList<SelfossModel.Item>) {
        val dateUtils = DateUtils(apiMajorVersion)
        items.sortByDescending { dateUtils.parseDate(it.datetime) }
    }

    private fun filterSelectedItems(items: ArrayList<SelfossModel.Item>): ArrayList<SelfossModel.Item> {
        val tmpItems = ArrayList(items)
        if (displayedItems == "unread") {
            tmpItems.removeAll { !it.unread }
        } else if (displayedItems == "starred") {
            tmpItems.removeAll { !it.starred }
        }

        if (tagFilter != null) {
            tmpItems.removeAll { !it.tags.contains(tagFilter!!.tag) }
        }

        return tmpItems
    }

    override suspend fun reloadBadges(): Boolean {
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

    override suspend fun getTags(): List<SelfossModel.Tag>? {
        // TODO: Check success, store in DB
        return api.tags()
    }

    override suspend fun getSpouts(): Map<String, SelfossModel.Spout>? {
        // TODO: Check success, store in DB
        return api.spouts()
    }

    override suspend fun getSources(): ArrayList<SelfossModel.Source>? {
        // TODO: Check success
        return api.sources()
    }

    override suspend fun markAsRead(id: Int): Boolean {
        // TODO: Check internet connection
        val success = api.markAsRead(id.toString())?.isSuccess == true

        if (success) {
            markAsReadLocally(id)
        }
        return success
    }

    override suspend fun unmarkAsRead(id: Int): Boolean {
        // TODO: Check internet connection
        val success = api.unmarkAsRead(id.toString())?.isSuccess == true

        if (success) {
            unmarkAsReadLocally(id)
        }
        return success
    }

    override suspend fun starr(id: Int): Boolean {
        // TODO: Check success, store in DB
        val success = api.starr(id.toString())?.isSuccess == true

        if (success) {
            starrLocally(id)
        }
        return success
    }

    override suspend fun unstarr(id: Int): Boolean {
        // TODO: Check internet connection
        val success = api.unstarr(id.toString())?.isSuccess == true

        if (success) {
            unstarrLocally(id)
        }
        return success
    }

    override suspend fun markAllAsRead(ids: List<Int>): Boolean {
        // TODO: Check Internet connectivity, store in DB

        val success = api.markAllAsRead(ids.map { it.toString() })?.isSuccess == true

        if (success) {
            for (id in ids) {
                markAsReadLocally(id)
            }
        }
        return success
    }

    private fun markAsReadLocally(id: Int) {
        // TODO: Mark also in the database
        if (items.first {it.id == id}.unread) {
            items.first {it.id == id}.unread = false
            badgeUnread -= 1
        }
    }

    private fun unmarkAsReadLocally(id: Int) {
        // TODO: Mark also in the database
        if (!items.first {it.id == id}.unread) {
            items.first {it.id == id}.unread = true
            badgeUnread += 1
        }
    }

    private fun starrLocally(id: Int) {
        // TODO: Mark also in the database
        if (!items.first {it.id == id}.starred) {
            items.first {it.id == id}.starred = true
            badgeStarred += 1
        }
    }

    private fun unstarrLocally(id: Int) {
        // TODO: Mark also in the database
        if (items.first {it.id == id}.starred) {
            items.first {it.id == id}.starred = false
            badgeStarred -= 1
        }
    }

    override suspend fun createSource(
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

    override suspend fun deleteSource(id: Int): Boolean {
        // TODO: Check connectivity, store in DB
        var success = false
        val response = api.deleteSource(id)
        if (response != null) {
            success = response.isSuccess
        }

        return success
    }

    override suspend fun updateRemote(): Boolean {
        // TODO: Handle connectivity issues
        val response = api.update()
        return response?.isSuccess ?: false
    }

    override suspend fun login(): Boolean {
        var result = false
        try {
            val response = api.login()
            if (response != null && response.isSuccess) {
                result = true
            }
        } catch (cause: Throwable) {
            Napier.e(cause.message!!, tag = "1")
            Napier.e(cause.stackTraceToString(),tag = "1")
        }
        return result
    }

    override fun refreshLoginInformation(url: String, login: String, password: String,
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