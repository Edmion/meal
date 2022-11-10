package com.meals.controller;

import com.meals.common.R;
import com.meals.entity.User;
import com.meals.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送验证码
     * 前端请求数据：{"phone":"1977167945@qq.com"}
     * User里面包含phone，可以直接拿来使用(User 来接收请求过来的手机号)
     * @param user
     * @param session
     * @return
     * @throws MessagingException
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {

        R<String> msg = userService.sendMsg(user, session);
        return msg;

    }

    /**
     * 移动端登录
     * 前端请求数据：{"phone":"12345@qq.com","code":"12345"}
     * User里面没有code，这里可以使用dto或者map（k=v）来进行接收
     *
     * R<User>:浏览器也需要保存一分用户信息
     * @param map
     * @param session
     * @return
     * @throws MessagingException
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) throws MessagingException {
        R<User> login = userService.login(map, session);
        return login;


    }

    /**
     * 移动端用户退出
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session){
        //移除session中存的信息
        session.removeAttribute("user");
        return R.success("用户已退出");
    }

}
