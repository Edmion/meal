package com.meals.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meals.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
