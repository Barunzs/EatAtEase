package com.shop.eatatease.ui.productdetail

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import com.shop.eatatease.R
import com.shop.eatatease.databinding.FragmentProductDetailBinding

/**
 * Displays full product details matching the Stitch "Realistic Product Detail" design.
 * Receives product data via fragment arguments (Bundle).
 */
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding

    private var quantity = 1

    companion object {
        const val ARG_PRODUCT_ID = "product_id"
        const val ARG_PRODUCT_NAME = "product_name"
        const val ARG_PRODUCT_CATEGORY = "product_category"
        const val ARG_PRODUCT_PRICE = "product_price"
        const val ARG_PRODUCT_ORIGINAL_PRICE = "product_original_price"
        const val ARG_PRODUCT_IMAGE_RES_ID = "product_image_res_id"
        const val ARG_PRODUCT_DESCRIPTION = "product_description"
        const val ARG_PRODUCT_RATING = "product_rating"
        const val ARG_PRODUCT_REVIEW_COUNT = "product_review_count"
        const val ARG_PRODUCT_FEATURES = "product_features"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        populateProductData()
        setupActions()

        return binding?.root
    }

    /**
     * Populate the UI with product data from fragment arguments.
     */
    private fun populateProductData() {
        val args = arguments ?: return

        val name = args.getString(ARG_PRODUCT_NAME, "")
        val category = args.getString(ARG_PRODUCT_CATEGORY, "")
        val price = args.getString(ARG_PRODUCT_PRICE, "")
        val originalPrice = args.getString(ARG_PRODUCT_ORIGINAL_PRICE)
        val imageResId = args.getInt(ARG_PRODUCT_IMAGE_RES_ID, 0)
        val description = args.getString(ARG_PRODUCT_DESCRIPTION, "")
        val rating = args.getFloat(ARG_PRODUCT_RATING, 0f)
        val reviewCount = args.getInt(ARG_PRODUCT_REVIEW_COUNT, 0)
        val features = args.getStringArrayList(ARG_PRODUCT_FEATURES) ?: emptyList()

        // Product image
        if (imageResId != 0) {
            binding?.detailProductImage?.setImageDrawable(
                ResourcesCompat.getDrawable(resources, imageResId, null)
            )
        }

        // Product name
        binding?.detailProductName?.text = name

        // Rating stars
        setupStars(rating)
        binding?.detailRatingText?.text = String.format("%.1f (%,d reviews)", rating, reviewCount)

        // Price — format with .00 if not already formatted
        val formattedPrice = if (price.contains(".")) price else "$price.00"
        binding?.detailPrice?.text = formattedPrice

        // Original price (for sale items)
        if (originalPrice != null) {
            binding?.detailOriginalPrice?.visibility = View.VISIBLE
            val formattedOriginal = if (originalPrice.contains(".")) originalPrice else "$originalPrice.00"
            binding?.detailOriginalPrice?.text = formattedOriginal
            binding?.detailOriginalPrice?.paintFlags =
                (binding?.detailOriginalPrice?.paintFlags ?: 0) or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding?.detailOriginalPrice?.visibility = View.GONE
        }

        // Description
        binding?.detailDescription?.text = description

        // Features
        setupFeatures(features)
    }

    /**
     * Render star icons based on the rating value.
     */
    private fun setupStars(rating: Float) {
        val container = binding?.detailStarsContainer ?: return
        container.removeAllViews()

        val fullStars = rating.toInt()
        val hasHalfStar = (rating - fullStars) >= 0.3f
        val totalStars = 5

        for (i in 0 until totalStars) {
            val star = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 3,
                    resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 3
                ).apply {
                    marginEnd = 2
                }
            }

            when {
                i < fullStars -> star.setImageResource(R.drawable.ic_star_filled)
                i == fullStars && hasHalfStar -> star.setImageResource(R.drawable.ic_star_outline)
                else -> star.setImageResource(R.drawable.ic_star_outline)
            }

            container.addView(star)
        }
    }

    /**
     * Build the features list with icon + text rows.
     */
    private fun setupFeatures(features: List<String>) {
        val container = binding?.detailFeaturesContainer ?: return
        container.removeAllViews()

        for (feature in features) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 12
                }
            }

            // Icon circle
            val iconFrame = android.widget.FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(36.dpToPx(), 36.dpToPx())
                setBackgroundResource(R.drawable.bg_feature_icon)
            }
            val icon = ImageView(requireContext()).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    18.dpToPx(), 18.dpToPx(), android.view.Gravity.CENTER
                )
                setImageResource(R.drawable.ic_check)
            }
            iconFrame.addView(icon)
            row.addView(iconFrame)

            // Text
            val text = MaterialTextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply {
                    marginStart = 12.dpToPx()
                }
                this.text = feature
                setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_secondary))
                textSize = 14f
            }
            row.addView(text)

            container.addView(row)
        }
    }

    /**
     * Set up button click handlers.
     */
    private fun setupActions() {
        // Back button
        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Favorite
        binding?.btnFavorite?.setOnClickListener {
            Toast.makeText(context, "Added to favorites!", Toast.LENGTH_SHORT).show()
        }

        // Share
        binding?.btnShare?.setOnClickListener {
            Toast.makeText(context, "Share coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Quantity controls
        binding?.btnQuantityMinus?.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding?.detailQuantity?.text = quantity.toString()
            }
        }
        binding?.btnQuantityPlus?.setOnClickListener {
            if (quantity < 99) {
                quantity++
                binding?.detailQuantity?.text = quantity.toString()
            }
        }

        // Add to cart → add to CartManager, then navigate to checkout
        binding?.btnAddToCart?.setOnClickListener {
            val args = arguments ?: return@setOnClickListener
            val productId = args.getString(ARG_PRODUCT_ID, "")
            val name = args.getString(ARG_PRODUCT_NAME, "")
            val category = args.getString(ARG_PRODUCT_CATEGORY, "")
            val price = args.getString(ARG_PRODUCT_PRICE, "")
            val imageResId = args.getInt(ARG_PRODUCT_IMAGE_RES_ID, 0)

            // Add to cart
            com.shop.eatatease.data.CartManager.addItem(
                productId = productId,
                name = name,
                category = category,
                price = price,
                imageResId = imageResId,
                quantity = quantity
            )

            Toast.makeText(
                context,
                getString(R.string.checkout_added_to_cart, name),
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to checkout
            findNavController().navigate(R.id.action_product_detail_to_checkout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Extension to convert dp to pixels.
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
