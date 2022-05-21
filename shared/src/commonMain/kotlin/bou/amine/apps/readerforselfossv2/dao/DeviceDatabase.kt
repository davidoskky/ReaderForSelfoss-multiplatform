package bou.amine.apps.readerforselfossv2.dao

interface DeviceDatabase<ItemEntity> {
    suspend fun items(): List<ItemEntity>
    suspend fun insertAllItems(vararg items: ItemEntity)
    suspend fun deleteAllItems()
    suspend fun delete(item: ItemEntity)
    suspend fun updateItem(item: ItemEntity)
}
