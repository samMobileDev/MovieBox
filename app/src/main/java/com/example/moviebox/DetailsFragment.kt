package com.example.moviebox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.moviebox.data.FilmDatabase
import com.example.moviebox.data.FilmEntity
import com.example.moviebox.data.MovieApi
import com.example.moviebox.data.WatchProvider
import com.example.moviebox.databinding.FragmentDetailsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

class DetailsFragment : Fragment(R.layout.fragment_details) {
    private var _binding: FragmentDetailsBinding? = null
    private val filmId by lazy {
        requireArguments().getInt("id")
    }

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailsBinding.bind(view)

        //room
        val db = FilmDatabase.getDatabase(requireContext())
        val dao = db.filmDao()

        //animation
        binding.wholeScroll.post {
            binding.wholeScroll.alpha = 0f
            binding.wholeScroll.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
        }

        //checking if film is in favorites
        lifecycleScope.launch {
            val saved = dao.getFilmById(filmId)

            if (saved != null) {
                binding.btnSaved.setImageResource(R.drawable.fav)
            } else {
                binding.btnSaved.setImageResource(R.drawable.simple_fav)
            }
        }

        binding.btnSaved.setOnClickListener {
            lifecycleScope.launch {
                val saved = dao.getFilmById(filmId)

                if (saved == null) {
                    val film = FilmEntity(
                        id = filmId,
                        title = requireArguments().getString("title") ?: "",
                        poster_path = requireArguments().getString("poster_path") ?: "",
                        overview = requireArguments().getString("overview") ?: "",
                        vote_average = requireArguments().getDouble("vote_average")
                    )
                    dao.insertFilm(film)
                    binding.btnSaved.setImageResource(R.drawable.fav)
                } else {
                    dao.deleteFilmById(filmId)
                    binding.btnSaved.setImageResource(R.drawable.simple_fav)
                }
            }
        }

        //retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(MovieApi::class.java)

        //get data from bundle
        val title = arguments?.getString("title")
        val overview = arguments?.getString("overview")
        val vote_average = arguments?.getDouble("vote_average")
        val release_date = arguments?.getString("release_date")
        val popularity = arguments?.getDouble("popularity")
        val vote_count = arguments?.getInt("vote_count")
        val movieId = arguments?.getInt("id") ?: -1

        // Fetch watch providers, credits, similar movies, reviews
        loadWatchProviders(api, movieId)
        loadMovieCredits(api, movieId)
        loadSimilarMovies(api, movieId)
        loadMovieReviews(api, movieId)

        //function to get genres and runtime by id
        lifecycleScope.launch {
            fun formatRuntime(minutes: Int): String {
                val hours = minutes / 60
                val mins = minutes % 60
                return if (hours > 0) {
                    "${hours}h ${mins}min"
                } else {
                    "${mins}m"
                }
            }

            try {
                val funGenres = api.getMovieDetails(
                    movieId = movieId.toString(),
                    apiKey = TMDB_KEY
                )
                if (funGenres.isSuccessful) {
                    val details = funGenres.body()

                    val genresList = details?.genres
                    binding.flexboxGenres.removeAllViews()

                    if (!genresList.isNullOrEmpty()) {
                        for (genreObj in genresList) {
                            val genreView = layoutInflater.inflate(R.layout.item_genre, binding.flexboxGenres, false)
                            val tvGenreName = genreView.findViewById<TextView>(R.id.genres)
                            tvGenreName.text = genreObj.name
                            binding.flexboxGenres.addView(genreView)
                        }
                    }

                    val runtime = details?.runtime
                    binding.runtime.text = "⏱️ Runtime: ${formatRuntime(runtime ?: 0)}"
                    binding.releaseDate.text = "📅 Release Date: ${details?.release_date}"
                    binding.popularity.text = "🔥 Trending Score: ${details?.popularity?.toInt()}"
                    binding.voteCount.text = "👥 Vote Count: ${details?.vote_count}"

                    val backdropUrl = details?.backdrop_path
                    if (!backdropUrl.isNullOrEmpty()) {
                        val imageUrl = "https://image.tmdb.org/t/p/w500$backdropUrl"
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .into(binding.backdrop)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // show initial bundle info
        binding.title.text = title
        binding.overview.text = overview
        binding.rating.text = "⭐ $vote_average"
        binding.releaseDate.text = "📅 Release Date:  $release_date"
        binding.popularity.text = "🔥 Trending Score:  ${popularity?.toInt()}"
        binding.voteCount.text = "👥 Vote Count:  $vote_count"

        val backdropUrl = arguments?.getString("backdrop_path")
        if (!backdropUrl.isNullOrEmpty()) {
            val imageUrl = "https://image.tmdb.org/t/p/w500$backdropUrl"
            Glide.with(requireContext())
                .load(imageUrl)
                .into(binding.backdrop)
        } else {
            binding.backdrop.setImageResource(R.drawable.def)
        }

        //back button
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        //video player (Feature 1: Trailer Fallback)
        binding.btnPlay.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val funVideos = api.getMovieVideos(
                        movieId = movieId.toString(),
                        apiKey = TMDB_KEY
                    )
                    if (funVideos.isSuccessful) {
                        val videos = funVideos.body()?.results
                        val video = videos?.find { it.site == "YouTube" && it.type == "Trailer" }
                            ?: videos?.firstOrNull { it.site == "YouTube" }
                        if (video != null) {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("vnd.youtube:${video.key}")
                                )
                                startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to browser if YouTube app is missing/fails
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=${video.key}")
                                )
                                startActivity(webIntent)
                            }
                        } else {
                            Snackbar.make(binding.root, "No trailer found", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Feature 5: Provider Logos & Watch Options
    private fun loadWatchProviders(api: MovieApi, movieId: Int) {
        val country = Locale.getDefault().country.ifEmpty { "US" }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getWatchProviders(movieId.toString(), TMDB_KEY)
                val section = _binding?.watchSection ?: return@launch

                val info = if (response.isSuccessful) {
                    response.body()?.results?.get(country)
                } else null

                val allProviders = mutableListOf<WatchProvider>()
                info?.flatrate?.let { allProviders.addAll(it) }
                info?.free?.let { allProviders.addAll(it) }
                info?.ads?.let { allProviders.addAll(it) }
                info?.rent?.let { allProviders.addAll(it) }
                info?.buy?.let { allProviders.addAll(it) }

                val distinctProviders = allProviders.distinctBy { it.provider_name }
                val currentContext = context ?: return@launch

                if (distinctProviders.isNotEmpty()) {
                    section.recyclerProviderLogos.visibility = View.VISIBLE
                    section.recyclerProviderLogos.layoutManager =
                        LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                    section.recyclerProviderLogos.adapter = ProviderLogoAdapter(distinctProviders)
                } else {
                    section.recyclerProviderLogos.visibility = View.GONE
                }

                val lines = listOfNotNull(
                    info?.flatrate.toLine("Stream"),
                    info?.free.toLine("Free"),
                    info?.ads.toLine("Free with ads"),
                    info?.rent.toLine("Rent"),
                    info?.buy.toLine("Buy")
                )

                section.txtWatchProviders.text = if (lines.isEmpty()) {
                    "No streaming info available for $country"
                } else {
                    lines.joinToString("\n")
                }

                val link = info?.link
                if (link.isNullOrEmpty()) {
                    section.btnWatchLink.visibility = View.GONE
                } else {
                    section.btnWatchLink.visibility = View.VISIBLE
                    section.btnWatchLink.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                }
                section.root.visibility = View.VISIBLE

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _binding?.watchSection?.root?.visibility = View.GONE
            }
        }
    }

    // Feature 2: Cast and Director
    private fun loadMovieCredits(api: MovieApi, movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getMovieCredits(movieId.toString(), TMDB_KEY)
                if (response.isSuccessful) {
                    val credits = response.body()
                    val director = credits?.crew?.find { it.job.equals("Director", ignoreCase = true) }
                    if (director != null) {
                        _binding?.txtDirector?.text = "🎬 Director: ${director.name}"
                    } else {
                        _binding?.txtDirector?.text = "🎬 Director: Unknown"
                    }

                    val castList = credits?.cast.orEmpty()
                    if (castList.isNotEmpty()) {
                        val currentContext = context ?: return@launch
                        _binding?.recyclerCast?.layoutManager =
                            LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                        _binding?.recyclerCast?.adapter = CastAdapter(castList.take(20))
                        _binding?.castSection?.visibility = View.VISIBLE
                    } else {
                        _binding?.castSection?.visibility = View.GONE
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _binding?.castSection?.visibility = View.GONE
            }
        }
    }

    // Feature 3: Similar Movies
    private fun loadSimilarMovies(api: MovieApi, movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getSimilarMovies(movieId.toString(), TMDB_KEY)
                if (response.isSuccessful) {
                    val movies = response.body()?.results.orEmpty()
                    if (movies.isNotEmpty()) {
                        val currentContext = context ?: return@launch
                        val adapter = SecondAdapter(movies) { film ->
                            val bundle = Bundle().apply {
                                putString("title", film.title)
                                putString("overview", film.overview)
                                putString("poster_path", film.poster_path)
                                putDouble("vote_average", film.vote_average)
                                putString("release_date", film.release_date)
                                putDouble("popularity", film.popularity)
                                putInt("vote_count", film.vote_count)
                                putString("backdrop_path", film.backdrop_path)
                                putInt("id", film.id)
                            }
                            val fragment = DetailsFragment()
                            fragment.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.container_fragment, fragment)
                                ?.addToBackStack(null)
                                ?.commit()
                        }
                        _binding?.recyclerSimilar?.layoutManager =
                            LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                        _binding?.recyclerSimilar?.adapter = adapter
                        _binding?.similarSection?.visibility = View.VISIBLE
                    } else {
                        _binding?.similarSection?.visibility = View.GONE
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _binding?.similarSection?.visibility = View.GONE
            }
        }
    }

    // Feature 4: User Reviews
    private fun loadMovieReviews(api: MovieApi, movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getMovieReviews(movieId.toString(), TMDB_KEY)
                if (response.isSuccessful) {
                    val reviews = response.body()?.results.orEmpty()
                    if (reviews.isNotEmpty()) {
                        val currentContext = context ?: return@launch
                        _binding?.txtNoReviews?.visibility = View.GONE
                        _binding?.recyclerReviews?.visibility = View.VISIBLE
                        _binding?.recyclerReviews?.layoutManager =
                            LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                        _binding?.recyclerReviews?.adapter = ReviewAdapter(reviews.take(5))
                    } else {
                        _binding?.txtNoReviews?.visibility = View.VISIBLE
                        _binding?.recyclerReviews?.visibility = View.GONE
                    }
                    _binding?.reviewsSection?.visibility = View.VISIBLE
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _binding?.reviewsSection?.visibility = View.GONE
            }
        }
    }

    private fun List<WatchProvider>?.toLine(label: String): String? =
        if (this.isNullOrEmpty()) null else "$label: ${joinToString { it.provider_name }}"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TMDB_KEY = "6f71a51841cae2c9bfcab7b8abae02e6"
    }
}
