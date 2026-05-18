package com.zsn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// JWT 鉴权拦截器
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 需要拦截的路径
                .addPathPatterns("/api/**")
                // 放行的路径（白名单）
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users/register"
                );
    }
}
