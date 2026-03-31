package com.example.visitorslogs.ui.resident

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.visitorslogs.utils.Resource

@Composable
fun RaiseComplaintDialog(
    flatNumber: String,
    societyId: String,
    onDismiss: () -> Unit,
    viewModel: ResidentComplaintViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Maintenance") }
    
    val submitStatus by viewModel.submitStatus.collectAsState()

    LaunchedEffect(submitStatus) {
        if (submitStatus is Resource.Success) {
            viewModel.clearSubmitStatus()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise Complaint") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Water, Lift, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (submitStatus is Resource.Error) {
                    Text(
                        text = (submitStatus as Resource.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.raiseComplaint(title, description, category, flatNumber, societyId) },
                enabled = submitStatus !is Resource.Loading
            ) {
                if (submitStatus is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
