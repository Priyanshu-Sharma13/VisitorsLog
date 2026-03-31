package com.example.visitorslogs.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visitorslogs.domain.model.Society
import com.example.visitorslogs.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    onLogout: () -> Unit,
    viewModel: SuperAdminViewModel = hiltViewModel()
) {
    val societies by viewModel.societies.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var societyToEdit by remember { mutableStateOf<Society?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Owner Dashboard") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Society")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (societies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No societies found. Click + to add one.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(societies) { society ->
                        SocietyCard(
                            society = society,
                            onEdit = { societyToEdit = society },
                            onRemove = { viewModel.removeSociety(society.societyId) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSocietyDialog(
                onDismiss = { showAddDialog = false },
                viewModel = viewModel
            )
        }

        societyToEdit?.let { society ->
            EditSocietyDialog(
                society = society,
                onDismiss = { societyToEdit = null },
                onSave = { updates ->
                    viewModel.updateSociety(society.societyId, updates)
                    societyToEdit = null
                }
            )
        }
    }
}

@Composable
fun SocietyCard(society: Society, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(society.name, style = MaterialTheme.typography.titleMedium)
                Text("ID: ${society.societyId}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(society.address, style = MaterialTheme.typography.bodyMedium)
                if (society.adminEmail.isNotBlank()) {
                    Text("Admin: ${society.adminEmail}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Society")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Society", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EditSocietyDialog(
    society: Society,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var name by remember { mutableStateOf(society.name) }
    var address by remember { mutableStateOf(society.address) }
    var adminEmail by remember { mutableStateOf(society.adminEmail) }
    var adminPassword by remember { mutableStateOf(society.adminPassword) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Society") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Society Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = adminEmail, onValueChange = { adminEmail = it }, label = { Text("Admin Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = adminPassword, onValueChange = { adminPassword = it }, label = { Text("Admin Password") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(mapOf(
                        "name" to name, 
                        "address" to address, 
                        "adminEmail" to adminEmail, 
                        "adminPassword" to adminPassword
                    ))
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSocietyDialog(
    onDismiss: () -> Unit,
    viewModel: SuperAdminViewModel
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    val status by viewModel.addSocietyStatus.collectAsState()

    LaunchedEffect(status) {
        if (status is Resource.Success) {
            viewModel.clearAddStatus()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Society") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Society Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Admin Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = { adminPassword = it },
                    label = { Text("Admin Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (status is Resource.Error) {
                    Text(
                        text = (status as Resource.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.addSociety(name, address, adminEmail, adminPassword) },
                enabled = status !is Resource.Loading
            ) {
                if (status is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Add Society")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
