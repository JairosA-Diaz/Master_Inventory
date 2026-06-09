package com.example.equipoOcho.view.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.example.equipoOcho.R
import com.example.equipoOcho.databinding.FragmentHomeInventoryBinding
import com.example.equipoOcho.view.adapter.InventoryAdapter
import com.example.equipoOcho.view.widget.InventoryWidgetProvider
import com.example.equipoOcho.viewmodel.InventoryViewModel
import com.example.equipoOcho.utils.SessionManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class HomeInventoryFragment : Fragment() {

    private lateinit var binding: FragmentHomeInventoryBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeInventoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarToolbar()
        controladores()
        observadorViewModel()   // dentro llamas a observerListInventory() y observerProgress()
    }

    /** Toolbar + icono de cerrar sesión */
    private fun configurarToolbar() {
        binding.toolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    // 1. Limpiar sesión
                    SessionManager.clearSession(requireContext())

                    // 2. Mensaje opcional
                    Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()

                    // 3. Navegar al Login y sacar Home del back stack
                    val navController = findNavController()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.homeInventoryFragment, true) // limpia hasta Home incluyéndolo
                        .build()

                    navController.navigate(R.id.loginFragment, null, navOptions)
                    true
                }
                else -> false
            }
        }
    }

    private fun controladores() {
        binding.fbagregar.setOnClickListener {
            findNavController().navigate(R.id.action_homeInventoryFragment_to_addItemFragment)
        }

        // Si quieres pedir que fijen el widget desde aquí, podrías llamar:
        // binding.btnAgregarWidget.setOnClickListener { requestPinWidget() }
    }

    private fun observadorViewModel() {
        observerListInventory()
        observerTotals()
        observerProgress()
    }

    private fun observerListInventory() {
        inventoryViewModel.getListInventory()
        inventoryViewModel.listInventory.observe(viewLifecycleOwner) { listInventory ->
            val recycler = binding.recyclerview
            recycler.layoutManager = LinearLayoutManager(context)
            val adapter = InventoryAdapter(listInventory, findNavController())
            recycler.adapter = adapter
            // No es necesario llamar a notifyDataSetChanged() al asignar un nuevo adapter
        }
    }

    private fun observerTotals() {
        inventoryViewModel.totalItems.observe(viewLifecycleOwner) { total ->
            binding.tvTotalItems.text = total.toString()
        }
        inventoryViewModel.totalValue.observe(viewLifecycleOwner) { total ->
            binding.tvTotalValue.text = "$ $total"
        }
    }

    private fun observerProgress() {
        inventoryViewModel.progresState.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.isVisible = isLoading          // círculo naranja
            binding.recyclerview.isVisible = !isLoading     // ocultar/mostrar lista
        }
    }

    private fun requestPinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(requireContext())
            val myProvider = ComponentName(
                requireContext(),
                InventoryWidgetProvider::class.java
            )

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Agrega el widget desde la pantalla de inicio.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "En esta versión de Android agrega el widget desde la pantalla de inicio.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}