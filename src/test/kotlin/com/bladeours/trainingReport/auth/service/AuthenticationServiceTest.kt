package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.auth.model.request.AuthenticationRequest
import com.bladeours.trainingReport.auth.model.request.RegisterRequest
import com.bladeours.trainingReport.auth.service.repository.UserRepository
import com.bladeours.trainingReport.exception.AppException
import com.bladeours.trainingReport.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder

class AuthenticationServiceTest {

    private val user =
        User(
            email = "",
            password = "",
            role = SimpleGrantedAuthority("USER"),
            firstName = "",
            lastName = "",
            refreshToken = "")

    @Test
    fun `authenticate() should authenticate by authManager when run authentication`() {
        // given
        val authManagerMock = mock<AuthenticationManager>()
        val userDetailsServiceMock =
            mock<CustomUserDetailsService> { on { loadUserByUsername(any()) } doReturn user }
        val tokenService = mock<TokenService> { on { generateAccessToken(any()) } doReturn "token" }
        val authenticationService =
            AuthenticationService(
                authManagerMock, userDetailsServiceMock, tokenService, mock(), mock())
        val authRequest = AuthenticationRequest("email", "password")
        // when
        authenticationService.authentication(authRequest)
        // then
        verify(authManagerMock)
            .authenticate(
                eq(UsernamePasswordAuthenticationToken(authRequest.email, authRequest.password)))
    }

    @Test
    fun `authenticate() should return authResponse when run authentication`() {
        // given
        val authManagerMock = mock<AuthenticationManager>()
        val userDetailsServiceMock =
            mock<CustomUserDetailsService> { on { loadUserByUsername(any()) } doReturn user }
        val tokenService = mock<TokenService> { on { generateAccessToken(any()) } doReturn "token" }
        val authenticationService =
            AuthenticationService(
                authManagerMock, userDetailsServiceMock, tokenService, mock(), mock())
        val authRequest = AuthenticationRequest("email", "password")
        // when
        val response = authenticationService.authentication(authRequest)
        // then
        assertThat(response.token).isEqualTo("token")
    }

    @Test
    fun `register() should throw AppException when user already exists`() {
        // given
        val authManagerMock = mock<AuthenticationManager>()
        val userDetailsServiceMock = mock<CustomUserDetailsService>()
        val tokenService = mock<TokenService>()
        val registerRequest = RegisterRequest("email", "", "", "")
        val userRepoMock =
            mock<UserRepository> { on { findByEmail(registerRequest.email) } doReturn user }
        val authenticationService =
            AuthenticationService(
                authManagerMock, userDetailsServiceMock, tokenService, userRepoMock, mock())
        // when then
        assertThrows<AppException> { authenticationService.register(registerRequest) }
    }

    @Test
    fun `register() should insert user with encoded password to database`() {
        // given
        val registerRequest = RegisterRequest("email", "password", "", "")
        val userRepoMock = mock<UserRepository>() { on { findByEmail(any()) } doReturn null }
        val userDetailsService =
            mock<CustomUserDetailsService>() { on { loadUserByUsername(any()) } doReturn user }
        val passwordEncoder =
            mock<PasswordEncoder> { on { encode(any()) } doReturn ("encodedPassword") }
        val tokenService =
            mock<TokenService>() { on { generateAccessToken(any()) } doReturn "token" }
        val authenticationService =
            AuthenticationService(
                mock(), userDetailsService, tokenService, userRepoMock, passwordEncoder)
        // when

        authenticationService.register(registerRequest)
        // then
        verify(userRepoMock)
            .insert(
                check<User> {
                    assertAll(
                        { assertThat(it.email).isEqualTo(registerRequest.email) },
                        { assertThat(it.lastName).isEqualTo(registerRequest.lastName) },
                        { assertThat(it.firstName).isEqualTo(registerRequest.firstName) },
                        { assertThat(it.role).isEqualTo(SimpleGrantedAuthority("USER")) },
                        { assertThat(it.password).isEqualTo("encodedPassword") })
                })
    }
}
