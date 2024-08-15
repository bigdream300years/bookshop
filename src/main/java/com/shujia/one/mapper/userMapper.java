package com.shujia.one.mapper;

import com.shujia.one.entity.Account;
import com.shujia.one.entity.AccountUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface userMapper {
    @Select("select * from user where username = #{test} or email = #{test}")
    Account findAccountByNameOrEmail(String test);

    @Insert("insert into user(username,password,email) values (#{username},#{password},#{email})")
    int createAccount(String username, String password, String email);

    @Update("update user set password=#{password} where email=#{email}")
    int resetPasswordByEmail(String email, String password);

    @Select("select * from user where username = #{test} or email = #{test}")
    AccountUser findAccountUserByNameOrEmail(String test);
}
