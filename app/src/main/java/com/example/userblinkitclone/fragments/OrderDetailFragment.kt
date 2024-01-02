package com.example.userblinkitclone.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProducts
import com.example.userblinkitclone.databinding.FragmentOrderDetailBinding
import com.example.userblinkitclone.roomdb.CartProductTable
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class OrderDetailFragment : Fragment() {
    private val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentOrderDetailBinding
    private lateinit var adapterCartProducts: AdapterCartProducts
    private var status = 0
    private var orderId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        getValues()
        setStatusBarColor()
        settingStatus()
        onBackButtonClicked()
        lifecycleScope.launch { getOrderedProducts() }
        return binding.root
    }

    private fun onBackButtonClicked() {
        binding.tbOrderDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_ordersFragment)
        }
    }

    private suspend  fun getOrderedProducts() {
        viewModel.getOrderedProducts(orderId).collect{cartList ->
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItems.adapter  = adapterCartProducts
            adapterCartProducts.differ.submitList(cartList)
        }
    }

    private fun settingStatus() {
        val statusToViews = mapOf(
            0 to listOf(binding.iv1),
            1 to listOf(binding.iv1, binding.iv2, binding.view1),
            2 to listOf(binding.iv1, binding.iv2, binding.view1, binding.iv3, binding.view2),
            3 to listOf(binding.iv1, binding.iv2, binding.view1, binding.iv3, binding.view2, binding.iv4, binding.view3)
        )

        val viewsToTint = statusToViews.getOrDefault(status, emptyList())

        for (view in viewsToTint) {
            view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
        }


    }

    private fun getValues() {
        val bundle = arguments
        status = bundle?.getInt("status")!!
        orderId = bundle.getString("orderId").toString()
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.white_yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}