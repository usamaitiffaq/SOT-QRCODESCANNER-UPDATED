package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemTabBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.TabItem

class TabAdapter(
    private val tabList: MutableList<TabItem>,
    private val onTabSelected: (Int) -> Unit
) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val binding = ItemTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabList[position], position)
    }

    override fun getItemCount() = tabList.size

    inner class TabViewHolder(private val binding: ItemTabBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tab: TabItem, position: Int) {
            binding.tabText.text = tab.name
            binding.tabText.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (tab.isSelected) R.color.tab_selected_color else R.color.tab_unselected_color
                )
            )
            binding.underline.visibility = if (tab.isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (selectedPosition != position) {
                    // ✅ Update the list properly
                    tabList[selectedPosition].isSelected = false
                    tabList[position].isSelected = true
                    selectedPosition = position

                    notifyDataSetChanged()
                    onTabSelected(position)
                }
            }
        }
    }
}
