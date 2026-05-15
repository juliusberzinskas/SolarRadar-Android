package com.example.solarradarapp.ui.report

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.solarradarapp.model.ReportStatus
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    firestoreId: String,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: ReportFormViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReportFormViewModel(firestoreId) as T
        }
    })
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    LaunchedEffect(viewModel.submitSuccess) {
        if (viewModel.submitSuccess) onSubmitSuccess()
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.addPhotos(uris) }

    var statusExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.submitReportTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.backgroundTopBar)
            )
        },
        containerColor = colors.backgroundScreen
    ) { padding ->
        if (viewModel.isLoadingJob) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = colors.primaryBlue) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Job header
            viewModel.job?.let { job ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column {
                            Text(job.jobId, style = MaterialTheme.typography.titleSmall)
                            Text(job.siteName, fontSize = 13.sp, color = colors.textSecondary)
                        }
                    }
                }
            }

            // Form card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, colors.divider)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status dropdown
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = strings.reportStatusLabel(viewModel.selectedStatus),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.workStatus) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryBlue,
                                focusedLabelColor = colors.primaryBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            ReportStatus.all.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(strings.reportStatusLabel(status)) },
                                    onClick = {
                                        viewModel.selectedStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        label = { Text(strings.notes) },
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryBlue,
                            focusedLabelColor = colors.primaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Photos card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, colors.divider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(strings.photos, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.textPrimary)

                    if (viewModel.photoUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            viewModel.photoUris.forEach { uri ->
                                Box(modifier = Modifier.size(80.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { viewModel.removePhoto(uri) },
                                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { photoPicker.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, colors.primaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.primaryBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.addPhotos, color = colors.primaryBlue)
                    }
                }
            }

            viewModel.errorMessage?.let { msg ->
                Text(msg, color = StatusError, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { viewModel.submitReport(context) },
                enabled = !viewModel.isSubmitting && viewModel.profile != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.uploading, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(strings.submitReport, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
