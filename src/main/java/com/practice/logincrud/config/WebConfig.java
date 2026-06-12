package com.practice.logincrud.config;

import com.practice.logincrud.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/join",
                        "/signup",
                        "/admin",
                        "/admin/join",
                        "/admin/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/api/user-info"
                );
    }
}
