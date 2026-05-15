package com.example.solarradarapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solarradarapp.model.JobStatus
import com.example.solarradarapp.model.ReportStatus
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.*

private val chipShape = RoundedCornerShape(50.dp)

@Composable
fun JobStatusChip(status: String) {
    val strings = LocalAppStrings.current
    val (label, color) = when (status) {
        JobStatus.OPEN -> strings.statusOpen to StatusWarning
        JobStatus.IN_PROGRESS -> strings.statusInProgress to StatusInfo
        JobStatus.RESOLVED -> strings.statusResolved to StatusSuccess
        else -> status to StatusDefault
    }
    StatusChip(label = label, color = color)
}

@Composable
fun ReportStatusChip(status: String) {
    val strings = LocalAppStrings.current
    val (label, color) = when (status) {
        ReportStatus.COMPLETED -> strings.statusCompleted to StatusSuccess
        ReportStatus.NOT_COMPLETED -> strings.statusNotCompleted to StatusError
        ReportStatus.REQUIRES_MAINTENANCE -> strings.statusRequiresMaintenance to StatusWarning
        else -> status to StatusDefault
    }
    StatusChip(label = label, color = color)
}

@Composable
fun StatusChip(label: String, color: Color) {
    Surface(
        shape = chipShape,
        color = Color.Transparent,
        modifier = Modifier.border(1.dp, color, chipShape)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun daysUntilDeadline(deadline: String?): Long? {
    if (deadline == null) return null
    return try {
        val date = java.time.LocalDate.parse(deadline)
        val today = java.time.LocalDate.now()
        java.time.temporal.ChronoUnit.DAYS.between(today, date)
    } catch (e: Exception) {
        null
    }
}

fun deadlineColor(deadline: String?): Color {
    if (deadline == null) return Color.Unspecified
    return try {
        val date = java.time.LocalDate.parse(deadline)
        val today = java.time.LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, date)
        when {
            days <= 6 -> StatusError
            days <= 21 -> StatusWarning
            else -> StatusInfo
        }
    } catch (e: Exception) {
        Color.Unspecified
    }
}
