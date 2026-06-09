package com.example.equipoOcho.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.equipoOcho.R
import com.example.equipoOcho.databinding.FragmentItemEditBinding
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.viewmodel.InventoryViewModel
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ItemEditFragment : Fragment() {
    private lateinit var binding: FragmentItemEditBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()
    private lateinit var receivedInventory: Inventory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemEditBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        dataInventory()
        setupFieldValidation()
        setupListeners()
    }

    /** Flecha de la toolbar -> volver al Detalle (HU 5.0) */
    private fun setupToolbar() {
        binding.toolbarEdit.setNavigationOnClickListener {
            // Regresa a la pantalla anterior (Detalle del producto)
            findNavController().popBackStack()
        }
    }

    /** Cargar datos del producto (Id + campos prellenados) */
    private fun dataInventory() {
        val receivedBundle = arguments
        receivedInventory = receivedBundle?.getSerializable("dataInventory") as Inventory

        // Id solo lectura
        binding.tvId.text = receivedInventory.id.toString()

        // Campos editables prellenados
        binding.etName.setText(receivedInventory.name)
        binding.etPrice.setText(receivedInventory.price.toString())
        binding.etQuantity.setText(receivedInventory.quantity.toString())
    }

    /** Habilitar/deshabilitar botón Editar según los campos */
    private fun setupFieldValidation() {
        val listEditText = listOf(
            binding.etName,
            binding.etPrice,
            binding.etQuantity
        )

        for (editText in listEditText) {
            editText.addTextChangedListener {
                val isListFull = listEditText.all { it.text?.isNotEmpty() == true }
                binding.btnEdit.isEnabled = isListFull
                binding.btnEdit.alpha = if (isListFull) 1f else 0.5f
            }
        }
    }

    /** Listeners del botón Editar */
    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            updateInventory()
        }
    }

    /** Actualizar en SQLite y volver al Home Inventario */
    private fun updateInventory() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim().toInt()
        val quantity = binding.etQuantity.text.toString().trim().toInt()

        val inventory = Inventory(
            receivedInventory.id,
            name,
            price,
            quantity
        )

        inventoryViewModel.updateInventory(inventory)

        // Ir a la Ventana Home Inventario (lista actualizada)
        findNavController().navigate(R.id.action_itemEditFragment_to_homeInventoryFragment)
    }
}