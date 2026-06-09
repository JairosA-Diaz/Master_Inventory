package com.example.equipoOcho.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.equipoOcho.R
import com.example.equipoOcho.databinding.FragmentAddItemBinding
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.viewmodel.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class AddItemFragment : Fragment() {
    private lateinit var binding: FragmentAddItemBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddItemBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        controladores()
        observerViewModel()
    }

    /** Toolbar: flecha regresa al HomeInventario */
    private fun setupToolbar() {
        binding.toolbarAdd.setNavigationOnClickListener {
            // Vuelve a la HU 3.0 – HomeInventoryFragment
            findNavController().popBackStack()
        }
    }

    private fun controladores() {
        validarDatos()

        binding.btnSaveInventory.setOnClickListener {
            saveInventory()
        }
    }

    /** Guarda el producto en Room y vuelve al Home */
    private fun saveInventory() {
        //val codeText = binding.etCode.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val quantityText = binding.etQuantity.text.toString().trim()

        val id = binding.etCode.text.toString().trim().toInt()
        val price = priceText.toLong()          // hasta 20 dígitos
        val quantity = quantityText.toInt()     // hasta 4 dígitos

        // TODO: si tu data class Inventory tiene un campo "code", pásalo aquí
        val inventory = Inventory(
            id = id,
            name = name,
            price = price.toInt(),   // ajusta al tipo real de tu modelo
            quantity = quantity
        )

        inventoryViewModel.saveInventory(inventory)
        Log.d("AddItemFragment", "Guardado: $inventory")

        Toast.makeText(requireContext(), "Artículo guardado !!", Toast.LENGTH_SHORT).show()

        // Al regresar, HomeInventoryFragment observará la lista y mostrará el nuevo ítem
        findNavController().popBackStack()
    }

    /** Habilita el botón solo cuando todos los campos tienen valor */
    private fun validarDatos() {
        val listEditText = listOf(
            binding.etCode,
            binding.etName,
            binding.etPrice,
            binding.etQuantity
        )

        for (editText in listEditText) {
            editText.addTextChangedListener {
                val isListFull = listEditText.all { it.text?.isNotEmpty() == true }

                binding.btnSaveInventory.isEnabled = isListFull
                binding.btnSaveInventory.alpha = if (isListFull) 1f else 0.5f
                // El texto ya es blanco y bold en XML; con la alpha baja se ve "inactivo"
            }
        }
    }

    /** Mantengo tu lógica de observer de la API */
    private fun observerViewModel() {
        observerListProduct()
    }

    private fun observerListProduct() {
        inventoryViewModel.getProducts()
        inventoryViewModel.listProducts.observe(viewLifecycleOwner) { lista ->
            if (lista.isNotEmpty()) {
                val product = lista[2]
                Glide.with(binding.root.context)
                    .load(product.id)
                    .into(binding.ivImagenApi)

                binding.tvTitleProduct.text = product.title
            }
        }
    }
}