package com.shop.eatatease.ui.productlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.eatatease.R
import com.shop.eatatease.data.model.BadgeType
import com.shop.eatatease.data.model.Product

/**
 * ViewModel for the Product List screen.
 * In production this would fetch from Firestore; for now uses the same rich demo data
 * that powers the home screen so the list renders immediately.
 */
class ProductListViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    init {
        loadProducts()
    }

    private fun loadProducts() {
        _products.value = listOf(
            Product(
                id = "p1",
                name = "Acoustic Serenity Pro",
                category = "Audio",
                price = "\$349.00",
                imageResId = R.drawable.avatar_1,
                description = "Immerse yourself in pristine sound. These high-fidelity headphones offer industry-leading noise cancellation and a refined, ergonomic fit.",
                rating = 4.8f,
                reviewCount = 1284,
                features = listOf(
                    "Bluetooth 5.3 Multi-point",
                    "30-hour battery life",
                    "Beamforming mics"
                )
            ),
            Product(
                id = "p2",
                name = "Aura Executive Chair",
                category = "Furniture",
                price = "\$895.00",
                imageResId = R.drawable.avatar_2,
                description = "Engineered for posture perfection. A seamless blend of breathable mesh and responsive lumbar support for enduring comfort.",
                rating = 4.6f,
                reviewCount = 738,
                features = listOf(
                    "Breathable mesh back",
                    "Responsive lumbar support",
                    "Adjustable armrests"
                )
            ),
            Product(
                id = "p3",
                name = "Nexus Command Hub",
                category = "Smart Home",
                price = "\$129.00",
                imageResId = R.drawable.avatar_3,
                description = "The invisible butler for your environment. Seamlessly choreograph lighting, climate, and security with intuitive grace.",
                rating = 4.5f,
                reviewCount = 562,
                features = listOf(
                    "Works with 10,000+ devices",
                    "Voice control ready",
                    "Energy monitoring"
                )
            ),
            Product(
                id = "p4",
                name = "Chronos Minimalist",
                category = "Wearables",
                price = "\$1,250.00",
                originalPrice = "\$1,450.00",
                imageResId = R.drawable.avatar_4,
                badgeText = "SALE",
                badgeType = BadgeType.SALE,
                description = "Timepieces redefined. A masterclass in restraint, featuring a sapphire crystal face and precision Swiss movement.",
                rating = 4.9f,
                reviewCount = 319,
                features = listOf(
                    "Swiss mechanical movement",
                    "Sapphire crystal glass",
                    "5 ATM water resistance"
                )
            ),
            Product(
                id = "p5",
                name = "Urban Commuter Backpack",
                category = "Accessories",
                price = "\$85.00",
                imageResId = R.drawable.avatar_1,
                description = "The perfect daily carry. Recycled waterproof fabric, padded laptop sleeve, and an anti-theft hidden pocket.",
                rating = 4.6f,
                reviewCount = 1102,
                features = listOf(
                    "Fits 15.6\" laptops",
                    "Recycled waterproof fabric",
                    "Hidden anti-theft pocket"
                )
            ),
            Product(
                id = "p6",
                name = "AeroKnit Running Shoes",
                category = "Footwear",
                price = "\$120.00",
                originalPrice = "\$150.00",
                imageResId = R.drawable.avatar_2,
                badgeText = "-20%",
                badgeType = BadgeType.SALE,
                description = "Carbon-plate energy return with a breathable flyknit upper and ultra-grip rubber outsole.",
                rating = 4.7f,
                reviewCount = 2103,
                features = listOf(
                    "Carbon-plate energy return",
                    "Breathable flyknit upper",
                    "Ultra-grip outsole"
                )
            )
        )
    }
}
