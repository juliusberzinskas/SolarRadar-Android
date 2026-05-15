package com.example.solarradarapp.model

data class Job(
    var firestoreId: String = "",
    val jobId: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val type: String = "",
    val status: String = "",
    val deadline: String? = null,
    val description: String = "",
    val requiredExpertise: List<String> = emptyList(),
    val assignedTo: String = "",
    val assignedName: String = "",
    val archived: Boolean = false,
    val updatedAt: String? = null
)