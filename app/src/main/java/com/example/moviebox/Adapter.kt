package com.example.moviebox

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moviebox.data.CastMember
import com.example.moviebox.data.FilmEntity
import com.example.moviebox.data.Films
import com.example.moviebox.data.Review
import com.example.moviebox.data.WatchProvider
import com.example.moviebox.databinding.ItemBinding
import com.example.moviebox.databinding.ItemCastBinding
import com.example.moviebox.databinding.ItemEtcBinding
import com.example.moviebox.databinding.ItemProviderLogoBinding
import com.example.moviebox.databinding.ItemReviewBinding
import com.example.moviebox.databinding.ItemSavedBinding
import com.example.moviebox.databinding.ItemTrendBinding

class Adapter(
    initialData: List<Films> = emptyList(),
    private val onClick: (Films) -> Unit
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private val data: MutableList<Films> = initialData.toMutableList()

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
        holder.itemView.setOnClickListener { onClick(item) }
        if (!item.poster_path.isNullOrEmpty()) {
            val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
            Glide.with(holder.itemView.context)
                .load(image)
                .into(holder.binding.imageSrch)
        } else {
            holder.binding.imageSrch.setImageResource(R.drawable.def2)
        }
    }

    override fun getItemCount(): Int = data.size

    fun submitList(newList: List<Films>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItems(moreList: List<Films>) {
        val startPos = data.size
        data.addAll(moreList)
        notifyItemRangeInserted(startPos, moreList.size)
    }
}

class TrendingAdapter(
    initialData: List<Films> = emptyList(),
    private val onClick: (Films) -> Unit
) : RecyclerView.Adapter<TrendingAdapter.ViewHolder>() {

    private val data: MutableList<Films> = initialData.toMutableList()

    class ViewHolder(val binding: ItemTrendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.setOnClickListener { onClick(item) }
        val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
        Glide.with(holder.itemView.context)
            .load(image)
            .into(holder.binding.imgMovie)
    }

    override fun getItemCount(): Int = data.size

    fun submitList(newList: List<Films>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItems(moreList: List<Films>) {
        val startPos = data.size
        data.addAll(moreList)
        notifyItemRangeInserted(startPos, moreList.size)
    }
}

class SecondAdapter(
    initialData: List<Films> = emptyList(),
    private val onClick: (Films) -> Unit
) : RecyclerView.Adapter<SecondAdapter.ViewHolder>() {

    private val data: MutableList<Films> = initialData.toMutableList()

    class ViewHolder(val binding: ItemEtcBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEtcBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.setOnClickListener { onClick(item) }
        val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
        Glide.with(holder.itemView.context)
            .load(image)
            .into(holder.binding.imageItem)
    }

    override fun getItemCount(): Int = data.size

    fun submitList(newList: List<Films>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItems(moreList: List<Films>) {
        val startPos = data.size
        data.addAll(moreList)
        notifyItemRangeInserted(startPos, moreList.size)
    }
}

class SavedAdapter(
    private var data: MutableList<FilmEntity>,
    private val onDelete: (FilmEntity) -> Unit,
    private val onClick: (FilmEntity) -> Unit
) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSavedBinding) : RecyclerView.ViewHolder(binding.root)

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

        holder.itemView.setOnClickListener { onClick(item) }

        if (!item.poster_path.isNullOrEmpty()) {
            val image = "https://image.tmdb.org/t/p/w500${item.poster_path}"
            Glide.with(holder.itemView.context)
                .load(image)
                .into(holder.binding.imageSrch)
        } else {
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

class CastAdapter(private val data: List<CastMember>) : RecyclerView.Adapter<CastAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.txtCastName.text = item.name
        holder.binding.txtCharacterName.text = item.character ?: ""

        if (!item.profile_path.isNullOrEmpty()) {
            val imageUrl = "https://image.tmdb.org/t/p/w185${item.profile_path}"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.def2)
                .error(R.drawable.def2)
                .into(holder.binding.imgCastProfile)
        } else {
            holder.binding.imgCastProfile.setImageResource(R.drawable.def2)
        }
    }

    override fun getItemCount(): Int = data.size
}

class ReviewAdapter(private val data: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.txtReviewAuthor.text = item.author
        val rating = item.author_details?.rating
        holder.binding.txtReviewRating.text = if (rating != null) "⭐ ${rating.toInt()}/10" else ""
        holder.binding.txtReviewContent.text = item.content
    }

    override fun getItemCount(): Int = data.size
}

class ProviderLogoAdapter(private val data: List<WatchProvider>) : RecyclerView.Adapter<ProviderLogoAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemProviderLogoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProviderLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.txtProviderName.text = item.provider_name

        if (!item.logo_path.isNullOrEmpty()) {
            val logoUrl = "https://image.tmdb.org/t/p/w92${item.logo_path}"
            Glide.with(holder.itemView.context)
                .load(logoUrl)
                .placeholder(R.drawable.def2)
                .error(R.drawable.def2)
                .into(holder.binding.imgProviderLogo)
        } else {
            holder.binding.imgProviderLogo.setImageResource(R.drawable.def2)
        }
    }

    override fun getItemCount(): Int = data.size
}
