package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R

import com.qrcodescanner.barcodereader.qrgenerator.adapters.TabAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.TemplateAdapter
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentTempletesBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.TabItem
import com.qrcodescanner.barcodereader.qrgenerator.models.TemplateModel
import com.qrcodescanner.barcodereader.qrgenerator.utils.TemplateUtils


class TempletesFragment : Fragment() {

    private var navController: NavController? = null
    private lateinit var binding: FragmentTempletesBinding
    private lateinit var tabAdapter: TabAdapter
    private lateinit var templateAdapter: TemplateAdapter

    private val tabNames = listOf(
        "Hot", "New", "Social", "Wifi", "Event",
        "Business",   "Work","BlockChain"
    )

    private val tabList = tabNames.map { TabItem(it) }.toMutableList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTempletesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        clickEvents()
    }

    private fun initViews() {
        tabAdapter = TabAdapter(tabList) { position ->
            updateContent(position)  // Ensure it's called after initializing templateAdapter
        }
        binding.tabRecyclerView.adapter = tabAdapter

        // ✅ Initialize templateAdapter before updating content
        templateAdapter = TemplateAdapter(emptyList()) { selectedTemplate ->
            val action =
                TempletesFragmentDirections.actionNavTemplateToQrCustumization("https://en.wikipedia.org/wiki/Muhammad_Ali_Jinnah")
            findNavController().navigate(action)
        }
        binding.templateRecyclerView.adapter = templateAdapter

        // Set the first tab as selected and update content
        tabList[0].isSelected = true
        tabAdapter.notifyDataSetChanged()
        updateContent(0)  // Call it after initializing templateAdapter
    }


    private fun clickEvents() {

        binding.ivBack.setOnClickListener {
       navController!!.navigate(R.id.action_nav_template_to_home)
        }
    }

    private fun updateContent(position: Int) {
        Log.d(
            "TemplateCheck",
            "Template list at position $position: ${TemplateUtils.getTemplateList[position]}"
        )

        try {
            if (::templateAdapter.isInitialized) {
                Log.d("hsdfhdhj", "updateContent: $position")
                templateAdapter.updateList(TemplateUtils.getTemplateList[position] ?: emptyList())
            } else {
                Log.e("TemplateAdapter", "templateAdapter is not initialized")
            }
        } catch (e: Exception) {
            Log.e("UpdateContentError", "Exception in updateContent: ${e.message}")
        }
    }


    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }
}
