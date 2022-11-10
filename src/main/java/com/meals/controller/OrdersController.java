package com.meals.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meals.common.R;
import com.meals.dto.OrdersDto;
import com.meals.entity.Orders;
import com.meals.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单
 */
@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
      return R.success("下单成功");
    }

    /**
     * 手机端历史订单
     * 前端请求:http://localhost:8080/order/userPage?page=1&pageSize=5
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(@RequestParam int page,@RequestParam int pageSize){
        Page<OrdersDto> ordersDtoPage = ordersService.pageAll(page, pageSize);
        return R.success(ordersDtoPage);
    }

    /**
     * 后台订单分页
     * 前端请求：http://localhost:8080/order/page?page=1&pageSize=10
     * &number=123&beginTime=2022-11-15 00:00:00&endTime=2022-12-22 23:59:59
     * 必要：page、pageSize
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */

    @GetMapping("/page")
    public R<Page> pageAll(@RequestParam int page,@RequestParam int pageSize,String number,String beginTime,String endTime){
        Page<OrdersDto> ordersDtoPage = ordersService.pageBackEnd(page, pageSize,number,beginTime,endTime);
        return R.success(ordersDtoPage);
    }

    /**
     * 订单派送
     * {
     *   "status": 3,
     *   "id": "1590009180608024578"
     * }
     *
     * switch(row.status){
     *     case 1:
     *         str =  '待付款'
     *     break;
     *     case 2:
     *         str =  '正在派送'
     *     break;
     *     case 3:
     *         str =  '已派送'
     *     break;
     *     case 4:
     *         str =  '已完成'
     *     break;
     *     case 5:
     *         str =  '已取消'
     *     break;
     * }
     * @param map
     * @return
     */
    @PutMapping
    public R<String> commit(@RequestBody Map<String, String> map){
        //获取status
        int status = Integer.parseInt(map.get("status"));
        //获取id
        Long orderId = Long.valueOf(map.get("id"));
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId);
        updateWrapper.set(Orders::getStatus, status);
        ordersService.update(updateWrapper);
        return R.success("订单状态修改成功");
    }

    /**
     * 再来一单
     * 点击再来一单，会直接将当前订单的数据添加到购物车，并且自动跳转到购物车详情页面
     * http://localhost:8080/order/again
     * 前端请求：{id: "1590009180608024578"}
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        log.info("id为{}",orders.getId());
        ordersService.again(orders);
        return R.success("该订单信息已添加到购物车");
    }

}
