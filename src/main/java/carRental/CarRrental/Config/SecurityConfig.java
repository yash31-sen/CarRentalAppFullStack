package carRental.CarRrental.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // for APIs (Postman), we disable CSRF for now
                .csrf(csrf -> csrf.disable())

                // allow these endpoints without login
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/verify", "/auth/login").permitAll()
                        .anyRequest().authenticated()
                )

                // keep default basic auth for now (temporary)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
