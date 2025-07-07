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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors().and()
        .csrf().disable()
        .authorizeRequests()
        // Allow pre-flight OPTIONS requests
        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        // Public endpoints (no JWT required)
        .antMatchers("/api/v1/auth/**", "/tymbl-service/api/v1/auth/**").permitAll()
        .antMatchers("/api/v1/registration", "/tymbl-service/api/v1/registration").permitAll()
        .antMatchers("/api/v1/registration/**", "/tymbl-service/api/v1/registration/**").permitAll()
        .antMatchers("/api/v1/locations/**", "/tymbl-service/api/v1/locations/**").permitAll()
        .antMatchers("/api/v1/crawler/**", "/tymbl-service/api/v1/crawler/**").permitAll()
        .antMatchers("/api/v1/dropdowns/**", "/tymbl-service/api/v1/dropdowns/**").permitAll()
        .antMatchers("/api/v1/skills/**", "/tymbl-service/api/v1/skills/**").permitAll()
        .antMatchers("/api/v1/companies/**", "/tymbl-service/api/v1/companies/**").permitAll()
        .antMatchers("/api/v1/oauth2/**", "/tymbl-service/api/v1/oauth2/**").permitAll()
        .antMatchers("/api/v1/health/**", "/tymbl-service/api/v1/health/**").permitAll()
        .antMatchers("/v3/api-docs/**", "/tymbl-service/v3/api-docs/**").permitAll()
        .antMatchers("/swagger-ui/**", "/tymbl-service/swagger-ui/**").permitAll()
        .antMatchers("/swagger-ui.html", "/tymbl-service/swagger-ui.html").permitAll()
        .antMatchers("/api/v1/jobsearch/**", "/tymbl-service/api/v1/jobsearch/**").permitAll()
        .antMatchers("/api-docs/**", "/tymbl-service/api-docs/**").permitAll()

        // Protected endpoints (JWT required)
        .antMatchers("/api/v1/jobmanagement/**").authenticated()
        .antMatchers("/api/v1/users/**").authenticated()
        .antMatchers("/api/v1/resumes/**").authenticated()
        .antMatchers("/api/v1/job-applications/**").authenticated()
        .anyRequest().authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(false);
    configuration.setMaxAge(3600L);

    // Specific configuration for /api/v1/users/profile
    CorsConfiguration profileConfig = new CorsConfiguration();

    profileConfig.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",        // ✅ Local development
        "https://www.tymblhub.com",     // ✅ Production
        "https://tymblhub.com"          // ✅ Production alternative
    ));
    profileConfig.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    profileConfig.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
    profileConfig.setExposedHeaders(Arrays.asList("Authorization"));
    profileConfig.setAllowCredentials(true);
    profileConfig.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/v1/users/**", profileConfig);
    source.registerCorsConfiguration("/api/v1/resumes/**", profileConfig);
    source.registerCorsConfiguration("/api/v1/jobmanagement/**", profileConfig);
    source.registerCorsConfiguration("/**", configuration);
    return source;
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
