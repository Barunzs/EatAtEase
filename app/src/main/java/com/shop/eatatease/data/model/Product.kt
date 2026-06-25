package com.shop.eatatease.data.model

/**
 * Represents a product displayed in the home screen grid.
 *
 * @param id unique identifier
 * @param name product display name
 * @param category category label (e.g., "Electronics", "Footwear")
 * @param price current price string (e.g., "$299")
 * @param originalPrice optional original price for discounted items (e.g., "$150")
 * @param imageResId drawable resource ID for the product image
 * @param badgeText optional badge text (e.g., "New", "-20%")
 * @param badgeType type of badge — NEW or SALE, determines badge color
 */
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: String,
    val originalPrice: String? = null,
    val imageResId: Int,
    val badgeText: String? = null,
    val badgeType: BadgeType = BadgeType.NONE,
    val description: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val features: List<String> = emptyList()
)

enum class BadgeType {
    NONE,
    NEW,
    SALE
}
