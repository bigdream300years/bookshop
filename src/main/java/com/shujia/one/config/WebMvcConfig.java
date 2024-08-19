package com.shujia.one.config;

import com.shujia.one.interceptor.AuthorizeInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
/*
网络配置
负责进行跨域配置操作
和对后端一些接口的拦截，只有通过验证才能访问后端接口
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 图片保存路径，自动从yml文件中获取数据
     *   示例： E:/images/
     */
    @Value("${file-save-path}")
    private String fileSavePath;
    /**
     自定义拦截器,主要拦截靠spring security框架进行
     */
    @Resource
    AuthorizeInterceptor interceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {    registry.addInterceptor(interceptor)//注入拦截器类
            .addPathPatterns("/**")//全部拦截，不在白名单里路径进入拦截器自定义方法拦截
            .excludePathPatterns("/api/auth/**");//不需被拦截白名单

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
    /**
     * 配置资源映射
     * 意思是：如果访问的资源路径是以“/images/”开头的，
     * 就给我映射到本机的“E:/images/”这个文件夹内，去找你要的资源
     * 注意：E:/images/ 后面的 “/”一定要带上
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:"+fileSavePath);
    }

}
