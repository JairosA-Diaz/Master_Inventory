package com.example.equipoOcho.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.equipoOcho.R
import com.example.equipoOcho.databinding.FragmentItemDetailsBinding
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.viewmodel.InventoryViewModel
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ItemDetailsFragment : Fragment() {
    private lateinit var binding: FragmentItemDetailsBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()
    private lateinit var receivedInventory: Inventory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataInventory()
        setupToolbar()
        setupListeners()
    }

    /** Flecha de la toolbar -> volver al Home (HU 3.0) */
    private fun setupToolbar() {
        binding.toolbarDetails.setNavigationOnClickListener {
            // Regresa a la pantalla anterior (Home Inventario)
            findNavController().popBackStack()
        }
    }

    /** Clicks de botones */
    private fun setupListeners() {
        // Botón Eliminar -> mostrar diálogo de confirmación
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // FAB Editar -> ir a HU 6.0: Ventana Editar Producto
        binding.fbEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("dataInventory", receivedInventory)
            }
            findNavController().navigate(
                R.id.action_itemDetailsFragment_to_itemEditFragment,
                bundle
            )
        }
    }

    /** Carga los datos del inventario recibido y calcula el total */
    private fun dataInventory() {
        val receivedBundle = arguments
        receivedInventory = receivedBundle?.getSerializable("clave") as Inventory

        // Coinciden con los IDs del XML que definimos
        binding.tvName.text = receivedInventory.name
        binding.tvPrice.text = "$ ${receivedInventory.price}"
        binding.tvQuantity.text = receivedInventory.quantity.toString()
        binding.txtTotal.text = "$ ${
            inventoryViewModel.totalProducto(
                receivedInventory.price,
                receivedInventory.quantity
            )
        }"
    }

    /** Diálogo estándar de confirmación para eliminar */
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("¿Deseas eliminar este producto?")
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Sí") { _, _ ->
                deleteInventory()
            }
            .create()
            .show()
    }

    /** Elimina en SQLite y regresa al Home Inventario */
    private fun deleteInventory() {
        inventoryViewModel.deleteInventory(receivedInventory)
        inventoryViewModel.getListInventory()
        // Volvemos a la lista de productos (Home Inventario)
        findNavController().popBackStack()
    }
}