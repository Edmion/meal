package com.meals.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meals.common.R;
import com.meals.dto.DishDto;
import com.meals.entity.Dish;
import com.meals.service.CategoryService;
import com.meals.service.DishFlavorService;
import com.meals.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**+
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        //在使用缓存过程中，要注意保证数据库中的数据和缓存的数据一致，如果数据库中的数据发生了改变，需要及时清理缓存数据，否则会产生脏数据
        //全部清理
        /*Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);*/
        //精确清理某个分类下的缓存数据
        //String key = "dish_" + dishDto.getCategoryId() +"_1";
        String key = "dish_" + dishDto.getCategoryId() +"_" + dishDto.getStatus();
        redisTemplate.delete(key);
        dishService.saveWithFavor(dishDto);
        return R.success("菜品新增成功");
    }

    /**
     * 后端分页
     * 前端需要返回菜品数据和菜品分类名字
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam int page,@RequestParam int pageSize,String name){//name这里不是必须属性，不能加@RequestParam\
        Page<DishDto> dtoPage = dishService.pageAll(page, pageSize, name);
        return R.success(dtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto byidWithFavor = dishService.getByidWithFavor(id);
        return R.success(byidWithFavor);
    }

    /**+
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        String key = "dish_" + dishDto.getCategoryId() +"_" + dishDto.getStatus();
        redisTemplate.delete(key);
        dishService.updateWithFavor(dishDto);
        return R.success("菜品修改成功");
    }

    /**
     * 批量删除或删除单个菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> batchDelete(@RequestParam List<Long> ids){
        dishService.batchDeleteByIds(ids);
        return R.success("删除成功");
    }

    /**
     * 批量/单个启售（禁售）菜品
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        //菜品的状态(1为售卖,0为停售)由前端修改完成后通过请求路径占位符的方式传到后端,
        // 然后请求参数的类型设置为list类型,这样就可以进行批量或者单个菜品进行修改售卖状态
        dishService.batchUpdateStatusByIds(status,ids);

        return R.success("修改成功");
    }

    /**
     * 根据id查询菜品的信息，可能有多个，采用List集合
     * 后台：需要返回前端菜品信息即可
     * 手机端：需要返回菜品信息和口味信息
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){//前端请求过来的是categoryId，但是这里不使用Long categoryId，使用Dish dish泛用性更大
        List<DishDto> dishes = dishService.listById(dish);
        return R.success(dishes);
    }
}
