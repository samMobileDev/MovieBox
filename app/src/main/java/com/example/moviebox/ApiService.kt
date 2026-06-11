package com.example.moviebox

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi{
    @GET("search/movie")
    suspend fun getMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String): Response<MovieResponse>

    @GET("trending/movie/week")
    suspend fun getTrending(
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>

    @GET("movie/top_rated")
    suspend fun getTopRated(
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>

    @GET("movie/popular")
    suspend fun getPopular(
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String?,
        @Query("api_key") apiKey: String
    ): Response<MovieDetails>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String
    ): Response<VideoResponse>
}
