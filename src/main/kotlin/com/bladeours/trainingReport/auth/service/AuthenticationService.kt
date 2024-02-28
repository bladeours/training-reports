package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.auth.model.request.AuthenticationRequest
import com.bladeours.trainingReport.auth.model.request.RegisterRequest
import com.bladeours.trainingReport.auth.model.response.TokenResponse
import com.bladeours.trainingReport.auth.service.repository.UserRepository
import com.bladeours.trainingReport.exception.AppException
import com.bladeours.trainingReport.model.User
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {
    fun login(authenticationRequest: AuthenticationRequest): TokenResponse {
        authenticate(authenticationRequest.email, authenticationRequest.password)
        return createAuthResponse(authenticationRequest.email)
    }

    private fun createAuthResponse(email: String): TokenResponse {
        val user = userDetailsService.loadUserByUsername(email)
        val accessToken = tokenService.generateAccessToken(user)
        return TokenResponse(accessToken)
    }

    private fun authenticate(email: String, password: String) {
        val auth = authManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
        SecurityContextHolder.getContext().authentication = auth
    }

    fun register(registerRequest: RegisterRequest): TokenResponse {
        userRepository.findByEmail(registerRequest.email).ifPresent {
            throw AppException("User already exists", HttpStatus.CONFLICT)
        }
        userRepository.insert(registerRequest.toUser())
        authenticate(registerRequest.email, registerRequest.password)
        return createAuthResponse(registerRequest.email)
    }

    private fun RegisterRequest.toUser(): User =
        User(
            email = this.email,
            password = encoder.encode(this.password),
            firstName = this.firstName,
            lastName = this.lastName,
            refreshToken = "",
            role = SimpleGrantedAuthority("USER"))
}
