package com.example.visitorslogs.ui.guard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.visitorslogs.utils.Resource

@Composable
fun AddVisitorDialog(
    guardId: String,
    societyId: String,
    onDismiss: () -> Unit,
    viewModel: GuardVisitorViewModel
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var flatNumber by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }

    val addStatus by viewModel.addVisitorStatus.collectAsState()

    LaunchedEffect(addStatus) {
        if (addStatus is Resource.Success) {
            viewModel.clearAddStatus()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Visitor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Visitor Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = flatNumber,
                    onValueChange = { flatNumber = it },
                    label = { Text("Flat Number (e.g., A-101)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose of Visit") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (addStatus is Resource.Error) {
                    Text(
                        text = (addStatus as Resource.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submitVisitorEntry(name, phone, flatNumber, purpose, guardId, societyId) },
                enabled = addStatus !is Resource.Loading
            ) {
                if (addStatus is Resource.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
