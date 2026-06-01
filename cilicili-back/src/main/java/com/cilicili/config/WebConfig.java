package com.cilicili.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 跨域检查白名单
     *
     * @param registry
     *
     * @author Nananan1479
     * @date 2026/5/25 14:26

     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    /**
     * JWT鉴权拦截器白名单
     *
     * @param registry
     *
     * @author Nananan1479
     * @date 2026/5/25 14:27

     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 需要拦截的路径
                .addPathPatterns("/api/**")
                // 放行的路径（白名单）
                .excludePathPatterns(
                        "/api/users/getById/{id}",
                        "/api/users/login",
                        "/api/users/register",
                        "/api/users/avatar/**"
                );
    }
}
