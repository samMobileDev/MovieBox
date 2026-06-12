package com.example.moviebox.data

data class Films(
    val id:Int,
    val title:String,
    val poster_path:String,
    val overview:String,
    val vote_average:Double,
    val release_date:String,
    val popularity:Double,
    val vote_count:Int,
    val backdrop_path:String,
    )

data class MovieResponse(
    val results: List<Films>
)

data class MovieDetails(
    val runtime: Int,
    val genres : List<Genres>,
    val release_date: String,
    val popularity: Double,
    val vote_count: Int,
    val backdrop_path: String)

data class Genres(
    val name: String
)
data class VideoResponse(
    val results: List<Video>
)
data class Video(
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

