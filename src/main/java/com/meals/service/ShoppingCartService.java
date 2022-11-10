package com.meals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    //向购物车添加商品
    ShoppingCart add(ShoppingCart shoppingCart);
    //购物车减少商品数量
    void sub(ShoppingCart shoppingCart);
}
