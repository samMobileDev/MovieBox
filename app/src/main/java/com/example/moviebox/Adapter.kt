package com.example.moviebox

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moviebox.data.FilmEntity
import com.example.moviebox.data.Films
import com.example.moviebox.databinding.ItemBinding
import com.example.moviebox.databinding.ItemEtcBinding
import com.example.moviebox.databinding.ItemSavedBinding
import com.example.moviebox.databinding.ItemTrendBinding

class Adapter(private val data: List<Films>, private val onClick: (Films) -> Unit) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.title.text = item.title
        holder.binding.overview.text = item.overview
        holder.binding.voteAverage.text = "⭐ ${item.vote_average}"
        holder.itemView.setOnClickListener{onClick(item)}
       if (!item.poster_path.isNullOrEmpty()){
           val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
           Glide.with(holder.itemView.context)
               .load(image)
               .into(holder.binding.imageSrch)
       }else{
           holder.binding.imageSrch.setImageResource(R.drawable.def2)
       }


    }
    override fun getItemCount(): Int {
        return data.size
    }

}

class TrendingAdapter(private val data: List<Films>, private val onClick: (Films) -> Unit) : RecyclerView.Adapter<TrendingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTrendBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = data[position]
    holder.itemView.setOnClickListener {onClick(item)}
    val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
    Glide.with(holder.itemView.context)
        .load(image)
        .into(holder.binding.imgMovie)
}
override fun getItemCount(): Int {
    return data.size
}
}

class SecondAdapter(private val data: List<Films>, private val onClick: (Films) -> Unit) : RecyclerView.Adapter<SecondAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemEtcBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEtcBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onClick(data[position]) }
        val item = data[position]
        val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
        Glide.with(holder.itemView.context)
            .load(image)
            .into(holder.binding.imageItem)
    }
    override fun getItemCount(): Int {
        return data.size
    }
    }


//GET DATA FROM TABLE ENTITY THEN SHOW AS A ITEM IN RECYCLER VIEW
class SavedAdapter(
    private var data: MutableList<FilmEntity>,
    private val onDelete: (FilmEntity) -> Unit,
    private val onClick: (FilmEntity) -> Unit) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSavedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.binding.title.text = item.title
        holder.binding.overview.text = item.overview
        holder.binding.voteAverage.text = "⭐ ${item.vote_average}"


        holder.itemView.setOnClickListener {
            onClick(item)
        }

        if (!item.poster_path.isNullOrEmpty()) {
            val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
            Glide.with(holder.itemView.context)
                .load(image)
                .into(holder.binding.imageSrch)
        }else{
            holder.binding.imageSrch.setImageResource(R.drawable.def2)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDelete(item)

            val index = holder.adapterPosition
            if (index != RecyclerView.NO_POSITION) {
                data.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    override fun getItemCount(): Int = data.size
}
