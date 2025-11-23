package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfigSecrurity {

    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req,res,e)->res.sendError(401))
                        .accessDeniedHandler((req,res,e)->res.sendError(403))
                );
        return http.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http,
                                        UserDetailsService userDetailsService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/images/**","/webjars/**","/assets/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/login", "/registration" , "/ForgotPassword", "/forgotSubmit").permitAll()
                        .requestMatchers("/", "/MainWindow", "/Catalog").permitAll()
                        .requestMatchers(HttpMethod.GET, "/orders").hasAnyAuthority("Администратор","Пользователь")

                        .requestMatchers("/admin/**", "/AdminPage").hasAuthority("Администратор")
                        .requestMatchers("/manager/**", "/OrderManager", "/ManagerPage").hasAuthority("Менаджер")
                        .requestMatchers("/manager/reports/**").hasAuthority("Менеджер")
                        .requestMatchers("/Set/**" , "/SetPage").hasAuthority("Сотрудник склада")

                        .requestMatchers("/favourites/**", "/cart/**", "/orders/**").hasAuthority("Пользователь")
                        .requestMatchers(HttpMethod.POST, "/cart/items/**", "/Delete/**").hasAuthority("Пользователь")
                        .requestMatchers(HttpMethod.POST, "/comments").hasAuthority("Пользователь")
                        .requestMatchers(HttpMethod.POST, "/comments/*/delete").hasAnyAuthority("Пользователь", "Администратор")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            var roles = authentication.getAuthorities().stream()
                                    .map(a -> a.getAuthority()).toList();
                            if (roles.contains("Администратор"))       response.sendRedirect("/AdminPage");
                            else if (roles.contains("Менаджер"))        response.sendRedirect("/manager/ManagerPage");
                            else if (roles.contains("Сотрудник склада"))response.sendRedirect("/Set/approvals");
                            else                                        response.sendRedirect("/MainWindow");
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
