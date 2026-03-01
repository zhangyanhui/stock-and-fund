package com.buxuesong.account.infrastructure.general.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${my.image.path}")
    private String myImagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 增加映射静态资源
        registry.addResourceHandler("/my-image/**")
            .addResourceLocations(myImagePath);
    }
}
