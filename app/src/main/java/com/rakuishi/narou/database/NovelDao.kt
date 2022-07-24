package com.rakuishi.narou.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rakuishi.narou.model.Novel

@Dao
interface NovelDao {

    @Insert
    suspend fun insert(novel: Novel)

    @Update
    suspend fun update(novel: Novel)

    @Query("SELECT * FROM novels WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): Novel?

    @Query("SELECT * FROM novels WHERE nid = :nid LIMIT 1")
    suspend fun getItemByNid(nid: String): Novel?

    @Query("SELECT * FROM novels ORDER BY latest_episode_updated_at DESC")
    suspend fun getList(): List<Novel>
}