package com.example.solarradarapp.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solarradarapp.model.Job
import com.example.solarradarapp.model.JobStatus
import com.example.solarradarapp.model.TechnicianProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JobListViewModel : ViewModel() {

    var currentJobs by mutableStateOf<List<Job>>(emptyList())
    var resolvedJobs by mutableStateOf<List<Job>>(emptyList())
    var profile by mutableStateOf<TechnicianProfile?>(null)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)
    var navigateToLogin by mutableStateOf(false)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: run {
            navigateToLogin = true
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                val active = doc.getBoolean("active") ?: false
                if (role != "technician" || !active) {
                    auth.signOut()
                    navigateToLogin = true
                    return@addOnSuccessListener
                }
                profile = TechnicianProfile(
                    displayName = doc.getString("displayName") ?: "",
                    email = doc.getString("email") ?: "",
                    role = role,
                    active = active,
                    memberId = doc.getString("memberId") ?: "",
                    expertise = (doc.get("expertise") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
                loadJobs(uid)
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Failed to load profile: ${e.message}"
            }
    }

    private fun loadJobs(uid: String) {
        db.collection("jobs")
            .whereEqualTo("assignedTo", uid)
            .whereEqualTo("archived", false)
            .get()
            .addOnSuccessListener { snap ->
                val allJobs = snap.documents.map { doc ->
                    Job(
                        firestoreId = doc.id,
                        jobId = doc.getString("jobId") ?: "",
                        siteId = doc.getString("siteId") ?: "",
                        siteName = doc.getString("siteName") ?: "",
                        type = doc.getString("type") ?: "",
                        status = doc.getString("status") ?: "",
                        deadline = doc.getString("deadline"),
                        description = doc.getString("description") ?: "",
                        requiredExpertise = (doc.get("requiredExpertise") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        assignedTo = doc.getString("assignedTo") ?: "",
                        assignedName = doc.getString("assignedName") ?: "",
                        archived = doc.getBoolean("archived") ?: false,
                        updatedAt = doc.getString("updatedAt")
                    )
                }
                currentJobs = allJobs.filter { it.status != JobStatus.RESOLVED }
                resolvedJobs = allJobs
                    .filter { it.status == JobStatus.RESOLVED && isWithinTwoWeeks(it.updatedAt) }
                    .sortedByDescending { it.updatedAt }
                isLoading = false
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Failed to load jobs: ${e.message}"
            }
    }

    private fun isWithinTwoWeeks(updatedAt: String?): Boolean {
        if (updatedAt == null) return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(updatedAt) ?: return true
            val twoWeeksAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -14) }.time
            !date.before(twoWeeksAgo)
        } catch (e: Exception) {
            true
        }
    }

    fun refresh() {
        isLoading = true
        errorMessage = null
        loadData()
    }

    fun logout() {
        auth.signOut()
        navigateToLogin = true
    }
}
