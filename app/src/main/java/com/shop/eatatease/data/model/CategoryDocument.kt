package com.shop.eatatease.data.model

/**
 * Represents a single category with its name and list of subcategories.
 *
 * Firestore document structure:
 * {
 *   "Groceries": ["Fresh From Kitchen", "Breakfast Essentials", ...],
 *   "Home Decor": ["Wall Clock", "Paintings"],
 *   "Lifestyle": ["Saree", "Mens Wear", ...]
 * }
 */
data class Category(
    val name: String = "",
    val subcategories: List<String> = emptyList()
)

/**
 * Holds the full category document parsed from Firestore.
 */
data class CategoryDocument(
    val categories: List<Category> = emptyList()
) {
    companion object {
        /**
         * Parses a Firestore document map into a [CategoryDocument].
         * Each key becomes a [Category.name] and its value list becomes [Category.subcategories].
         */
        fun fromMap(data: Map<String, Any>): CategoryDocument {
            val categories = data.mapNotNull { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                val items = value as? List<String> ?: return@mapNotNull null
                Category(name = key, subcategories = items)
            }
            return CategoryDocument(categories = categories)
        }
    }
}
