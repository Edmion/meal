package com.meals.dto;


import com.meals.entity.Dish;
import com.meals.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    //菜品分类名字，对应category表中的name属性
    private String categoryName;

    private Integer copies;
}
