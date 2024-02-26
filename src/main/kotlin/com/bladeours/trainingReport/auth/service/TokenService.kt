package com.bladeours.trainingReport.auth.service

import com.bladeours.trainingReport.config.JwtProperties
import com.bladeours.trainingReport.service.UserService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class TokenService(private val jwtProperties: JwtProperties, private val userService: UserService) {
    private val secretKey = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generateAccessToken(userDetails: UserDetails): String =
        generateToken(
            userDetails, Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration))

    private fun generateToken(userDetails: UserDetails, expiration: Date): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expiration)
            .and()
            .signWith(secretKey)
            .compact()

    fun generateRefreshToken(userDetails: UserDetails): String =
        generateToken(
            userDetails, Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration))

    fun isValid(token: String): Boolean {
        val email = extractEmail(token)
        val username = userService.getLoggedUser().username
        return username == email && !isExpired(token)
    }

    fun extractEmail(token: String): String? = getAllClaims(token).subject

    fun isExpired(token: String): Boolean =
        getAllClaims(token).expiration.before(Date(System.currentTimeMillis()))

    private fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser().verifyWith(secretKey).build()

        return parser.parseSignedClaims(token).payload
    }
}
