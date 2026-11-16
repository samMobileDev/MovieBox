package com.example.moviebox

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.moviebox.data.Films
import com.example.moviebox.data.MovieApi
import com.example.moviebox.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: MovieApi

    private var trendingAdapter: TrendingAdapter? = null
    private var topRatedAdapter: SecondAdapter? = null
    private var popularAdapter: SecondAdapter? = null

    private var trendingPage = 1
    private var isTrendingLoading = false

    private var topRatedPage = 1
    private var isTopRatedLoading = false

    private var popularPage = 1
    private var isPopularLoading = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(MovieApi::class.java)

        setupAdapters()
        setupScrollListeners()
        loadInitialData()

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.trendingRecycler)
    }

    private fun setupAdapters() {
        val currentContext = context ?: return

        trendingAdapter = TrendingAdapter { film -> openDetails(film) }
        binding.trendingRecycler.layoutManager =
            LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
        binding.trendingRecycler.adapter = trendingAdapter

        topRatedAdapter = SecondAdapter { film -> openDetails(film) }
        binding.topRatedRecycler.layoutManager =
            LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
        binding.topRatedRecycler.adapter = topRatedAdapter

        popularAdapter = SecondAdapter { film -> openDetails(film) }
        binding.popularRecycler.layoutManager =
            LinearLayoutManager(currentContext, LinearLayoutManager.HORIZONTAL, false)
        binding.popularRecycler.adapter = popularAdapter
    }

    private fun loadInitialData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    val getFun = api.getTrending(apiKey = TMDB_KEY, page = 1)
                    val getFun2 = api.getTopRated(apiKey = TMDB_KEY, page = 1)
                    val getFun3 = api.getPopular(apiKey = TMDB_KEY, page = 1)

                    if (getFun.isSuccessful && getFun2.isSuccessful && getFun3.isSuccessful) {
                        val body = getFun.body()
                        val body2 = getFun2.body()
                        val body3 = getFun3.body()

                        if (body != null && body2 != null && body3 != null) {
                            binding.wholeLayout.post {
                                if (_binding == null) return@post
                                binding.wholeLayout.alpha = 0f
                                binding.wholeLayout.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .setListener(null)
                            }

                            binding.firstTXT.visibility = View.VISIBLE
                            binding.secondTXT.visibility = View.VISIBLE
                            binding.thirdTXT.visibility = View.VISIBLE
                            binding.txtNoInternet.visibility = View.GONE
                            binding.imgNoInternet.visibility = View.GONE

                            trendingAdapter?.submitList(body.results)
                            topRatedAdapter?.submitList(body2.results)
                            popularAdapter?.submitList(body3.results)
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
    }

    private fun setupScrollListeners() {
        binding.trendingRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isTrendingLoading) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val total = lm.itemCount
                val lastVisible = lm.findLastVisibleItemPosition()
                if (total > 0 && lastVisible >= total - 3) {
                    loadMoreTrending()
                }
            }
        })

        binding.topRatedRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isTopRatedLoading) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val total = lm.itemCount
                val lastVisible = lm.findLastVisibleItemPosition()
                if (total > 0 && lastVisible >= total - 3) {
                    loadMoreTopRated()
                }
            }
        })

        binding.popularRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isPopularLoading) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val total = lm.itemCount
                val lastVisible = lm.findLastVisibleItemPosition()
                if (total > 0 && lastVisible >= total - 3) {
                    loadMorePopular()
                }
            }
        })
    }

    private fun loadMoreTrending() {
        val nextPage = trendingPage + 1
        isTrendingLoading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = api.getTrending(apiKey = TMDB_KEY, page = nextPage)
                isTrendingLoading = false
                if (res.isSuccessful) {
                    res.body()?.results?.let { newItems ->
                        trendingPage = nextPage
                        trendingAdapter?.addItems(newItems)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                isTrendingLoading = false
            }
        }
    }

    private fun loadMoreTopRated() {
        val nextPage = topRatedPage + 1
        isTopRatedLoading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = api.getTopRated(apiKey = TMDB_KEY, page = nextPage)
                isTopRatedLoading = false
                if (res.isSuccessful) {
                    res.body()?.results?.let { newItems ->
                        topRatedPage = nextPage
                        topRatedAdapter?.addItems(newItems)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                isTopRatedLoading = false
            }
        }
    }

    private fun loadMorePopular() {
        val nextPage = popularPage + 1
        isPopularLoading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = api.getPopular(apiKey = TMDB_KEY, page = nextPage)
                isPopularLoading = false
                if (res.isSuccessful) {
                    res.body()?.results?.let { newItems ->
                        popularPage = nextPage
                        popularAdapter?.addItems(newItems)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                isPopularLoading = false
            }
        }
    }

    private fun openDetails(film: Films) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        val TMDB_KEY = BuildConfig.TMDB_API_KEY
    }
}
