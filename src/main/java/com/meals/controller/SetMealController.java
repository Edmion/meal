package com.meals.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meals.common.R;
import com.meals.dto.SetmealDto;
import com.meals.entity.Setmeal;
import com.meals.service.SetMealDishService;
import com.meals.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setMealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam int page, @RequestParam int pageSize, String name) {
        Page<SetmealDto> setmealDtoPage = setMealService.pageAll(page, pageSize, name);
        return R.success(setmealDtoPage);
    }

    /**
     * 单个/多个起售（停售）套餐
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        setMealService.batchUpdateStatusByIds(status, ids);
        return R.success("修改成功");
    }

    /**
     * 批量删除或删除单个套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> batchDelete(@RequestParam List<Long> ids) {
        setMealService.batchDeleteByIds(ids);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询套餐数据
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")//向缓存中添加数据
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null,Setmeal::getStatus, setmeal.getStatus());
        lqw.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setMealService.list(lqw);

        return R.success(list);
    }

    /**
     * 根据id查询套餐信息和对应的菜品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        SetmealDto setmealDto = setMealService.getWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 点击图片查看信息
     * 前端请求：http://localhost:8080/setmeal/dish/1415580119015145474
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<Setmeal> showSetmealDish(@PathVariable Long id){
        //从Setmeal表中根据id查询套餐或菜品信息
        Setmeal setmeal = setMealService.getById(id);
        //直接将套餐或菜品信息返回给前端页面展实
        return R.success(setmeal);
    }
}
