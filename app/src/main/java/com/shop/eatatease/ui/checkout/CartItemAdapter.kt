package com.shop.eatatease.ui.checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shop.eatatease.data.model.CartItem
import com.shop.eatatease.databinding.ItemCheckoutProductBinding

/**
 * RecyclerView adapter for cart items on the checkout page.
 *
 * Displays product thumbnail, name, category, price, and
 * inline quantity controls (+/−). Fires callbacks when
 * quantity changes or an item is removed.
 */
class CartItemAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onItemRemoved: (CartItem) -> Unit
) : ListAdapter<CartItem, CartItemAdapter.CartViewHolder>(CartDiffCallback()) {

    inner class CartViewHolder(
        private val binding: ItemCheckoutProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            // Product image
            if (item.imageResId != 0) {
                binding.cartItemImage.setImageDrawable(
                    ResourcesCompat.getDrawable(binding.root.resources, item.imageResId, null)
                )
            }

            // Product info
            binding.cartItemName.text = item.name
            binding.cartItemCategory.text = item.category
            binding.cartItemPrice.text = item.price
            binding.cartItemQuantity.text = item.quantity.toString()

            // Quantity controls
            binding.cartItemMinus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    binding.cartItemQuantity.text = item.quantity.toString()
                    onQuantityChanged(item, item.quantity)
                } else {
                    // Remove if quantity reaches 0
                    onItemRemoved(item)
                }
            }

            binding.cartItemPlus.setOnClickListener {
                if (item.quantity < 99) {
                    item.quantity++
                    binding.cartItemQuantity.text = item.quantity.toString()
                    onQuantityChanged(item, item.quantity)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCheckoutProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
