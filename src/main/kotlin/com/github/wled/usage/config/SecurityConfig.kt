package com.github.wled.usage.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${github.oauth.client-id:}") private val githubClientId: String,
    @Value("\${github.oauth.client-secret:}") private val githubClientSecret: String
) {

    private fun oauthEnabled() = githubClientId.isNotBlank() && githubClientSecret.isNotBlank()

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository::class)
    fun clientRegistrationRepository(): ClientRegistrationRepository? {
        if (!oauthEnabled()) return null
        val registration = ClientRegistration.withRegistrationId("github")
            .clientId(githubClientId)
            .clientSecret(githubClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("read:user", "public_repo")
            .authorizationUri("https://github.com/login/oauth/authorize")
            .tokenUri("https://github.com/login/oauth/access_token")
            .userInfoUri("https://api.github.com/user")
            .userNameAttributeName("login")
            .clientName("GitHub")
            .build()
        return InMemoryClientRegistrationRepository(registration)
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2AuthorizedClientService::class)
    fun authorizedClientService(
        clientRegistrationRepository: ClientRegistrationRepository?
    ): OAuth2AuthorizedClientService? {
        if (!oauthEnabled() || clientRegistrationRepository == null) return null
        return InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/repos").authenticated()
                    .anyRequest().permitAll()
            }
            .csrf { csrf -> csrf.disable() }

        if (oauthEnabled()) {
            http
                .oauth2Login { oauth ->
                    oauth.defaultSuccessUrl("/", true)
                }
                .logout { logout ->
                    logout.logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/")
                }
        }

        return http.build()
    }
}
