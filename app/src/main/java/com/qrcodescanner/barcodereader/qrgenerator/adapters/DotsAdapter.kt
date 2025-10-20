package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemDotsBinding

class DotsAdapter(
    private val backgroundImageList: List<Drawable>,
    private val onDotsSelected: (Drawable,Int) -> Unit
) : RecyclerView.Adapter<DotsAdapter.DotsImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DotsImageViewHolder {
        val binding = ItemDotsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DotsImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DotsImageViewHolder, position: Int) {
        holder.bind(backgroundImageList[position],position)
    }

    override fun getItemCount(): Int = backgroundImageList.size

    inner class DotsImageViewHolder(private val binding: ItemDotsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(backgroundImage: Drawable, position: Int) {
            binding.icDots.background = backgroundImage
            binding.root.setOnClickListener {
                onDotsSelected(backgroundImage,position)
            }
        }
    }
}
