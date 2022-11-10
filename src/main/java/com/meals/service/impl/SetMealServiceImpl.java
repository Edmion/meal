package com.meals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.dto.SetmealDto;
import com.meals.entity.*;
import com.meals.exception.CustomException;
import com.meals.mapper.SetMealMapper;
import com.meals.service.CategoryService;
import com.meals.service.SetMealDishService;
import com.meals.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增套餐
     * @param setmealDto
     */
    @Override
    @Transactional//涉及多张表，开启事务控制
    @CacheEvict(value = "setmealCache",allEntries = true)
    //@CacheEvict(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")//从缓存中删除数据
    public void saveWithDish(SetmealDto setmealDto) {
    setMealService.save(setmealDto);

        Long id = setmealDto.getId();
        //前端请求过来的数据中没有给setmealId属性赋值
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : list) {
            setmealDish.setSetmealId(id);
        }
        setMealDishService.saveBatch(list);
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<SetmealDto> pageAll(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null,Setmeal::getName,name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        setMealService.page(setmealPage,lqw);
        //对象拷贝
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = new ArrayList<>();
        //给records的赋值
        for (Setmeal record : records) {
            Long categoryId = record.getCategoryId();
            //根据从category表中查询出整个对象
            Category category = categoryService.getById(categoryId);
            //新建个对象用于存放名字等信息
            SetmealDto setmealDto = new SetmealDto();
            if (category != null) {
                //从对象中获取name赋值给SetmealDto的category
                setmealDto.setCategoryName(category.getName());
            }
            //对象拷贝
            BeanUtils.copyProperties(record,setmealDto);
            //每遍历一次，添加名字和拷贝完全的全部信息到setmealDtoList中
            setmealDtoList.add(setmealDto);
        }
        setmealDtoPage.setRecords(setmealDtoList);
        return setmealDtoPage;

    }

    /**
     * 单个/多个起售（停售）
     * @param status
     * @param ids
     */
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true)
    public void batchUpdateStatusByIds(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(ids != null,Setmeal::getId,ids);
        //根据id查询出对应的所以套餐信息
        List<Setmeal> list = setMealService.list(lqw);
        if (list != null) {
            for (Setmeal setmeal : list) {
                setmeal.setStatus(status);
                setMealService.updateById(setmeal);
               /* Long categoryId = setmeal.getCategoryId();
                Set keys = redisTemplate.keys("setmealCache::" + categoryId + "_*");
                redisTemplate.delete(keys);*/
            }
        }
    }

    /**
     * 批量删除或删除单个套餐
     * @param ids
     */
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true)
    @Transactional//涉及多张表，开启事务控制
    public void batchDeleteByIds(List<Long> ids) {
        /*LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(ids != null,Setmeal::getId,ids);
        List<Setmeal> list = setMealService.list(lqw);
        if (list != null) {
            for (Setmeal setmeal : list) {
                //判断是否处于禁售状态
                if (setmeal.getStatus() == 0){
                    LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
                    setMealDishService.remove(lambdaQueryWrapper);
                    setMealService.removeByIds(ids);
                    return;
                }else{
                    throw new CustomException("有套餐正在售卖，无法全部删除");
                }
            }
        }*/
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(ids != null,Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
        //List<Setmeal> list = setMealService.list(lqw);

        int count = setMealService.count(lqw);
        if (count > 0){
            throw new CustomException("有套餐正在售卖，无法全部删除");
        }
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setMealDishService.remove(lambdaQueryWrapper);
        setMealService.removeByIds(ids);
        /*for (Setmeal setmeal : list) {
            Long categoryId = setmeal.getCategoryId();
            Set keys = redisTemplate.keys("setmealCache::" + categoryId + "_1");
            redisTemplate.delete(keys);
        }*/
    }

    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getWithDish(Long id) {
        //从Setmeal表中根据id查询套餐信息
        Setmeal setmeal = setMealService.getById(id);
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        //根据套餐对应的菜品表（setmeal_dish）中查询出菜品信息
        List<SetmealDish> setmealDishes = setMealDishService.list(lqw);
        //SetmealDto
        SetmealDto setmealDto = new SetmealDto();
        //将查询出的setmeal表的信息拷到setmealDto中
        BeanUtils.copyProperties(setmeal,setmealDto);
        //给setmealDto的setmealDishes赋值
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;

    }
}
