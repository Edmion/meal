package com.meals.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meals.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
