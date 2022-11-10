package com.meals.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meals.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
