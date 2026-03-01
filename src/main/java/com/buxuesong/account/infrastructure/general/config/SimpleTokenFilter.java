package com.buxuesong.account.infrastructure.general.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 简单的Token验证过滤器 用于验证请求头中的Token，无需登录页面
 */
@Component
public class SimpleTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SimpleTokenFilter.class);

    // 生成的复杂Token，建议定期更换
    private static final String SECRET_TOKEN = "Bearer stock-fund-app-secret-token-2026-03-01-xyz789";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        logger.debug("Token过滤器开始处理请求: {}", request.getRequestURI());

        // 从请求头获取Authorization
        String authorizationHeader = request.getHeader("Authorization");
        logger.debug("请求头中的Authorization: {}", authorizationHeader);
        logger.debug("预期的Token: {}", SECRET_TOKEN);

        // 检查Token是否正确
        if (authorizationHeader != null && authorizationHeader.equals(SECRET_TOKEN)) {
            logger.debug("Token验证成功");
            // 创建认证对象
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin", null, authorities);

            // 设置认证信息到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("认证信息已设置到安全上下文");
        } else {
            logger.debug("Token验证失败，Authorization: {}", authorizationHeader);
        }

        filterChain.doFilter(request, response);
    }
}
