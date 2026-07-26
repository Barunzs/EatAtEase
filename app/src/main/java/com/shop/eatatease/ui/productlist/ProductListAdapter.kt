package com.shop.eatatease.ui.productlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.shop.eatatease.R
import com.shop.eatatease.data.model.Product

/**
 * Vertical-list product adapter for the Product List screen.
 * Matches the Stitch design: thumbnail + name + description + price + ADD TO CART pill.
 */
class ProductListAdapter(
    private val onAddClick: (Product) -> Unit,
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductListAdapter.ListViewHolder>(
    object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
) {
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ShapeableImageView = itemView.findViewById(R.id.list_product_image)
        val name: MaterialTextView    = itemView.findViewById(R.id.list_product_name)
        val desc: MaterialTextView    = itemView.findViewById(R.id.list_product_description)
        val price: MaterialTextView   = itemView.findViewById(R.id.list_product_price)
        val addBtn: MaterialTextView  = itemView.findViewById(R.id.list_btn_add_to_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_list_card, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val product = getItem(position)
        holder.image.setImageResource(product.imageResId)
        holder.name.text  = product.name
        holder.desc.text  = product.description
        holder.price.text = product.price

        holder.addBtn.setOnClickListener { onAddClick(product) }
        holder.itemView.setOnClickListener { onItemClick(product) }
    }
}
