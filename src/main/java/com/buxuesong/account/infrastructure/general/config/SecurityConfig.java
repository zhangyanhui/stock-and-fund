package com.buxuesong.account.infrastructure.general.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 本地表对应的Security文件
 */
@Order(1)
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Value("${my.image.path}")
    private String myImagePath;

    // 不再注入DataSource，因为我们不再使用数据库进行身份验证

    private final static String ACCOUNT_CLIENT_AUTHORITY = "ADMIN";

    // 配置内存中的用户账号密码（不使用数据库）
    @Bean
    UserDetailsService userDetailsService() {
        PasswordEncoder encoder = passwordEncoder();
        User.UserBuilder users = User.builder()
            .passwordEncoder(encoder::encode);
        org.springframework.security.provisioning.InMemoryUserDetailsManager manager = new org.springframework.security.provisioning.InMemoryUserDetailsManager();
        manager.createUser(users.username("admin")
            .password("admin123")
            .authorities(ACCOUNT_CLIENT_AUTHORITY).build());
        return manager;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
            // 下面配置对/helloWorld1接口需要验证 ADMIN 的 authoritie
            // 和 Controller 中的 @PreAuthorize("hasAuthority('ADMIN')")注解配置效果一样
            // 这两种方式用哪一种都可以
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/login", "/chrome/**", "/login.html", "/test/**", "/api/test/**", "/fund/updateFundInfo",
                    "/fund/api/list")
                .permitAll()
                .anyRequest().hasAuthority(ACCOUNT_CLIENT_AUTHORITY))
            .formLogin((formLogin) -> formLogin.loginPage("/login.html").loginProcessingUrl("/login").defaultSuccessUrl("/main.html", true))
            .logout(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .requestCache(withDefaults())
            .headers(headers -> headers.cacheControl(withDefaults()))
            .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 增加映射静态资源
        registry.addResourceHandler("/my-image/**")
            .addResourceLocations(myImagePath);
    }
}