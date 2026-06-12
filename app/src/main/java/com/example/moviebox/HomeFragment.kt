package com.example.moviebox

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviebox.data.MovieApi
import com.example.moviebox.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class HomeFragment: Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(MovieApi::class.java)


        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    val getFun = api.getTrending(apiKey = "6f71a51841cae2c9bfcab7b8abae02e6")
                    val getFun2 = api.getTopRated(apiKey = "6f71a51841cae2c9bfcab7b8abae02e6")
                    val getFun3 = api.getPopular(apiKey = "6f71a51841cae2c9bfcab7b8abae02e6")

                    if (getFun.isSuccessful && getFun2.isSuccessful && getFun3.isSuccessful) {
                        val body = getFun.body()
                        val body2 = getFun2.body()
                        val body3 = getFun3.body()

                        if (body != null && body2 != null && body3 != null) {

                            //animations
                            binding.wholeLayout.post {
                                binding.wholeLayout.alpha = 0f
                                binding.wholeLayout.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .setListener(null)
                            }

                            //visibilities
                            binding.firstTXT.visibility = View.VISIBLE
                            binding.secondTXT.visibility = View.VISIBLE
                            binding.thirdTXT.visibility = View.VISIBLE
                            binding.txtNoInternet.visibility = View.GONE
                            binding.imgNoInternet.visibility = View.GONE

                            val adapter = TrendingAdapter(body.results) { films ->
                                val bundle = Bundle().apply {
                                    putString("title", films.title)
                                    putString("overview", films.overview)
                                    putString("poster_path", films.poster_path)
                                    putDouble("vote_average", films.vote_average)
                                    putString("release_date", films.release_date)
                                    putDouble("popularity", films.popularity)
                                    putInt("vote_count", films.vote_count)
                                    putString("backdrop_path", films.backdrop_path)
                                    putInt("id", films.id)
                                }

                                val fragment = DetailsFragment()
                                fragment.arguments = bundle


                                activity?.supportFragmentManager?.beginTransaction()
                                    ?.replace(R.id.container_fragment, fragment)
                                    ?.addToBackStack(null)
                                    ?.commit()
                            }


                            val currentContext = context ?: return@repeatOnLifecycle

                            binding.trendingRecycler.layoutManager =
                                LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                            binding.trendingRecycler.adapter = adapter

                            val adapter2 = SecondAdapter(body2.results) { films ->
                                val bundle = Bundle().apply {
                                    putString("title", films.title)
                                    putString("overview", films.overview)
                                    putString("poster_path", films.poster_path)
                                    putDouble("vote_average", films.vote_average)
                                    putString("release_date", films.release_date)
                                    putDouble("popularity", films.popularity)
                                    putInt("vote_count", films.vote_count)
                                    putString("backdrop_path", films.backdrop_path)
                                    putInt("id", films.id)
                                }
                                val fragment = DetailsFragment()
                                fragment.arguments = bundle
                                activity?.supportFragmentManager?.beginTransaction()
                                    ?.replace(R.id.container_fragment, fragment)
                                    ?.addToBackStack(null)
                                    ?.commit()
                            }

                            binding.topRatedRecycler.layoutManager =
                                LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                            binding.topRatedRecycler.adapter = adapter2

                            val adapter3 = SecondAdapter(body3.results) { films ->
                                val bundle = Bundle().apply {
                                    putString("title", films.title)
                                    putString("overview", films.overview)
                                    putString("poster_path", films.poster_path)
                                    putDouble("vote_average", films.vote_average)
                                    putString("release_date", films.release_date)
                                    putDouble("popularity", films.popularity)
                                    putInt("vote_count", films.vote_count)
                                    putString("backdrop_path", films.backdrop_path)
                                    putInt("id", films.id)
                                }

                                val fragment = DetailsFragment()
                                fragment.arguments = bundle
                                activity?.supportFragmentManager?.beginTransaction()
                                    ?.replace(R.id.container_fragment, fragment)
                                    ?.addToBackStack(null)
                                    ?.commit()
                            }

                            binding.popularRecycler.layoutManager =
                                LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
                            binding.popularRecycler.adapter = adapter3
                        }
                    }
                } catch (e: IOException) {
                    view?.let {
                        Snackbar.make(it, "No internet connection", Snackbar.LENGTH_SHORT).show()
                    }
                    binding.imgNoInternet.visibility = View.VISIBLE
                    binding.txtNoInternet.visibility = View.VISIBLE
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    binding.imgNoInternet.visibility = View.VISIBLE
                    binding.txtNoInternet.visibility = View.VISIBLE
                    view?.let {
                        Snackbar.make(it, "Something went wrong", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val pagerSnapHelper = androidx.recyclerview.widget.PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.trendingRecycler)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
