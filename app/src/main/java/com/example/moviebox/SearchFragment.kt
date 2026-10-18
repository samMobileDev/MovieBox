package com.example.moviebox

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviebox.data.Films
import com.example.moviebox.data.MovieApi
import com.example.moviebox.databinding.FragmentSearchBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Calendar
import kotlin.coroutines.cancellation.CancellationException

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: MovieApi

    // Adapters
    private var searchAdapter: Adapter? = null
    private var popularAdapter: SecondAdapter? = null

    // Pagination states
    private var isSearchMode = false
    private var currentSearchQuery = ""
    private var currentSearchPage = 1
    private var totalSearchPages = 1
    private var isSearchLoading = false

    private var currentPopularPage = 1
    private var totalPopularPages = 1
    private var isPopularLoading = false

    // Filter states
    private var selectedGenreId: Int? = null
    private var selectedYear: String? = null

    // Search-as-you-type Job
    private var searchJob: Job? = null

    private val PREFS_NAME = "moviebox_search_prefs"
    private val KEY_RECENT_SEARCHES = "recent_searches"

    data class GenreOption(val id: Int?, val name: String) {
        override fun toString(): String = name
    }

    private val genreList = listOf(
        GenreOption(null, "All Genres"),
        GenreOption(28, "Action"),
        GenreOption(12, "Adventure"),
        GenreOption(16, "Animation"),
        GenreOption(35, "Comedy"),
        GenreOption(80, "Crime"),
        GenreOption(99, "Documentary"),
        GenreOption(18, "Drama"),
        GenreOption(10751, "Family"),
        GenreOption(14, "Fantasy"),
        GenreOption(36, "History"),
        GenreOption(27, "Horror"),
        GenreOption(10402, "Music"),
        GenreOption(9648, "Mystery"),
        GenreOption(10749, "Romance"),
        GenreOption(878, "Sci-Fi"),
        GenreOption(53, "Thriller"),
        GenreOption(10752, "War"),
        GenreOption(37, "Western")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(MovieApi::class.java)

        setupAdapters()
        setupSpinners()
        setupRecentSearches()
        setupSearchInput()
        setupScrollListeners()

        // Load initial popular / discover list
        performFilterOrPopularLoad(resetPage = true)
    }

    private fun setupAdapters() {
        val currentContext = context ?: return

        searchAdapter = Adapter { film -> openDetails(film) }
        binding.recyclerview.layoutManager = LinearLayoutManager(currentContext)
        binding.recyclerview.adapter = searchAdapter

        popularAdapter = SecondAdapter { film -> openDetails(film) }
        binding.searchTrendR.layoutManager = GridLayoutManager(currentContext, 2)
        binding.searchTrendR.adapter = popularAdapter
    }

    private fun setupSpinners() {
        val hostContext = context ?: return

        // Genre Spinner
        val genreAdapter = ArrayAdapter(hostContext, android.R.layout.simple_spinner_item, genreList)
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGenre.adapter = genreAdapter

        binding.spinnerGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val genre = genreList[position]
                if (selectedGenreId != genre.id) {
                    selectedGenreId = genre.id
                    triggerSearchOrFilter()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Year Spinner
        val years = mutableListOf("All Years")
        val currentYearNum = Calendar.getInstance().get(Calendar.YEAR)
        for (y in currentYearNum downTo 1980) {
            years.add(y.toString())
        }

        val yearAdapter = ArrayAdapter(hostContext, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val year = if (position == 0) null else years[position]
                if (selectedYear != year) {
                    selectedYear = year
                    triggerSearchOrFilter()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecentSearches() {
        renderRecentSearches()

        binding.btnClearRecent.setOnClickListener {
            clearRecentSearches()
            renderRecentSearches()
        }
    }

    private fun renderRecentSearches() {
        val recentList = getRecentSearches()
        val group = binding.chipGroupRecent
        group.removeAllViews()

        if (recentList.isEmpty()) {
            binding.recentSearchesContainer.visibility = View.GONE
            return
        }

        binding.recentSearchesContainer.visibility = View.VISIBLE

        for (query in recentList) {
            val chip = Chip(requireContext()).apply {
                text = query
                isClickable = true
                isFocusable = true
                setChipBackgroundColorResource(android.R.color.white)
                setTextColor(resources.getColor(android.R.color.black, null))
                setOnClickListener {
                    binding.editxtsrch.setText(query)
                    binding.editxtsrch.setSelection(query.length)
                    executeSearch(query)
                }
            }
            group.addView(chip)
        }
    }

    private fun setupSearchInput() {
        binding.btnsearch.setOnClickListener {
            val text = binding.editxtsrch.text.toString().trim()
            searchJob?.cancel()
            if (text.isNotEmpty()) {
                executeSearch(text)
            } else {
                triggerSearchOrFilter()
            }
        }

        // Search-as-you-type with short delay (debounce)
        binding.editxtsrch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                searchJob?.cancel()

                if (query.isEmpty()) {
                    renderRecentSearches()
                    triggerSearchOrFilter()
                    return
                }

                // Debounce delay of 400ms
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(400)
                    executeSearch(query)
                }
            }
        })
    }

    private fun setupScrollListeners() {
        // Pagination for Search Results List
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isSearchMode || isSearchLoading) return

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount > 0 && lastVisibleItem >= totalItemCount - 4 && currentSearchPage < totalSearchPages) {
                    loadNextSearchPage()
                }
            }
        })

        // Pagination for Popular / Discover List
        binding.searchTrendR.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isSearchMode || isPopularLoading) return

                val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount > 0 && lastVisibleItem >= totalItemCount - 4 && currentPopularPage < totalPopularPages) {
                    loadNextPopularPage()
                }
            }
        })
    }

    private fun triggerSearchOrFilter() {
        val query = binding.editxtsrch.text.toString().trim()
        if (query.isNotEmpty()) {
            executeSearch(query)
        } else {
            performFilterOrPopularLoad(resetPage = true)
        }
    }

    private fun executeSearch(query: String) {
        if (query.isEmpty()) return

        isSearchMode = true
        currentSearchQuery = query
        currentSearchPage = 1

        saveRecentSearch(query)
        renderRecentSearches()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    isSearchLoading = true
                    binding.progressBar.visibility = View.VISIBLE
                    binding.searchTrendR.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE

                    val response = api.getMovie(
                        apiKey = TMDB_KEY,
                        query = query,
                        year = selectedYear,
                        page = 1
                    )

                    isSearchLoading = false
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            var results = body.results

                            // Filter locally by genre if a genre is selected
                            selectedGenreId?.let { genreId ->
                                results = results.filter { film ->
                                    film.genre_ids?.contains(genreId) == true
                                }
                            }

                            if (results.isEmpty()) {
                                Snackbar.make(binding.root, "No results found", Snackbar.LENGTH_SHORT).show()
                                binding.recyclerview.visibility = View.GONE
                                return@repeatOnLifecycle
                            }

                            totalSearchPages = body.total_pages
                            searchAdapter?.submitList(results)
                            binding.recyclerview.visibility = View.VISIBLE
                        }
                    } else {
                        Snackbar.make(binding.root, "Error: ${response.code()}", Snackbar.LENGTH_SHORT).show()
                    }

                } catch (e: IOException) {
                    isSearchLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.recyclerview.visibility = View.GONE
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    isSearchLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.recyclerview.visibility = View.GONE
                }
            }
        }
    }

    private fun loadNextSearchPage() {
        if (isSearchLoading || currentSearchPage >= totalSearchPages) return

        val nextPage = currentSearchPage + 1
        isSearchLoading = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getMovie(
                    apiKey = TMDB_KEY,
                    query = currentSearchQuery,
                    year = selectedYear,
                    page = nextPage
                )

                isSearchLoading = false

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        currentSearchPage = nextPage
                        totalSearchPages = body.total_pages

                        var results = body.results
                        selectedGenreId?.let { genreId ->
                            results = results.filter { film ->
                                film.genre_ids?.contains(genreId) == true
                            }
                        }

                        searchAdapter?.addItems(results)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                isSearchLoading = false
            }
        }
    }

    private fun performFilterOrPopularLoad(resetPage: Boolean) {
        isSearchMode = false
        if (resetPage) currentPopularPage = 1

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    isPopularLoading = true
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerview.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE

                    val genreString = selectedGenreId?.toString()

                    val response = if (selectedGenreId != null || selectedYear != null) {
                        api.discoverMovies(
                            apiKey = TMDB_KEY,
                            genreId = genreString,
                            year = selectedYear,
                            page = currentPopularPage
                        )
                    } else {
                        api.getPopular(apiKey = TMDB_KEY, page = currentPopularPage)
                    }

                    isPopularLoading = false
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            totalPopularPages = body.total_pages
                            popularAdapter?.submitList(body.results)
                            binding.searchTrendR.visibility = View.VISIBLE
                        }
                    } else {
                        binding.errorLayout.visibility = View.VISIBLE
                    }

                } catch (e: IOException) {
                    isPopularLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    isPopularLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadNextPopularPage() {
        if (isPopularLoading || currentPopularPage >= totalPopularPages) return

        val nextPage = currentPopularPage + 1
        isPopularLoading = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val genreString = selectedGenreId?.toString()

                val response = if (selectedGenreId != null || selectedYear != null) {
                    api.discoverMovies(
                        apiKey = TMDB_KEY,
                        genreId = genreString,
                        year = selectedYear,
                        page = nextPage
                    )
                } else {
                    api.getPopular(apiKey = TMDB_KEY, page = nextPage)
                }

                isPopularLoading = false

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        currentPopularPage = nextPage
                        totalPopularPages = body.total_pages
                        popularAdapter?.addItems(body.results)
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

    // Recent Searches Helper Functions using SharedPreferences
    private fun getRecentSearches(): List<String> {
        val hostContext = context ?: return emptyList()
        val prefs = hostContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;;").filter { it.isNotBlank() }
    }

    private fun saveRecentSearch(query: String) {
        val hostContext = context ?: return
        val current = getRecentSearches().toMutableList()
        current.remove(query) // remove duplicates
        current.add(0, query) // add to top
        val trimmed = current.take(10) // keep top 10

        val prefs = hostContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RECENT_SEARCHES, trimmed.joinToString(";;;")).apply()
    }

    private fun clearRecentSearches() {
        val hostContext = context ?: return
        val prefs = hostContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }

    private companion object {
        const val TMDB_KEY = "6f71a51841cae2c9bfcab7b8abae02e6"
    }
}
