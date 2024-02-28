package com.bladeours.trainingReport.auth.controller

import com.bladeours.trainingReport.auth.model.request.AuthenticationRequest
import com.bladeours.trainingReport.auth.model.request.RegisterRequest
import com.bladeours.trainingReport.auth.model.response.TokenResponse
import com.bladeours.trainingReport.auth.service.AuthenticationService
import com.bladeours.trainingReport.auth.service.RefreshTokenService
import com.bladeours.trainingReport.exception.AppException
import io.github.oshai.kotlinlogging.KLogger
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val httpServletResponse: HttpServletResponse,
    private val refreshTokenService: RefreshTokenService,
    private val log: KLogger,
) {
    @PostMapping("/login")
    fun authenticate(@RequestBody authRequest: AuthenticationRequest): TokenResponse {
        val token = authenticationService.login(authRequest)
        addCookieWithRefreshTokenToResponse()
        return token
    }

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): TokenResponse {
        val response = authenticationService.register(registerRequest)
        addCookieWithRefreshTokenToResponse()
        return response
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@CookieValue("\${jwt.refresh-token-cookie}") cookie: String) {
        if (!refreshTokenService.isValid(cookie)) {
            log.warn { "invalid refresh token" }
            throw AppException("invalid refreshToken", HttpStatus.BAD_REQUEST)
        }
        addCookieWithRefreshTokenToResponse()
    }

    private fun addCookieWithRefreshTokenToResponse() =
        httpServletResponse.addCookie(
            refreshTokenService.createCookieWithRefreshTokenAndUpdateInUser())
}
