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
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/", "/home").authenticated()

                        // VIEW ACCESS
                        .requestMatchers("/members/**").hasAnyRole("HEAD", "SUBHEAD", "MEMBER")

                        .requestMatchers("/years/*/budget/**").hasAnyRole("HEAD", "SUBHEAD", "TREASURER", "MEMBER")
                        .requestMatchers("/years/*/donations/**").hasAnyRole("HEAD", "SUBHEAD", "TREASURER", "DONOR", "SPONSOR", "MEMBER")
                        .requestMatchers("/years/*/projects/**").hasAnyRole("HEAD", "SUBHEAD", "PROJECT_MANAGER", "VOLUNTEER", "MEMBER")
                        .requestMatchers("/years/*/events/**").hasAnyRole("HEAD", "SUBHEAD", "EVENT_MANAGER", "VOLUNTEER", "MEMBER")

                        .requestMatchers("/years/**").hasAnyRole("HEAD", "SUBHEAD", "MEMBER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }
}