package com.shujia.one.config;

import com.alibaba.fastjson.JSONObject;
import com.shujia.one.entity.RestBean;
import com.shujia.one.service.AuthorizeService;
import com.shujia.one.service.impl.AuthorizeServiceImp;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.io.IOException;
/*
spring security配置类
功能负责处理登录登出等判断操作，记住我的实现
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Resource
    AuthorizeService authorizeService;//通过名字自动注入service层AuthorizeService类到bean中
    //security登录验证
    @Resource
    DataSource dataSource;//注入数据源类到bean中
    @Bean
    /*  1、SecurityFilterChain负责将请求通过一系列的过滤器进行处理，以确保Web应用的安全性
        2、PersistentTokenRepository用于持久化token实现记住我功能类，由自己手动实现
        3、HttpSecurity主要用于构建和配置HTTP安全相关的设置，提供了一套链式API，用于配置应用中的URL权限、身份验证、会话管理和CSRF防护等安全措施。
     */
    public SecurityFilterChain filterChain(HttpSecurity http,PersistentTokenRepository repository) throws Exception {
        return http
                .authorizeHttpRequests( authorize -> authorize
                        .requestMatchers("api/auth/**")
                        .permitAll()//对于api/auth/开头请求都放行，随后进登录与登出判断
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/api/auth/login")
                        .permitAll()//对于登录请求通过手写登录成功与失败方法返回对应值
                        .successHandler(this::onAuthenticationSuccess)//成功提交
                        .failureHandler(this::onAuthenticationFailure)//失败提交
                )
                .logout(logout->logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onAuthenticationSuccess)//对于登出请求返回对应值
                )
                .rememberMe()//实现后面记住我方法，创建token当用户成功登录时，通过拦截器中的验证
                .rememberMeParameter("remember")
                .tokenRepository(repository)//
                .tokenValiditySeconds(3600*24*7)//设置记住我token过期时间
                .and()
                //用户信息验证注入自己写的校验类，由authotizeService类中自己写的loadUserByUsername获取同账户名账户，然后进行账号和密码的校验
                //.userDetailsService(authorizeService)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception->exception
                        .authenticationEntryPoint(this::onAuthenticationFailure)
                )
                .build();

    }
    //实现token持久化记住我功能类
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);//将生成token存入数据源实现持久化
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }
    //用户验证服务，用户信息验证注入自己写的校验类，由authotizeService类中自己写的loadUserByUsername获取同账户名账户，然后进行账号和密码的校验
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity security) throws Exception {
        return security.getSharedObject(AuthenticationManagerBuilder.class)//拿到对应AuthenticationManagerBuilder实例用于注入authorizeService
                .userDetailsService(authorizeService).and().build();//构建AuthenticationManager方法实现其中AuthenticationProvider身份验证

    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.fail(401,exception.getMessage())));
    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setCharacterEncoding("UTF-8");
        if(request.getRequestURI().endsWith("/login")) {response.getWriter().write(JSONObject.toJSONString(RestBean.success("登录成功")));}
        else if(request.getRequestURI().endsWith("/logout")){
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("退出登录成功")));
        }
    }
}
