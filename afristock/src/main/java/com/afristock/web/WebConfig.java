package com.afristock.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SpaNavInterceptor spaNavInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spaNavInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/css/**", "/js/**", "/images/**", "/public/**", "/logout");
    }
}
