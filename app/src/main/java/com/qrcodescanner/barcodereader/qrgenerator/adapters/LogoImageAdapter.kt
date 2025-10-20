package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemBackgroundImagesBinding

class LogoImageAdapter(
    private val logoImageList: List<Drawable>,
    private val onlogoSelected: (Drawable, Any?) -> Unit
) : RecyclerView.Adapter<LogoImageAdapter.BackgroundImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundImageViewHolder {
        val binding = ItemBackgroundImagesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BackgroundImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackgroundImageViewHolder, position: Int) {
        holder.bind(logoImageList[position],position)
    }

    override fun getItemCount(): Int = logoImageList.size

    inner class BackgroundImageViewHolder(private val binding: ItemBackgroundImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(backgroundImage: Drawable, position: Int) {
            binding.icBackgroundImage.background = backgroundImage
            binding.root.setOnClickListener {
                onlogoSelected(backgroundImage,position)
            }
        }
    }
}
