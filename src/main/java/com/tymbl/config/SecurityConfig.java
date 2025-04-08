package com.tymbl.config;

import com.tymbl.auth.filter.JwtAuthenticationFilter;
import com.tymbl.auth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserDetailsService userDetailsService;
  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .cors().and()
        .authorizeRequests()
        // Allow pre-flight OPTIONS requests
        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        // Public endpoints
        .antMatchers("/api/v1/auth/**", "/tymbl-service/api/v1/auth/**").permitAll()
        .antMatchers("/api/v1/registration", "/tymbl-service/api/v1/registration").permitAll()
        .antMatchers("/api/v1/registration/**", "/tymbl-service/api/v1/registration/**").permitAll()
        .antMatchers("/api/v1/locations/**", "/tymbl-service/api/v1/locations/**").permitAll()
        .antMatchers("/api/v1/dropdowns/**", "/tymbl-service/api/v1/dropdowns/**").permitAll()
        .antMatchers("/api/v1/skills/**", "/tymbl-service/api/v1/skills/**").permitAll()
        .antMatchers("/api/v1/oauth2/**", "/tymbl-service/api/v1/oauth2/**").permitAll()
        .antMatchers("/api/v1/health/**", "/tymbl-service/api/v1/health/**").permitAll()
        .antMatchers("/v3/api-docs/**", "/tymbl-service/v3/api-docs/**").permitAll()
        .antMatchers("/swagger-ui/**", "/tymbl-service/swagger-ui/**").permitAll()
        .antMatchers("/swagger-ui.html", "/tymbl-service/swagger-ui.html").permitAll()
        .antMatchers("/api-docs/**", "/tymbl-service/api-docs/**").permitAll()

        // Protected endpoints
        .antMatchers("/tymbl-service/api/v1/users/**").authenticated()
        .antMatchers("/tymbl-service/api/v1/jobs/**").authenticated()
        .antMatchers("/tymbl-service/api/v1/locations/**").authenticated()
        .anyRequest().authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  @SuppressWarnings("deprecation")
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
