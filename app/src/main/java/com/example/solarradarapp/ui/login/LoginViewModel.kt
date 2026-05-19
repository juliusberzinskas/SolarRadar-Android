package com.example.solarradarapp.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solarradarapp.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class LoginError { EMPTY_FIELDS, ACCESS_DENIED, FIREBASE_ERROR }

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var loginError by mutableStateOf<LoginError?>(null)
    var firebaseErrorMessage by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf(false)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        // If Firebase already has a session, skip the login screen
        val uid = auth.currentUser?.uid
        if (uid != null) {
            isLoading = true
            verifyTechnicianRole(uid)
        }
    }

    private fun verifyTechnicianRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                val active = doc.getBoolean("active") ?: false
                if (role != "technician" || !active) {
                    auth.signOut()
                    loginError = LoginError.ACCESS_DENIED
                    isLoading = false
                } else {
                    NotificationHelper.registerToken(uid)
                    isLoading = false
                    loginSuccess = true
                }
            }
            .addOnFailureListener {
                auth.signOut()
                isLoading = false
            }
    }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            loginError = LoginError.EMPTY_FIELDS
            return
        }
        isLoading = true
        loginError = null

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                verifyTechnicianRole(result.user!!.uid)
            }
            .addOnFailureListener { e ->
                isLoading = false
                firebaseErrorMessage = e.message
                loginError = LoginError.FIREBASE_ERROR
            }
    }
}
