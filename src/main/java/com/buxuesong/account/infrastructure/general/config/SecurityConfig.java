package com.buxuesong.account.infrastructure.general.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
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

/**
 * 完全基于内存用户的Security配置文件 不依赖任何数据库
 */
@Order(1)
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final static String ACCOUNT_CLIENT_AUTHORITY = "ADMIN";

    /**
     * 配置内存中的用户账号密码（完全不使用数据库）
     */
    @Bean
    public UserDetailsService userDetailsService() {
        PasswordEncoder encoder = passwordEncoder();

        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        // 创建管理员用户
        User.UserBuilder userBuilder = User.builder()
            .passwordEncoder(encoder::encode);

        manager.createUser(userBuilder
            .username("admin")
            .password("admin123")
            .roles("ADMIN") // 使用角色方式，更简单
            .build());

        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain staticEndpoints(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/css/**", "/js/**", "/fonts/**", "/images/**", "/i/**", "/resources/**", "/my-image/**")
            .headers(headers -> headers.cacheControl(cache -> cache.disable()))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    /**
     * 配置不同接口访问权限
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/chrome/**", "/login.html", "/test/**", "/api/test/**", "/fund/updateFundInfo",
                    "/fund/api/list", "/favicon.ico")
                .permitAll()
                .anyRequest().hasRole("ADMIN"))
            .formLogin(formLogin -> formLogin
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/main.html", true)
                .permitAll())
            .logout(logout -> logout.permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .requestCache(Customizer.withDefaults())
            .headers(headers -> headers.cacheControl(Customizer.withDefaults()))
            .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源处理
        registry.addResourceHandler("/my-image/**")
            .addResourceLocations("file:./", "file:../", "file:/app/");
    }
}