package com.example.moviebox

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviebox.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(MovieApi::class.java)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    val fun2 = api.getPopular(apiKey = "6f71a51841cae2c9bfcab7b8abae02e6")
                    if (fun2.isSuccessful) {
                        val body = fun2.body()
                        if (body != null) {


                            binding.searchTrendR.post {
                                if (_binding == null) return@post
                                binding.searchTrendR.alpha = 0f
                                binding.searchTrendR.animate()
                                    .alpha(1f)
                                    .setDuration(400)
                                    .setListener(null)
                            }

                            binding.errorLayout.visibility = View.GONE

                            val adapter = SecondAdapter(body.results) { films ->
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
                            binding.searchTrendR.layoutManager = GridLayoutManager(currentContext, 2)
                            binding.searchTrendR.adapter = adapter
                        }
                    }
                } catch (e: IOException) {
                    view.let {
                        Snackbar.make(it, "No internet connection", Snackbar.LENGTH_SHORT).show()
                    }
                    binding.errorLayout.visibility = View.VISIBLE
                } catch (e: Exception) {
                    view.let {
                        Snackbar.make(it, "Something went wrong", Snackbar.LENGTH_SHORT).show()
                    }
                    binding.errorLayout.visibility = View.VISIBLE
                }
            }
        }


        binding.btnsearch.setOnClickListener {
            val editText = binding.editxtsrch.text.toString().trim()
            binding.editxtsrch.error = null

            if (editText.isEmpty()) {
                binding.editxtsrch.error = "Enter something"
                return@setOnClickListener
            }

            if (editText.length > 30) {
                binding.editxtsrch.error = "too long"
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.searchTrendR.visibility = View.GONE
                        binding.recyclerview.visibility = View.GONE
                        binding.errorLayout.visibility = View.GONE

                        val getFun = api.getMovie(
                            query = editText,
                            apiKey = "6f71a51841cae2c9bfcab7b8abae02e6"
                        )

                        if (getFun.isSuccessful) {
                            val body = getFun.body()

                            if (body != null) {
                                if (body.results.isEmpty()) {
                                    Snackbar.make(view, "No results found", Snackbar.LENGTH_SHORT)
                                        .show()
                                    binding.editxtsrch.error = "Nothing matched your search"
                                    binding.progressBar.visibility = View.GONE
                                    return@repeatOnLifecycle
                                }


                                binding.recyclerview.post {
                                    if (_binding == null) return@post
                                    binding.recyclerview.alpha = 0f
                                    binding.recyclerview.animate()
                                        .alpha(1f)
                                        .setDuration(500)
                                        .setListener(null)
                                }

                                binding.recyclerview.visibility = View.VISIBLE
                                binding.searchTrendR.visibility = View.GONE
                                binding.errorLayout.visibility = View.GONE
                                binding.progressBar.visibility = View.GONE

                                val adapter = Adapter(body.results) { films ->
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
                                binding.recyclerview.layoutManager =
                                    LinearLayoutManager(currentContext)
                                binding.recyclerview.adapter = adapter

                            } else {
                                Snackbar.make(
                                    view,
                                    "API error: ${getFun.code()}",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    } catch (e: IOException) {
                        Snackbar.make(view, "No internet connection", Snackbar.LENGTH_SHORT).show()
                        binding.errorLayout.visibility = View.VISIBLE
                        binding.searchTrendR.visibility = View.GONE
                        binding.recyclerview.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_SHORT).show()
                        binding.errorLayout.visibility = View.VISIBLE
                        binding.searchTrendR.visibility = View.GONE
                        binding.recyclerview.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                    }
                   }
              }
         }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





