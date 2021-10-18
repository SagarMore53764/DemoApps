package com.example.mytask.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mytask.R
import com.example.mytask.databinding.ItemCellLayoutBinding
import com.example.mytask.model.ProductDTO

class ProductsListAdapter : RecyclerView.Adapter<ProductsListAdapter.ViewHolder>() {

    var arrayList = listOf<ProductDTO>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = arrayList[position]
        holder.bind(position, item)
    }

    class ViewHolder(private val binding: ItemCellLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            position: Int,
            dto: ProductDTO
        ) {

            val placeholder = R.drawable.placeholder

            Glide.with(binding.itemImg.context)
                .load(dto.imageUrl ?: "")
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.itemImg)

            binding.itemImg.clipToOutline = true

            binding.itemImg.setOnClickListener {
                binding.checkbox.visibility = View.GONE
            }

            binding.itemImg.setOnLongClickListener {
                binding.checkbox.visibility = View.VISIBLE
                true
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ItemCellLayoutBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(
                    binding
                )
            }
        }
    }

}