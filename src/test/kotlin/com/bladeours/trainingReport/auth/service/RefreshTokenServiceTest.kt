package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.config.JwtProperties
import com.bladeours.trainingReport.model.User
import com.bladeours.trainingReport.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority

class RefreshTokenServiceTest {
    private val user =
        User(
            email = "",
            password = "",
            role = SimpleGrantedAuthority("USER"),
            firstName = "",
            lastName = "",
            refreshToken = "token")

    @Test
    fun `isValid() should return true if token service return true and logged user has proper refresh token`() {
        // given
        val tokenService = mock<TokenService>() { on { isValid(any()) } doReturn true }
        val userService = mock<UserService>() { on { getLoggedUser() } doReturn user }
        val jwtProperties = mock<JwtProperties>()
        val refreshTokenService = RefreshTokenService(tokenService, jwtProperties, userService)
        // when
        val isValid = refreshTokenService.isValid(user.refreshToken)
        // then
        assertThat(isValid).isTrue()
    }

    @Test
    fun `isValid() should return false if token service returns false and logged user has proper refresh token`() {
        // given
        val tokenService = mock<TokenService>() { on { isValid(any()) } doReturn false }
        val userService = mock<UserService>() { on { getLoggedUser() } doReturn user }
        val jwtProperties = mock<JwtProperties>()
        val refreshTokenService = RefreshTokenService(tokenService, jwtProperties, userService)
        // when
        val isValid = refreshTokenService.isValid(user.refreshToken)
        // then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `isValid() should return false if token service returns true and logged user has wrong refresh token`() {
        // given
        val tokenService = mock<TokenService>() { on { isValid(any()) } doReturn true }
        val userService = mock<UserService>() { on { getLoggedUser() } doReturn user }
        val jwtProperties = mock<JwtProperties>()
        val refreshTokenService = RefreshTokenService(tokenService, jwtProperties, userService)
        // when
        val isValid = refreshTokenService.isValid("some token")
        // then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `should create cookie with refresh token`() {
        // given
        val tokenService =
            mock<TokenService>() { on { generateRefreshToken(any()) } doReturn "new token" }
        val userService = mock<UserService>() { on { getLoggedUser() } doReturn user }
        val jwtProperties = mock<JwtProperties>() { on { refreshTokenCookie } doReturn "cookie" }
        val refreshTokenService = RefreshTokenService(tokenService, jwtProperties, userService)
        // when
        val cookie = refreshTokenService.createCookieWithRefreshTokenAndUpdateInUser()
        // then
        assertAll(
            { assertThat(cookie.name).isEqualTo(jwtProperties.refreshTokenCookie) },
            { assertThat(cookie.value).isEqualTo("new token") },
            { assertThat(cookie.isHttpOnly).isTrue() },
            { assertThat(cookie.secure).isTrue() })
    }

    @Test
    fun `should update refresh token in user`() {
        // given
        val tokenService =
            mock<TokenService>() { on { generateRefreshToken(any()) } doReturn "new token" }
        val userService = mock<UserService>() { on { getLoggedUser() } doReturn user }
        val jwtProperties = mock<JwtProperties>() { on { refreshTokenCookie } doReturn "cookie" }
        val refreshTokenService = RefreshTokenService(tokenService, jwtProperties, userService)
        // when
        refreshTokenService.createCookieWithRefreshTokenAndUpdateInUser()
        // then
        verify(userService, times(1)).updateRefreshToken("new token")
    }
}
