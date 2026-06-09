package com.example.equipoOcho.repository

import com.example.equipoOcho.data.InventoryDao
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.model.ProductModelResponse
import com.example.equipoOcho.webservice.ApiService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val apiService: ApiService
) {

    // Firestore
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val inventoryCollection = firestore.collection("inventory")

    /** CREATE */
    suspend fun saveInventory(inventory: Inventory) {
        withContext(Dispatchers.IO) {
            // 1) local
            inventoryDao.saveInventory(inventory)

            // 2) remote
            try {
                // We use the item id as the document id
                inventoryCollection
                    .document(inventory.id.toString())
                    .set(inventory.toMap())
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** READ */
    suspend fun getListInventory(): MutableList<Inventory> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = inventoryCollection.get().await()
                val remoteList = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Inventory.fromMap(data)
                }.toMutableList()

                remoteList
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to local Room data
                inventoryDao.getListInventory()
            }
        }
    }

    /** DELETE */
    suspend fun deleteInventory(inventory: Inventory) {
        withContext(Dispatchers.IO) {
            // 1) local
            inventoryDao.deleteInventory(inventory)

            // 2) remote
            try {
                // Try by doc id = item id
                inventoryCollection
                    .document(inventory.id.toString())
                    .delete()
                    .await()

                // Also delete any stray docs that only match by field "id"
                val snapshots = inventoryCollection
                    .whereEqualTo("id", inventory.id)
                    .get()
                    .await()

                for (doc in snapshots.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** UPDATE */
    suspend fun updateRepositoy(inventory: Inventory) {
        withContext(Dispatchers.IO) {
            // 1) local
            inventoryDao.updateInventory(inventory)

            // 2) remote
            try {
                // Write main doc id = item id
                inventoryCollection
                    .document(inventory.id.toString())
                    .set(inventory.toMap())
                    .await()

                // And update any docs that only match by field "id"
                val snapshots = inventoryCollection
                    .whereEqualTo("id", inventory.id)
                    .get()
                    .await()

                for (doc in snapshots.documents) {
                    doc.reference.set(inventory.toMap()).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** PRODUCTS FROM API (unchanged) */
    suspend fun getProducts(): MutableList<ProductModelResponse> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getProducts()
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }
        }
    }
}
