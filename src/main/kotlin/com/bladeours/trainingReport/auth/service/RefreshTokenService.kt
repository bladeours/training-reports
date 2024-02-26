package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.config.JwtProperties
import com.bladeours.trainingReport.service.UserService
import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val userService: UserService
) {

    fun isValid(refreshToken: String) =
        tokenService.isValid(refreshToken) &&
            userService.getLoggedUser().refreshToken == refreshToken

    fun createCookieWithRefreshTokenAndUpdateInUser(): Cookie {
        val token = tokenService.generateRefreshToken(userService.getLoggedUser())
        userService.updateRefreshToken(token)
        val cookie = Cookie(jwtProperties.refreshTokenCookie, token)
        cookie.isHttpOnly = true
        cookie.secure = true
        return cookie
    }
}
