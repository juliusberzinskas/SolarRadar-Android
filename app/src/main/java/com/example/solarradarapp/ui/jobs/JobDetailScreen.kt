package com.example.solarradarapp.ui.jobs

import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.solarradarapp.model.JobStatus
import com.example.solarradarapp.model.Report
import com.example.solarradarapp.ui.components.JobStatusChip
import com.example.solarradarapp.ui.components.ReportStatusChip
import com.example.solarradarapp.ui.components.daysUntilDeadline
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    firestoreId: String,
    onBack: () -> Unit,
    onSubmitReport: (String) -> Unit,
    onEditReport: (String) -> Unit = {},
    viewModel: JobDetailViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return JobDetailViewModel(firestoreId) as T
        }
    })
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        viewModel.job?.jobId ?: strings.jobDetail,
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.primaryBlue
                )
                viewModel.job == null -> Text(
                    viewModel.errorMessage ?: strings.jobDetail,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = StatusError
                )
                else -> {
                    val job = viewModel.job!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, colors.divider)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(strings.jobTypeLabel(job.type), style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(job.siteName, fontSize = 13.sp, color = colors.textSecondary)
                                }
                                JobStatusChip(job.status)
                            }
                        }

                        // Detail rows card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, colors.divider)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                DetailRow(strings.assignedTo, job.assignedName)
                                if (job.deadline != null) {
                                    HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                                    val days = daysUntilDeadline(job.deadline)
                                    val deadlineDisplay = if (days != null)
                                        "${job.deadline}  ·  ${strings.daysLeftLabel(days)}"
                                    else job.deadline
                                    DetailRow(strings.deadline, deadlineDisplay)
                                }
                                if (job.description.isNotBlank()) {
                                    HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                                    DetailRow(strings.description, job.description)
                                }
                                viewModel.siteAddress?.let { address ->
                                    HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                                    AddressDetailRow(
                                        label = strings.address,
                                        address = address,
                                        onClick = {
                                            val gmmUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri)
                                                .apply { setPackage("com.google.android.apps.maps") }
                                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(mapIntent)
                                            } else {
                                                val browserUri = Uri.parse(
                                                    "https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}"
                                                )
                                                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Job attachments from website (loaded from Firebase Storage)
                        if (viewModel.isLoadingAttachments) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colors.primaryBlue
                            )
                        } else if (viewModel.jobAttachmentUrls.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                                elevation = CardDefaults.cardElevation(0.dp),
                                border = BorderStroke(1.dp, colors.divider)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        strings.jobAttachments,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        viewModel.jobAttachmentUrls.forEach { url ->
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Expertise card
                        if (job.requiredExpertise.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
                                elevation = CardDefaults.cardElevation(0.dp),
                                border = BorderStroke(1.dp, colors.divider)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        strings.requiredExpertise,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        job.requiredExpertise.forEach { exp ->
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(strings.expertiseLabel(exp), fontSize = 12.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Site map — shown only when admin has placed an exact marker
                        viewModel.siteLocation?.let { (lat, lng) ->
                            SiteMapCard(lat = lat, lng = lng)
                        }

                        viewModel.errorMessage?.let {
                            Text(it, color = StatusError, fontSize = 13.sp)
                        }

                        // Action buttons
                        if (job.status == JobStatus.OPEN) {
                            Button(
                                onClick = { viewModel.startJob() },
                                enabled = !viewModel.isUpdating,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (viewModel.isUpdating) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Text(strings.startJob, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (job.status == JobStatus.IN_PROGRESS) {
                            Button(
                                onClick = { onSubmitReport(firestoreId) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(strings.submitReport, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Report section — only for resolved jobs
                        if (job.status == JobStatus.RESOLVED) {
                            ReportSection(
                                isLoading = viewModel.isLoadingReport,
                                report = viewModel.report,
                                strings = strings,
                                onEditReport = onEditReport
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSection(
    isLoading: Boolean,
    report: Report?,
    strings: com.example.solarradarapp.ui.strings.AppStrings,
    onEditReport: (String) -> Unit
) {
    val colors = LocalAppColors.current

    // Section header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            strings.reportSection,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary
        )
        if (report?.editedByTechnicianName != null) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = StatusInfo.copy(alpha = 0.12f)
            ) {
                Text(
                    strings.editedLabel,
                    fontSize = 11.sp,
                    color = StatusInfo,
                    fontWeight = FontWeight.Medium,
                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator(
            modifier = androidx.compose.ui.Modifier
                .padding(vertical = 8.dp),
            color = colors.primaryBlue,
            strokeWidth = 2.dp
        )
        return
    }

    if (report == null) return

    // Report info card
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, colors.divider),
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        Column(modifier = androidx.compose.ui.Modifier.padding(vertical = 4.dp)) {
            // Status row
            Row(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(strings.workStatus, fontSize = 14.sp, color = colors.textSecondary,
                    modifier = androidx.compose.ui.Modifier.width(140.dp))
                ReportStatusChip(report.status)
            }
            HorizontalDivider(color = colors.divider, modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp))
            DetailRow(strings.submittedOn, report.submittedAt)
            if (report.notes.isNotBlank()) {
                HorizontalDivider(color = colors.divider, modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp))
                Column(modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(strings.notes, fontSize = 14.sp, color = colors.textSecondary)
                    Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                    Text(report.notes, fontSize = 14.sp, color = colors.textPrimary)
                }
            }
        }
    }

    // Photos card
    if (report.photoUrls.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(1.dp, colors.divider),
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
                Text(
                    strings.photos,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    report.photoUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = androidx.compose.ui.Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }

    // Edit button
    OutlinedButton(
        onClick = { onEditReport(report.firestoreId) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, colors.primaryBlue),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryBlue),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = androidx.compose.ui.Modifier.size(16.dp))
        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
        Text(strings.editReport, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AddressDetailRow(label: String, address: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.textSecondary,
            modifier = Modifier.width(140.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = colors.primaryBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = address,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primaryBlue,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
private fun SiteMapCard(lat: Double, lng: Double) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.backgroundCard),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = "SolarRadar/1.0"
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    val geoPoint = GeoPoint(lat, lng)
                    controller.setCenter(geoPoint)
                    val marker = Marker(this)
                    marker.position = geoPoint
                    marker.icon = createOrangeMarkerIcon(ctx)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    marker.title = null
                    overlays.add(marker)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

private fun createOrangeMarkerIcon(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val px = (36 * density).toInt()
    val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#f97316")
        style = Paint.Style.FILL
    }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = px * 0.10f
    }
    val r = px / 2f - stroke.strokeWidth
    canvas.drawCircle(px / 2f, px / 2f, r, fill)
    canvas.drawCircle(px / 2f, px / 2f, r, stroke)
    return BitmapDrawable(context.resources, bitmap)
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.textSecondary,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
