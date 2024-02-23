package com.bladeours.trainingReport.model.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)
