package bou.amine.apps.readerforselfossv2.service

import bou.amine.apps.readerforselfossv2.dao.DeviceDatabase
import bou.amine.apps.readerforselfossv2.utils.parseDate
import bou.amine.apps.readerforselfossv2.rest.SelfossModel

abstract class DeviceDataBaseService<ItemEntity>(val db: DeviceDatabase<ItemEntity>, private val searchService: SearchService) {
    var itemsCaching = false
    var items: ArrayList<SelfossModel.Item> = arrayListOf()
        get() {
            return ArrayList(field)
        }
        set(value) {
            field = ArrayList(value)
        }

    abstract suspend fun updateDatabase()
    abstract suspend fun clearDBItems()
    abstract fun appendNewItems(items: List<SelfossModel.Item>)
    abstract fun getFromDB()

    fun sortItems() {
        val tmpItems = ArrayList(items.sortedByDescending { it.parseDate(searchService.dateUtils) })
        items = tmpItems
    }

    // This filtered items from items val. Do not use
    fun getFocusedItems() {}
    fun computeBadges() {
        searchService.badgeUnread = items.filter { item -> item.unread }.size
        searchService.badgeStarred = items.filter { item -> item.starred }.size
        searchService.badgeAll = items.size
    }
}