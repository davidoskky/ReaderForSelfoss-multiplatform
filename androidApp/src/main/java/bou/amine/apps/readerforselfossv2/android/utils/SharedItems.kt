package bou.amine.apps.readerforselfossv2.android.utils

import android.content.Context
import android.widget.Toast
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Item
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossApi
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SuccessResponse
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ActionEntity
import bou.amine.apps.readerforselfossv2.android.utils.persistence.toEntity
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAccessible
import bou.amine.apps.readerforselfossv2.android.utils.persistence.toView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

/*
* Singleton class that contains the articles fetched from Selfoss, it allows sharing the items list
* between Activities and Fragments
*/
object SharedItems {
    var items: ArrayList<Item> = arrayListOf<Item>()
        get() {
            return ArrayList(field)
        }
        set(value) {
            field = ArrayList(value)
        }
    var focusedItems: ArrayList<Item> = arrayListOf<Item>()
        get() {
            return ArrayList(field)
        }
        set(value) {
            field = ArrayList(value)
        }
    var position = 0
        set(value) {
            field = when {
                value < 0 -> 0
                value > items.size -> items.size
                else -> value
            }
        }
    var displayedItems: String = "unread"
        set(value) {
            field = when (value) {
                "all" -> "all"
                "unread" -> "unread"
                "read" -> "read"
                "starred" -> "starred"
                else -> "all"
            }
        }

    var searchFilter: String? = null
    var sourceIDFilter: Long? = null
    var sourceFilter: String? = null
    var tagFilter: String? = null
    var itemsCaching = false

    var fetchedUnread = false
    var fetchedAll = false
    var fetchedStarred = false

    var badgeUnread = -1
    var badgeAll = -1
    var badgeStarred = -1

    /**
     * Add new items to the SharedItems list
     *
     * The new items are considered more updated than the ones already in the list.
     * The old items present in the new list are discarded and replaced by the new ones.
     * Items are compared according to the selfoss id, which should always be unique.
     */
    fun appendNewItems(newItems: ArrayList<Item>) {
        var tmpItems = items
        if (tmpItems != newItems) {
            tmpItems = tmpItems.filter { item -> newItems.find { it.id == item.id } == null } as ArrayList<Item>
            tmpItems.addAll(newItems)
            items = tmpItems

            sortItems()
            getFocusedItems()
        }
    }

    fun refreshFocusedItems(newItems: ArrayList<Item>) {
        val tmpItems = items
        tmpItems.removeAll(focusedItems)

        appendNewItems(newItems)
    }

    suspend fun clearDBItems(db: AppDatabase) {
        db.itemsDao().deleteAllItems()
    }

    suspend fun updateDatabase(db: AppDatabase) {
        if (itemsCaching) {
            if (items.isEmpty()) {
                getFromDB(db)
            }
            db.itemsDao().deleteAllItems()
            db.itemsDao().insertAllItems(*(items.map { it.toEntity() }).toTypedArray())
        }
    }

    fun filter() {
        fun filterSearch(item: Item): Boolean {
            return if (!searchFilter.isEmptyOrNullOrNullString()) {
                var matched = item.title.contains(searchFilter.toString(), true)
                matched = matched || item.content.contains(searchFilter.toString(), true)
                matched = matched || item.sourcetitle.contains(searchFilter.toString(), true)
                matched
            } else {
                true
            }
        }

        var tmpItems = focusedItems
        if (tagFilter != null) {
            tmpItems = tmpItems.filter { it.tags.tags.contains(tagFilter.toString()) } as ArrayList<Item>
        }
        if (searchFilter != null) {
            tmpItems = tmpItems.filter { filterSearch(it) } as ArrayList<Item>
        }
        if (sourceFilter != null) {
            tmpItems = tmpItems.filter { it.sourcetitle == sourceFilter } as ArrayList<Item>
        }
        focusedItems = tmpItems
    }

    private fun getFocusedItems() {
        when (displayedItems) {
            "all" -> getAll()
            "unread" -> getUnRead()
            "read" -> getRead()
            "starred" -> getStarred()
            else -> getUnRead()
        }
    }

    fun getUnRead() {
        displayedItems = "unread"
        focusedItems = items.filter { item -> item.unread } as ArrayList<Item>
        filter()
    }

    fun getRead() {
        displayedItems = "read"
        focusedItems = items.filter { item -> !item.unread } as ArrayList<Item>
        filter()
    }

    fun getStarred() {
        displayedItems = "starred"
        focusedItems = items.filter { item -> item.starred } as ArrayList<Item>
        filter()
    }

    fun getAll() {
        displayedItems = "all"
        focusedItems = items
        filter()
    }

    suspend fun getFromDB(db: AppDatabase) {
        if (itemsCaching) {
                    val dbItems = db.itemsDao().items().map { it.toView() } as ArrayList<Item>
                    appendNewItems(dbItems)
        }
    }

    private fun removeItemAtIndex(index: Int) {
        val i = focusedItems[index]
        val tmpItems = focusedItems
        tmpItems.remove(i)
        focusedItems = tmpItems
    }

    fun addItemAtIndex(newItem: Item, index: Int) {
        val tmpItems = focusedItems
        tmpItems.add(index, newItem)
        focusedItems = tmpItems
    }

    fun readItem(app: Context, api: SelfossApi, db: AppDatabase, item: Item) {
        if (items.contains(item)) {
            position = items.indexOf(item)
            readItemAtPosition(app, api, db)
        }
    }

    fun readItems(db: AppDatabase, ids: List<String>) {
        for (id in ids) {
            val match = items.filter { it -> it.id == id }
            if (match.isNotEmpty() && match.size == 1) {
                position = items.indexOf(match[0])
                val tmpItems = items
                tmpItems[position].unread = false
                items = tmpItems
                resetDBItem(db)
                badgeUnread--
            }
        }
    }

    private fun readItemAtPosition(app: Context, api: SelfossApi, db: AppDatabase) {
        val i = items[position]

        if (app.isNetworkAccessible(null)) {
            api.markItem(i.id).enqueue(object : Callback<SuccessResponse> {
                override fun onResponse(
                    call: Call<SuccessResponse>,
                    response: Response<SuccessResponse>
                ) {

                    val tmpItems = items
                    tmpItems[position].unread = false
                    items = tmpItems

                    resetDBItem(db)
                    getFocusedItems()
                    badgeUnread--
                }

                override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                    Toast.makeText(
                            app,
                            app.getString(R.string.cant_mark_read),
                            Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else if (itemsCaching) {
            thread {
                db.actionsDao().insertAllActions(ActionEntity(i.id, true, false, false, false))
            }
        }

        if (position > items.size) {
            position -= 1
        }
    }

    fun unreadItem(app: Context, api: SelfossApi, db: AppDatabase, item: Item) {
        if (items.contains(item) && !item.unread) {
            position = items.indexOf(item)
            unreadItemAtPosition(app, api, db)
        }
    }

    private fun unreadItemAtPosition(app: Context, api: SelfossApi, db: AppDatabase) {
        val i = items[position]

        if (app.isNetworkAccessible(null)) {
            api.unmarkItem(i.id).enqueue(object : Callback<SuccessResponse> {
                override fun onResponse(
                    call: Call<SuccessResponse>,
                    response: Response<SuccessResponse>
                ) {

                    val tmpItems = items
                    tmpItems[position].unread = true
                    items = tmpItems

                    resetDBItem(db)
                    getFocusedItems()
                    badgeUnread++
                }

                override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                    Toast.makeText(
                        app,
                        app.getString(R.string.cant_mark_unread),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else if (itemsCaching) {
            thread {
                db.actionsDao().insertAllActions(ActionEntity(i.id, false, true, false, false))
            }
        }
    }

    fun starItem(app: Context, api: SelfossApi, db: AppDatabase, item: Item) {
        if (items.contains(item) && !item.starred) {
            position = items.indexOf(item)
            starItemAtPosition(app, api, db)
        }
    }

    private fun starItemAtPosition(app: Context, api: SelfossApi, db: AppDatabase) {
        val i = items[position]

        if (app.isNetworkAccessible(null)) {
            api.starrItem(i.id).enqueue(object : Callback<SuccessResponse> {
                override fun onResponse(
                    call: Call<SuccessResponse>,
                    response: Response<SuccessResponse>
                ) {
                    val tmpItems = items
                    tmpItems[position].starred = true
                    items = tmpItems

                    resetDBItem(db)
                    getFocusedItems()
                    badgeStarred++
                }

                override fun onFailure(
                    call: Call<SuccessResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        app,
                        app.getString(R.string.cant_mark_favortie),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            thread {
                db.actionsDao().insertAllActions(ActionEntity(i.id, false, false, true, false))
            }
        }
    }

    fun unstarItem(app: Context, api: SelfossApi, db: AppDatabase, item: Item) {
        if (items.contains(item) && item.starred) {
            position = items.indexOf(item)
            unstarItemAtPosition(app, api, db)
        }
    }

    private fun unstarItemAtPosition(app: Context, api: SelfossApi, db: AppDatabase) {
        val i = items[position]

        if (app.isNetworkAccessible(null)) {
            api.unstarrItem(i.id).enqueue(object : Callback<SuccessResponse> {
                override fun onResponse(
                    call: Call<SuccessResponse>,
                    response: Response<SuccessResponse>
                ) {
                    val tmpItems = items
                    tmpItems[position].starred = false
                    items = tmpItems

                    resetDBItem(db)
                    getFocusedItems()
                    badgeStarred--
                }

                override fun onFailure(
                    call: Call<SuccessResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        app,
                        app.getString(R.string.cant_unmark_favortie),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            thread {
                db.actionsDao().insertAllActions(ActionEntity(i.id, false, false, false, true))
            }
        }
    }

    private fun resetDBItem(db: AppDatabase) {
        if (itemsCaching) {
            val i = items[position]
            CoroutineScope(Dispatchers.IO).launch {
                db.itemsDao().delete(i.toEntity())
                db.itemsDao().insertAllItems(i.toEntity())
            }
        }
    }

    fun unreadItemStatusAtIndex(position: Int): Boolean {
        return focusedItems[position].unread
    }

    fun computeBadges() {
        badgeUnread = items.filter { item -> item.unread }.size
        badgeStarred = items.filter { item -> item.starred }.size
        badgeAll = items.size
    }

    private fun sortItems() {
        val tmpItems = ArrayList(items.sortedByDescending { parseDate(it.datetime) })
        items = tmpItems
    }
}