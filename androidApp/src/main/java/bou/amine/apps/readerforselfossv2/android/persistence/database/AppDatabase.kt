package bou.amine.apps.readerforselfossv2.android.persistence.database

import androidx.room.RoomDatabase
import androidx.room.Database
import bou.amine.apps.readerforselfossv2.android.persistence.dao.ActionsDao
import bou.amine.apps.readerforselfossv2.android.persistence.dao.DrawerDataDao
import bou.amine.apps.readerforselfossv2.android.persistence.dao.ItemsDao
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ActionEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ItemEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.SourceEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.TagEntity

@Database(entities = [TagEntity::class, SourceEntity::class, ItemEntity::class, ActionEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawerDataDao(): DrawerDataDao

    abstract fun itemsDao(): ItemsDao

    abstract fun actionsDao(): ActionsDao
}