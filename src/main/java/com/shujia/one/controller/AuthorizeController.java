package com.shujia.one.controller;

import com.shujia.one.entity.RestBean;
import com.shujia.one.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    private final String EMAIL_REGEX="^[A-Za-z0-9+_.-]+@(.+)$";
    private final String USERNAME_REGEX="^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";
    @Resource
    AuthorizeService authorizeService;
    @PostMapping("/valid-register-email")
    public RestBean<String> validateEmail(@Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,HttpSession session) {

        if(authorizeService.sendValidateEmail(email,session.getId(),false)==null) return RestBean.success("邮件已发送");
        else return RestBean.fail(400,"邮件发送失败");

    }
    @PostMapping("/valid-reset-email")
    public RestBean<String> validateResetEmail(@Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,HttpSession session) {

        if(authorizeService.sendValidateEmail(email,session.getId(),true)==null) return RestBean.success("邮件已发送");
        else return RestBean.fail(400,"邮件发送失败");

    }
    @PostMapping("/register")
    public RestBean<String> registUser(@Pattern(regexp = USERNAME_REGEX) @Length(min = 2,max = 8) @RequestParam("username") String username,
                                       @Length(min = 6,max = 16) @RequestParam("password") String password,
                                       @Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,
                                       @Length(min = 6,max = 6) @RequestParam("code") String code,
                                       HttpSession session){
        String s= authorizeService.validateAndRegister(username,password,email,code,session.getId());
        if(s==null){
            return RestBean.success("注册成功");
        }else {
            return RestBean.fail(400,s);
        }
    }
    /**
     1发送邮件，验证验证码是否正确
     2在session中存标记
     3用户发起重置密码请求，如标记存在就重置
     */
    @PostMapping("/start-reset")
    public RestBean<String> startReset(@Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,
                                       @Length(min = 6,max = 6) @RequestParam("code") String code,
                                       HttpSession session){
            String s=authorizeService.validateOnly(email,code,session.getId());
            if(s==null){
                session.setAttribute("reset-password",email);
                return RestBean.success();
        }else {
                return RestBean.fail(400,s);
            }
    }
    @PostMapping("/do-reset")
    public RestBean<String> resetPassword( @Length(min = 6,max = 16) @RequestParam("password") String password,
                                           HttpSession session){
        String email =(String) session.getAttribute("reset-password");
        if(email==null){
            return RestBean.fail(401,"请先完成邮箱验证");
        }else if(authorizeService.resetPassword(email,password)){
            session.removeAttribute("reset-password");
            return RestBean.success("密码重置成功");
        }else {
            return RestBean.fail(500,"内部错误请联系管理员");
        }
    }
}
