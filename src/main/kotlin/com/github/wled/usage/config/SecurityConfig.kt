package com.github.wled.usage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(private val environment: Environment) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/repos").authenticated()
                    .anyRequest().permitAll()
            }
            .csrf { csrf -> csrf.disable() }

        val clientId = environment.getProperty("spring.security.oauth2.client.registration.github.client-id")
        if (!clientId.isNullOrBlank()) {
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
