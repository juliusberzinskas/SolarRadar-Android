package com.example.solarradarapp.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solarradarapp.model.Job
import com.example.solarradarapp.model.JobStatus
import com.example.solarradarapp.model.Report
import com.example.solarradarapp.model.SiteMounting
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobDetailViewModel(private val firestoreId: String) : ViewModel() {

    var job by mutableStateOf<Job?>(null)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)
    var isUpdating by mutableStateOf(false)

    var report by mutableStateOf<Report?>(null)
    var isLoadingReport by mutableStateOf(false)

    var siteAddress by mutableStateOf<String?>(null)
    var siteLocation by mutableStateOf<Pair<Double, Double>?>(null)
    var siteMounting by mutableStateOf<SiteMounting?>(null)
    var jobAttachmentUrls by mutableStateOf<List<String>>(emptyList())
    var isLoadingAttachments by mutableStateOf(false)

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    init {
        loadJob()
    }

    private fun loadJob() {
        db.collection("jobs").document(firestoreId).get()
            .addOnSuccessListener { doc ->
                val loadedJob = Job(
                    firestoreId = doc.id,
                    jobId = doc.getString("jobId") ?: "",
                    siteId = doc.getString("siteId") ?: "",
                    siteName = doc.getString("siteName") ?: "",
                    type = doc.getString("type") ?: "",
                    status = doc.getString("status") ?: "",
                    deadline = doc.getString("deadline"),
                    description = doc.getString("description") ?: "",
                    requiredExpertise = (doc.get("requiredExpertise") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),
                    assignedTo = doc.getString("assignedTo") ?: "",
                    assignedName = doc.getString("assignedName") ?: "",
                    archived = doc.getBoolean("archived") ?: false,
                    updatedAt = doc.getString("updatedAt")
                )
                job = loadedJob
                isLoading = false
                if (loadedJob.siteId.isNotBlank()) loadSiteAddress(loadedJob.siteId)
                loadJobAttachments(firestoreId)
                if (loadedJob.status == JobStatus.RESOLVED) {
                    loadReport(loadedJob.jobId)
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Failed to load job: ${e.message}"
            }
    }

    private fun loadSiteAddress(siteId: String) {
        db.collection("sites").document(siteId).get()
            .addOnSuccessListener { doc ->
                val addr = doc.getString("address")
                if (!addr.isNullOrBlank()) siteAddress = addr

                val loc = doc.get("location") as? Map<*, *>
                val lat = loc?.get("lat") as? Double
                val lng = loc?.get("lng") as? Double
                if (lat != null && lng != null) siteLocation = Pair(lat, lng)

                val m = doc.get("mounting") as? Map<*, *>
                if (m != null) {
                    siteMounting = SiteMounting(
                        panelType = m["panelType"] as? String,
                        panelCount = (m["panelCount"] as? Long)?.toInt(),
                        inverterModel = m["inverterModel"] as? String,
                        mountingType = m["mountingType"] as? String,
                        installationDate = m["installationDate"] as? String
                    )
                }
            }
    }

    private fun loadJobAttachments(jobFirestoreId: String) {
        isLoadingAttachments = true
        val ref = storage.reference.child("jobs/$jobFirestoreId/attachments")
        ref.listAll()
            .addOnSuccessListener { result ->
                if (result.items.isEmpty()) {
                    isLoadingAttachments = false
                    return@addOnSuccessListener
                }
                val urls = mutableListOf<String>()
                var remaining = result.items.size
                result.items.forEach { item ->
                    item.downloadUrl
                        .addOnSuccessListener { uri ->
                            urls.add(uri.toString())
                            remaining--
                            if (remaining == 0) {
                                jobAttachmentUrls = urls.toList()
                                isLoadingAttachments = false
                            }
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) {
                                jobAttachmentUrls = urls.toList()
                                isLoadingAttachments = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                isLoadingAttachments = false
            }
    }

    private fun loadReport(jobId: String) {
        val uid = auth.currentUser?.uid ?: return
        isLoadingReport = true
        db.collection("reports")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("technicianId", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                if (doc != null) {
                    val timestamp = doc.getTimestamp("submittedAt")
                    val submittedAt = timestamp?.toDate()?.let {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                    } ?: ""
                    report = Report(
                        firestoreId = doc.id,
                        jobId = doc.getString("jobId") ?: "",
                        siteName = doc.getString("siteName") ?: "",
                        technicianId = doc.getString("technicianId") ?: "",
                        technicianName = doc.getString("technicianName") ?: "",
                        submittedAt = submittedAt,
                        status = doc.getString("status") ?: "",
                        notes = doc.getString("notes") ?: "",
                        photoUrls = (doc.get("photoUrls") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        adminNotes = doc.getString("adminNotes") ?: "",
                        editedByTechnicianName = doc.getString("editedByTechnicianName")
                    )
                }
                isLoadingReport = false
            }
            .addOnFailureListener {
                isLoadingReport = false
            }
    }

    fun startJob() {
        updateStatus(JobStatus.IN_PROGRESS)
    }

    private fun updateStatus(newStatus: String) {
        isUpdating = true
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.collection("jobs").document(firestoreId)
            .update(mapOf("status" to newStatus, "updatedAt" to today))
            .addOnSuccessListener {
                job = job?.copy(status = newStatus)
                isUpdating = false
            }
            .addOnFailureListener { e ->
                isUpdating = false
                errorMessage = "Update failed: ${e.message}"
            }
    }
}
