package com.meals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
