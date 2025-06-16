package com.diplom.diplom.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class ConfSpringSecurity {
    private final UserDetailsService userDetailsService;

    @Autowired
    public ConfSpringSecurity(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpsec) throws Exception {
        HttpSessionCsrfTokenRepository csrfRepo = new HttpSessionCsrfTokenRepository();
        csrfRepo.setParameterName("csrf");
        httpsec
                .headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepo))
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registrationpage/**","/login","/pwrestorepage/**","/js/**","/css/**","/files/logo.png","/files/favicon.png").permitAll()
                        .requestMatchers("/conference/{id}/update","/conference/create","/tasks/create","/task/{id}/update").hasAnyRole("ADMIN","TEACHER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/**").hasAnyRole("STUDENT","TEACHER","ADMIN")
                       // .requestMatchers("/**").permitAll()
                ).formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/loginpage")
                                .permitAll()
                                .defaultSuccessUrl("/",true)
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                ).
                logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/loginpage")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .permitAll()
                );
        return httpsec.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16,32,2,65536,3);
       /* String defaultEncodingId = "argon2";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("argon2", new Argon2PasswordEncoder(16,32,2,65536,3));
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder(defaultEncodingId, encoders);*/
    }
}
