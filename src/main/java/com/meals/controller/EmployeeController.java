package com.meals.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meals.common.R;
import com.meals.entity.Employee;
import com.meals.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     *  http://localhost:8080/employee/login
     *  员工登录
     *
     *  @RequestBody：用于将前台发送过来固定格式的数据（xml格式或json等）封装为对应的JavaBean对象
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){

        R<Employee> data = employeeService.login(request, employee);


        return data;//将刚刚从数据库中查出来的对象转给前端，前端将其存放到浏览器中
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("成功退出");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        //设置初始密码，采用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //获得当前创建人的id，被创建的id是由雪花算法自动生成的
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);//创建人id
        employee.setUpdateUser(empId);//最后的更新人id*/
        employeeService.save(employee);

        return R.success("更新成功");
    }

    //http://localhost:8080/employee/page?page=1&pageSize=10&name=123

    /**
     * protected List<T> records;当前页面数据
     * protected long total;当前页面总数量
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam int page,@RequestParam int pageSize,String name){
        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        //添加过滤条件 StringUtils.isNotEmpty(name)是true才加入后面的sql语句
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, lqw);//这里不需要写返回值，因为最终查询的内容都封装到Page中了，相当于重新给pageInfo赋值了
        return R.success(pageInfo);
    }

    /**
     * 修改员工状态码（启用，禁用）
     * 配置了消息转换器来解决js页面只能处理16位long型数据的限制
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){//{"id":1,"status":0}
        /*Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/
        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    /**
     * 通过id查询员工信息，并显示到修改信息页面
     * http://localhost:8080/employee/1589142913047105537
     * PathVariable：@GetMapping("/{id}")从这个语法中获取id
     * 前端页面会传过来一个点击的id，通过 @GetMapping("/{id}")接收到请求过来的id，
     * 根据这个id从数据库中查询数据并返回到前端的修改页面上，
     * 前端修改后，再点击修改，这时调用上面的update方法，自动调用（修改和更新方法相同，都是修改信息）
     * add.html页面为公共页面，新增和修改员工都在此页面进行
     * // 修改页面反查详情接口
     * function queryEmployeeById (id) {
     *   return $axios({
     *     url: `/employee/${id}`,
     *     method: 'get'
     *   })
     * @param id
     * @return
     */
    @GetMapping("/{id}")//前端是请求路径方式传过来，通过这样来接收
    public R<Employee> update(@PathVariable Long id){//这里的id要和上面的相同
        //@PathVariable("id")
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到此员工信息");
    }
}
