package com.shop.eatatease.ui.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shop.eatatease.R
import com.shop.eatatease.data.model.Product
import com.shop.eatatease.databinding.FragmentProductListBinding

/**
 * Product List screen — matches the Stitch "Product List" design:
 * sticky top bar | "Curated Selection" header | vertical list of product cards
 */
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProductList()
    }

    private fun setupProductList() {
        val adapter = ProductListAdapter(
            onAddClick = { product ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.checkout_added_to_cart, product.name),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onItemClick = { product -> navigateToDetail(product) }
        )

        binding.plProductsRecyclerview.adapter = adapter

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
    }

    private fun navigateToDetail(product: Product) {
        val args = Bundle().apply {
            putString("product_id",           product.id)
            putString("product_name",          product.name)
            putString("product_category",      product.category)
            putString("product_price",         product.price)
            putString("product_original_price", product.originalPrice)
            putInt("product_image_res_id",     product.imageResId)
            putString("product_description",   product.description)
            putFloat("product_rating",         product.rating)
            putInt("product_review_count",     product.reviewCount)
        }
        findNavController().navigate(R.id.action_product_list_to_product_detail, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
