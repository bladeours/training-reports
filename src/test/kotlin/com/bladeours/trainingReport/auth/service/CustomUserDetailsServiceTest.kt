package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.auth.service.repository.UserRepository
import com.bladeours.trainingReport.exception.AppException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.security.core.authority.SimpleGrantedAuthority

class CustomUserDetailsServiceTest {
    private val user =
        com.bladeours.trainingReport.model.User(
            email = "",
            password = "",
            role = SimpleGrantedAuthority("USER"),
            firstName = "",
            lastName = "",
            refreshToken = "")

    @Test
    fun `should throw AppException when can not find user`() {
        // given
        val userRepoMock = mock<UserRepository> { on { findByEmail(any()) } doReturn null }
        val customUserDetailsService = CustomUserDetailsService(userRepoMock)

        // when then
        assertThrows<AppException> { customUserDetailsService.loadUserByUsername("") }
    }

    @Test
    fun `should return user if is in database`() {
        // given
        val userRepoMock = mock<UserRepository> { on { findByEmail(any()) } doReturn user }
        val customUserDetailsService = CustomUserDetailsService(userRepoMock)
        // when
        val actualUser = customUserDetailsService.loadUserByUsername("")
        // then
        assertThat(actualUser).isEqualTo(user)
    }
}
