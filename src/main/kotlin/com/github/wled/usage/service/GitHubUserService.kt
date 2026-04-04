package com.github.wled.usage.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

data class GitHubRepo(
    @JsonProperty("full_name") val fullName: String,
    val permissions: GitHubRepoPermissions?
)

data class GitHubRepoPermissions(
    val admin: Boolean = false,
    val maintain: Boolean = false,
    val push: Boolean = false,
    val pull: Boolean = false
)

data class GitHubCollaboratorPermission(
    val permission: String = ""
)

@Service
@ConditionalOnExpression("'\${github.oauth.client-id:}'.length() > 0")
class GitHubUserService(
    private val authorizedClientService: OAuth2AuthorizedClientService
) {
    private val logger = LoggerFactory.getLogger(GitHubUserService::class.java)
    private val restClient = RestClient.create()

    fun getWriteAccessRepos(authentication: OAuth2AuthenticationToken): List<String> =
        getWriteAccessRepos(authentication, emptyList())

    fun getWriteAccessRepos(authentication: OAuth2AuthenticationToken, knownRepos: List<String>): List<String> {
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

        val foundByBulk = allRepos
            .filter { hasWritePermissions(it.permissions) }
            .map { it.fullName }
            .toSet()

        // For known repos not returned by the bulk list (e.g. outside collaborators on org repos),
        // check each one directly via the single-repo API endpoint.
        val username = authentication.name
        val foundByBulkLower = foundByBulk.map { it.lowercase() }.toSet()
        val extraRepos = knownRepos
            .filter { it.lowercase() !in foundByBulkLower }
            .filter { hasWriteAccess(token, it, username) }

        return (foundByBulk + extraRepos).toList()
    }

    private fun hasWritePermissions(permissions: GitHubRepoPermissions?): Boolean {
        if (permissions == null) return false
        return permissions.admin || permissions.maintain || permissions.push
    }

    internal fun hasWriteAccess(token: String, fullName: String, username: String): Boolean {
        val parts = fullName.split("/")
        if (parts.size != 2) return false
        val (owner, repo) = parts
        return try {
            // First check the repo's permissions block
            val repoResult = restClient.get()
                .uri("https://api.github.com/repos/{owner}/{repo}", owner, repo)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(GitHubRepo::class.java)
            if (hasWritePermissions(repoResult?.permissions)) return true

            // Fall back to the collaborator permission endpoint — this correctly reflects
            // org admin access even when the permissions block omits it due to missing read:org scope
            val collab = restClient.get()
                .uri("https://api.github.com/repos/{owner}/{repo}/collaborators/{username}/permission", owner, repo, username)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(GitHubCollaboratorPermission::class.java)
            collab?.permission in setOf("admin", "maintain", "write")
        } catch (e: Exception) {
            logger.warn("Failed to check repo access for $fullName", e)
            false
        }
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
