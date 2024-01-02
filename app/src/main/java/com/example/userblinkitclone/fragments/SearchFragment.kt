package com.example.userblinkitclone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.databinding.FragmentSearchBinding
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentSearchBinding
    private lateinit var adapterProduct: AdapterProduct
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)

        getAllTheProducts()

        backToHomeFragment()
        return binding.root
    }

    private fun backToHomeFragment() {
        binding.searchEt.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

    }

    private fun getAllTheProducts() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllTheProducts().collect{

                if(it.isEmpty()){
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                }
                else{
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                adapterProduct = AdapterProduct()
                binding.rvProducts.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
//                adapterProduct.originalList = it as ArrayList<Product>
                binding.shimmerViewContainer.visibility = View.GONE

            }
        }
    }

}