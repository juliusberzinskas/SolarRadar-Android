package com.example.solarradarapp.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solarradarapp.model.TechnicianProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    var profile by mutableStateOf<TechnicianProfile?>(null)
    var isLoading by mutableStateOf(true)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
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
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }
}
