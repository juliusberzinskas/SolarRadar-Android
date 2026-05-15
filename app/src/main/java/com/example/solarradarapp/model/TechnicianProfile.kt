package com.example.solarradarapp.model

data class TechnicianProfile(
    val displayName: String = "",
    val email: String = "",
    val role: String = "",
    val active: Boolean = false,
    val memberId: String = "",
    val expertise: List<String> = emptyList()
)
