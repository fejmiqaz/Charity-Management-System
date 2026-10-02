package emd.charitymanagementsystem.Config;

import emd.charitymanagementsystem.Security.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final AccessDeniedConfig accessDeniedConfig;
    private final emd.charitymanagementsystem.Security.GoogleAccountService googleAccounts;
    private final org.springframework.core.env.Environment environment;
    private final emd.charitymanagementsystem.Repository.UserAccountRepository accounts;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(customUserDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.addFilterAfter(new emd.charitymanagementsystem.Security.AccountPermissionsFilter(accounts),
                org.springframework.security.web.context.SecurityContextHolderFilter.class);
        if (environment.getProperty("app.auth.google-enabled", Boolean.class, false)) {
            var delegate = new org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService();
            String frontend = environment.getProperty("app.auth.frontend-url", "").replaceAll("/+$", "");
            http.oauth2Login(oauth -> oauth.loginPage("/login")
                    .userInfoEndpoint(info -> info.oidcUserService(request -> googleAccounts.authenticate(delegate.loadUser(request))))
                    .successHandler((request, response, auth) -> {
                        request.getSession().setAttribute("accountAuthenticated", true);
                        var account = accounts.findByEmailIgnoreCase(auth.getName()).orElseThrow();
                        response.sendRedirect(frontend + ("!".equals(account.getPassword()) ? "/profile" : "/dashboard"));
                    })
                    .failureHandler((request, response, ex) -> response.sendRedirect(frontend +
                            (ex instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthError
                                    && "registration_full".equals(oauthError.getError().getErrorCode())
                                    ? "/login?registrationFull" : "/login?googleError"))));
        }
        http
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/").permitAll()
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/access-denied",
                                "/index.html",
                                "/assets/**",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        .requestMatchers("/dashboard", "/home").authenticated()

                        .requestMatchers("/members/**")
                        .hasAnyRole("HEAD", "SUBHEAD", "TREASURER", "MEMBER")

                        .requestMatchers("/years/*/budget/**")
                        .hasAnyRole("HEAD", "SUBHEAD", "TREASURER", "MEMBER")

                        .requestMatchers("/years/*/donations/**")
                        .hasAnyRole(
                                "HEAD",
                                "SUBHEAD",
                                "TREASURER",
                                "DONOR",
                                "SPONSOR",
                                "MEMBER"
                        )

                        .requestMatchers("/years/*/projects/**")
                        .hasAnyRole(
                                "HEAD",
                                "SUBHEAD",
                                "PROJECT_MANAGER",
                                "VOLUNTEER",
                                "MEMBER"
                        )

                        .requestMatchers("/years/*/events/**")
                        .hasAnyRole(
                                "HEAD",
                                "SUBHEAD",
                                "EVENT_MANAGER",
                                "VOLUNTEER",
                                "MEMBER"
                        )

                        .requestMatchers("/years/**")
                        .hasAnyRole("HEAD", "SUBHEAD", "MEMBER")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, auth) -> {
                            request.getSession().setAttribute("accountAuthenticated", true);
                            response.sendRedirect("/dashboard");
                        })
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, exceptionThrown) ->
                                response.sendRedirect("/access-denied")
                        )
                );

        return http.build();
    }
}
