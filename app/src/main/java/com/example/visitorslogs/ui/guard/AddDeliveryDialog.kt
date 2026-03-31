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
fun AddDeliveryDialog(
    guardId: String,
    societyId: String,
    onDismiss: () -> Unit,
    viewModel: GuardDeliveryViewModel
) {
    var personName by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var flatNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val addStatus by viewModel.addDeliveryStatus.collectAsState()

    LaunchedEffect(addStatus) {
        if (addStatus is Resource.Success) {
            viewModel.clearAddStatus()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Delivery") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Delivery Person Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company (Amazon, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = flatNumber,
                    onValueChange = { flatNumber = it },
                    label = { Text("Flat Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Package Description") },
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
                onClick = { viewModel.logDelivery(personName, company, flatNumber, description, guardId, societyId) },
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
