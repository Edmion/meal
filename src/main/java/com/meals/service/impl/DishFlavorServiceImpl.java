package com.meals.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.entity.DishFlavor;
import com.meals.mapper.DishFlavorMapper;
import com.meals.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
