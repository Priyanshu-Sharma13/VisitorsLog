package com.example.visitorslogs.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.UserRole

@Composable
fun EditUserDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var phone by remember { mutableStateOf(user.phoneNumber ?: "") }
    var flat by remember { mutableStateOf(user.flatNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                if (user.role == UserRole.RESIDENT) {
                    OutlinedTextField(
                        value = flat,
                        onValueChange = { flat = it },
                        label = { Text("Flat Number (e.g. A-101)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updates = mutableMapOf<String, Any>(
                        "name" to name,
                        "phoneNumber" to phone
                    )
                    if (user.role == UserRole.RESIDENT) {
                        updates["flatNumber"] = flat
                    }
                    onSave(updates)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
