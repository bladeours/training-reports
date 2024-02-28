package com.bladeours.trainingReport.auth.controller

import com.bladeours.trainingReport.auth.model.request.AuthenticationRequest
import com.bladeours.trainingReport.auth.model.request.RegisterRequest
import com.bladeours.trainingReport.auth.service.AuthenticationService
import com.bladeours.trainingReport.config.JwtProperties
import com.bladeours.trainingReport.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired lateinit var jwtProperties: JwtProperties
    @Autowired lateinit var userService: UserService
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var authService: AuthenticationService
    @Autowired lateinit var mongoTemplate: MongoTemplate

    companion object {
        private val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest"))

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }

        @JvmStatic @BeforeAll fun startDB(): Unit = mongoDBContainer.start()

        @JvmStatic @AfterAll fun stopDB(): Unit = mongoDBContainer.stop()
    }

    @AfterEach
    fun cleanDB() {
        mongoTemplate.db.drop()
    }

    @Test
    fun `should register user`() {
        val email = "test"
        val password = "test"
        mockMvc
            .post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                {
                "email":"$email",
                "password":"$password",
                "firstName":"test",
                "lastName":"test"
                }
                
                """
                        .trimIndent()
            }
            .andExpect { status { isOk() } }
        assertDoesNotThrow { authService.login(AuthenticationRequest(email, password)) }
    }

    @Test
    fun `should return cookie with refresh token when register`() {
        val email = "test"
        val password = "test"
        val cookie =
            mockMvc
                .post("/api/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                {
                "email":"$email",
                "password":"$password",
                "firstName":"test",
                "lastName":"test"
                }
                
                """
                            .trimIndent()
                }
                .andExpect {
                    status { isOk() }
                    cookie {
                        exists(jwtProperties.refreshTokenCookie)
                        httpOnly(jwtProperties.refreshTokenCookie, true)
                        secure(jwtProperties.refreshTokenCookie, true)
                    }
                }
                .andReturn()
                .response
                .cookies
                .filter { it.name == jwtProperties.refreshTokenCookie }
                .get(0)

        authService.login(AuthenticationRequest(email, password))
        assertThat(userService.getLoggedUser().refreshToken).isEqualTo(cookie.value)
    }

    @Test
    fun `should return 409 when user already exists`() {
        val email = "test"
        val password = "test"
        authService.register(RegisterRequest(email, password, "test", "test"))
        mockMvc
            .post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                {
                "email":"$email",
                "password":"$password",
                "firstName":"test",
                "lastName":"test"
                }
                
                """
                        .trimIndent()
            }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `should login user and returns JWT`() {
        val email = "test"
        val password = "test"
        authService.register(RegisterRequest(email, password, "test", "test"))
        mockMvc
            .post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                {
                "email":"$email",
                "password":"$password"
                }
                
                """
                        .trimIndent()
            }
            .andExpect {
                status { isOk() }
                jsonPath("$.token") { exists() }
            }
    }

    @Test
    fun `should return 403 when wrong credentials`() {
        val email = "test"
        val password = "test"
        authService.register(RegisterRequest(email, password, "test", "test"))
        mockMvc
            .post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                {
                "email":"$email",
                "password":"wrong-password"
                }
                
                """
                        .trimIndent()
            }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `should return cookie with refresh token when logging`() {
        val email = "test"
        val password = "test"
        authService.register(RegisterRequest(email, password, "test", "test"))
        val cookie =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                {
                "email":"$email",
                "password":"$password"
                }
                
                """
                            .trimIndent()
                }
                .andExpect {
                    status { isOk() }
                    cookie {
                        exists(jwtProperties.refreshTokenCookie)
                        httpOnly(jwtProperties.refreshTokenCookie, true)
                        secure(jwtProperties.refreshTokenCookie, true)
                    }
                }
                .andReturn()
                .response
                .cookies
                .filter { it.name == jwtProperties.refreshTokenCookie }
                .get(0)

        authService.login(AuthenticationRequest(email, password))
        assertThat(userService.getLoggedUser().refreshToken).isEqualTo(cookie.value)
    }
}
