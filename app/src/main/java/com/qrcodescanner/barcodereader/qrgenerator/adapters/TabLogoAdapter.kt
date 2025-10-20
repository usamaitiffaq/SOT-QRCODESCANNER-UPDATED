package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemTabBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.TabItem


internal class TabLogoAdapter(
    private val tabList: List<TabItem>,
    private val onTabSelected: (Int) -> Unit) :
    RecyclerView.Adapter<TabLogoAdapter.TabViewHolder>() {
    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val binding = ItemTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabList[position], position)
    }

    override fun getItemCount(): Int {
        return tabList.size
    }

    internal inner class TabViewHolder(private val binding: ItemTabBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(tab: TabItem, position: Int) {
            binding.underline.visibility = View.VISIBLE
            binding.underline.requestLayout()
            binding.underline.invalidate()

            binding.tabText.setText(tab.name)
            binding.tabText.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (tab.isSelected) R.color.tab_selected_color else R.color.tab_unselected_color
                )
            )
            if (binding.underline != null) {
                binding.underline.visibility = if (tab.isSelected) View.VISIBLE else View.GONE
            } else {
                Log.e("TabViewHolder", "Underline view is null")
            }

            itemView.setOnClickListener {
                if (selectedPosition != position) {
                    tabList[selectedPosition].isSelected=false
                    tabList[position].isSelected=true
                    selectedPosition = position
                    notifyDataSetChanged()
                    onTabSelected(position)

                }
            }
        }
    }
}
