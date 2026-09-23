package emd.charitymanagementsystem.Config;

import emd.charitymanagementsystem.Api.ApiError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.authentication.session.*;
import org.springframework.web.cors.*;
import java.util.*;

@Configuration
public class ApiSecurityConfig {
    @Bean public HttpSessionCsrfTokenRepository apiCsrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }
    @Bean public HttpSessionSecurityContextRepository apiSecurityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    @Bean public AuthenticationManager apiAuthenticationManager(DaoAuthenticationProvider provider) { return new ProviderManager(provider); }
    @Bean public SessionAuthenticationStrategy apiSessionStrategy(HttpSessionCsrfTokenRepository csrf) {
        return new CompositeSessionAuthenticationStrategy(List.of(new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrf)));
    }

    @Bean @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, HttpSessionCsrfTokenRepository csrf,
            HttpSessionSecurityContextRepository contexts, @Value("${app.api.allowed-origins:}") String origins) throws Exception {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        cors.setAllowCredentials(true); cors.validateAllowCredentials();
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN", "Accept"));
        cors.setExposedHeaders(List.of("Location"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        http.securityMatcher("/api/**")
                .cors(c -> c.configurationSource(source))
                .csrf(c -> c.csrfTokenRepository(csrf))
                .securityContext(c -> c.securityContextRepository(contexts))
                .requestCache(c -> c.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/public/home").permitAll()
                        .requestMatchers("/api/auth/csrf", "/api/auth/login", "/api/auth/register").permitAll()
                        // Match the existing web URL restrictions as well as controller method permissions.
                        .requestMatchers("/api/members/**", "/api/years/*/budget/**").hasAnyRole("HEAD", "SUBHEAD", "TREASURER", "MEMBER")
                        .requestMatchers("/api/years/*/donations/**").hasAnyRole("HEAD", "SUBHEAD", "TREASURER", "DONOR", "SPONSOR", "MEMBER")
                        .requestMatchers("/api/years/*/projects/**").hasAnyRole("HEAD", "SUBHEAD", "PROJECT_MANAGER", "VOLUNTEER", "MEMBER")
                        .requestMatchers("/api/years/*/events/**").hasAnyRole("HEAD", "SUBHEAD", "EVENT_MANAGER", "VOLUNTEER", "MEMBER")
                        .requestMatchers("/api/years/**").hasAnyRole("HEAD", "SUBHEAD", "MEMBER")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> ApiError.write(res, 401))
                        .accessDeniedHandler((req, res, ex) -> ApiError.write(res, 403)))
                .logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((req, res, auth) -> res.setStatus(204)));
        return http.build();
    }
}

