package com.web.flower.security.config;

import com.web.flower.domain.refresh_token.repository.RefreshTokenRepository;
import com.web.flower.utils.JwtUtils;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.auth.JwtAuthenticationFilter;
import com.web.flower.security.auth.JwtAuthenticationProvider;
import com.web.flower.security.auth.JwtAuthorizationFilter;
import com.web.flower.security.auth.PrincipalUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalUserDetailsService principalUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));
        JwtAuthenticationFilter jwtAuthenticationFilter = jwtAuthenticationFilter(authenticationManager);
        JwtAuthorizationFilter jwtAuthorizationFilter = jwtAuthorizationFilter(authenticationManager);

        http
                .cors().disable()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);    // jwt???????????? ???????????? ??????

        http
                .headers().frameOptions().sameOrigin();

        http
                .authorizeRequests()
                .antMatchers("/api/social-login/**", "/api/login", "/api/user/one", "/api/user/logout")
                .permitAll()
                .antMatchers("/api/**")
                .access("hasRole('ROLE_USER')")
                .anyRequest().permitAll();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(jwtAuthorizationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() throws Exception {
        return new JwtAuthenticationProvider(principalUserDetailsService, passwordEncoder());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        JwtAuthenticationFilter authenticationFilter = new JwtAuthenticationFilter(authenticationManager, refreshTokenRepository, jwtUtils);
        authenticationFilter.setAuthenticationManager(authenticationManager);

        SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
        authenticationFilter.setSecurityContextRepository(contextRepository);

        return authenticationFilter;
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        JwtAuthorizationFilter authorizationFilter = new JwtAuthorizationFilter(authenticationManager, userRepository, refreshTokenRepository, jwtUtils);
        return authorizationFilter;
    }
}
