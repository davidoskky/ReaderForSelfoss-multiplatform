package bou.amine.apps.readerforselfossv2.android.persistence

import android.content.Context
import androidx.room.Room
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.persistence.entities.AndroidItemEntity
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_1_2
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_2_3
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_3_4
import bou.amine.apps.readerforselfossv2.dao.DeviceDatabase

class AndroidDeviceDatabase(applicationContext: Context): DeviceDatabase<AndroidItemEntity> {
    var db: AppDatabase = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "selfoss-database"
    ).addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3).addMigrations(MIGRATION_3_4).build()


    override suspend fun items(): List<AndroidItemEntity> = db.itemsDao().items()

    override suspend fun insertAllItems(vararg items: AndroidItemEntity) = db.itemsDao().insertAllItems(*items)

    override suspend fun deleteAllItems() = db.itemsDao().deleteAllItems()

    override suspend fun delete(item: AndroidItemEntity) = db.itemsDao().delete(item)

    override suspend fun updateItem(item: AndroidItemEntity) = db.itemsDao().updateItem(item)
}