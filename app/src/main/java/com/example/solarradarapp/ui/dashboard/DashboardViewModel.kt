package com.example.solarradarapp.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solarradarapp.model.Job
import com.example.solarradarapp.model.JobStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class DashboardViewModel : ViewModel() {

    var openCount by mutableStateOf(0)
    var inProgressCount by mutableStateOf(0)
    var dueThisWeekCount by mutableStateOf(0)
    var urgentJobs by mutableStateOf<List<Job>>(emptyList())
    var isLoading by mutableStateOf(true)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadStats()
    }

    private fun loadStats() {
        val uid = auth.currentUser?.uid ?: run {
            isLoading = false
            return
        }

        db.collection("jobs")
            .whereEqualTo("assignedTo", uid)
            .whereEqualTo("archived", false)
            .get()
            .addOnSuccessListener { snap ->
                val today = LocalDate.now()
                val weekLater = today.plusDays(7)

                var open = 0
                var inProgress = 0
                var dueThisWeek = 0

                snap.documents.forEach { doc ->
                    val status = doc.getString("status") ?: ""
                    val deadline = doc.getString("deadline")

                    when (status) {
                        JobStatus.OPEN -> open++
                        JobStatus.IN_PROGRESS -> inProgress++
                    }

                    if (status != JobStatus.RESOLVED && deadline != null) {
                        try {
                            val deadlineDate = LocalDate.parse(deadline)
                            if (!deadlineDate.isAfter(weekLater)) dueThisWeek++
                        } catch (e: Exception) { /* ignore malformed dates */ }
                    }
                }

                // Urgent jobs: non-resolved with a deadline, sorted nearest first, top 3
                urgentJobs = snap.documents
                    .mapNotNull { doc ->
                        val status = doc.getString("status") ?: ""
                        if (status == JobStatus.RESOLVED) return@mapNotNull null
                        val deadline = doc.getString("deadline") ?: return@mapNotNull null
                        Job(
                            firestoreId = doc.id,
                            jobId = doc.getString("jobId") ?: "",
                            siteId = doc.getString("siteId") ?: "",
                            siteName = doc.getString("siteName") ?: "",
                            type = doc.getString("type") ?: "",
                            status = status,
                            deadline = deadline,
                            description = doc.getString("description") ?: "",
                            requiredExpertise = (doc.get("requiredExpertise") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            assignedTo = doc.getString("assignedTo") ?: "",
                            assignedName = doc.getString("assignedName") ?: "",
                            archived = doc.getBoolean("archived") ?: false,
                            updatedAt = doc.getString("updatedAt")
                        )
                    }
                    .sortedBy { it.deadline }
                    .take(3)

                openCount = open
                inProgressCount = inProgress
                dueThisWeekCount = dueThisWeek
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }
}
