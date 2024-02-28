package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.auth.service.repository.UserRepository
import com.bladeours.trainingReport.exception.AppException
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByEmail(username).orElseThrow {
            throw AppException("username not Found!", HttpStatus.NOT_FOUND)
        }
}
