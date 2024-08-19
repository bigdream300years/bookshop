package com.shujia.one.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//MybatisPlus分页插件配置
@Configuration
public class MybatisPlusConfig {
    @Bean
    /*
    在这个配置类中，定义了一个名为mybatisPlusInterceptor的方法，该方法返回一个MybatisPlusInterceptor对象。
    在这个方法中，首先创建了一个MybatisPlusInterceptor实例，然后添加了一个PaginationInnerInterceptor内部拦截器，
    用于处理MySQL数据库的分页查询。最后，将这个拦截器返回。
     */
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}

