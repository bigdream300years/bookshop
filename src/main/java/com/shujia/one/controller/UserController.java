package com.shujia.one.controller;

import com.shujia.one.entity.AccountUser;
import com.shujia.one.entity.RestBean;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

//用户权限判断接口
@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping("/me")
    //@SessionAttribute("account")从session中检索account的传给accountUser，这一步在拦截器中只有登录用户才有此信息
    public RestBean<AccountUser> me(@SessionAttribute("account") AccountUser accountUser) {
        return RestBean.success(accountUser);
    }
}
