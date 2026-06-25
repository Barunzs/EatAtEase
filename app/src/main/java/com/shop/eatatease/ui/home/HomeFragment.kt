package com.shop.eatatease.ui.home

import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.shop.eatatease.R
import com.shop.eatatease.data.model.BadgeType
import com.shop.eatatease.data.model.Product
import com.shop.eatatease.databinding.FragmentHomeBinding
import com.shop.eatatease.ui.productdetail.ProductDetailFragment

/**
 * Fragment that displays the Realistic Home screen matching the Stitch design:
 * - Search bar
 * - Horizontal category chips
 * - Hero banner with sale promotion
 * - 2-column product grid
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding?.root

        setupCategoryChips(homeViewModel)
        setupProductsGrid(homeViewModel)
        setupHeroBanner()

        return root
    }

    /**
     * Set up the horizontal category chip RecyclerView.
     */
    private fun setupCategoryChips(viewModel: HomeViewModel) {
        val chipAdapter = CategoryChipAdapter { chipName ->
            Toast.makeText(context, chipName, Toast.LENGTH_SHORT).show()
        }
        binding?.categoryChipsRecyclerview?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = chipAdapter
            setHasFixedSize(false)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.right = 8
                }
            })
        }

        viewModel.categoryChips.observe(viewLifecycleOwner) { chips ->
            chipAdapter.submitList(chips)
        }
    }

    /**
     * Set up the 2-column product grid RecyclerView.
     */
    private fun setupProductsGrid(viewModel: HomeViewModel) {
        val productAdapter = ProductAdapter(
            onAddClick = { product ->
                com.shop.eatatease.data.CartManager.addProduct(product)
                Toast.makeText(
                    context,
                    getString(R.string.checkout_added_to_cart, product.name),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onItemClick = { product ->
                navigateToProductDetail(product)
            }
        )
        binding?.productsRecyclerview?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(6, 6, 6, 6)
                }
            })
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }
    }

    /**
     * Navigate to the product detail page, passing all product data.
     */
    private fun navigateToProductDetail(product: Product) {
        val bundle = Bundle().apply {
            putString(ProductDetailFragment.ARG_PRODUCT_ID, product.id)
            putString(ProductDetailFragment.ARG_PRODUCT_NAME, product.name)
            putString(ProductDetailFragment.ARG_PRODUCT_CATEGORY, product.category)
            putString(ProductDetailFragment.ARG_PRODUCT_PRICE, product.price)
            putString(ProductDetailFragment.ARG_PRODUCT_ORIGINAL_PRICE, product.originalPrice)
            putInt(ProductDetailFragment.ARG_PRODUCT_IMAGE_RES_ID, product.imageResId)
            putString(ProductDetailFragment.ARG_PRODUCT_DESCRIPTION, product.description)
            putFloat(ProductDetailFragment.ARG_PRODUCT_RATING, product.rating)
            putInt(ProductDetailFragment.ARG_PRODUCT_REVIEW_COUNT, product.reviewCount)
            putStringArrayList(ProductDetailFragment.ARG_PRODUCT_FEATURES, ArrayList(product.features))
        }
        findNavController().navigate(R.id.action_home_to_product_detail, bundle)
    }

    /**
     * Hero banner click handler.
     */
    private fun setupHeroBanner() {
        binding?.btnShopNow?.setOnClickListener {
            Toast.makeText(context, "Shop Now!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ═══════════════════════════════════════════════════════════════
    // Category Chip Adapter
    // ═══════════════════════════════════════════════════════════════

    class CategoryChipAdapter(
        private val onChipClick: (String) -> Unit
    ) : ListAdapter<String, CategoryChipAdapter.ChipViewHolder>(
        object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    ) {
        private var selectedPosition = 0

        class ChipViewHolder(val textView: MaterialTextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_chip, parent, false) as MaterialTextView
            return ChipViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
            val chipName = getItem(position)
            holder.textView.text = chipName

            if (position == selectedPosition) {
                holder.textView.setBackgroundResource(R.drawable.bg_category_chip_active)
                holder.textView.setTextColor(0xFFFFFFFF.toInt())
            } else {
                holder.textView.setBackgroundResource(R.drawable.bg_category_chip_inactive)
                holder.textView.setTextColor(ContextCompat.getColor(holder.textView.context, R.color.text_primary))
            }

            holder.textView.setOnClickListener {
                val oldSelected = selectedPosition
                selectedPosition = holder.bindingAdapterPosition
                notifyItemChanged(oldSelected)
                notifyItemChanged(selectedPosition)
                onChipClick(chipName)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Product Card Adapter
    // ═══════════════════════════════════════════════════════════════

    class ProductAdapter(
        private val onAddClick: (Product) -> Unit,
        private val onItemClick: (Product) -> Unit
    ) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(
        object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
        }
    ) {

        class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val productImage: ImageView = itemView.findViewById(R.id.product_image)
            val productBadge: MaterialTextView = itemView.findViewById(R.id.product_badge)
            val productCategory: MaterialTextView = itemView.findViewById(R.id.product_category)
            val productName: MaterialTextView = itemView.findViewById(R.id.product_name)
            val productPrice: MaterialTextView = itemView.findViewById(R.id.product_price)
            val productOriginalPrice: MaterialTextView = itemView.findViewById(R.id.product_original_price)
            val btnAddToCart: ImageButton = itemView.findViewById(R.id.btn_add_to_cart)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_card, parent, false)
            return ProductViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = getItem(position)

            // Product image
            holder.productImage.setImageDrawable(
                ResourcesCompat.getDrawable(holder.productImage.resources, product.imageResId, null)
            )

            // Badge
            when (product.badgeType) {
                BadgeType.NEW -> {
                    holder.productBadge.visibility = View.VISIBLE
                    holder.productBadge.text = product.badgeText
                    holder.productBadge.setBackgroundResource(R.drawable.bg_product_badge_new)
                }
                BadgeType.SALE -> {
                    holder.productBadge.visibility = View.VISIBLE
                    holder.productBadge.text = product.badgeText
                    holder.productBadge.setBackgroundResource(R.drawable.bg_product_badge_sale)
                }
                BadgeType.NONE -> {
                    holder.productBadge.visibility = View.GONE
                }
            }

            // Text fields
            holder.productCategory.text = product.category
            holder.productName.text = product.name
            holder.productPrice.text = product.price

            // Original price (strikethrough)
            if (product.originalPrice != null) {
                holder.productOriginalPrice.visibility = View.VISIBLE
                holder.productOriginalPrice.text = product.originalPrice
                holder.productOriginalPrice.paintFlags =
                    holder.productOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.productOriginalPrice.visibility = View.GONE
            }

            // Add to cart click
            holder.btnAddToCart.setOnClickListener {
                onAddClick(product)
            }

            // Whole card click → navigate to detail
            holder.itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }
}
