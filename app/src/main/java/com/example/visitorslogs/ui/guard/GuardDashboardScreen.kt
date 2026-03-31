package com.example.visitorslogs.ui.guard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visitorslogs.domain.model.Delivery
import com.example.visitorslogs.domain.model.DeliveryStatus
import com.example.visitorslogs.domain.model.Visitor
import com.example.visitorslogs.domain.model.VisitorStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardDashboardScreen(
    guardId: String,
    societyId: String,
    onLogout: () -> Unit,
    visitorViewModel: GuardVisitorViewModel = hiltViewModel(),
    deliveryViewModel: GuardDeliveryViewModel = hiltViewModel()
) {
    val visitors by visitorViewModel.activeVisitors.collectAsState()
    val deliveries by deliveryViewModel.activeDeliveries.collectAsState()
    var showAddVisitorDialog by remember { mutableStateOf(false) }
    var showAddDeliveryDialog by remember { mutableStateOf(false) }
    
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(societyId) {
        visitorViewModel.loadActiveVisitors(societyId)
        deliveryViewModel.loadActiveDeliveries(societyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guard Dashboard") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (selectedTab == 0) showAddVisitorDialog = true else showAddDeliveryDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Visitors") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Deliveries") })
            }

            if (selectedTab == 0) {
                if (visitors.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active visitors")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visitors) { visitor ->
                            VisitorCard(visitor, onMarkExit = {
                                visitorViewModel.markVisitorExit(visitor.visitorId)
                            })
                        }
                    }
                }
            } else {
                if (deliveries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active deliveries")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(deliveries) { delivery ->
                            GuardDeliveryCard(delivery, onMarkExit = {
                                deliveryViewModel.markDeliveryExit(delivery.deliveryId)
                            })
                        }
                    }
                }
            }
        }

        if (showAddVisitorDialog) {
            AddVisitorDialog(
                guardId = guardId,
                societyId = societyId,
                onDismiss = { showAddVisitorDialog = false },
                viewModel = visitorViewModel
            )
        }

        if (showAddDeliveryDialog) {
            AddDeliveryDialog(
                guardId = guardId,
                societyId = societyId,
                onDismiss = { showAddDeliveryDialog = false },
                viewModel = deliveryViewModel
            )
        }
    }
}

@Composable
fun VisitorCard(visitor: Visitor, onMarkExit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${visitor.name}", style = MaterialTheme.typography.titleMedium)
            Text("Flat: ${visitor.flatNumber}")
            Text("Phone: ${visitor.phoneNumber}")
            val time = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(visitor.entryTimeMillis))
            Text("Entry: $time")
            
            if (visitor.exitTimeMillis != null) {
                val exitTime = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(visitor.exitTimeMillis))
                Text("Exit: $exitTime")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${visitor.status.name}",
                    color = when (visitor.status) {
                        VisitorStatus.APPROVED -> MaterialTheme.colorScheme.primary
                        VisitorStatus.DENIED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
                
                if (visitor.status == VisitorStatus.APPROVED) {
                    Button(onClick = onMarkExit) {
                        Text("Mark Exit")
                    }
                }
            }
        }
    }
}

@Composable
fun GuardDeliveryCard(delivery: Delivery, onMarkExit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${delivery.company} Delivery", style = MaterialTheme.typography.titleMedium)
            Text("Flat: ${delivery.flatNumber}")
            Text("To: ${delivery.deliveryPersonName}")
            val time = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(delivery.arrivalTimeMillis))
            Text("Arrived: $time")
            
            if (delivery.exitTimeMillis != null) {
                val exitTime = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(delivery.exitTimeMillis))
                Text("Exit: $exitTime")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${delivery.status.name}",
                    color = when (delivery.status) {
                        DeliveryStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
                        DeliveryStatus.REJECTED -> MaterialTheme.colorScheme.error
                        DeliveryStatus.EXITED -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (delivery.status == DeliveryStatus.ACCEPTED) {
                    Button(onClick = onMarkExit) {
                        Text("Mark Exit")
                    }
                }
            }
        }
    }
}
