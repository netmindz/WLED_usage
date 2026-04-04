package com.github.wled.usage.controller

import com.github.wled.usage.service.GitHubUserService
import com.github.wled.usage.service.StatsService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class, excludeAutoConfiguration = [
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration::class
])
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var gitHubUserService: GitHubUserService

    @MockBean
    private lateinit var statsService: StatsService

    @Test
    fun `getCurrentUser should return authenticated false when not logged in`() {
        mockMvc.perform(
            get("/api/auth/user")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.authenticated").value(false))
    }

    @Test
    fun `getUserRepos should return only repos the user has access to AND that have device data`() {
        val mockAuth = createMockAuth()

        whenever(statsService.getKnownRepos())
            .thenReturn(listOf("owner/repo1", "owner/repo2"))
        whenever(gitHubUserService.getWriteAccessRepos(any()))
            .thenReturn(listOf("owner/repo1", "owner/repo2", "owner/repo3"))

        mockMvc.perform(
            get("/api/auth/repos")
                .principal(mockAuth)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0]").value("owner/repo1"))
            .andExpect(jsonPath("$[1]").value("owner/repo2"))
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `getUserRepos should return empty list when user has write access to no known repos`() {
        val mockAuth = createMockAuth()

        whenever(statsService.getKnownRepos())
            .thenReturn(listOf("owner/repo1", "owner/repo2"))
        whenever(gitHubUserService.getWriteAccessRepos(any()))
            .thenReturn(listOf("owner/unknown-repo"))

        mockMvc.perform(
            get("/api/auth/repos")
                .principal(mockAuth)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `getUserRepos should match repos case-insensitively`() {
        val mockAuth = createMockAuth()

        // DB stores "wled/wled", GitHub returns "wled/WLED"
        whenever(statsService.getKnownRepos())
            .thenReturn(listOf("netmindz/WLED-MM", "MoonModules/WLED-MM", "wled/wled"))
        whenever(gitHubUserService.getWriteAccessRepos(any()))
            .thenReturn(listOf("netmindz/WLED-MM", "MoonModules/WLED-MM", "wled/WLED"))

        mockMvc.perform(
            get("/api/auth/repos")
                .principal(mockAuth)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
    }

    private fun createMockAuth(): OAuth2AuthenticationToken {
        val attributes = mapOf(
            "login" to "testuser",
            "avatar_url" to "https://example.com/avatar.png",
            "id" to 12345
        )
        val authority = OAuth2UserAuthority(attributes)
        val user = DefaultOAuth2User(listOf(authority), attributes, "login")
        return OAuth2AuthenticationToken(user, listOf(authority), "github")
    }
}
