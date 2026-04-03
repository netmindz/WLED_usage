package com.github.wled.usage.controller

import com.github.wled.usage.service.GitHubUserService
import com.github.wled.usage.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val gitHubUserService: GitHubUserService?, private val statsService: StatsService) {

    @GetMapping("/user")
    fun getCurrentUser(authentication: OAuth2AuthenticationToken?): ResponseEntity<Map<String, Any>> {
        if (authentication == null) {
            return ResponseEntity.ok(mapOf("authenticated" to false))
        }
        val user: OAuth2User = authentication.principal
        return ResponseEntity.ok(mapOf(
            "authenticated" to true,
            "login" to (user.getAttribute<String>("login") ?: ""),
            "avatar_url" to (user.getAttribute<String>("avatar_url") ?: "")
        ))
    }

    @GetMapping("/repos")
    fun getUserRepos(authentication: OAuth2AuthenticationToken?): ResponseEntity<List<String>> {
        if (authentication == null || gitHubUserService == null) {
            return ResponseEntity.status(401).body(emptyList())
        }
        val writeAccessRepos = gitHubUserService.getWriteAccessRepos(authentication)
        val knownRepos = statsService.getKnownRepos().toSet()
        val repos = writeAccessRepos.filter { it in knownRepos }
        return ResponseEntity.ok(repos)
    }
}
