package com.example.equipoOcho.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.model.ProductModelResponse
import com.example.equipoOcho.repository.InventoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class InventoryViewModelTest {

    // LiveData runs synchronously
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Controls Dispatchers.Main for coroutines
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var application: Application

    @Mock
    lateinit var repository: InventoryRepository

    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        // ViewModel with mocked repository
        viewModel = InventoryViewModel(application, repository)
        println(">>> setUp: InventoryViewModel created with mocked repository")
    }

    // --- totalProducto -------------------------------------------------------

    @Test
    fun `totalProducto devuelve price por quantity en Double`() {
        // given
        val price = 25      // e.g. 25.000
        val quantity = 4    // 4 items

        // when
        val result = viewModel.totalProducto(price = price, quantity = quantity)

        // log
        println(">>> totalProducto test - price=$price, quantity=$quantity, result=$result")

        // then
        Assert.assertEquals(100.0, result, 0.0) // 25 * 4 = 100.0
    }

    // --- saveInventory -------------------------------------------------------

    @Test
    fun `saveInventory llama al repo y deja el progreso en false`() = runTest {
        // given - real Inventory with values
        val inventory = Inventory(
            id = 1,
            name = "Zapatos deportivos",
            price = 120000,   // adjust to your model type (Int)
            quantity = 3
        )

        println(">>> saveInventory test - given inventory: id=${inventory.id}, name=${inventory.name}, price=${inventory.price}, quantity=${inventory.quantity}")

        // when
        viewModel.saveInventory(inventory)
        advanceUntilIdle()

        // log current progress state
        println(">>> saveInventory test - progresState=${viewModel.progresState.value}")

        // then
        verify(repository).saveInventory(inventory)
        Assert.assertEquals(false, viewModel.progresState.value)
    }

    // --- getListInventory ----------------------------------------------------

    @Test
    fun `getListInventory actualiza listInventory desde el repo`() = runTest {
        // given - a list with real Inventory
        val inventory1 = Inventory(
            id = 1,
            name = "Camisa",
            price = 50000,
            quantity = 10
        )
        val inventory2 = Inventory(
            id = 2,
            name = "Pantalón",
            price = 80000,
            quantity = 5
        )
        val list = mutableListOf(inventory1, inventory2)

        `when`(repository.getListInventory()).thenReturn(list)

        println(">>> getListInventory test - repo will return list with ${list.size} items")
        list.forEach {
            println("    item -> id=${it.id}, name=${it.name}, price=${it.price}, quantity=${it.quantity}")
        }

        // when
        viewModel.getListInventory()
        advanceUntilIdle()

        // log result from ViewModel
        val vmList = viewModel.listInventory.value
        println(">>> getListInventory test - viewModel.listInventory.size=${vmList?.size}")
        vmList?.forEach {
            println("    viewModel item -> id=${it.id}, name=${it.name}, price=${it.price}, quantity=${it.quantity}")
        }
        println(">>> getListInventory test - progresState=${viewModel.progresState.value}")

        // then
        Assert.assertEquals(list, viewModel.listInventory.value)
        Assert.assertEquals(false, viewModel.progresState.value)
    }

    // --- deleteInventory -----------------------------------------------------

    @Test
    fun `deleteInventory llama al repo y deja el progreso en false`() = runTest {
        // given
        val inventory = Inventory(
            id = 3,
            name = "Medias",
            price = 10000,
            quantity = 20
        )

        println(">>> deleteInventory test - deleting inventory: id=${inventory.id}, name=${inventory.name}, price=${inventory.price}, quantity=${inventory.quantity}")

        // when
        viewModel.deleteInventory(inventory)
        advanceUntilIdle()

        // log result
        println(">>> deleteInventory test - progresState=${viewModel.progresState.value}")

        // then
        verify(repository).deleteInventory(inventory)
        Assert.assertEquals(false, viewModel.progresState.value)
    }

    // --- updateInventory -----------------------------------------------------

    @Test
    fun `updateInventory llama al repo y deja el progreso en false`() = runTest {
        // given - simulate an item already saved but edited
        val inventory = Inventory(
            id = 4,
            name = "Chaqueta",
            price = 150000,
            quantity = 2
        )

        println(">>> updateInventory test - updating inventory: id=${inventory.id}, name=${inventory.name}, price=${inventory.price}, quantity=${inventory.quantity}")

        // when
        viewModel.updateInventory(inventory)
        advanceUntilIdle()

        // log
        println(">>> updateInventory test - progresState=${viewModel.progresState.value}")

        // then
        verify(repository).updateRepositoy(inventory)
        Assert.assertEquals(false, viewModel.progresState.value)
    }

    // --- getProducts ---------------------------------------------------------

    @Test
    fun `getProducts actualiza listProducts desde el repo`() = runTest {
        // OPTION 1 (safe for compile): keep using mocks for ProductModelResponse
        val product = mock(ProductModelResponse::class.java)
        val listProducts = mutableListOf(product)

        `when`(repository.getProducts()).thenReturn(listProducts)

        println(">>> getProducts test - repo will return listProducts.size=${listProducts.size}")

        viewModel.getProducts()
        advanceUntilIdle()

        // log result from ViewModel
        val vmProducts = viewModel.listProducts.value
        println(">>> getProducts test - viewModel.listProducts.size=${vmProducts?.size}")
        println(">>> getProducts test - progresState=${viewModel.progresState.value}")

        Assert.assertEquals(listProducts, viewModel.listProducts.value)
        Assert.assertEquals(false, viewModel.progresState.value)
    }
}
