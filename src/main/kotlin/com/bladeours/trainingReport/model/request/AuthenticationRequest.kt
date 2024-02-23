package com.bladeours.trainingReport.model.request

data class AuthenticationRequest(
    val email: String,
    val password: String
)