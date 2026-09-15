package com.example.mobilnyskanermagazynowy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PartViewModel(private val dao: PartDao) : ViewModel() {

    // Pobieranie listy z bazy danych na żywo
    val allParts: StateFlow<List<PartItem>> = dao.getAllParts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDummyPart() {
        viewModelScope.launch {
            val newPart = PartItem(
                code = "QR-${(1000..9999).random()}",
                name = "Sterownik PLC S7-1200",
                quantity = 1,
                location = "Regał A2"
            )
            dao.insertPart(newPart)
        }
    }
}

class PartViewModelFactory(private val dao: PartDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}