package bou.amine.apps.readerforselfossv2.android.persistence

import bou.amine.apps.readerforselfossv2.android.persistence.entities.AndroidItemEntity
import bou.amine.apps.readerforselfossv2.android.utils.persistence.toEntity
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.DeviceDataBaseService
import bou.amine.apps.readerforselfossv2.service.SearchService

class AndroidDeviceDatabaseService(db: AndroidDeviceDatabase, searchService: SearchService) :
    DeviceDataBaseService<AndroidItemEntity>(db, searchService) {
    override suspend fun updateDatabase() {
        if (itemsCaching) {
            if (items.isEmpty()) {
                getFromDB()
            }
            db.deleteAllItems()
            db.insertAllItems(*(items.map { it.toEntity() }).toTypedArray())
        }
    }

    override suspend fun clearDBItems() {
        db.deleteAllItems()
    }

    override fun appendNewItems(newItems: List<SelfossModel.Item>) {
        var tmpItems = items
        if (tmpItems != newItems) {
            tmpItems = tmpItems.filter { item -> newItems.find { it.id == item.id } == null } as ArrayList<SelfossModel.Item>
            tmpItems.addAll(newItems)
            items = tmpItems

            sortItems()
            getFocusedItems()
        }
    }

    override fun getFromDB() {
        TODO("Not yet implemented")
    }
}