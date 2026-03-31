package com.example.visitorslogs.ui.resident

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
import com.example.visitorslogs.domain.model.Complaint
import com.example.visitorslogs.domain.model.ComplaintStatus
import com.example.visitorslogs.ui.admin.NoticeCard
import com.example.visitorslogs.ui.admin.NoticeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDashboardScreen(
    flatNumber: String,
    societyId: String,
    currentUserId: String,
    onLogout: () -> Unit,
    visitorViewModel: ResidentVisitorViewModel = hiltViewModel(),
    deliveryViewModel: ResidentDeliveryViewModel = hiltViewModel(),
    noticeViewModel: NoticeViewModel = hiltViewModel(),
    complaintViewModel: ResidentComplaintViewModel = hiltViewModel()
) {
    val visitors by visitorViewModel.flatVisitors.collectAsState()
    val deliveries by deliveryViewModel.flatDeliveries.collectAsState()
    val notices by noticeViewModel.notices.collectAsState()
    val complaints by complaintViewModel.flatComplaints.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showRaiseComplaintDialog by remember { mutableStateOf(false) }

    LaunchedEffect(flatNumber, societyId) {
        visitorViewModel.loadVisitorsForFlat(societyId, flatNumber)
        deliveryViewModel.loadDeliveriesForFlat(societyId, flatNumber)
        complaintViewModel.loadComplaintsForFlat(societyId, flatNumber)
        noticeViewModel.loadNotices(societyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resident Dashboard") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 3) {
                FloatingActionButton(onClick = { showRaiseComplaintDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Raise Complaint")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Visitors") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Deliveries") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Notices") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Complaints") })
            }

            when (selectedTab) {
                0 -> {
                    if (visitors.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No visitors yet")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(visitors) { visitor ->
                                ResidentVisitorCard(visitor, onRespond = { approved ->
                                    visitorViewModel.respondToVisitor(visitor.visitorId, approved)
                                })
                            }
                        }
                    }
                }
                1 -> {
                    if (deliveries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No deliveries yet")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(deliveries) { delivery ->
                                ResidentDeliveryCard(delivery, onRespond = { status ->
                                    deliveryViewModel.updateDeliveryStatus(delivery.deliveryId, status)
                                })
                            }
                        }
                    }
                }
                2 -> {
                    if (notices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No notices posted")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notices) { notice ->
                                NoticeCard(
                                    notice = notice,
                                    currentUserId = currentUserId,
                                    onVote = { option -> noticeViewModel.voteOnPoll(notice.noticeId, option, currentUserId) }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    if (complaints.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No complaints raised")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(complaints) { complaint ->
                                ResidentComplaintCard(complaint = complaint, onResolve = {
                                    complaintViewModel.resolveComplaint(complaint.complaintId)
                                })
                            }
                        }
                    }
                }
            }
        }
        
        if (showRaiseComplaintDialog) {
            RaiseComplaintDialog(
                flatNumber = flatNumber,
                societyId = societyId,
                onDismiss = { showRaiseComplaintDialog = false },
                viewModel = complaintViewModel
            )
        }
    }
}

@Composable
fun ResidentComplaintCard(complaint: Complaint, onResolve: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${complaint.title} (${complaint.category})", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(complaint.description)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${complaint.status.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (complaint.status == ComplaintStatus.RESOLVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                if (complaint.status != ComplaintStatus.RESOLVED) {
                    TextButton(onClick = onResolve) {
                        Text("Mark Resolved")
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentVisitorCard(visitor: Visitor, onRespond: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Visitor: ${visitor.name}", style = MaterialTheme.typography.titleMedium)
            Text("Phone: ${visitor.phoneNumber}")
            
            val time = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(visitor.entryTimeMillis))
            Text("Arrived: $time", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (visitor.status == VisitorStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onRespond(true) }) { Text("Approve") }
                    Button(
                        onClick = { onRespond(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Deny") }
                }
            } else {
                Text(
                    text = "Status: ${visitor.status.name}",
                    color = if (visitor.status == VisitorStatus.APPROVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun ResidentDeliveryCard(delivery: Delivery, onRespond: (DeliveryStatus) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${delivery.company} Delivery", style = MaterialTheme.typography.titleMedium)
            
            val time = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(delivery.arrivalTimeMillis))
            Text("Arrived: $time", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (delivery.status == DeliveryStatus.PENDING) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onRespond(DeliveryStatus.ACCEPTED) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Accept") }
                        
                        Button(
                            onClick = { onRespond(DeliveryStatus.REJECTED) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) { Text("Reject") }
                    }
                    OutlinedButton(
                        onClick = { onRespond(DeliveryStatus.LEAVE_AT_GATE) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Leave at Gate") }
                }
            } else {
                Text(
                    text = "Status: ${delivery.status.name}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
