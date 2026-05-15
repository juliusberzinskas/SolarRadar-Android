package com.example.solarradarapp.ui.report

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditReportViewModel(private val reportId: String) : ViewModel() {

    var isLoading by mutableStateOf(true)
    var isSaving by mutableStateOf(false)
    var saveSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Display-only fields
    var reportJobId by mutableStateOf("")
    var reportSiteName by mutableStateOf("")

    // Editable fields
    var notes by mutableStateOf("")

    /** URLs already stored in Firestore — shown as existing photos */
    val existingPhotoUrls = mutableStateListOf<String>()

    /** New photos selected from device — not yet uploaded */
    val newPhotoUris = mutableStateListOf<Uri>()

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Stored so we can write editedByTechnicianName on save
    private var technicianName = ""

    init {
        loadReport()
    }

    private fun loadReport() {
        db.collection("reports").document(reportId).get()
            .addOnSuccessListener { doc ->
                reportJobId = doc.getString("jobId") ?: ""
                reportSiteName = doc.getString("siteName") ?: ""
                technicianName = doc.getString("technicianName") ?: ""
                notes = doc.getString("notes") ?: ""
                existingPhotoUrls.addAll(
                    (doc.get("photoUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                errorMessage = it.message
            }
    }

    fun addPhotos(uris: List<Uri>) {
        newPhotoUris.addAll(uris)
    }

    fun removeExistingPhoto(url: String) {
        existingPhotoUrls.remove(url)
        // Note: the file remains in Firebase Storage until a server-side cleanup job removes orphans.
    }

    fun removeNewPhoto(uri: Uri) {
        newPhotoUris.remove(uri)
    }

    fun saveChanges(context: Context) {
        val uid = auth.currentUser?.uid ?: run {
            errorMessage = "Not authenticated."
            return
        }
        isSaving = true
        errorMessage = null

        uploadNewPhotos(context, newPhotoUris.toList()) { newUrls, err ->
            if (err != null) {
                isSaving = false
                errorMessage = "Photo upload failed: $err"
                return@uploadNewPhotos
            }

            val finalPhotoUrls = existingPhotoUrls.toList() + newUrls

            val updates: Map<String, Any> = mapOf(
                "notes"                   to notes,
                "photoUrls"               to finalPhotoUrls,
                // ── Website alert variables ──────────────────────────────────
                // Website listens: db.collection("reports").where("editFlag","==",true)
                // After acknowledging, website resets editFlag to false.
                "editFlag"                to true,
                "editedAt"                to FieldValue.serverTimestamp(),
                "editedByTechnicianId"    to uid,
                "editedByTechnicianName"  to technicianName
                // ────────────────────────────────────────────────────────────
            )

            db.collection("reports").document(reportId)
                .update(updates)
                .addOnSuccessListener {
                    isSaving = false
                    saveSuccess = true
                }
                .addOnFailureListener { e ->
                    isSaving = false
                    errorMessage = "Failed to save: ${e.message}"
                }
        }
    }

    private fun uploadNewPhotos(
        context: Context,
        uris: List<Uri>,
        onComplete: (List<String>, String?) -> Unit
    ) {
        if (uris.isEmpty()) {
            onComplete(emptyList(), null)
            return
        }
        val urls = mutableListOf<String>()
        var index = 0

        fun uploadNext() {
            if (index >= uris.size) {
                onComplete(urls, null)
                return
            }
            val uri = uris[index]
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (nameIndex >= 0) cursor.getString(nameIndex) else "photo_$index.jpg"
            } ?: "photo_$index.jpg"

            val ref = storage.reference
                .child("reports/$reportId/photos/${System.currentTimeMillis()}_$fileName")
            ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    ref.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    urls.add(downloadUri.toString())
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
