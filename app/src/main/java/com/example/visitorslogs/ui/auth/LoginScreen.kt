package com.example.visitorslogs.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visitorslogs.domain.model.Society

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(2) } // 0=App, 1=Admin, 2=Resident, 3=Guard
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var expanded by remember { mutableStateOf(false) }
    var selectedSociety by remember { mutableStateOf<Society?>(null) }

    val state by viewModel.authState.collectAsState()
    val societies by viewModel.societies.collectAsState()

    LaunchedEffect(state.user) {
        if (state.user != null) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(selectedTab) {
        identifier = ""
        password = ""
        selectedSociety = null
        viewModel.clearError()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome to Visitors Logs",
                style = MaterialTheme.typography.headlineMedium
            )

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("App Owner") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Admin") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Resident") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Guard") })
            }
            
            // Show society dropdown if not App Owner
            if (selectedTab != 0) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedSociety?.name ?: "Select Society",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Society") }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (societies.isEmpty()) {
                            DropdownMenuItem(text = { Text("No societies found") }, onClick = { expanded = false })
                        }
                        societies.forEach { society ->
                            DropdownMenuItem(
                                text = { Text(society.name) },
                                onClick = {
                                    selectedSociety = society
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text(if (selectedTab == 3) "Username" else "Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (selectedTab == 3) KeyboardType.Text else KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (selectedTab != 0 && selectedSociety == null) {
                        viewModel.loginAdmin("", "", "") // trigger error logic internally or display custom error
                    } else {
                        when (selectedTab) {
                            0 -> viewModel.loginSuperAdmin(identifier, password)
                            1 -> viewModel.loginAdmin(identifier, password, selectedSociety?.societyId ?: "")
                            2 -> viewModel.loginResident(identifier, password, selectedSociety?.societyId ?: "")
                            3 -> viewModel.loginGuard(identifier, password, selectedSociety?.societyId ?: "")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Login")
            }
        }
        
        if (state.isLoading) {
            CircularProgressIndicator()
        }
    }
}
