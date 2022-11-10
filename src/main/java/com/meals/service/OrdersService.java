package com.meals.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.dto.OrdersDto;
import com.meals.entity.Orders;

import java.util.List;

public interface OrdersService extends IService<Orders> {
    //用户下单
    void submit(Orders orders);
    //历史订单
    Page<OrdersDto> pageAll(int page, int pageSize);
    //后台订单详情
    Page<OrdersDto> pageBackEnd(int page, int pageSize, String number, String beginTime, String endTime);
    //再来一单
    void again(Orders orders);
}
