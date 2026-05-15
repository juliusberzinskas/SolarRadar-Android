package com.example.solarradarapp.model

object JobType {
    val displayLabels = mapOf(
        "inverter_fault" to "Inverter Fault",
        "communication" to "Communication Issue",
        "string_issue" to "String Issue",
        "inspection" to "Inspection",
        "maintenance" to "Maintenance"
    )

    fun label(value: String) = displayLabels[value] ?: value
}

object JobStatus {
    const val OPEN = "open"
    const val IN_PROGRESS = "in_progress"
    const val RESOLVED = "resolved"

    val displayLabels = mapOf(
        OPEN to "Open",
        IN_PROGRESS to "In Progress",
        RESOLVED to "Resolved"
    )

    fun label(value: String) = displayLabels[value] ?: value
}

object ReportStatus {
    const val COMPLETED = "completed"
    const val NOT_COMPLETED = "not_completed"
    const val REQUIRES_MAINTENANCE = "requires_maintenance"

    val all = listOf(COMPLETED, NOT_COMPLETED, REQUIRES_MAINTENANCE)

    val displayLabels = mapOf(
        COMPLETED to "Completed",
        NOT_COMPLETED to "Not Completed",
        REQUIRES_MAINTENANCE to "Requires Maintenance"
    )

    fun label(value: String) = displayLabels[value] ?: value
}

object ExpertiseType {
    val displayLabels = mapOf(
        "electrician" to "Electrician",
        "inv_elect" to "Inverter Electrician",
        "mount_spec" to "Mounting System Specialist",
        "panel_spec" to "Panel Specialist"
    )

    fun label(value: String) = displayLabels[value] ?: value
}
