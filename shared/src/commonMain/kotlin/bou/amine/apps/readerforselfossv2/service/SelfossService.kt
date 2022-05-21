package bou.amine.apps.readerforselfossv2.service


import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import kotlinx.coroutines.*

class SelfossService<ItemEntity>(val api: SelfossApi, private val dbService: DeviceDataBaseService<ItemEntity>, private val searchService: SearchService) {

    suspend fun getAndStoreAllItems(isNetworkAvailable: Boolean) = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            launch {
                try {
                    enqueueArticles(allNewItems(), true)
                } catch (e: Throwable) {}
            }
            launch {
                try {
                    enqueueArticles(allReadItems(), false)
                } catch (e: Throwable) {}
            }
            launch {
                try {
                    enqueueArticles(allStarredItems(), false)
                } catch (e: Throwable) {}
            }
        } else {
            launch { dbService.updateDatabase() }
        }
    }

    suspend fun refreshFocusedItems(itemsNumber: Int, isNetworkAvailable: Boolean) = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            val response = when (searchService.displayedItems) {
                "read" -> readItems(itemsNumber, 0)
                "unread" -> newItems(itemsNumber, 0)
                "starred" -> starredItems(itemsNumber, 0)
                else -> readItems(itemsNumber, 0)
            }

            if (response != null) {
                // TODO:
                // dbService.refreshFocusedItems(response.body() as ArrayList<SelfossModel.Item>)
                dbService.updateDatabase()
            }
        }
    }

    suspend fun getReadItems(itemsNumber: Int, offset: Int, isNetworkAvailable: Boolean) = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            try {
                enqueueArticles(readItems( itemsNumber, offset), false)
                searchService.fetchedAll = true
                dbService.updateDatabase()
            } catch (e: Throwable) {}
        }
    }

    suspend fun getUnreadItems(itemsNumber: Int, offset: Int, isNetworkAvailable: Boolean) = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            try {
                if (!searchService.fetchedUnread) {
                    dbService.clearDBItems()
                }
                enqueueArticles(newItems(itemsNumber, offset), false)
                searchService.fetchedUnread = true
            } catch (e: Throwable) {}
        }
        dbService.updateDatabase()
    }

    suspend fun getStarredItems(itemsNumber: Int, offset: Int, isNetworkAvailable: Boolean) = withContext(
        Dispatchers.Default) {
        if (isNetworkAvailable) {
            try {
                enqueueArticles(starredItems(itemsNumber, offset), false)
                searchService.fetchedStarred = true
                dbService.updateDatabase()
            } catch (e: Throwable) {
            }
        }
    }

    suspend fun readAll(isNetworkAvailable: Boolean): Boolean {
        var success = false
        if (isNetworkAvailable) {
          // Do api call to read all
        } else {
            // Do db call to read all
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

    private suspend fun allNewItems(): List<SelfossModel.Item>? =
        readItems(200, 0)

    private suspend fun allReadItems(): List<SelfossModel.Item>? =
        newItems(200, 0)

    private suspend fun allStarredItems(): List<SelfossModel.Item>? =
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