package com.example.moviebox.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "films")
data class FilmEntity (
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val id: Int,
    val title: String,
    val poster_path: String,
    val overview: String,
    val vote_average: Double,
    )