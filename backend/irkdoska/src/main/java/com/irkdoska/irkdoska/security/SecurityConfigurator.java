package com.irkdoska.irkdoska.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurator {

    public SecurityConfigurator() {}

    private TmaFilter tmaFilter; 

    @Autowired
    public void setTmaFilter(TmaFilter tmaFilter) {
        this.tmaFilter = tmaFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(httpSecurityCorsConfigurer -> 
                httpSecurityCorsConfigurer.configurationSource(request -> 
                    new CorsConfiguration().applyPermitDefaultValues())
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/photos").permitAll()
                .requestMatchers("/api/moderation/bot/**").permitAll()
                .requestMatchers("/api/shop/packages").permitAll()
                .requestMatchers("/api/payments/success").permitAll()
                .requestMatchers("/api/**").fullyAuthenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(tmaFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
