package com.meals.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meals.dto.DishDto;
import com.meals.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入对应的口味数据，需要操作两张表：dish，dish_flavor
    public void saveWithFavor(DishDto dishDto);
    //根据id查询菜品信息和对应的口味信息
    public DishDto getByidWithFavor(Long id);
    //更新菜品信息，同时更新口味信息
    public void updateWithFavor(DishDto dishDto);
    //批量/单个启售（禁售）菜品
    public void batchUpdateStatusByIds(Integer status, List<Long> ids);
    //批量/单个删除菜品
    public void batchDeleteByIds(List<Long> ids);
    //根据id查询菜品的信息，可能有多个，采用List集合
    public List<DishDto> listById(Dish dish);
    //分页，前端需要返回菜品数据和菜品分类名字
    Page<DishDto> pageAll(int page, int pageSize, String name);
}
