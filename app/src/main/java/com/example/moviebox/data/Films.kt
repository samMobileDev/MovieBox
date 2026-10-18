package com.example.moviebox.data

data class Films(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val overview: String?,
    val vote_average: Double,
    val release_date: String?,
    val popularity: Double,
    val vote_count: Int,
    val backdrop_path: String?,
    val genre_ids: List<Int>? = null
)

data class MovieResponse(
    val page: Int = 1,
    val results: List<Films>,
    val total_pages: Int = 1,
    val total_results: Int = 0
)

data class MovieDetails(
    val runtime: Int,
    val genres: List<Genres>,
    val release_date: String?,
    val popularity: Double,
    val vote_count: Int,
    val backdrop_path: String?
)

data class Genres(
    val id: Int = 0,
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

data class WatchProvidersResponse(
    val results: Map<String, CountryProviders>?
)

data class CountryProviders(
    val link: String?,
    val flatrate: List<WatchProvider>?,
    val free: List<WatchProvider>?,
    val ads: List<WatchProvider>?,
    val rent: List<WatchProvider>?,
    val buy: List<WatchProvider>?
)

data class WatchProvider(
    val provider_id: Int? = null,
    val provider_name: String,
    val logo_path: String? = null
)

data class CreditsResponse(
    val cast: List<CastMember>?,
    val crew: List<CrewMember>?
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?
)

data class CrewMember(
    val id: Int,
    val name: String,
    val job: String?,
    val profile_path: String?
)

data class ReviewsResponse(
    val results: List<Review>?
)

data class Review(
    val id: String,
    val author: String,
    val content: String,
    val author_details: AuthorDetails?
)

data class AuthorDetails(
    val rating: Double?
)
