package bou.amine.apps.readerforselfossv2.android.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ActionEntity

@Dao
interface ActionsDao {
    @Query("SELECT * FROM actions order by id asc")
    suspend fun actions(): List<ActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllActions(vararg actions: ActionEntity)

    @Query("DELETE FROM actions WHERE articleid = :article_id AND read = 1")
    fun deleteReadActionForArticle(article_id: String)

    @Delete
    fun delete(action: ActionEntity)
}