package com.meals.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meals.common.BaseContext;
import com.meals.common.R;
import com.meals.entity.ShoppingCart;
import com.meals.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车模块
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 向购物车添加商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        ShoppingCart add = shoppingCartService.add(shoppingCart);
        return R.success(add);
    }

    /**
     * 购物车减少商品数量
     * 前端请求：{dishId: "1589547207709814785", setmealId: null}菜品或套餐（dish_id）
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("菜品{}",shoppingCart.getDishId());
        log.info("套餐{}",shoppingCart.getSetmealId());
        shoppingCartService.sub(shoppingCart);
        return R.success("删除成功");
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("list")
    public R<List<ShoppingCart>> list(){//没有东西传过来
        //根据登录用户的id来查询
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> deleteAll(){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);
        return R.success("购物车已清空");
    }
}
