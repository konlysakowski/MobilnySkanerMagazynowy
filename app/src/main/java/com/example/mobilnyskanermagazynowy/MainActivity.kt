package com.example.mobilnyskanermagazynowy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val factory = PartViewModelFactory(database.partDao())
        val viewModel = ViewModelProvider(this, factory)[PartViewModel::class.java]
        setContent {
            MaterialTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PartViewModel) {
    val parts by viewModel.allParts.collectAsState()
    var showScanner by remember { mutableStateOf(false) }
    var scannedCodeToProcess by remember { mutableStateOf<String?>(null) }

    scannedCodeToProcess?.let { code ->
        AddPartDialog(
            scannedCode = code,
            onDismiss = { scannedCodeToProcess = null },
            onConfirm = { name, qty, loc ->
                viewModel.addScannedPart(code, name, qty, loc)
                scannedCodeToProcess = null
            }
        )
    }

    if (showScanner) {
        QRScannerScreen(
            onCodeScanned = { scannedCode ->
                showScanner = false
                viewModel.checkAndProcessCode(scannedCode) {
                    scannedCodeToProcess = scannedCode
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Magazyn Części") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showScanner = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Skanuj kod")
                }
            }
        ) { paddingValues ->
            if (parts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Magazyn jest pusty.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Użyj przycisku +, aby zeskanować asortyment.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(parts) { part ->
                        PartListItem(part, onDelete = {viewModel.deletePart(part) })
                    }
                }
            }
        }
    }
}

@Composable
fun PartListItem(part: PartItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = part.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Kod: ${part.code}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Ilość: ${part.quantity} szt. | Lokalizacja: ${part.location}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Usuń"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
    }
}

@Composable
fun AddPartDialog(
    scannedCode: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nowy produkt") },
        text = {
            Column {
                Text("Zeskanowany kod: $scannedCode")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa produktu") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Ilość (szt.)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokalizacja (np. Półka 1)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qtyInt = quantity.toIntOrNull() ?: 1
                onConfirm(name, qtyInt, location)
            }) {
                Text("Zapisz w bazie")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}