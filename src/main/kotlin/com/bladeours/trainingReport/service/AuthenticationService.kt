package com.bladeours.trainingReport.service

import com.bladeours.trainingReport.model.User
import com.bladeours.trainingReport.model.request.AuthenticationRequest
import com.bladeours.trainingReport.model.request.RegisterRequest
import com.bladeours.trainingReport.model.response.AuthenticationResponse
import com.bladeours.trainingReport.repository.RefreshTokenRepository
import com.bladeours.trainingReport.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {
    fun authentication(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
        authenticate(authenticationRequest.email, authenticationRequest.password)
        return createAuthResponse(authenticationRequest.email)
    }

    private fun createAuthResponse(email: String): AuthenticationResponse {
        val user = userDetailsService.loadUserByUsername(email)
        val accessToken = tokenService.generateAccessToken(user)
        val refreshToken = tokenService.generateRefreshToken(user)
        refreshTokenRepository.save(refreshToken, user)
        return AuthenticationResponse(accessToken, refreshToken)
    }

    private fun authenticate(email: String, password: String) {
        authManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
    }

    fun refreshAccessToken(refreshToken: String): String? {
        val extractedEmail = tokenService.extractEmail(refreshToken)
        return extractedEmail?.let { email ->
            val currentUserDetails = userDetailsService.loadUserByUsername(email)
            val refreshTokenUserDetails = refreshTokenRepository.findUserDetailsByToken(refreshToken)
            if (!tokenService.isExpired(refreshToken) && refreshTokenUserDetails?.username == currentUserDetails.username)
                tokenService.generateAccessToken(currentUserDetails)
            else
                null
        }
    }

    fun register(registerRequest: RegisterRequest): AuthenticationResponse {
        if (userRepository.findByEmail(registerRequest.email) != null) {
            throw IllegalStateException("User already exists")
        }
        userRepository.insert(registerRequest.toUser())
        return createAuthResponse(registerRequest.email)
    }

    private fun RegisterRequest.toUser(): User =
        User(
            email = this.email,
            password = encoder.encode(this.password),
            firstName = this.firstName,
            lastName = this.lastName,
            role = SimpleGrantedAuthority("USER")
        )
}




