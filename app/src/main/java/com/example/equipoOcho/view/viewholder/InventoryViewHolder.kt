package com.example.equipoOcho.view.viewholder

import android.os.Bundle
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.equipoOcho.R
import com.example.equipoOcho.databinding.ItemInventoryBinding
import com.example.equipoOcho.model.Inventory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class InventoryViewHolder(
    private val binding: ItemInventoryBinding,
    private val navController: NavController
) : RecyclerView.ViewHolder(binding.root) {

    fun setItemInventory(inventory: Inventory) {

        // Nombre del producto
        binding.tvName.text = inventory.name

        // Formato del precio igual al del widget: $ 3.326,00
        val symbols = DecimalFormatSymbols().apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }

        val formatter = DecimalFormat("#,##0.00").apply {
            decimalFormatSymbols = symbols
        }

        val formattedPrice = formatter.format(inventory.price.toDouble())
        binding.tvPrice.text = "$ $formattedPrice"

        // Id del producto (solo lectura)
        // Usa el campo que tengas en Inventory: id, code, etc.
        binding.tvProductId.text = inventory.id.toString()

        // Cantidad (solo lectura)
        binding.tvQuantity.text = inventory.quantity.toString()

        // Navegar a detalle al tocar la tarjeta
        binding.cvInventory.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("clave", inventory)
            }
            navController.navigate(
                R.id.action_homeInventoryFragment_to_itemDetailsFragment,
                bundle
            )
        }
    }
}