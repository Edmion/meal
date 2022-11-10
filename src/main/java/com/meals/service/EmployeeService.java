package com.meals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.common.R;
import com.meals.entity.Employee;

import javax.servlet.http.HttpServletRequest;

public interface EmployeeService extends IService<Employee> {
    //后台登录
    R<Employee> login(HttpServletRequest request, Employee employee);
}
