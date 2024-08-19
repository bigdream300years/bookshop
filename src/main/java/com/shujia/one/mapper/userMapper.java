package com.shujia.one.mapper;

import com.shujia.one.entity.Account;
import com.shujia.one.entity.AccountUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface userMapper {
    //登录时查询是否有用户
    @Select("select * from user where username = #{test} or email = #{test}")
    Account findAccountByNameOrEmail(String test);
    //注册账户
    @Insert("insert into user(username,password,email) values (#{username},#{password},#{email})")
    int createAccount(String username, String password, String email);
    //忘记密码找回重置账户
    @Update("update user set password=#{password} where email=#{email}")
    int resetPasswordByEmail(String email, String password);
    //获取用户信息
    @Select("select * from user where username = #{test} or email = #{test}")
    AccountUser findAccountUserByNameOrEmail(String test);
}
