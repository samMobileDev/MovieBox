package com.example.moviebox.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moviebox.data.FilmEntity

@Dao
interface FilmDao {

@Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
suspend fun insertFilm(film: FilmEntity)


   @Delete
   suspend fun deleteFilm(film: FilmEntity)

   @Query("SELECT * FROM films")
suspend fun getAllFilms(): List<FilmEntity>

    @Query("DELETE FROM films WHERE id = :id")
    suspend fun deleteFilmById(id: Int)

    @Query("SELECT * FROM films WHERE id = :id LIMIT 1")
    suspend fun getFilmById(id: Int): FilmEntity?
}