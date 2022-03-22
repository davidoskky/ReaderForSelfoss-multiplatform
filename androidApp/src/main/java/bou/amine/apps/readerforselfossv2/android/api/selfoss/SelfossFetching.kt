package bou.amine.apps.readerforselfossv2.android.api.selfoss

import android.content.Context
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.utils.SharedItems
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAvailable
import kotlinx.coroutines.*
import retrofit2.Response

suspend fun getAndStoreAllItems(context: Context, api: SelfossApi, db: AppDatabase) = withContext(Dispatchers.IO) {
    if (isNetworkAvailable(context)) {
        launch {
            try {
                enqueueArticles(api.allNewItems(), db, true)
            } catch (e: Throwable) {}
        }
        launch {
            try {
                enqueueArticles(api.allReadItems(), db, false)
            } catch (e: Throwable) {}
        }
        launch {
            try {
                enqueueArticles(api.allStarredItems(), db, false)
            } catch (e: Throwable) {}
        }
    } else {
        launch { SharedItems.updateDatabase(db) }
    }
}

suspend fun updateItems(context: Context, api: SelfossApi, db: AppDatabase) = coroutineScope {
    if (isNetworkAvailable(context)) {
        launch {
            try {
                enqueueArticles(api.updateItems(SharedItems.items[0].datetime), db, true)
            } catch (e: Throwable) {}
        }
    }
}

suspend fun refreshFocusedItems(context: Context, api: SelfossApi, db: AppDatabase, itemsNumber: Int) = withContext(Dispatchers.IO) {
    if (isNetworkAvailable(context)) {
        val response = when (SharedItems.displayedItems) {
            "read" -> api.readItems(itemsNumber, 0)
            "unread" -> api.newItems(itemsNumber, 0)
            "starred" -> api.starredItems(itemsNumber, 0)
            else -> api.readItems(itemsNumber, 0)
        }

        if (response.isSuccessful) {
            SharedItems.refreshFocusedItems(response.body() as ArrayList<Item>)
            SharedItems.updateDatabase(db)
        }
    }
}

suspend fun getReadItems(context: Context, api: SelfossApi, db: AppDatabase, itemsNumber: Int, offset: Int) = withContext(Dispatchers.IO) {
    if (isNetworkAvailable(context)) {
            try {
                enqueueArticles(api.readItems( itemsNumber, offset), db, false)
                SharedItems.fetchedAll = true
                SharedItems.updateDatabase(db)
            } catch (e: Throwable) {}
    }
}

suspend fun getUnreadItems(context: Context, api: SelfossApi, db: AppDatabase, itemsNumber: Int, offset: Int) = withContext(Dispatchers.IO) {
    if (isNetworkAvailable(context)) {
        try {
            if (!SharedItems.fetchedUnread) {
                SharedItems.clearDBItems(db)
            }
            enqueueArticles(api.newItems(itemsNumber, offset), db, false)
            SharedItems.fetchedUnread = true
        } catch (e: Throwable) {}
    }
    SharedItems.updateDatabase(db)
}

suspend fun getStarredItems(context: Context, api: SelfossApi, db: AppDatabase, itemsNumber: Int, offset: Int) = withContext(Dispatchers.IO) {
    if (isNetworkAvailable(context)) {
        try {
            enqueueArticles(api.starredItems(itemsNumber, offset), db, false)
            SharedItems.fetchedStarred = true
            SharedItems.updateDatabase(db)
        } catch (e: Throwable) {
        }
    }
}

suspend fun readAll(context: Context, api: SelfossApi, db: AppDatabase): Boolean {
    var success = false
    if (isNetworkAvailable(context)) {
        try {
            val ids = SharedItems.focusedItems.map { it.id }
            if (ids.isNotEmpty()) {
                val result = api.readAll(ids)
                SharedItems.readItems(db, ids)
                success = result.isSuccess
            }
        } catch (e: Throwable) {}
    }
    return success
}

suspend fun reloadBadges(context: Context, api: SelfossApi) = withContext(Dispatchers.IO) {
    if (isNetworkAvailable(context)) {
        try {
            val response = api.stats()

            if (response.isSuccessful) {
                val badges = response.body()
                SharedItems.badgeUnread = badges!!.unread
                SharedItems.badgeAll = badges.total
                SharedItems.badgeStarred = badges.starred
            }
        } catch (e: Throwable) {}
    } else {
        SharedItems.computeBadges()
    }
}

private fun enqueueArticles(response: Response<List<Item>>, db: AppDatabase, clearDatabase: Boolean) {
        if (response.isSuccessful) {
            if (clearDatabase) {
                CoroutineScope(Dispatchers.IO).launch {
                    SharedItems.clearDBItems(db)
                }
            }
            val allItems = response.body() as ArrayList<Item>
            SharedItems.appendNewItems(allItems)
        }
}