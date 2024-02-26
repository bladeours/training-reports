package com.bladeours.trainingReport.auth.model.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)
