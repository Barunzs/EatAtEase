package com.shop.eatatease.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.shop.eatatease.R
import com.shop.eatatease.data.model.BadgeType
import com.shop.eatatease.data.model.Category
import com.shop.eatatease.data.model.CategoryDocument
import com.shop.eatatease.data.model.Product

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // Category chip names for the horizontal scroll
    private val _categoryChips = MutableLiveData<List<String>>()
    val categoryChips: LiveData<List<String>> = _categoryChips

    // Featured products for the grid
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    // Groceries subcategories → recyclerview_transform (kept for backward compat)
    private val _texts = MutableLiveData<List<String>>()
    val texts: LiveData<List<String>> = _texts

    // Home Decor subcategories → grocessary_recycleriew
    private val _grocessaryTexts = MutableLiveData<List<String>>()
    val grocessaryTexts: LiveData<List<String>> = _grocessaryTexts

    // Lifestyle subcategories → section_recyclerview
    private val _sectionTexts = MutableLiveData<List<String>>()
    val sectionTexts: LiveData<List<String>> = _sectionTexts

    init {
        fetchCategories()
        loadFeaturedProducts()
    }

    private fun fetchCategories() {
        db.collection("category")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val categoryDoc = CategoryDocument.fromMap(data)
                    _categories.value = categoryDoc.categories

                    // Build chip list: "All" + each category name
                    val chipNames = mutableListOf("All")
                    for (category in categoryDoc.categories) {
                        chipNames.add(category.name)
                    }
                    _categoryChips.value = chipNames
                    Log.d("HomeViewModel", "Parsed categories: ${categoryDoc.categories}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("HomeViewModel", "Error fetching categories.", exception)
                _texts.value = emptyList()
                _grocessaryTexts.value = emptyList()
                _sectionTexts.value = emptyList()
                // Fallback category chips
                _categoryChips.value = listOf("All", "Electronics", "Fashion", "Home", "Beauty")
            }
    }

    /**
     * Load demo featured products. In production, this would fetch from Firestore.
     * Uses existing avatar drawables as placeholder product images.
     */
    private fun loadFeaturedProducts() {
        _products.value = listOf(
            Product(
                id = "1",
                name = "Sonic Pro Wireless Headphones",
                category = "Electronics",
                price = "$299",
                imageResId = R.drawable.avatar_1,
                badgeText = "New",
                badgeType = BadgeType.NEW,
                description = "Immerse yourself in high-fidelity audio with the Sonic Pro. Featuring next-generation active noise cancellation and custom-tuned 40mm drivers, these headphones deliver pristine clarity across the entire frequency spectrum. The ergonomic memory foam ear cups provide all-day comfort, while the 30-hour battery life ensures your playlist never stops.",
                rating = 4.8f,
                reviewCount = 1284,
                features = listOf(
                    "Bluetooth 5.3 Multi-point connection",
                    "Fast charging: 5 mins = 4 hours playback",
                    "Beamforming mics for crystal-clear calls"
                )
            ),
            Product(
                id = "2",
                name = "Minimalist Smart Watch",
                category = "Wearables",
                price = "$149",
                imageResId = R.drawable.avatar_2,
                description = "Track your fitness goals with the Minimalist Smart Watch. Featuring a stunning AMOLED display, advanced health sensors, and 7-day battery life. Water-resistant to 50 meters with built-in GPS for outdoor activities.",
                rating = 4.5f,
                reviewCount = 892,
                features = listOf(
                    "1.4\" AMOLED always-on display",
                    "Heart rate & SpO2 monitoring",
                    "Built-in GPS with route tracking"
                )
            ),
            Product(
                id = "3",
                name = "Urban Commuter Backpack",
                category = "Accessories",
                price = "$85",
                imageResId = R.drawable.avatar_3,
                description = "The perfect companion for your daily commute. Made from premium recycled materials with a padded laptop compartment, multiple organizer pockets, and a hidden anti-theft pocket. Water-resistant coating keeps your gear dry.",
                rating = 4.6f,
                reviewCount = 567,
                features = listOf(
                    "Fits laptops up to 15.6 inches",
                    "Recycled waterproof fabric",
                    "Ergonomic padded back panel"
                )
            ),
            Product(
                id = "4",
                name = "AeroKnit Running Shoes",
                category = "Footwear",
                price = "$120",
                originalPrice = "$150",
                imageResId = R.drawable.avatar_4,
                badgeText = "-20%",
                badgeType = BadgeType.SALE,
                description = "Engineered for speed and comfort, the AeroKnit features a breathable flyknit upper and responsive foam midsole. The carbon-plate technology provides explosive energy return, while the rubber outsole delivers exceptional grip on any surface.",
                rating = 4.7f,
                reviewCount = 2103,
                features = listOf(
                    "Carbon-plate energy return",
                    "Breathable flyknit upper",
                    "Ultra-grip rubber outsole"
                )
            )
        )
    }
}