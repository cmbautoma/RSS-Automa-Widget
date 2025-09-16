package com.moncho.feedlywidget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val url: String,
    val title: String? = null
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val link: String,
    val title: String,
    val feedTitle: String?,
    val pubDateMillis: Long,
)
