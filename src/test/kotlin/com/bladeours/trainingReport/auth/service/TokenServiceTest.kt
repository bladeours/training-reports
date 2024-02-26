package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.config.JwtProperties
import com.bladeours.trainingReport.model.User
import com.bladeours.trainingReport.service.UserService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.security.core.authority.SimpleGrantedAuthority

class TokenServiceTest {
    private val user =
        User(
            email = "",
            password = "",
            role = SimpleGrantedAuthority("USER"),
            firstName = "",
            lastName = "",
            refreshToken = "")

    @Test
    fun `should generate access token`() {
        // given
        val jwtProperties = JwtProperties("45k3kofmweaofaetogetyeeahaehksdfgnp;4o0p-ttgk09r034g3k4gbosfk0320", 1000, 1000, "cookie")
        val userService = mock<UserService>()
        {on { getLoggedUser() } doReturn user}
        val tokenService = TokenService(jwtProperties, userService)
        // when
        val accessToken = tokenService.generateAccessToken(user)
        // then
        assertNotNull(accessToken)
    }
}