package bou.amine.apps.readerforselfossv2.android.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bou.amine.apps.readerforselfossv2.android.persistence.entities.AndroidItemEntity
import androidx.room.Update



@Dao
interface ItemsDao {
    @Query("SELECT * FROM items order by id desc")
    suspend fun items(): List<AndroidItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(vararg items: AndroidItemEntity)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Delete
    suspend fun delete(item: AndroidItemEntity)

    @Update
    suspend fun updateItem(item: AndroidItemEntity)
}