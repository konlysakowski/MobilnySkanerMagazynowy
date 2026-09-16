package com.example.mobilnyskanermagazynowy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PartViewModel(private val dao: PartDao) : ViewModel() {

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
    fun addScannedPart(scannedCode: String, name: String, quantity: Int, location: String) {
        viewModelScope.launch {
            val newPart = PartItem(
                code = scannedCode,
                name = name,
                quantity = quantity,
                location = location
            )
            dao.insertPart(newPart)
        }
    }

    fun checkAndProcessCode(scannedCode: String, onNewCode: () -> Unit) {
        viewModelScope.launch {
            val existingPart = dao.getPartByCode(scannedCode)
            if (existingPart != null) {
                dao.updateQuantity(scannedCode, existingPart.quantity + 1)
            } else {
                onNewCode()
            }
        }
    }

    fun deletePart(part: PartItem) {
        viewModelScope.launch {
            dao.deletePart(part)
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