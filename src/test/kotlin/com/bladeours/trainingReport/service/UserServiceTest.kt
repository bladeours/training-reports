package com.bladeours.trainingReport.service

import com.bladeours.trainingReport.auth.service.repository.UserRepository
import com.bladeours.trainingReport.exception.AppException
import com.bladeours.trainingReport.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class UserServiceTest {
    private val user =
        User(
            email = "",
            password = "",
            role = SimpleGrantedAuthority("USER"),
            firstName = "",
            lastName = "",
            refreshToken = "")

    @Test
    fun `should get user when authentication is not null`() {
        // given
        val userRepoMock = mock<UserRepository> { on { findByEmail(any()) } doReturn user }
        val userService = UserService(userRepoMock)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities)

        // when
        val actualUser = userService.getLoggedUser()

        // then
        assertThat(actualUser).isEqualTo(user)
    }

    @Test
    fun `should return AppException when can not find user`() {
        // given
        val userRepoMock = mock<UserRepository> { on { findByEmail(any()) } doReturn null }
        val userService = UserService(userRepoMock)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities)

        // when then
        assertThrows<AppException> { userService.getLoggedUser() }
    }

    @Test
    fun `should return AppException when securityContextHolder auth is null`() {
        // given
        val userRepoMock = mock<UserRepository>()
        val userService = UserService(userRepoMock)

        // when then
        assertThrows<AppException> { userService.getLoggedUser() }
    }

    @Test
    fun `should update refresh token in user`() {
        // given
        val userRepoMock = mock<UserRepository> { on { findByEmail(any()) } doReturn user }
        val userService = UserService(userRepoMock)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities)

        // when
        userService.updateRefreshToken("refreshToken")

        // then
        verify(userRepoMock).save(argThat { this.refreshToken == "refreshToken" })
    }
}
