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

    override var selectedItems = ArrayList<SelfossModel.Item>()
    get() { return ArrayList(field) }
    set(value) { field = ArrayList(value) }

    override var baseUrl = apiDetails.getBaseUrl()

    // TODO: Validate the string in the setter
    private var selectedType = "new"
    private var selectedTag: SelfossModel.Tag? = null
    private var selectedSource: SelfossModel.Source? = null
    private var search: String? = null

    override var apiMajorVersion = 0

    init {
        // TODO: Dispatchers.IO not available in KMM, an alternative solution should be found
        CoroutineScope(Dispatchers.Main).launch {
            updateApiVersion()
        }
    }

    override suspend fun getNewerItems(): ArrayList<SelfossModel.Item> {
        // TODO: Check connectivity
        val fetchedItems = api.getItems(selectedType,
            settings.getInt("prefer_api_items_number", 200),
            offset = 0,
            selectedTag?.tag,
            selectedSource?.id?.toLong(),
            search)

        if (fetchedItems != null) {
            storeItems(fetchedItems)
        }
        return selectedItems
    }

    override suspend fun getOlderItems(): ArrayList<SelfossModel.Item> {
        // TODO: Check connectivity
        val offset = selectedItems.size
        val fetchedItems = api.getItems(selectedType,
            settings.getInt("prefer_api_items_number", 200),
            offset,
            selectedTag?.tag,
            selectedSource?.id?.toLong(),
            search)

        if (fetchedItems != null) {
            storeItems(fetchedItems)
        }
        return selectedItems
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
        items.sortBy { dateUtils.parseDate(it.datetime) }
    }

    override fun stats(): SelfossModel.Stats {
        TODO("Not yet implemented")
    }

    override fun getTags(): List<SelfossModel.Tag> {
        TODO("Not yet implemented")
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
        // TODO: Check success, store in DB
        api.markAsRead(id.toString())
        return true
    }

    override suspend fun unmarkAsRead(id: Int): Boolean {
        // TODO: Check success, store in DB
        api.unmarkAsRead(id.toString())
        return true    }

    override suspend fun starr(id: Int): Boolean {
        // TODO: Check success, store in DB
        api.starr(id.toString())
        return true
    }

    override suspend fun unstarr(id: Int): Boolean {
        // TODO: Check success, store in DB
        api.unstarr(id.toString())
        return true
    }

    override fun markAllAsRead(ids: List<Int>): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun createSource(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String
    ): Boolean {
        // TODO: Check connectivity
        var result = false
        val response = api.createSourceForVersion(
            title,
            url,
            spout,
            tags,
            filter,
            apiMajorVersion
        )

        if (response != null) {
            result = true
        }

        return result
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

    override fun updateRemote(): Boolean {
        TODO("Not yet implemented")
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

    override fun refreshLoginInformation() {
        api.refreshLoginInformation()
        baseUrl = apiDetails.getBaseUrl()
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
}