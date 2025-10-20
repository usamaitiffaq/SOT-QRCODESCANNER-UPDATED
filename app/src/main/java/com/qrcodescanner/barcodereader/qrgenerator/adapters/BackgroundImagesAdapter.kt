package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemBackgroundImagesBinding

class BackgroundImagesAdapter(
    private val backgroundImageList: List<Drawable>,
    private val onBackgroundSelected: (Drawable) -> Unit
) : RecyclerView.Adapter<BackgroundImagesAdapter.BackgroundImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundImageViewHolder {
        val binding = ItemBackgroundImagesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BackgroundImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackgroundImageViewHolder, position: Int) {
        holder.bind(backgroundImageList[position])
    }

    override fun getItemCount(): Int = backgroundImageList.size

    inner class BackgroundImageViewHolder(private val binding: ItemBackgroundImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(backgroundImage: Drawable) {
            binding.icBackgroundImage.background = backgroundImage
            binding.root.setOnClickListener {
                onBackgroundSelected(backgroundImage)
            }
        }
    }
}
