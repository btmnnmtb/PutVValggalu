package com.example.demo.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfigSecrurity {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserDetailsService userDetailsService) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // публичные
                        .requestMatchers("/css/**","/js/**","/images/**","/webjars/**","/assets/**").permitAll()
                        .requestMatchers("/registration","/login").permitAll()

                        // админка
                        .requestMatchers("/admin/**", "/AdminPage").hasAuthority("Администратор")

                        // пользовательские разделы (и админ может сюда)
                        .requestMatchers("/MainWindow", "/Catalog", "/Cart",
                                "/favourites", "/favourites/**",
                                "/cart/**",
                                "/orders", "/orders/**")
                        .hasAnyAuthority("Пользователь","Администратор")

                        // явные POST-операции пользователя
                        .requestMatchers(HttpMethod.POST, "/cart/items/**", "/Delete/**")
                        .hasAnyAuthority("Пользователь","Администратор")

                        // комментарии: только POST
                        .requestMatchers(HttpMethod.POST, "/comments")
                        .hasAnyAuthority("Пользователь","Администратор")
                        .requestMatchers(HttpMethod.POST, "/comments/{id}/delete")
                        .hasAnyAuthority("Пользователь","Администратор")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            var roles = authentication.getAuthorities().stream()
                                    .map(a -> a.getAuthority()).toList();
                            if (roles.contains("Администратор")) {
                                response.sendRedirect("/AdminPage");
                            } else {
                                response.sendRedirect("/MainWindow");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}


