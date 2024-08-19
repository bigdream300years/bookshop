package com.shujia.one.config;

import com.shujia.one.interceptor.AuthorizeInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/*
网络配置
负责进行跨域配置操作
和对后端一些接口的拦截，只有通过验证才能访问后端接口
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //加入手写的拦截器,先通过拦截器才能访问其它网页
    @Resource
    AuthorizeInterceptor interceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)//注入拦截器类
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**");
    }
    /**
     * 跨域配置
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .maxAge(3600)
                .allowCredentials(true);
    }
}
