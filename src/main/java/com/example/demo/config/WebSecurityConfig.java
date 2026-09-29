package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.constant.UrlConst;
import com.example.demo.security.CustomAuthenticationSuccessHandler; 

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private final String USERNAME_PARAMETER = "loginId";

    private final CustomAuthenticationSuccessHandler successHandler;

    // ✅ コンストラクタインジェクション
    public WebSecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                		"/public/**",  "/publicUsers/**","/profile-images/**"
                ).permitAll()//公開サイト
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers(UrlConst.NO_AUTHENTICATION).permitAll()
                .requestMatchers("/users/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage(UrlConst.LOGIN)
                .usernameParameter(USERNAME_PARAMETER)
                .successHandler(successHandler) // ✅ handler使用に変更
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(UrlConst.LOGIN)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
