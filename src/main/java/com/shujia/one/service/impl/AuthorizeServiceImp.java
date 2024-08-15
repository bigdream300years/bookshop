package com.shujia.one.service.impl;

import com.shujia.one.entity.Account;
import com.shujia.one.mapper.userMapper;
import com.shujia.one.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImp implements AuthorizeService {

    @Value("${spring.mail.username}")
    String from;

    @Resource
    userMapper mapper;
    @Resource
    MailSender mailSender;
    @Resource
    StringRedisTemplate template;

    BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username==null){
            throw new UsernameNotFoundException("用户名不能为空");
        }
       // System.out.println(":::"+username);
        Account account= mapper.findAccountByNameOrEmail(username);
        if(account==null){
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }
    @Override
    public String sendValidateEmail(String email,String sessionId,boolean hasAccount){

        String key = "email: " + sessionId + ":" +email+":"+hasAccount;
        if(Boolean.TRUE.equals(template.hasKey(key))){
            Long expire= Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(180L) ;
            if(expire>120){
                return "请求频繁，请稍后再试";
            }
        }
        Account account= mapper.findAccountByNameOrEmail(email);
        if(account==null&&hasAccount){
            return  "没有此邮件地址账户";
        }
        if(account!=null&&!hasAccount){
            return  "此邮箱已被注册";
        }
        Random random = new Random();
        int code=random.nextInt(899999)+100000;
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("您的验证邮件");//邮件标题
        message.setText("验证码是: "+code);//邮件内容
        try {
            mailSender.send(message);

            template.opsForValue().set(key,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }catch (MailException e) {
            e.printStackTrace();
            return "邮件发送失败，检查邮件地址是否有效";
        }

    }
    //key中bool值true与false区分注册和修改密码功能
    @Override
    public String validateAndRegister(String username,String password,String email,String code,String sessionId){
        /**
         1、先生成对应验证码2、把邮箱和对应验证码作为键值对放到redis里（过期时间三分钟，如重新发送邮件只要剩余时间低于2分钟就重新发送一次）
         3、发送验证码到指定邮箱4、发送失败把redis里信息删除5、用户注册时再从redis中取出验证码进行验证
         */
        String key = "email: " + sessionId + ":" +email+":false";
        if(Boolean.TRUE.equals(template.hasKey(key))){
            String s=template.opsForValue().get(key);
            if(s==null){
                return "验证码失效，请重新验证";
            }
            if(s.equals(code)){
              password=encoder.encode(password);
              template.delete(key);
              Account account= mapper.findAccountByNameOrEmail(username);
              if(account!=null){return "此用户已被注册";}
              if(mapper.createAccount(username,password,email)>0){
                 return null;
                }else{
                  return "内部错误，请联系管理员";
              }
            } else{
                return "验证码错误，请重新提交";
            }
        }else{
            return "请先完成邮件验证";
        }
    }
    @Override
    public String validateOnly(String email,String code,String sessionId){
        String key = "email: " + sessionId + ":" +email+":true";
        if(Boolean.TRUE.equals(template.hasKey(key))){
            String s=template.opsForValue().get(key);
            if(s==null){
                return "验证码失效，请重新验证";
            }
            if(s.equals(code)) {
                template.delete(key);
                return null;
            }else {
                return "验证码错误，请稍后再提交";
            }
        }else{
            return "请先完成邮件验证";
        }
    }
    @Override
    public boolean resetPassword(String email,String password){
        password=encoder.encode(password);
        return mapper.resetPasswordByEmail(email,password)>0;
    }
}
