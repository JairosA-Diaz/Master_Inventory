package com.example.equipoOcho.di

import android.content.Context
import com.example.equipoOcho.data.InventoryDB
import com.example.equipoOcho.data.InventoryDao
import com.example.equipoOcho.webservice.ApiService
import com.example.equipoOcho.webservice.ApiUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideInventoryDatabase(
        @ApplicationContext context: Context
    ): InventoryDB = InventoryDB.getDatabase(context)

    @Provides
    fun provideInventoryDao(
        db: InventoryDB
    ): InventoryDao = db.inventoryDao()

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiUtils.getApiService()
}
