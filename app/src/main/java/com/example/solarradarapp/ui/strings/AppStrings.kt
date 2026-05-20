package com.example.solarradarapp.ui.strings

data class AppStrings(
    // Login
    val technicianPortal: String,
    val email: String,
    val password: String,
    val logIn: String,
    val enterEmailPassword: String,
    val accessDenied: String,

    // Navigation
    val navHome: String,
    val navJobs: String,
    val navProfile: String,

    // Top bar / general
    val settings: String,
    val logout: String,
    val retry: String,
    val back: String,

    // Job list
    val myJobs: String,
    val tabCurrent: String,
    val tabResolved: String,
    val noJobsAssigned: String,
    val noActiveJobs: String,
    val noResolvedJobs: String,
    val noResolvedJobsBody: String,
    val failedToLoadJobs: String,

    // Job detail
    val jobDetail: String,
    val assignedTo: String,
    val deadline: String,
    val description: String,
    val requiredExpertise: String,
    val startJob: String,
    val submitReport: String,

    // Report form
    val submitReportTitle: String,
    val workStatus: String,
    val notes: String,
    val photos: String,
    val addPhotos: String,
    val uploading: String,

    // Profile
    val profileTitle: String,
    val emailLabel: String,
    val memberId: String,
    val expertiseLabel: String,
    val technicianRole: String,

    // Settings
    val settingsTitle: String,
    val language: String,
    val english: String,
    val lithuanian: String,
    val darkMode: String,

    // Job status labels
    val statusOpen: String,
    val statusInProgress: String,
    val statusResolved: String,

    // Report status labels
    val statusCompleted: String,
    val statusNotCompleted: String,
    val statusRequiresMaintenance: String,

    // Job type labels
    val typeInverterFault: String,
    val typeCommunication: String,
    val typeStringIssue: String,
    val typeInspection: String,
    val typeMaintenance: String,

    // Expertise labels
    val expertiseElectrician: String,
    val expertiseInvElect: String,
    val expertiseMountSpec: String,
    val expertisePanelSpec: String,

    // Dashboard
    val dueThisWeek: String,
    val daysLeft: String,
    val dueToday: String,
    val overdue: String,
    val urgentDeadlines: String,
    val noUrgentJobs: String,
    val allOnTrack: String,

    // Report view / edit
    val reportSection: String,
    val editReport: String,
    val submittedOn: String,
    val saveChanges: String,
    val editedLabel: String,

    // Site / address
    val address: String,
    val jobAttachments: String,
    val openInMaps: String,

    // Job detail tabs
    val tabJobInfo: String,
    val tabMounting: String,
    val tabPictures: String,

    // Mounting system
    val panelType: String,
    val panelCount: String,
    val inverterModel: String,
    val mountingType: String,
    val installationDate: String,
    val noMountingInfo: String,
    val noPictures: String,
    val mountingTypeRoof: String,
    val mountingTypeGround: String,
) {
    fun jobStatusLabel(status: String) = when (status) {
        "open" -> statusOpen
        "in_progress" -> statusInProgress
        "resolved" -> statusResolved
        else -> status
    }

    fun reportStatusLabel(status: String) = when (status) {
        "completed" -> statusCompleted
        "not_completed" -> statusNotCompleted
        "requires_maintenance" -> statusRequiresMaintenance
        else -> status
    }

    fun jobTypeLabel(type: String) = when (type) {
        "inverter_fault" -> typeInverterFault
        "communication" -> typeCommunication
        "string_issue" -> typeStringIssue
        "inspection" -> typeInspection
        "maintenance" -> typeMaintenance
        else -> type
    }

    fun daysLeftLabel(days: Long): String = when {
        days < 0 -> overdue
        days == 0L -> dueToday
        else -> "$days $daysLeft"
    }

    fun mountingTypeLabel(value: String?) = when (value) {
        "Stogo" -> mountingTypeRoof
        "Žemės" -> mountingTypeGround
        else -> value ?: "—"
    }

    fun expertiseLabel(key: String) = when (key) {
        "electrician" -> expertiseElectrician
        "inv_elect" -> expertiseInvElect
        "mount_spec" -> expertiseMountSpec
        "panel_spec" -> expertisePanelSpec
        else -> key
    }

    fun reportStatusList() = listOf(
        "completed" to statusCompleted,
        "not_completed" to statusNotCompleted,
        "requires_maintenance" to statusRequiresMaintenance
    )
}
