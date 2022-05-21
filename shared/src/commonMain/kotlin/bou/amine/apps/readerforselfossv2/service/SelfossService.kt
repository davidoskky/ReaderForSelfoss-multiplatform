package bou.amine.apps.readerforselfossv2.service


import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import kotlinx.coroutines.*

class SelfossService<ItemEntity>(val api: SelfossApi, private val dbService: DeviceDataBaseService<ItemEntity>, private val searchService: SearchService) {

    suspend fun getReadItems(itemsNumber: Int, offset: Int, isNetworkAvailable: Boolean): List<SelfossModel.Item>? = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            val apiItems = readItems( itemsNumber, offset)
            // SAVE OR UPDATE IN DB
            return@withContext apiItems
        } else {
            // GET FROM DB
            return@withContext emptyList()
        }
    }

    suspend fun getUnreadItems(itemsNumber: Int, offset: Int, isNetworkAvailable: Boolean): List<SelfossModel.Item>? = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            val apiItems = newItems(itemsNumber, offset)
            // SAVE OR UPDATE IN DB
            return@withContext apiItems
        } else {
            // GET FROM DB
            return@withContext emptyList()
        }
    }

    suspend fun getStarredItems(itemsNumber: Int, offset: Int, isNetworkAvailable: Boolean): List<SelfossModel.Item>? = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            val apiItems = starredItems(itemsNumber, offset)
            // SAVE OR UPDATE IN DB
            return@withContext apiItems
        } else {
            // GET FROM DB
            return@withContext emptyList()
        }
    }

    suspend fun readAll(ids: List<String>, isNetworkAvailable: Boolean): Boolean {
        // Add ids params
        var success = false
        if (isNetworkAvailable) {
          success = api.markAllAsRead(ids)?.isSuccess == true
            // SAVE OR UPDATE IN DB
        }
        // refresh view
        return success
    }

    suspend fun reloadBadges(isNetworkAvailable: Boolean) = withContext(Dispatchers.Default) {
        if (isNetworkAvailable) {
            try {
                val response = api.stats()
                if (response != null) {
                    searchService.badgeUnread = response.unread
                    searchService.badgeAll = response.total
                    searchService.badgeStarred = response.starred
                }
            } catch (e: Throwable) {}
        } else {
            dbService.computeBadges()
        }
    }

    private fun enqueueArticles(response: List<SelfossModel.Item>?, clearDatabase: Boolean) {
        if (response != null) {
            if (clearDatabase) {
                CoroutineScope(Dispatchers.Default).launch {
                    dbService.clearDBItems()
                }
            }
            dbService.appendNewItems(response)
        }
    }

    suspend fun allNewItems(): List<SelfossModel.Item>? =
        readItems(200, 0)

    suspend fun allReadItems(): List<SelfossModel.Item>? =
        newItems(200, 0)

    suspend fun allStarredItems(): List<SelfossModel.Item>? =
        starredItems(200, 0)

    private suspend fun readItems(
        itemsNumber: Int,
        offset: Int
    ): List<SelfossModel.Item>? =
        api.getItems("read", itemsNumber, offset, searchService.tagFilter, searchService.sourceIDFilter, searchService.searchFilter)

    private suspend fun newItems(
        itemsNumber: Int,
        offset: Int
    ): List<SelfossModel.Item>? =
        api.getItems("unread", itemsNumber, offset, searchService.tagFilter, searchService.sourceIDFilter, searchService.searchFilter)

    private suspend fun starredItems(
        itemsNumber: Int,
        offset: Int
    ): List<SelfossModel.Item>? =
        api.getItems("starred", itemsNumber, offset, searchService.tagFilter, searchService.sourceIDFilter, searchService.searchFilter)
}