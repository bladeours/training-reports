package com.bladeours.trainingReport.service

import com.bladeours.trainingReport.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    private val jwtProperties: JwtProperties
) {
    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.key.toByteArray()
    )

    fun generateAccessToken(
        userDetails: UserDetails,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String = generateToken(userDetails, Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration))

    private fun generateToken(
        userDetails: UserDetails,
        expiration: Date
    ): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expiration)
            .and()
            .signWith(secretKey)
            .compact()

    fun generateRefreshToken(
        userDetails: UserDetails
    ): String = generateToken(userDetails, Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration))

    fun isValid(token: String, userDetails: UserDetails): Boolean {
        val email = extractEmail(token)
        return userDetails.username == email && !isExpired(token)
    }

    fun extractEmail(token: String): String? =
        getAllClaims(token).subject

    fun isExpired(token: String): Boolean =
        getAllClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))

    private fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser().verifyWith(secretKey).build()

        return parser.parseSignedClaims(token).payload
    }
}