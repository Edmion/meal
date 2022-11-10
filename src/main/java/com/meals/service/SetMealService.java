package com.meals.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.common.R;
import com.meals.dto.SetmealDto;
import com.meals.entity.Setmeal;

import java.util.List;

public interface SetMealService extends IService<Setmeal> {
    //添加套餐
    void saveWithDish(SetmealDto setmealDto);
    //分页
    Page<SetmealDto> pageAll(int page, int pageSize, String name);
    //单个/多个起售（停售）套餐
    void batchUpdateStatusByIds(Integer status, List<Long> ids);
    //批量删除或删除单个套餐
    void batchDeleteByIds(List<Long> ids);
    //根据id查询套餐信息和对应的菜品信息
    SetmealDto getWithDish(Long id);
}
