package com.buxuesong.account.infrastructure.general.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 本地表对应的Security文件
 */
@Order(1)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    DataSource dataSource;

    @Autowired
    private SimpleTokenFilter simpleTokenFilter;

    public SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final static String ACCOUNT_CLIENT_AUTHORITY = "ADMIN";

    // 配置内存用户存储
    @Bean
    UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager users = new InMemoryUserDetailsManager();
        users.createUser(org.springframework.security.core.userdetails.User.withUsername("admin")
            .password("{noop}admin123") // {noop}表示不加密，直接使用明文密码
            .authorities(ACCOUNT_CLIENT_AUTHORITY).build());
        return users;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Order(0)
    SecurityFilterChain staticEndpoints(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/css/**", "/js/**", "/fonts/**", "/images/**", "/i/**", "/resources/**", "/my-image/**")
            .headers((headers) -> headers.cacheControl((cache) -> cache.disable()))
            .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll());
        return http.build();
    }

    // 配置不同接口访问权限
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // 添加Token过滤器
            .addFilterBefore(simpleTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置访问权限
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/login", "/login.html")
                .permitAll()
                .anyRequest().hasAuthority(ACCOUNT_CLIENT_AUTHORITY))
            // 配置表单登录
            .formLogin((formLogin) -> formLogin.loginPage("/login.html").loginProcessingUrl("/login")
                .defaultSuccessUrl("/stockAndFund", true))
            .logout(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

}