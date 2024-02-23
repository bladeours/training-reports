package com.bladeours.trainingReport.model.response

data class AuthenticationResponse (
    val accessToken: String,
    val refreshToken: String
)