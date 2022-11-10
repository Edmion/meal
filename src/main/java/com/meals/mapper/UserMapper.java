package com.meals.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meals.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
