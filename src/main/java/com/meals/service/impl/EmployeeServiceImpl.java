package com.meals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.common.R;
import com.meals.entity.Employee;
import com.meals.mapper.EmployeeMapper;
import com.meals.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{

    @Autowired
    private EmployeeService employeeService;

    @Override
    public R<Employee> login(HttpServletRequest request, Employee employee) {

        //1.将提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        //eq(列名，值)
        lqw.eq(Employee::getUsername,employee.getUsername());//Employee::getUsername数据库中的列名，它是登录账户，不能重复
        Employee emp = employeeService.getOne(lqw);//因为数据库中的username加了唯一约束，只会查询出一条结果，所以这里用getOne()
        //3.如果没有查询到则返回失败结果
        if (emp == null){
            return R.error("用户名错误");
        }
        //4.将提交的密码与数据库中查询出的密码比对，如果不一样则返回登录失败结果
        if (!password.equals(emp.getPassword())){
            return R.error("密码错误");
        }
        //5.查看查询出的员工状态，如果已被禁用，则返回禁用结果
        if (emp.getStatus() != 1){
            return R.error("账户已被禁用");
        }
        //6.登录成功，将员工id存入Session并返回结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
}
