package com.github.k7.coursein.security;

import com.github.k7.coursein.enums.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;

    private final UserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final AccessDeniedHandler accessDeniedHandler;

    private static final String[] AUTH_WHITE_LIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/v2/api-docs/**",
        "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(AUTH_WHITE_LIST).permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/users/{username}/orders/**").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/users/resend-otp").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/users/verify-otp").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/users/count/**").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.GET, "/api/v1/courses/count/**").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.GET, "/api/v1/courses/count/premium/**").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.GET, "/api/v1/courses").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/courses/{courseCode}").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/users/{username}").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.PATCH, "/api/v1/users/{username}").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.PUT, "/api/v1/users/update-password/{username}").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.POST, "/api/v1/users/upload/profile-picture/{username}").permitAll()
            .antMatchers(HttpMethod.DELETE, "/api/v1/users/delete/{username}").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.POST, "/api/v1/courses").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.PATCH, "/api/v1/courses/{courseCode}").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.DELETE, "/api/v1/courses/delete/{courseCode}").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.GET, "/api/v1/users/{username}/**").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.PATCH, "/api/v1/users/{username}/**").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.PUT, "/api/v1/users/update-password/{username}/**").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.DELETE, "/api/v1/users/delete/{username}/**").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.POST, "/api/v1/courses/**").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.PATCH, "/api/v1/courses/{courseCode}/**").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.DELETE, "/api/v1/courses/delete/{courseCode}/**").hasRole(UserRole.ADMIN.name())
            .antMatchers(HttpMethod.POST, "/api/v1/orders/**").hasRole(UserRole.USER.name())
            .antMatchers(HttpMethod.GET, "/api/v1/orders/dashboard/**").hasRole(UserRole.ADMIN.name())
            .antMatchers("/api/v1/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

}
