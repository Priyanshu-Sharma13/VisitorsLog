package com.example.visitorslogs.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visitorslogs.domain.model.Complaint
import com.example.visitorslogs.domain.model.ComplaintStatus
import com.example.visitorslogs.domain.model.Notice
import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.UserRole
import com.example.visitorslogs.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminName: String,
    societyId: String,
    onLogout: () -> Unit,
    noticeViewModel: NoticeViewModel = hiltViewModel(),
    complaintViewModel: AdminComplaintViewModel = hiltViewModel(),
    registrationViewModel: AdminUserRegistrationViewModel = hiltViewModel()
) {
    val notices by noticeViewModel.notices.collectAsState()
    val complaints by complaintViewModel.allComplaints.collectAsState()
    val users by registrationViewModel.societyUsers.collectAsState()
    
    var showAddNotice by remember { mutableStateOf(false) }
    var showRegisterUserDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var userToEdit by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(societyId) {
        noticeViewModel.loadNotices(societyId)
        complaintViewModel.loadAllComplaints(societyId)
        registrationViewModel.loadSocietyUsers(societyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = { showRegisterUserDialog = true }) {
                        Text("Add User", color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showAddNotice = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Post Notice")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Notices") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Complaints") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Users") })
            }

            if (selectedTab == 0) {
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
                                isAdmin = true,
                                onDelete = { noticeViewModel.deleteNotice(notice.noticeId) },
                                onTogglePoll = { isActive -> noticeViewModel.togglePollActive(notice.noticeId, isActive) }
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                var selectedComplaintTab by remember { mutableStateOf(0) }
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = selectedComplaintTab) {
                        Tab(selected = selectedComplaintTab == 0, onClick = { selectedComplaintTab = 0 }, text = { Text("Pending") })
                        Tab(selected = selectedComplaintTab == 1, onClick = { selectedComplaintTab = 1 }, text = { Text("Resolved") })
                    }
                    
                    val pendingComplaints = complaints.filter { it.status != ComplaintStatus.RESOLVED }
                    val resolvedComplaints = complaints.filter { it.status == ComplaintStatus.RESOLVED }
                    
                    val displayList = if (selectedComplaintTab == 0) pendingComplaints else resolvedComplaints
                    
                    if (displayList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (selectedComplaintTab == 0) "No pending complaints" else "No resolved complaints")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayList) { complaint ->
                                AdminComplaintCard(complaint = complaint)
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                if (users.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No users found")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { userProfile ->
                            AdminUserCard(
                                userProfile = userProfile,
                                onEdit = { userToEdit = userProfile },
                                onDelete = { registrationViewModel.deleteUser(userProfile.userId) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddNotice) {
            PostNoticeDialog(
                adminName = adminName,
                societyId = societyId,
                onDismiss = { showAddNotice = false },
                viewModel = noticeViewModel
            )
        }

        if (showRegisterUserDialog) {
            RegisterUserDialog(
                societyId = societyId,
                onDismiss = { showRegisterUserDialog = false },
                viewModel = registrationViewModel
            )
        }

        userToEdit?.let { userProfile ->
            EditUserDialog(
                user = userProfile,
                onDismiss = { userToEdit = null },
                onSave = { updates ->
                    registrationViewModel.updateUser(userProfile.userId, updates)
                    userToEdit = null
                }
            )
        }
    }
}

@Composable
fun AdminUserCard(userProfile: UserProfile, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(userProfile.name, style = MaterialTheme.typography.titleMedium)
                Text("Role: ${userProfile.role.name}", style = MaterialTheme.typography.bodyMedium)
                if (userProfile.role == UserRole.RESIDENT) {
                    Text("Flat: ${userProfile.flatNumber}", style = MaterialTheme.typography.bodyMedium)
                }
                Text("Phone: ${userProfile.phoneNumber}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit User")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete User", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminComplaintCard(complaint: Complaint) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${complaint.title} (${complaint.category})", style = MaterialTheme.typography.titleMedium)
            Text("Flat: ${complaint.flatNumber}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(complaint.description)
            Spacer(modifier = Modifier.height(8.dp))
            
            val time = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(complaint.dateMillis))
            Text(time, style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Status: ${complaint.status.name}", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun NoticeCard(
    notice: Notice,
    currentUserId: String? = null,
    isAdmin: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onTogglePoll: ((Boolean) -> Unit)? = null,
    onVote: ((String) -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(notice.title, style = MaterialTheme.typography.titleMedium)
                if (isAdmin) {
                    Row {
                        if (notice.isPoll) {
                            TextButton(onClick = { onTogglePoll?.invoke(!notice.isPollActive) }) {
                                Text(if (notice.isPollActive) "Disable" else "Enable")
                            }
                        }
                        IconButton(onClick = { onDelete?.invoke() }) {
                            Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = "Delete")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(notice.description)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (notice.isPoll) {
                Text("Poll:", style = MaterialTheme.typography.labelLarge)
                val totalVotes = notice.pollVotes.values.sum()
                
                Text(
                    text = "Total Votes: $totalVotes", 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val hasVoted = currentUserId != null && notice.votedUserIds.contains(currentUserId)
                
                notice.pollOptions.forEach { option ->
                    val votes = notice.pollVotes[option] ?: 0
                    val percentage = if (totalVotes > 0) (votes.toFloat() / totalVotes * 100).toInt() else 0
                    val optionText = "$option - $votes votes ($percentage%)"
                    
                    if (notice.isPollActive && !hasVoted && !isAdmin && currentUserId != null) {
                        OutlinedButton(
                            onClick = { onVote?.invoke(option) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(optionText)
                        }
                    } else {
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = if (totalVotes > 0) votes.toFloat() / totalVotes else 0f,
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            val time = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(notice.dateMillis))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Author: ${notice.authorName}", style = MaterialTheme.typography.bodySmall)
                Text(time, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun PostNoticeDialog(
    adminName: String,
    societyId: String,
    onDismiss: () -> Unit,
    viewModel: NoticeViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPoll by remember { mutableStateOf(false) }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }
    var option3 by remember { mutableStateOf("") }
    val status by viewModel.postNoticeStatus.collectAsState()

    LaunchedEffect(status) {
        if (status is Resource.Success) {
            viewModel.clearPostStatus()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post New Notice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPoll, onCheckedChange = { isPoll = it })
                    Text("Create as Poll")
                }
                if (isPoll) {
                    OutlinedTextField(value = option1, onValueChange = { option1 = it }, label = { Text("Option 1") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = option2, onValueChange = { option2 = it }, label = { Text("Option 2") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = option3, onValueChange = { option3 = it }, label = { Text("Option 3 (Optional)") }, modifier = Modifier.fillMaxWidth())
                }
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
                onClick = { 
                    val opts = listOf(option1, option2, option3).filter { it.isNotBlank() }
                    viewModel.postNotice(title, description, adminName, societyId, isPoll, opts) 
                },
                enabled = status !is Resource.Loading
            ) {
                if (status is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Post Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
