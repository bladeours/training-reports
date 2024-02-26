package com.bladeours.trainingReport.service

import com.bladeours.trainingReport.auth.service.repository.UserRepository
import com.bladeours.trainingReport.exception.AppException
import com.bladeours.trainingReport.model.User
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun getLoggedUser(): User {
        val auth =
            SecurityContextHolder.getContext().authentication
                ?: throw AppException("can not find user", HttpStatus.NOT_FOUND)
        return userRepository.findByEmail(auth.name)
            ?: throw AppException("can not find user", HttpStatus.NOT_FOUND)
    }

    fun updateRefreshToken(refreshToken: String) {
        val user = getLoggedUser()
        user.refreshToken = refreshToken
        userRepository.save(user)
    }
}
