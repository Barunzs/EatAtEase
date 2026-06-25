package com.shop.eatatease.data.model

/**
 * Represents a single item in the shopping cart.
 *
 * @param productId unique product identifier
 * @param name product display name
 * @param category category label
 * @param price price string (e.g. "$299")
 * @param imageResId drawable resource ID for the product image
 * @param quantity number of units
 */
data class CartItem(
    val productId: String,
    val name: String,
    val category: String,
    val price: String,
    val imageResId: Int,
    var quantity: Int = 1
)
