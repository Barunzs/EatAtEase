package com.shop.eatatease.data

import com.shop.eatatease.data.model.CartItem
import com.shop.eatatease.data.model.Product

/**
 * In-memory singleton cart manager.
 *
 * Provides add / remove / update operations for cart items.
 * Each product is identified by its [Product.id]; adding a product
 * that already exists increments its quantity.
 *
 * TODO: Persist to Room or Firebase for cross-session cart retention.
 */
object CartManager {

    private val items = mutableListOf<CartItem>()

    /** Snapshot of current cart items. */
    fun getItems(): List<CartItem> = items.toList()

    /** Number of distinct line items. */
    fun itemCount(): Int = items.size

    /** Total number of units across all items. */
    fun totalUnits(): Int = items.sumOf { it.quantity }

    /** Whether the cart is empty. */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Add a product to the cart.
     * If the product already exists, increments its quantity by [quantity].
     */
    fun addProduct(product: Product, quantity: Int = 1) {
        val existing = items.find { it.productId == product.id }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            items.add(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    category = product.category,
                    price = product.price,
                    imageResId = product.imageResId,
                    quantity = quantity
                )
            )
        }
    }

    /**
     * Add a cart item directly (used from product detail page
     * where we don't have a full Product object).
     */
    fun addItem(
        productId: String,
        name: String,
        category: String,
        price: String,
        imageResId: Int,
        quantity: Int = 1
    ) {
        val existing = items.find { it.productId == productId }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            items.add(
                CartItem(
                    productId = productId,
                    name = name,
                    category = category,
                    price = price,
                    imageResId = imageResId,
                    quantity = quantity
                )
            )
        }
    }

    /** Update quantity for a specific item. Removes if quantity <= 0. */
    fun updateQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(productId)
        } else {
            items.find { it.productId == productId }?.quantity = newQuantity
        }
    }

    /** Remove an item from the cart by product ID. */
    fun removeItem(productId: String) {
        items.removeAll { it.productId == productId }
    }

    /** Clear all items from the cart. */
    fun clear() {
        items.clear()
    }
}
