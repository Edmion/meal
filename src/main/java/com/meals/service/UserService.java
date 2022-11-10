package com.meals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.common.R;
import com.meals.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface UserService extends IService<User> {
    //发送邮件（短信）
    R<String> sendMsg(User user, HttpSession session);
    //登录
    R<User> login(Map map, HttpSession session);
}
