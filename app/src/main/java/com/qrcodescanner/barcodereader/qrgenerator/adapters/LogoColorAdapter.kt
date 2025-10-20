package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemSolidColorsBinding

class LogoColorAdapter(
    private val colorList: List<Int>,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<LogoColorAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemSolidColorsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colorList[position])
    }

    override fun getItemCount(): Int = colorList.size

    inner class ColorViewHolder(private val binding: ItemSolidColorsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(color: Int) {
            binding.icColor.setBackgroundColor(color)

            // Click listener to return selected color
            binding.root.setOnClickListener {
                onColorSelected(color)
            }
        }
    }
}
