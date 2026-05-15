package com.example.solarradarapp.model

data class Report(
    val firestoreId: String,
    val jobId: String,
    val siteName: String,
    val technicianId: String,
    val technicianName: String,
    /** Formatted display string, e.g. "08 Apr 2026" */
    val submittedAt: String,
    val status: String,
    val notes: String,
    val photoUrls: List<String>,
    val adminNotes: String = "",
    /** Non-null when the report has been edited after initial submission */
    val editedByTechnicianName: String? = null
)
