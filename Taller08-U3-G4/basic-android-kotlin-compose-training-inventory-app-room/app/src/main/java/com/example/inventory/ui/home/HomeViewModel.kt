package com.example.inventory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(
    itemDao: ItemDao
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    // 🔹 Lista de ítems (ya existía en el proyecto base)
    val homeUiState: StateFlow<HomeUiState> =
        itemDao.getAllItems()
            .map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    // 🔹 NUEVO: total de productos en stock
    val totalProducts: StateFlow<Int> =
        itemDao.totalProducts()
            .map { it ?: 0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = 0
            )

    // 🔹 NUEVO: valor total del inventario
    val totalValue: StateFlow<Double> =
        itemDao.totalValue()
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = 0.0
            )
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(
    val itemList: List<Item> = listOf()
)
