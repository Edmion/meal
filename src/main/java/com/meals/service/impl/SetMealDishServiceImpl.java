package com.meals.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.entity.SetmealDish;
import com.meals.mapper.SetMealDishMapper;
import com.meals.service.SetMealDishService;
import com.meals.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class SetMealDishServiceImpl extends ServiceImpl<SetMealDishMapper, SetmealDish> implements SetMealDishService {
}
