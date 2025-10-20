package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemDotsBinding
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemEyesBinding

class EyesAdapter(
    private val backgroundImageList: List<Drawable>,
    private val onDotsSelected: (Drawable) -> Unit
) : RecyclerView.Adapter<EyesAdapter.EyesImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EyesImageViewHolder {
        val binding = ItemEyesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EyesImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EyesImageViewHolder, position: Int) {
        holder.bind(backgroundImageList[position])
    }

    override fun getItemCount(): Int = backgroundImageList.size

    inner class EyesImageViewHolder(private val binding: ItemEyesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(backgroundImage: Drawable) {
            binding.icEyes.background = backgroundImage
            binding.root.setOnClickListener {
                onDotsSelected(backgroundImage)
            }
        }
    }
}
