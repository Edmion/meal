package com.meals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.common.BaseContext;
import com.meals.entity.Setmeal;
import com.meals.entity.ShoppingCart;
import com.meals.mapper.ShoppingCartMapper;
import com.meals.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 向购物车添加商品
     * 前端请求的参数中没有userId属性
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart add(ShoppingCart shoppingCart) {
        //设置用户id，指定当前购物车数据是哪个用户的
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询当菜品是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        if (dishId != null) {
            //添加到购物车的是菜品
            lqw.eq(ShoppingCart::getDishId, dishId);
        }else {
            //dishId = null,添加到购物车的是套餐
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //根据传过来的查询条件在shoppingCart表中查询数据
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(lqw);
        if (shoppingCartServiceOne != null) {
            //如果已存在，则在原基础上数量加1
            shoppingCartServiceOne.setNumber(shoppingCartServiceOne.getNumber() + 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else{
            //如果不存在，则添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //shoppingCartOne为null
            shoppingCartServiceOne = shoppingCart;
        }
        return shoppingCartServiceOne;
}

    /**
     * 购物车减少商品数量
     * 前端请求：{"setmealId":"1589613689978245122"}
     * @param shoppingCart
     * @return
     */
    @Override
    public void sub(ShoppingCart shoppingCart) {//dish id
        log.info("菜品{}",shoppingCart.getDishId());
        log.info("套餐{}",shoppingCart.getSetmealId());
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        //查询当前用户购物车的商品
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(lqw);

        Long setmealId = shoppingCart.getSetmealId();
        //判断传递过来的是菜品还是套餐
        if (setmealId != null) {
            //套餐
            lqw.eq(ShoppingCart::getSetmealId,setmealId);
        }else{
            //菜品
            lqw.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        //根据id和菜品或套餐id查询ShoppingCart对象
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(lqw);
        //获取当前数据库中number的值并且减1
        Integer number = shoppingCartServiceOne.getNumber() - 1;
        //将修改好的值赋予shoppingCartServiceOne对象
        shoppingCartServiceOne.setNumber(number);
        if (number >0){
            //大于0更新数量
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            //小于0，将其移除
            shoppingCartService.removeById(shoppingCartServiceOne);
        }

    }



}
