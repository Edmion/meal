package com.meals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.common.R;
import com.meals.entity.User;
import com.meals.mapper.UserMapper;
import com.meals.service.UserService;
import com.meals.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public R<String> sendMsg(User user, HttpSession session) {
        //从请求中获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info("code={}",code);
            //这里的phone其实就是邮箱，code是我们生成的验证码
            //发送验证码给邮箱
            //MailUtils.sendTestMail(phone,code);
            //用手机号作为key（邮箱）将验证码value存session，方便后面拿出来比对
            //session.setAttribute(phone,code);

            //session存改为用redis存，并且设置验证码有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("验证码发送成功");
        }
        return R.success("验证码发送失败");
    }

    @Override
    public R<User> login(Map map, HttpSession session) {
        //获取手机号（邮箱）
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中取出已经保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //从redis中取出验证码
        Object codeInRedis = redisTemplate.opsForValue().get(phone);
        //进行验证码的比对（页面提交的验证码和Session中保存的比对）
        if (codeInRedis != null && codeInRedis.equals(code)) {
            //如果能比对成功，说明登录成功

            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            //根据请求的phone在数据库中查询信息
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);
            User user = userService.getOne(lqw);
            //没有查询到数据，表示为新用户
            if (user == null) {
                ////如果不存在，则创建一个，存入数据库
                user = new User();
                user.setPhone(phone);
                //数据库默认就是1，可以不用手动设置
                user.setStatus(1);
                userService.save(user);
            }
            //存个session，表示登录状态
            session.setAttribute("user",user.getId());

            //，如果登录成功，将存在redis中的验证码删除
            redisTemplate.delete(phone);
            return R.success(user);
        }


        return R.error("登录失败");
    }
}
