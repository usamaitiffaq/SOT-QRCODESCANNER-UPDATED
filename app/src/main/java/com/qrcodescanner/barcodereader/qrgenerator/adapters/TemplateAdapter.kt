package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemTemplateBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.TemplateModel

class TemplateAdapter(
    private var itemList: List<TemplateModel>,
    private val onItemClick: (TemplateModel) -> Unit
) :
    RecyclerView.Adapter<TemplateAdapter.ContentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding =
            ItemTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<TemplateModel>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class ContentViewHolder(private val binding: ItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TemplateModel) {
            binding.ivTemplate.setImageResource(item.templateImage)
            binding.root.setOnClickListener { onItemClick(item) }

        }
    }
}
