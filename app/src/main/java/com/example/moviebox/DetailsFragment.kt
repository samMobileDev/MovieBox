package com.example.moviebox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.moviebox.databinding.FragmentDetailsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

                if (saved != null){
                    binding.btnSaved.setImageResource(R.drawable.fav)
                }else{
                    binding.btnSaved.setImageResource(R.drawable.simple_fav)
                }
        }


        binding.btnSaved.setOnClickListener {
            lifecycleScope.launch {

                val saved = dao.getFilmById(filmId)

                if (saved == null) {
                    //PUT DATA TO ENTITY TABLE FROM BUNDLE
                    val film = FilmEntity(
                        id = filmId,
                        title = requireArguments().getString("title") ?: "",
                        poster_path = requireArguments().getString("poster_path") ?: "",
                        overview = requireArguments().getString("overview") ?: "",
                        vote_average = requireArguments().getDouble("vote_average")
                    )
                    //FUN FROM ROOM TO PUT DATA TO TABLE ENTITY
                    dao.insertFilm(film)

                    binding.btnSaved.setImageResource(R.drawable.fav)

                } else {
                    //FUN TO DELETE DATA FROM TABLE ENTITY
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

        //get data from bundle from home fragment
        val title = arguments?.getString("title")
        val overview = arguments?.getString("overview")
        val vote_average = arguments?.getDouble("vote_average")
        val release_date = arguments?.getString("release_date")
        val popularity = arguments?.getDouble("popularity")
        val vote_count = arguments?.getInt("vote_count")
        val movieId = arguments?.getInt("id") ?: -1


        //function to get genres and runtime by id
        lifecycleScope.launch {

            //function to format runtime
            fun formatRuntime(minutes: Int): String {
                val hours = minutes / 60
                val mins = minutes % 60

                return if (hours > 0) {
                    "${hours}h ${mins}min"
                } else {
                    "${mins}м"
                }
            }

            try {
                val funGenres = api.getMovieDetails(
                    movieId = movieId.toString(),
                    apiKey = "6f71a51841cae2c9bfcab7b8abae02e6"
                )
                if (funGenres.isSuccessful) {
                    val details = funGenres.body()

                    // Get the list of genres
                    val genresList = funGenres.body()?.genres
                    binding.flexboxGenres.removeAllViews()

                    if (!genresList.isNullOrEmpty()) {
                        for (genreObj in genresList) {

                            val genreView = layoutInflater.inflate(R.layout.item_genre, binding.flexboxGenres, false)


                            val tvGenreName = genreView.findViewById<TextView>(R.id.genres)
                            tvGenreName.text = genreObj.name


                            binding.flexboxGenres.addView(genreView)
                        }
                    }


                    val runtime = funGenres.body()?.runtime
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
            }
        }


        // show things that we got from home fragment as bundle
        binding.title.text = title
        binding.overview.text = overview
        binding.rating.text = "⭐ $vote_average"
        binding.releaseDate.text = "📅 Release Date:  $release_date"
        binding.popularity.text = "🔥 Trending Score:  ${popularity?.toInt()}"
        binding.voteCount.text = "👥 Vote Count:  $vote_count"


        //checking if backdrop_path is null or empty
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

        //video player
        binding.btnPlay.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val funVideos = api.getMovieVideos(
                        movieId = movieId.toString(),
                        apiKey = "6f71a51841cae2c9bfcab7b8abae02e6"
                    )
                    if (funVideos.isSuccessful) {
                        val videos = funVideos.body()?.results
                        val video = videos?.find { it.site == "YouTube" && it.type == "Trailer" }
                        if (video != null) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("vnd.youtube:${video.key}")
                            )
                            startActivity(intent)
                        }else{
                            Snackbar.make(binding.root, "No trailer found", Snackbar.LENGTH_SHORT).show()
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }




    }
}

