package com.shujia.one.controller;

import com.shujia.one.entity.RestBean;
import com.shujia.one.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
//用户信息验证接口，负责登录、重置密码、注册等功能接口
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    private final String EMAIL_REGEX="^[A-Za-z0-9+_.-]+@(.+)$";
    private final String USERNAME_REGEX="^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";

    @Resource
    AuthorizeService authorizeService;
    //注册时候发送邮件验证接口
    //@RequestParam("email")将validateEmail中的email参数自动设置为前端传来的email
    @PostMapping("/valid-register-email")
    public RestBean<String> validateEmail(@Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,HttpSession session) {
        //获取邮箱和客服端session后验证，根据成功失败返回对应信息
        if(authorizeService.sendValidateEmail(email,session.getId(),false)==null) return RestBean.success("邮件已发送");
        else return RestBean.fail(400,"邮件发送失败");

    }
    //重置密码时邮件发送接口
    @PostMapping("/valid-reset-email")
    public RestBean<String> validateResetEmail(@Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,HttpSession session) {

        if(authorizeService.sendValidateEmail(email,session.getId(),true)==null) return RestBean.success("邮件已发送");
        else return RestBean.fail(400,"邮件发送失败");

    }
    //注册信息接口
    @PostMapping("/register")
    public RestBean<String> registUser(@Pattern(regexp = USERNAME_REGEX) @Length(min = 2,max = 8) @RequestParam("username") String username,
                                       @Length(min = 6,max = 16) @RequestParam("password") String password,
                                       @Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,
                                       @Length(min = 6,max = 6) @RequestParam("code") String code,
                                       HttpSession session){
        //得到前端传来信息
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
    //开始重置密码接口
    @PostMapping("/start-reset")
    public RestBean<String> startReset(@Pattern(regexp =EMAIL_REGEX ) @RequestParam("email") String email,
                                       @Length(min = 6,max = 6) @RequestParam("code") String code,
                                       HttpSession session){
            String s=authorizeService.validateOnly(email,code,session.getId());//验证账户唯一
            if(s==null){
                session.setAttribute("reset-password",email);//生成对应键值对存入session中
                return RestBean.success();
        }else {
                return RestBean.fail(400,s);
            }
    }
    //确认重置密码接口
    @PostMapping("/do-reset")
    public RestBean<String> resetPassword( @Length(min = 6,max = 16) @RequestParam("password") String password,
                                           HttpSession session){
        String email =(String) session.getAttribute("reset-password");//先取得之前设置的email
        if(email==null){
            return RestBean.fail(401,"请先完成邮箱验证");
        }else if(authorizeService.resetPassword(email,password)){//有的话把要重置账户和新密码送到service层中对应处理
            session.removeAttribute("reset-password");//再删除session
            return RestBean.success("密码重置成功");
        }else {
            return RestBean.fail(500,"内部错误请联系管理员");
        }
    }
}
