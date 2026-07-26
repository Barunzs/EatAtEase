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
        setupSubcategoryChips(homeViewModel)
        setupProductsGrid(homeViewModel)
        setupHeroBanner()

        return root
    }

    /**
     * Set up the horizontal category chip RecyclerView.
     */
    private fun setupCategoryChips(viewModel: HomeViewModel) {
        val chipAdapter = CategoryChipAdapter { chipName ->
            viewModel.filterByCategory(chipName)
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
     * Set up the subcategory chips RecyclerView below the category chips.
     * Visible only when a specific category is selected; hidden for "All".
     */
    private fun setupSubcategoryChips(viewModel: HomeViewModel) {
        // Navigate to Product List when a subcategory card is tapped
        val navigateToList = { subcategoryName: String ->
            val args = android.os.Bundle().apply {
                putString("filter_subcategory", subcategoryName)
            }
            findNavController().navigate(R.id.action_home_to_product_list, args)
        }

        val cardAdapter = SubcategoryCardAdapter(onCardClick = navigateToList)
        binding?.subcategoryCardsRecyclerview?.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
            adapter = cardAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }

        viewModel.selectedCategoryName.observe(viewLifecycleOwner) { name ->
            binding?.subcategoryLabel?.text = when {
                name == null  -> ""
                name == "All" -> "All Categories"
                else          -> name
            }
        }

        viewModel.subcategories.observe(viewLifecycleOwner) { subs ->
            cardAdapter.submitList(subs)
            val section = binding?.subcategorySection
            if (subs.isNullOrEmpty()) {
                section?.animate()?.alpha(0f)?.setDuration(180)?.withEndAction {
                    section.visibility = View.GONE
                }?.start()
            } else {
                section?.alpha = 0f
                section?.visibility = View.VISIBLE
                section?.animate()?.alpha(1f)?.setDuration(220)?.start()
            }
        }

        // "View All" in subcategory header → Product List (no filter)
        binding?.subcategoryViewAll?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_product_list)
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
     * Hero banner + "View All" click handlers.
     */
    private fun setupHeroBanner() {
        binding?.btnShopNow?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_product_list)
        }
        binding?.btnViewAll?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_product_list)
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
    // Subcategory Card Adapter
    // ═══════════════════════════════════════════════════════════════

    class SubcategoryCardAdapter(
        private val onCardClick: (String) -> Unit = {}
    ) : ListAdapter<String, SubcategoryCardAdapter.CardViewHolder>(
        object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    ) {
        /**
         * Bundles the emoji icon and the MD3-tonal header colour for a subcategory card.
         * @param emoji  Unicode emoji that best represents the subcategory
         * @param headerColor  ARGB int used as the full-bleed card-header background
         */
        private data class SubcategoryInfo(val emoji: String, val headerColor: Int)

        companion object {
            /** Returns [SubcategoryInfo] for a given subcategory name using keyword matching. */
            private fun infoFor(name: String): SubcategoryInfo = when {
                // ── Food & Grocery ──────────────────────────────────
                name.contains("kitchen", true) || name.contains("cook", true) ->
                    SubcategoryInfo("🍳", 0xFFFF8A65.toInt())   // deep orange tonal
                name.contains("breakfast", true) ->
                    SubcategoryInfo("🥞", 0xFFFFCC02.toInt())   // amber
                name.contains("snack", true) || name.contains("chips", true) ->
                    SubcategoryInfo("\uD83C\uDF5F", 0xFFFF7043.toInt()) // red-orange
                name.contains("bakery", true) || name.contains("bread", true) ->
                    SubcategoryInfo("🍞", 0xFFBCAAA4.toInt())   // warm brown
                name.contains("drink", true) || name.contains("beverage", true) || name.contains("juice", true) ->
                    SubcategoryInfo("🥤", 0xFF4FC3F7.toInt())   // light blue
                name.contains("dairy", true) || name.contains("milk", true) ->
                    SubcategoryInfo("🥛", 0xFFB3E5FC.toInt())   // pale blue
                name.contains("fruit", true) ->
                    SubcategoryInfo("🍎", 0xFFEF9A9A.toInt())   // soft red
                name.contains("vegetable", true) || name.contains("veggie", true) ->
                    SubcategoryInfo("🥦", 0xFFA5D6A7.toInt())   // soft green
                name.contains("rice", true) || name.contains("grain", true) ->
                    SubcategoryInfo("🌾", 0xFFFFE082.toInt())   // warm yellow
                name.contains("meat", true) || name.contains("chicken", true) || name.contains("fish", true) ->
                    SubcategoryInfo("🍗", 0xFFFFAB91.toInt())   // salmon
                name.contains("spice", true) || name.contains("masala", true) ->
                    SubcategoryInfo("🌶️", 0xFFEF5350.toInt())   // chilli red
                name.contains("sweet", true) || name.contains("dessert", true) || name.contains("cake", true) ->
                    SubcategoryInfo("🍰", 0xFFF48FB1.toInt())   // pink
                name.contains("oil", true) || name.contains("ghee", true) ->
                    SubcategoryInfo("🫙", 0xFFFFD54F.toInt())   // golden
                name.contains("noodle", true) || name.contains("pasta", true) ->
                    SubcategoryInfo("🍜", 0xFFFFF176.toInt())   // light yellow
                name.contains("tea", true) || name.contains("coffee", true) ->
                    SubcategoryInfo("☕", 0xFFA1887F.toInt())   // coffee brown

                // ── Home Décor ───────────────────────────────────────
                name.contains("clock", true) ->
                    SubcategoryInfo("🕒", 0xFF90CAF9.toInt())   // steel blue
                name.contains("paint", true) || name.contains("art", true) ->
                    SubcategoryInfo("🖼️", 0xFFCE93D8.toInt())   // lavender
                name.contains("wall", true) || name.contains("decor", true) ->
                    SubcategoryInfo("🏮", 0xFFEF9A9A.toInt())   // warm red
                name.contains("lamp", true) || name.contains("light", true) ->
                    SubcategoryInfo("💡", 0xFFFFEE58.toInt())   // yellow
                name.contains("curtain", true) || name.contains("blind", true) ->
                    SubcategoryInfo("🪟", 0xFF80CBC4.toInt())   // teal
                name.contains("cushion", true) || name.contains("pillow", true) ->
                    SubcategoryInfo("🛋️", 0xFFB39DDB.toInt())   // soft purple
                name.contains("rug", true) || name.contains("carpet", true) ->
                    SubcategoryInfo("🎨", 0xFFFFAB91.toInt())   // peach

                // ── Fashion & Lifestyle ──────────────────────────────
                name.contains("saree", true) ->
                    SubcategoryInfo("🥻", 0xFFE91E63.toInt())   // hot pink
                name.contains("wear", true) || name.contains("cloth", true) ->
                    SubcategoryInfo("👗", 0xFFF06292.toInt())   // pink
                name.contains("men", true) ->
                    SubcategoryInfo("👔", 0xFF42A5F5.toInt())   // blue
                name.contains("kids", true) || name.contains("children", true) ->
                    SubcategoryInfo("🧸", 0xFFFFB74D.toInt())   // orange
                name.contains("shoe", true) || name.contains("footwear", true) ->
                    SubcategoryInfo("👟", 0xFF78909C.toInt())   // blue-grey
                name.contains("bag", true) || name.contains("purse", true) ->
                    SubcategoryInfo("👜", 0xFF26A69A.toInt())   // teal
                name.contains("jewel", true) || name.contains("accessory", true) ->
                    SubcategoryInfo("💎", 0xFF7E57C2.toInt())   // deep purple
                name.contains("beauty", true) || name.contains("makeup", true) ->
                    SubcategoryInfo("💄", 0xFFEC407A.toInt())   // rose
                name.contains("skin", true) || name.contains("care", true) ->
                    SubcategoryInfo("🧴", 0xFFA5D6A7.toInt())   // mint green
                name.contains("hair", true) ->
                    SubcategoryInfo("💇", 0xFFFFCC02.toInt())   // amber
                name.contains("perfume", true) || name.contains("fragrance", true) ->
                    SubcategoryInfo("🌸", 0xFFF48FB1.toInt())   // blush pink

                // ── Electronics ─────────────────────────────────────
                name.contains("phone", true) || name.contains("mobile", true) ->
                    SubcategoryInfo("📱", 0xFF42A5F5.toInt())   // blue
                name.contains("laptop", true) || name.contains("computer", true) ->
                    SubcategoryInfo("💻", 0xFF78909C.toInt())   // slate
                name.contains("tv", true) || name.contains("television", true) ->
                    SubcategoryInfo("📺", 0xFF5C6BC0.toInt())   // indigo
                name.contains("audio", true) || name.contains("headphone", true) ->
                    SubcategoryInfo("🎧", 0xFF7E57C2.toInt())   // violet
                name.contains("camera", true) ->
                    SubcategoryInfo("📷", 0xFF8D6E63.toInt())   // brown
                name.contains("tablet", true) ->
                    SubcategoryInfo("📟", 0xFF4DB6AC.toInt())   // teal

                // ── Default ──────────────────────────────────────────
                else ->
                    SubcategoryInfo("🛍️", 0xFF9E9E9E.toInt())   // neutral grey
            }
        }

        class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardHeaderBg: View = itemView.findViewById(R.id.card_header_bg)
            val emoji: MaterialTextView = itemView.findViewById(R.id.subcategory_emoji)
            val name: MaterialTextView = itemView.findViewById(R.id.subcategory_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_subcategory_card, parent, false)
            return CardViewHolder(view)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val subcategory = getItem(position)
            val info = infoFor(subcategory)

            holder.name.text = subcategory
            holder.emoji.text = info.emoji
            holder.cardHeaderBg.setBackgroundColor(info.headerColor)

            // Tap → navigate to Product List filtered by this subcategory
            holder.itemView.setOnClickListener { onCardClick(subcategory) }
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
