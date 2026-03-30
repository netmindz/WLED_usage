package com.github.wled.usage.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

data class GitHubRepo(
    @JsonProperty("full_name") val fullName: String,
    val permissions: GitHubRepoPermissions?
)

data class GitHubRepoPermissions(
    val admin: Boolean = false,
    val push: Boolean = false,
    val pull: Boolean = false
)

@Service
@ConditionalOnProperty(name = ["github.oauth.client-id"], matchIfMissing = false)
class GitHubUserService(
    private val authorizedClientService: OAuth2AuthorizedClientService
) {
    private val logger = LoggerFactory.getLogger(GitHubUserService::class.java)
    private val restClient = RestClient.create()

    fun getWriteAccessRepos(authentication: OAuth2AuthenticationToken): List<String> {
        val authorizedClient = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
            authentication.authorizedClientRegistrationId,
            authentication.name
        ) ?: return emptyList()

        val token = authorizedClient.accessToken.tokenValue
        val allRepos = mutableListOf<GitHubRepo>()
        var page = 1

        while (true) {
            val repos = fetchRepoPage(token, page) ?: break
            if (repos.isEmpty()) break
            allRepos.addAll(repos)
            if (repos.size < 100) break
            page++
        }

        return allRepos
            .filter { it.permissions?.push == true || it.permissions?.admin == true }
            .map { it.fullName }
    }

    private fun fetchRepoPage(token: String, page: Int): List<GitHubRepo>? {
        return try {
            restClient.get()
                .uri("https://api.github.com/user/repos?per_page=100&page={page}&type=all&sort=full_name", page)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(object : ParameterizedTypeReference<List<GitHubRepo>>() {})
        } catch (e: Exception) {
            logger.error("Failed to fetch repos from GitHub (page $page)", e)
            null
        }
    }
}
