package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemScanResultsBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.ScanResultOption

class ScanResultSocialAdapter(
    private val items: List<ScanResultOption>,
    private val onItemClick: (ScanResultOption) -> Unit
) : RecyclerView.Adapter<ScanResultSocialAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemScanResultsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanResultOption) {
            binding.icOption.setImageResource(item.iconOption)
            binding.txtOptionname.text = item.optionName

            binding.root.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}
