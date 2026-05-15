package com.example.solarradarapp.ui.jobs

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solarradarapp.model.Job
import com.example.solarradarapp.ui.components.JobStatusChip
import com.example.solarradarapp.ui.components.TopBarAvatar
import com.example.solarradarapp.ui.components.deadlineColor
import com.example.solarradarapp.ui.components.daysUntilDeadline
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onJobClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: JobListViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel.navigateToLogin) {
        if (viewModel.navigateToLogin) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.myJobs,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = strings.retry, tint = colors.textSecondary)
                    }
                    viewModel.profile?.let { profile ->
                        TopBarAvatar(
                            displayName = profile.displayName,
                            photoUrl = profile.photoUrl,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.backgroundTopBar)
            )
        },
        containerColor = colors.backgroundScreen
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.backgroundTopBar,
                contentColor = colors.primaryBlue,
                divider = { HorizontalDivider(color = colors.divider) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            strings.tabCurrent,
                            fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            strings.tabResolved,
                            fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }

            val isInitialLoading = viewModel.isLoading &&
                viewModel.currentJobs.isEmpty() && viewModel.resolvedJobs.isEmpty()
            val jobs = if (selectedTab == 0) viewModel.currentJobs else viewModel.resolvedJobs
            val emptyTitle = if (selectedTab == 0) strings.noJobsAssigned else strings.noResolvedJobs
            val emptyBody = if (selectedTab == 0) strings.noActiveJobs else strings.noResolvedJobsBody

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isInitialLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.primaryBlue
                    )
                    viewModel.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(viewModel.errorMessage!!, color = StatusError)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(strings.retry) }
                        }
                    }
                    else -> {
                        PullToRefreshBox(
                            isRefreshing = viewModel.isLoading,
                            onRefresh = { viewModel.refresh() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (jobs.isEmpty()) {
                                EmptyJobsState(
                                    emptyTitle,
                                    emptyBody,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(jobs) { job ->
                                        JobCard(job = job, onClick = { onJobClick(job.firestoreId) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCard(job: Job, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    val dColor = deadlineColor(job.deadline)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 0f else 2f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        border = BorderStroke(1.dp, colors.divider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(job.jobId, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                JobStatusChip(job.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(job.siteName, fontSize = 14.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(strings.jobTypeLabel(job.type), fontSize = 13.sp, color = colors.textSecondary)

            job.deadline?.let { deadline ->
                val daysLeft = daysUntilDeadline(deadline)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = colors.divider)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = dColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(deadline, fontSize = 12.sp, color = dColor, fontWeight = FontWeight.Medium)
                    }
                    if (daysLeft != null) {
                        Text(
                            strings.daysLeftLabel(daysLeft),
                            fontSize = 11.sp,
                            color = dColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyJobsState(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Work,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = colors.textPrimary.copy(alpha = 0.26f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(body, fontSize = 14.sp, color = colors.textSecondary)
    }
}
