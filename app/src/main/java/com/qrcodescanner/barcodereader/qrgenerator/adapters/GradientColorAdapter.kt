package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemGradientColorsBinding

class GradientColorAdapter(
    private val gradientList: List<Drawable>,
    private val onGradientSelected: (Drawable) -> Unit
) : RecyclerView.Adapter<GradientColorAdapter.GradientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientViewHolder {
        val binding = ItemGradientColorsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GradientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GradientViewHolder, position: Int) {
        holder.bind(gradientList[position])
    }

    override fun getItemCount(): Int = gradientList.size

    inner class GradientViewHolder(private val binding: ItemGradientColorsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(gradient: Drawable) {
                binding.icGradientColor.background = gradient
                binding.root.setOnClickListener {
                    onGradientSelected(gradient)
                }
        }
    }
}
