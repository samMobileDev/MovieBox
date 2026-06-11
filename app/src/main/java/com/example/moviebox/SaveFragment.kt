package com.example.moviebox

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviebox.databinding.FragmentSaveBinding
import kotlinx.coroutines.launch

class SaveFragment : Fragment(R.layout.fragment_save) {

    private var _binding: FragmentSaveBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSaveBinding.bind(view)

        val db = FilmDatabase.getDatabase(requireContext())
        val dao = db.filmDao()

        viewLifecycleOwner.lifecycleScope.launch {

            //animation
            _binding?.wholeFrame?.let { frame ->
                frame.alpha = 0f
                frame.animate().alpha(1f).setDuration(400).start()
            }

            try {
                val films = dao.getAllFilms()
                val currentBinding = _binding ?: return@launch
                val currentContext = context ?: return@launch

                if (films.isEmpty()){
                    currentBinding.txtEmptySaved.visibility = View.VISIBLE
                    currentBinding.recyclerSaved.visibility = View.GONE
                    return@launch
                }else{

                    currentBinding.txtEmptySaved.visibility = View.GONE
                    currentBinding.recyclerSaved.visibility = View.VISIBLE
                }


                val adapter = SavedAdapter(
                    films.toMutableList(),

                    onDelete = { film ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            dao.deleteFilmById(film.id)
                        }
                    },
                    onClick = { film ->
                        val bundle = Bundle().apply {
                            putInt("id", film.id)
                            putString("title", film.title)
                            putString("poster_path", film.poster_path)
                            putString("overview", film.overview)
                            putDouble("vote_average", film.vote_average)
                        }

                        val fragment = DetailsFragment()
                        fragment.arguments = bundle

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.container_fragment, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                )

                currentBinding.recyclerSaved.layoutManager = LinearLayoutManager(currentContext)
                currentBinding.recyclerSaved.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
