package com.example.solarradarapp.ui.report

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solarradarapp.model.Job
import com.example.solarradarapp.model.JobStatus
import com.example.solarradarapp.model.ReportStatus
import com.example.solarradarapp.model.TechnicianProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportFormViewModel(private val firestoreId: String) : ViewModel() {

    var job by mutableStateOf<Job?>(null)
    var profile by mutableStateOf<TechnicianProfile?>(null)
    var isLoadingJob by mutableStateOf(true)

    var selectedStatus by mutableStateOf(ReportStatus.COMPLETED)
    var notes by mutableStateOf("")
    val photoUris = mutableStateListOf<Uri>()

    var isSubmitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var submitSuccess by mutableStateOf(false)

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadJobAndProfile()
    }

    private fun loadJobAndProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("jobs").document(firestoreId).get()
            .addOnSuccessListener { doc ->
                job = Job(
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
                isLoadingJob = false
            }
            .addOnFailureListener { isLoadingJob = false }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                profile = TechnicianProfile(
                    displayName = doc.getString("displayName") ?: "",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "",
                    active = doc.getBoolean("active") ?: false,
                    memberId = doc.getString("memberId") ?: "",
                    expertise = (doc.get("expertise") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
            }
    }

    fun addPhotos(uris: List<Uri>) {
        photoUris.addAll(uris)
    }

    fun removePhoto(uri: Uri) {
        photoUris.remove(uri)
    }

    fun submitReport(context: Context) {
        val currentJob = job ?: return
        val currentProfile = profile ?: return
        isSubmitting = true
        errorMessage = null

        val uid = auth.currentUser?.uid ?: run {
            errorMessage = "Not authenticated."
            isSubmitting = false
            return
        }

        val reportRef = db.collection("reports").document()
        val reportId = reportRef.id

        uploadPhotos(context, reportId, photoUris.toList()) { downloadUrls, uploadError ->
            if (uploadError != null) {
                isSubmitting = false
                errorMessage = "Photo upload failed: $uploadError"
                return@uploadPhotos
            }

            val report = hashMapOf(
                "jobId" to currentJob.jobId,
                "jobTitle" to currentJob.jobId,
                "siteId" to currentJob.siteId,
                "siteName" to currentJob.siteName,
                "technicianId" to uid,
                "technicianName" to currentProfile.displayName,
                "submittedAt" to FieldValue.serverTimestamp(),
                "status" to selectedStatus,
                "notes" to notes,
                "photoUrls" to downloadUrls,
                "adminNotes" to ""
            )

            reportRef.set(report)
                .addOnSuccessListener {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    db.collection("jobs").document(firestoreId)
                        .update(mapOf("status" to JobStatus.RESOLVED, "updatedAt" to today))
                        .addOnSuccessListener {
                            isSubmitting = false
                            submitSuccess = true
                        }
                        .addOnFailureListener { e ->
                            isSubmitting = false
                            errorMessage = "Report saved but job status update failed: ${e.message}"
                        }
                }
                .addOnFailureListener { e ->
                    isSubmitting = false
                    errorMessage = "Failed to save report: ${e.message}"
                }
        }
    }

    private fun uploadPhotos(
        context: Context,
        reportId: String,
        uris: List<Uri>,
        onComplete: (List<String>, String?) -> Unit
    ) {
        if (uris.isEmpty()) {
            onComplete(emptyList(), null)
            return
        }
        val downloadUrls = mutableListOf<String>()
        var index = 0

        fun uploadNext() {
            if (index >= uris.size) {
                onComplete(downloadUrls, null)
                return
            }
            val uri = uris[index]
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (nameIndex >= 0) cursor.getString(nameIndex) else "photo_$index.jpg"
            } ?: "photo_$index.jpg"

            val ref = storage.reference.child("reports/$reportId/photos/${System.currentTimeMillis()}_$fileName")
            ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    ref.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    downloadUrls.add(downloadUri.toString())
                    index++
                    uploadNext()
                }
                .addOnFailureListener { e ->
                    onComplete(emptyList(), e.message)
                }
        }

        uploadNext()
    }
}
