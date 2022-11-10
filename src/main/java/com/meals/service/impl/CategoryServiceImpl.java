package com.meals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.entity.Category;
import com.meals.entity.Dish;
import com.meals.entity.Setmeal;
import com.meals.exception.CustomException;
import com.meals.mapper.CategoryMapper;
import com.meals.service.CategoryService;
import com.meals.service.DishService;
import com.meals.service.SetMealService;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    /**
     * 根据id删除分类，删除之前要进行判断
     * @param id 创建套餐时，自动生成的id（category表），点击删除时由前端的请求传给后端
     */
    @Autowired
    private CategoryService categoryService;

    @Resource
    private DishService dishService;

    @Resource
    private SetMealService setMealService;

    @Override
    public void remove(Long id) {
        //添加查询条件，根据分类id进行查询
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联菜品，如果已关联，抛出一个业务异常
        if (count1 > 0) {
            //已关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //查询当前分类是否关联套餐，如果已关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setMealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0) {
            //已关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //正常删除分类
        categoryService.removeById(id);
        //super.removeById(id);
    }
}
