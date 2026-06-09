package com.example.equipoOcho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.model.ProductModelResponse
import com.example.equipoOcho.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _listInventory = MutableLiveData<MutableList<Inventory>>()
    val listInventory: LiveData<MutableList<Inventory>> get() = _listInventory

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> get() = _totalItems

    private val _totalValue = MutableLiveData(0.0)
    val totalValue: LiveData<Double> get() = _totalValue

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    private val _listProducts = MutableLiveData<MutableList<ProductModelResponse>>()
    val listProducts: LiveData<MutableList<ProductModelResponse>> = _listProducts

    fun saveInventory(inventory: Inventory) {
        viewModelScope.launch {
            _progresState.value = true
            try {
                inventoryRepository.saveInventory(inventory)
            } finally {
                _progresState.value = false
            }
        }
    }

    fun getListInventory() {
        viewModelScope.launch {
            _progresState.value = true
            try {
                val list = inventoryRepository.getListInventory()
                _listInventory.value = list
                calculateTotals(list)
            } finally {
                _progresState.value = false
            }
        }
    }

    private fun calculateTotals(list: List<Inventory>) {
        var items = 0
        var value = 0.0
        for (item in list) {
            items += item.quantity
            value += (item.price * item.quantity)
        }
        _totalItems.value = items
        _totalValue.value = value
    }

    fun deleteInventory(inventory: Inventory) {
        viewModelScope.launch {
            _progresState.value = true
            try {
                inventoryRepository.deleteInventory(inventory)
                _progresState.value = false
            } catch (e: Exception) {
                _progresState.value = false
            }
        }
    }

    fun updateInventory(inventory: Inventory) {
        viewModelScope.launch {
            _progresState.value = true
            try {
                inventoryRepository.updateRepositoy(inventory)
                _progresState.value = false
            } catch (e: Exception) {
                _progresState.value = false
            }
        }
    }


    fun getProducts() {
        viewModelScope.launch {
            _progresState.value = true
            try {
                _listProducts.value = inventoryRepository.getProducts()
            } finally {
                _progresState.value = false
            }
        }
    }

    fun totalProducto(price: Int, quantity: Int): Double {
        val total = price * quantity
        return total.toDouble()
    }
}
