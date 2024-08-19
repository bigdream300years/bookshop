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
    //返回数据库中名称与前端输入用户名相同的详细用户信息
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
        return User.withUsername(account.getUsername())//存在账户验证通过返回一个数据库中名为前端传入用户名的User对象给Userdetail
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    @Override
    public String sendValidateEmail(String email,String sessionId,boolean hasAccount){

        String key = "email: " + sessionId + ":" +email+":"+hasAccount;//以sessionId和email做为key值
        if(Boolean.TRUE.equals(template.hasKey(key))){//如果redis中有这个key，上一次已经发送过验证码生成过key防止系统太频繁
            Long expire= Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(180L) ;//计算上一次发验证码时间
            if(expire>120){//验证码时间太频繁
                return "请求频繁，请稍后再试";
            }
        }
        Account account= mapper.findAccountByNameOrEmail(email);//根据邮件查找是否有这个账户
        if(account==null&&hasAccount){//有账户但按输入地址没查到账户
            return  "没有此邮件地址的账户";
        }
        if(account!=null&&!hasAccount){//查到已经有这个账户
            return  "此邮箱已被注册";
        }
        //只有没账户满足hasAccount==false且account==null两个都查不到才进注册账户
        Random random = new Random();
        int code=random.nextInt(899999)+100000;//生成验证码
        //根据配置类往email里发送相应邮件信息
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
            String s=template.opsForValue().get(key);//检查是否有redis里是否有对应key，进行验证
            if(s==null){
                return "验证码失效，请重新验证";
            }
            if(s.equals(code)){//验证码验证通过时
              password=encoder.encode(password);
              template.delete(key);//删除key防止重复利用验证码注册账户
              Account account= mapper.findAccountByNameOrEmail(username);//先查找账户，防止已有账户重复注册
              if(account!=null){return "此用户已被注册";}
              if(mapper.createAccount(username,password,email)>0){//进行账户创建，并根据结果返回对应值
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
        if(Boolean.TRUE.equals(template.hasKey(key))){//用key验证
            String s=template.opsForValue().get(key);
            if(s==null){
                return "验证码失效，请重新验证";
            }
            if(s.equals(code)) {
                template.delete(key);//成功删除key返回正确值
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
