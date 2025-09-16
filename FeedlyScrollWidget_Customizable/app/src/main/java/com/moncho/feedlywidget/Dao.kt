package com.moncho.feedlywidget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds")
    suspend fun getAllFeeds(): List<FeedEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeeds(feeds: List<FeedEntity>)

    @Query("DELETE FROM feeds")
    suspend fun clearFeeds()
}

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY pubDateMillis DESC LIMIT :limit")
    suspend fun latest(limit: Int): List<ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clear()
}
