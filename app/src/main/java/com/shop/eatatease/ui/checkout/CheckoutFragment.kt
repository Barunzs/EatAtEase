package com.shop.eatatease.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shop.eatatease.R
import com.shop.eatatease.data.CartManager
import com.shop.eatatease.databinding.FragmentCheckoutBinding
import java.util.Locale

/**
 * Checkout page that displays all cart items as a scrollable list.
 *
 * Reads items from [CartManager] (in-memory singleton), displays them
 * in a RecyclerView with per-item quantity controls, computes a live
 * price breakdown, and handles order placement.
 */
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding

    private var selectedPayment = PaymentMethod.CARD
    private lateinit var cartAdapter: CartItemAdapter

    enum class PaymentMethod { CARD, UPI, COD }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)

        setupCartList()
        setupPaymentSelection()
        setupActions()
        updatePriceSummary()

        return binding?.root
    }

    // ═══════════════════════════════════════════════════════════════
    // Cart Item List
    // ═══════════════════════════════════════════════════════════════

    private fun setupCartList() {
        cartAdapter = CartItemAdapter(
            onQuantityChanged = { item, newQty ->
                CartManager.updateQuantity(item.productId, newQty)
                updatePriceSummary()
                updateItemCount()
            },
            onItemRemoved = { item ->
                CartManager.removeItem(item.productId)
                refreshCartList()
                updatePriceSummary()
                updateItemCount()
            }
        )

        binding?.checkoutItemsRecyclerview?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
            isNestedScrollingEnabled = false
        }

        refreshCartList()
        updateItemCount()
    }

    /**
     * Refresh the adapter data from CartManager and toggle empty state.
     */
    private fun refreshCartList() {
        val items = CartManager.getItems()
        cartAdapter.submitList(items.toList())

        if (items.isEmpty()) {
            binding?.checkoutItemsRecyclerview?.visibility = View.GONE
            binding?.checkoutEmptyCart?.visibility = View.VISIBLE
            binding?.btnPlaceOrder?.isEnabled = false
            binding?.btnPlaceOrder?.alpha = 0.5f
        } else {
            binding?.checkoutItemsRecyclerview?.visibility = View.VISIBLE
            binding?.checkoutEmptyCart?.visibility = View.GONE
            binding?.btnPlaceOrder?.isEnabled = true
            binding?.btnPlaceOrder?.alpha = 1.0f
        }
    }

    /**
     * Update the item count badge in the Order Summary header.
     */
    private fun updateItemCount() {
        val totalItems = CartManager.totalUnits()
        binding?.checkoutItemCount?.text = if (totalItems == 1) {
            getString(R.string.checkout_item_count_single, totalItems)
        } else {
            getString(R.string.checkout_items_count, totalItems)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Price Calculation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Recalculate and display the subtotal, tax, and total from all cart items.
     */
    private fun updatePriceSummary() {
        val items = CartManager.getItems()

        var subtotal = 0.0
        for (item in items) {
            val unitPrice = parsePriceToDouble(item.price)
            subtotal += unitPrice * item.quantity
        }

        val tax = subtotal * 0.09   // 9% tax
        val total = subtotal + tax  // Free shipping

        binding?.checkoutSubtotalValue?.text = formatPrice(subtotal)
        binding?.checkoutTaxValue?.text = formatPrice(tax)
        binding?.checkoutTotalValue?.text = formatPrice(total)
        binding?.checkoutBottomTotal?.text = formatPrice(total)
    }

    /**
     * Parse a price string like "$299" or "$299.00" to a Double.
     */
    private fun parsePriceToDouble(price: String): Double {
        return try {
            price.replace("[^0-9.]".toRegex(), "").toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    /**
     * Format a Double as a price string like "$299.00".
     */
    private fun formatPrice(value: Double): String {
        return String.format(Locale.US, "$%.2f", value)
    }

    // ═══════════════════════════════════════════════════════════════
    // Payment Method Selection
    // ═══════════════════════════════════════════════════════════════

    private fun setupPaymentSelection() {
        val cardOption = binding?.paymentCard
        val upiOption = binding?.paymentUpi
        val codOption = binding?.paymentCod

        cardOption?.setOnClickListener {
            selectPayment(PaymentMethod.CARD, cardOption, upiOption, codOption)
        }
        upiOption?.setOnClickListener {
            selectPayment(PaymentMethod.UPI, upiOption, cardOption, codOption)
        }
        codOption?.setOnClickListener {
            selectPayment(PaymentMethod.COD, codOption, cardOption, upiOption)
        }
    }

    private fun selectPayment(
        method: PaymentMethod,
        selected: LinearLayout?,
        vararg others: LinearLayout?
    ) {
        selectedPayment = method

        // Animate selected
        selected?.setBackgroundResource(R.drawable.bg_payment_selected)
        selected?.animate()?.scaleX(1.02f)?.scaleY(1.02f)?.setDuration(150)?.withEndAction {
            selected.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(100)?.start()
        }?.start()

        // Reset others
        for (other in others) {
            other?.setBackgroundResource(R.drawable.bg_payment_unselected)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Actions
    // ═══════════════════════════════════════════════════════════════

    private fun setupActions() {
        // Back button
        binding?.btnCheckoutBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Change address
        binding?.btnChangeAddress?.setOnClickListener {
            Toast.makeText(context, "Address selection coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Place Order
        binding?.btnPlaceOrder?.setOnClickListener {
            placeOrder()
        }
    }

    /**
     * Handle order placement — show progress, simulate API call,
     * then show success toast, clear cart, and navigate back to home.
     */
    private fun placeOrder() {
        val btn = binding?.btnPlaceOrder ?: return
        if (CartManager.isEmpty()) return

        // Disable button and show loading state
        btn.isEnabled = false
        btn.alpha = 0.7f
        btn.text = getString(R.string.checkout_placing_order)

        btn.postDelayed({
            // TODO: Replace with real order API call
            Toast.makeText(
                context,
                getString(R.string.checkout_order_placed),
                Toast.LENGTH_LONG
            ).show()

            // Clear the cart
            CartManager.clear()

            // Navigate back to home
            findNavController().popBackStack(R.id.nav_transform, false)
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
