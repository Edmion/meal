package com.meals.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.entity.AddressBook;
import com.meals.mapper.AddressBookMapper;
import com.meals.service.AddressBookService;
import org.springframework.stereotype.Service;



@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
