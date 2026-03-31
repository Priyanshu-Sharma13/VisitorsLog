package com.example.visitorslogs.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.visitorslogs.utils.Resource

@Composable
fun RegisterUserDialog(
    societyId: String,
    onDismiss: () -> Unit,
    viewModel: AdminUserRegistrationViewModel
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Resident, 1 = Guard

    var name by remember { mutableStateOf("") }
    var emailOrUsername by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var flat by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val status by viewModel.registrationStatus.collectAsState()

    LaunchedEffect(status) {
        if (status is Resource.Success) {
            viewModel.clearStatus()
            onDismiss()
        }
    }

    LaunchedEffect(selectedTab) {
        // Clear fields when switching tabs
        name = ""
        emailOrUsername = ""
        phone = ""
        flat = ""
        password = ""
        viewModel.clearStatus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Resident") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Guard") })
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailOrUsername,
                    onValueChange = { emailOrUsername = it },
                    label = { Text(if (selectedTab == 0) "Email Address" else "Username (e.g. guard1)") },
                    keyboardOptions = KeyboardOptions(keyboardType = if (selectedTab == 0) KeyboardType.Email else KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = flat,
                        onValueChange = { flat = it },
                        label = { Text("Flat Number (e.g. A-101)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Assign Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (status is Resource.Error) {
                    Text(
                        text = (status as Resource.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedTab == 0) {
                        viewModel.registerResident(name, emailOrUsername, phone, flat, password, societyId)
                    } else {
                        viewModel.registerGuard(name, emailOrUsername, phone, password, societyId)
                    }
                },
                enabled = status !is Resource.Loading
            ) {
                if (status is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
