package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemsFontsBinding

class FontsLogoAdapter(
    private val fontList: List<String>,
    private val onFontSelected: (String) -> Unit
) : RecyclerView.Adapter<FontsLogoAdapter.FontViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        val binding = ItemsFontsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FontViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
        holder.bind(fontList[position], holder.itemView.context)
    }

    override fun getItemCount(): Int = fontList.size

    inner class FontViewHolder(private val binding: ItemsFontsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fontText: String, requireContext: Context) {
            // Convert string name (like "Poppins") to font resource ID from res/font/
            val fontResId = requireContext.resources.getIdentifier(
                fontText.lowercase(), // Make sure it matches the file name exactly
                "font",
                requireContext.packageName
            )

            if (fontResId != 0) {
                val typeface = ResourcesCompat.getFont(requireContext, fontResId)
                binding.txtFont.typeface = typeface
            }

            binding.txtFont.text = fontText.replace('_', ' ') // Optional: make it look nicer

            binding.root.setOnClickListener {
                onFontSelected(fontText)
            }
        }

    }
}
