package com.meals.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.entity.OrderDetail;
import com.meals.mapper.OrderDetailMapper;
import com.meals.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
