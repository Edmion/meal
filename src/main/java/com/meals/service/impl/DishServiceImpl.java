package com.meals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.dto.DishDto;
import com.meals.entity.Category;
import com.meals.entity.Dish;
import com.meals.entity.DishFlavor;
import com.meals.exception.CustomException;
import com.meals.mapper.DishMapper;
import com.meals.service.CategoryService;
import com.meals.service.DishFlavorService;
import com.meals.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService flavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional//涉及多张表，开启事务控制
    public void saveWithFavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        //this.save(dishDto)
        dishService.save(dishDto);
        //菜品id
        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        //前端请求的数据中没有dishId，所以DishDto中没有给dishId赋值,这里遍历集合并且给DishFlavor中的dishId赋值
     /*   for (int i = 0; i < dishFlavors.size(); i++) {
            dishFlavors.get(i).setDishId(dishId);
        }*/
        for (DishFlavor dishFlavor : dishFlavors) {
            dishFlavor.setDishId(dishId);
        }
        flavorService.saveBatch(dishFlavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByidWithFavor(Long id) {
        //从Dish表中根据id查询菜品信息
        Dish dish = dishService.getById(id);
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,id);
        //根据DishId查询出口味信息
        List<DishFlavor> dishFlavors = flavorService.list(lqw);
        //new一个新的DishDto
        DishDto dishDto = new DishDto();
        //将查询出的dish表的信息拷到dishDto中
        BeanUtils.copyProperties(dish,dishDto);
        //给dishDto的flavors赋值
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    /**
     * 更新菜品信息，同时更新口味信息
     * @param dishDto
     */
    @Override
    public void updateWithFavor(DishDto dishDto) {
        //更新dish表基本信息
        dishService.updateById(dishDto);
        //清理当前菜品对应口味数据，dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        flavorService.remove(lqw);
        //提交当前页面的口味数据，dish_flavor表的insert操作,DishFlavor中dishId没有被封装得有数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor dishFlavor : flavors) {
            dishFlavor.setDishId(dishDto.getId());
        }
        flavorService.saveBatch(flavors);
    }

    /**
     * 批量/单个启售（禁售）菜品
     * @param status
     * @param ids
     */
    @Override
    public void batchUpdateStatusByIds(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(ids != null,Dish::getId,ids);
        //根据id查询出信息(Dish对象在List集合中)
        List<Dish> list = dishService.list(lqw);
        //不等于空表示查询到状态
        if (list != null) {
            //遍历list集合（遍历出多个或一个Dish对象），给他们的status赋值，然后进行update操作
            for (Dish dish : list) {
                dish.setStatus(status);
                dishService.updateById(dish);
                Long categoryId = dish.getCategoryId();

                //停售/起售后，清除对应的缓存
                Set keys = redisTemplate.keys("dish_" + categoryId + "_*");
                redisTemplate.delete(keys);
            }
        }
    }

    /**
     * 批量/单个删除菜品
     * @param ids
     */
    @Override
    @Transactional//涉及多张表，开启事务控制
    public void batchDeleteByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(ids != null,Dish::getId,ids);
        //根据id查询出信息(Dish对象在List集合中)
        List<Dish> list = dishService.list(lqw);
        if (list != null) {
            for (Dish dish : list) {
                //要先判断该菜品是否为停售状态,否则无法删除并且抛出异常处理
                if (dish.getStatus() == 0){
                    //从dish_flavor表中删除
                    LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
                    flavorService.remove(lambdaQueryWrapper);
                    //从dish表中删除
                    dishService.removeByIds(ids);
                    //停售/起售后，清除对应的缓存
                    Long categoryId = dish.getCategoryId();
                    Set keys = redisTemplate.keys("dish_" + categoryId + "_1");
                    redisTemplate.delete(keys);
                    return;
                }else{
                 throw new CustomException("有菜品正在售卖，无法全部删除");
                }

            }
        }



    }

    /**
     * 根据id查询菜品的信息，可能有多个，采用List集合
     * 后台：需要返回前端菜品信息即可
     * 手机端：需要返回菜品信息和口味信息
     *
     * 使用redis缓存数据
     * 一个分类缓存一个
     * @param dish
     * @return
     */
    @Override
    public List<DishDto> listById(Dish dish) {
        List<DishDto> dishDtoList = new ArrayList<>();
        //设置key
        String key = "dish_"+ dish.getCategoryId() + "_" +dish.getStatus();//dish_1234566789_1

        //从redis缓存中获取数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，则无需查询数据库，直接返回
        if (dishDtoList != null){
            return dishDtoList;
        }

        //若不存在，从数据库中查询完成后，将其存放到redis缓存中
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        //根据前端请求的categoryid查询菜品的信息
        lqw.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //起售状态才查询出来
        lqw.eq(Dish::getStatus,1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lqw);
        List<DishDto> dishDtoListElse = new ArrayList<>();
        for (Dish dishes : list) {
            //获取dish表中的id
            Long id = dishes.getId();
            //根据dish的id去口味表查询口味的信息
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
            List<DishFlavor> dishFlavors = flavorService.list(lambdaQueryWrapper);
            //创建新的DishDto来存放dishFlavors
            DishDto dishDto = new DishDto();
            dishDto.setFlavors(dishFlavors);
            //DishDto目前还缺菜品信息，使用拷贝
            BeanUtils.copyProperties(dishes,dishDto);
            //这里不能再使用最初那个dishDtoList了，因为他为空（null）
            dishDtoListElse.add(dishDto);
        }
        //没有在缓存中查询到，将数据库中查询的数据存放到redis中，并设置有效时间60分钟
        redisTemplate.opsForValue().set(key,dishDtoListElse,60, TimeUnit.MINUTES);
        return dishDtoListElse;
    }

    /**
     * 分页，前端需要返回菜品数据和菜品分类名字
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<DishDto> pageAll(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        //DishDto含有前端需要的categoryName属性，最终返回的也是DishDto
        Page<DishDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name),Dish::getName,name);//name != null        StringUtils.isNotEmpty(name)
        lqw.orderByDesc(Dish::getUpdateTime);
        //1.前端通过请求查询到页面所有数据，然后继续做处理
        dishService.page(pageInfo,lqw);

        //records属性是一个集合，它是页面显示的所有数据
        //分页对象的拷贝,其中records属性不进行拷贝,因为dishpage的erecords的泛型为dish,而dishDtoPage的泛型为dishDto
        //2.对象拷贝 将pageInfo的属性拷贝到dtoPage中，同时排除records属性不拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

       /* 3.获得records集合，由于页面的records的数据没有，需要将pageInfo中records的所有数据转移到dtoPage中，
        同时单独给categoryName赋值*/
        List<Dish> records = pageInfo.getRecords();
        //4.最后要把dtoPage（DishDto）中的records赋值，这里需要创建个集合来存放
        List<DishDto> dishDtoList = new ArrayList<>();
        /*
         * List<DishDto> dishDtoList = dtoPage.getRecords();
         * dishDtoList.add(dishDto);这里不能这么写，由于是protected属性，只能通过set方法给其赋值
         * */

        for (int i = 0; i < records.size(); i++) {
            //菜品对象的分类id，records.get(i)：遍历获得全部Dish对象
            //6.获得Dish对象中的菜品分类id属性
            Long categoryId = records.get(i).getCategoryId();
            //7.根据菜品分类id向category表中查询菜的名字（对应category表中的name属性）
            //8.根据categoryId查到Category中的所有信息（前端页面的“分类类型”需要返回categoryId字段）
            Category category = categoryService.getById(categoryId);

            //这个不能放外面，放外面就只能单独给一个对象赋值了，页面就只会展实同一个数据了
            //放里面遍历一次单独创建一个对象，分别给其赋值
            //9.创建DishDto对象并给其categoryName赋值
            DishDto dishDto = new DishDto();
            if (category != null) {
                //根据Category对象，获得它的name属性值
                String categoryName = category.getName();
                //将name属性赋值到dishDto中的categoryName
                dishDto.setCategoryName(categoryName);
            }
            //10.进行到这里，dishDto的categoryName才有值，其他都为空
            // 获得Dish中的每个对象
            Dish dish = records.get(i);
            //11.DishDto继承了Dish，这里需要将Dish中的所有值拷贝到DishDto
            BeanUtils.copyProperties(dish,dishDto);
            //12.遍历一次，分别将对象添加到集合中
            dishDtoList.add(dishDto);
        }

        /*//遍历dishPage中的records集合
        for (Dish dish : records.getRecords()) {
            DishDto dishDto = new DishDto();

            //将父类对象拷贝给子类对象
            BeanUtils.copyProperties(dish, dishDto);

            //根据dishDTO中的CategoryId(继承父类dish的属性)的id查询关联的实体类category(菜品分类)
            Category category = categoryService.getById(dishDto.getCategoryId());
            //获取菜品分类的name并且赋值
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //将拷贝过来的dishDto并且赋值了categoryName的对象放到dishDtoPage中的records中
            list.add(dishDto);
        }*/
        //13.将集合中的数据设置到dtoPage中
        dtoPage.setRecords(dishDtoList);
        return dtoPage;
    }
}
