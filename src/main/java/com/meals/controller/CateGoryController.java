package com.meals.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meals.common.R;
import com.meals.entity.Category;
import com.meals.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CateGoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("创建菜品分类成功");
    }

    /**
     * 分页
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam int page,@RequestParam int pageSize){
        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        //添加排序条件
        lqw.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long ids){//改成和前端传过来的一样
        //public R<String> delete(@RequestParam Long ids)加参数改成和前端传过来的一样
        //public R<String> delete(@RequestParam("ids") Long id)在括号里面写成和前端传过来的一样

        //categoryService.removeById(id);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 根据条件查询分类数据（加载分类数据）
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //构造条件过滤器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        //添加过滤方法
        lqw.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lqw);
        return R.success(list);
    }
}
