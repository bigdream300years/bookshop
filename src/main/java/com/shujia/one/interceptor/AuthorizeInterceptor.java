package com.shujia.one.interceptor;

import com.shujia.one.entity.AccountUser;
import com.shujia.one.mapper.userMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
//拦截器层，拦截非登录情况下的请求
//获取当前登录用户的用户名，并根据用户名查询用户信息，然后将用户信息存储到session中
@Component
public class AuthorizeInterceptor implements HandlerInterceptor {
    @Resource
    userMapper mapper;
    //请求前拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,  Object handler) throws Exception {
        SecurityContext context= SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();//获取已经经过认证用户信息
        User user= (User) authentication.getPrincipal();//取得登录用户信息
        String username=user.getUsername();
        AccountUser accountUser =mapper.findAccountUserByNameOrEmail(username);//在用户表中查找
        //往sessionn中存入键名为account，值为accountUser自定义数据，持续整个对话
        request.getSession().setAttribute("account", accountUser);
        return true;
    }
}
