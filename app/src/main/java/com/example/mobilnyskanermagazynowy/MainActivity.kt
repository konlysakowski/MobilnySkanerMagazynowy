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

    if (showScanner) {
        QRScannerScreen(
            onCodeScanned = { scannedCode ->
                viewModel.addScannedPart(scannedCode)
                showScanner = false
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
                        PartListItem(part)
                    }
                }
            }
        }
    }
}

@Composable
fun PartListItem(part: PartItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = part.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Kod: ${part.code}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Ilość: ${part.quantity} szt. | Lokalizacja: ${part.location}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
    }
}