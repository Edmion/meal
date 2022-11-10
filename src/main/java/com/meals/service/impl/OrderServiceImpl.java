package com.meals.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meals.common.BaseContext;
import com.meals.dto.OrdersDto;
import com.meals.entity.*;
import com.meals.exception.CustomException;
import com.meals.mapper.OrdersMapper;
import com.meals.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrdersMapper,Orders> implements OrdersService {
    /**
     * 用户下单
     *
     * @param orders
     */

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * 前端请求：{remark: "", payMethod: 1, addressBookId: "1589987987603619842"}
     *
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        //根据用户id查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lqw);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }
        //查询用户数据
        User user = userService.getById(userId);
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
            throw new CustomException("地址有误，不能下单");
        }
        //生成订单号
        long orderId = IdWorker.getId();//订单号

        //累加，多线程下是安全的
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart shoppingCart : shoppingCarts) {
            //创建orderDetail对象，依次赋值
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setAmount(shoppingCart.getAmount());
            //单价乘以份数，再转换成intValue，算完后，自动累加到外面的amount中
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            orderDetailList.add(orderDetail);
        }

        //向订单表orders中插入数据（一条数据）
        //依次给orders对象赋值
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        ordersService.save(orders);
        //向订单明细表setmeal_dish中插入数据（多条数据，一个商品一条）
        orderDetailService.saveBatch(orderDetailList);
        //下单完成后，清空购物车数据
        //根据用户id清空购物车数据
        shoppingCartService.remove(lqw);
    }

    /**
     * 历史订单
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<OrdersDto> pageAll(int page, int pageSize) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        Page<Orders> orderPage = new Page<>(page, pageSize);

        Page<OrdersDto> orderDetailPage = new Page<>();
        //根据用户id查询出订单信息（一条或多数据，用户可能点很多次）
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, userId);
        lqw.orderByDesc(Orders::getOrderTime);
        ordersService.page(orderPage, lqw);//page完成后自动赋值到orderPage中
        //拷贝
        BeanUtils.copyProperties(orderPage, orderDetailPage, "records");

        //从orderPage中获取records
        List<Orders> orderPageRecords = orderPage.getRecords();
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        for (Orders orderPageRecord : orderPageRecords) {
            //创建新的ordersDto用于存放
            OrdersDto ordersDto = new OrdersDto();
            //获取order_id,然后用它去order_detail中查询出数据（一条或多条）
            Long orderPageRecordId = orderPageRecord.getId();
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, orderPageRecordId);
            //查询出该订单对应的详细信息
            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
            //设置ordersDto对象的orderDetails属性（订单明细属性）
            ordersDto.setOrderDetails(orderDetails);
            //将Orders的全部信息(订单信息)拷贝进去
            BeanUtils.copyProperties(orderPageRecord, ordersDto);
            ordersDtoList.add(ordersDto);
        }
        //给orderDetailPage的records设置值
        orderDetailPage.setRecords(ordersDtoList);
        return orderDetailPage;
    }

    @Override
    public Page<OrdersDto> pageBackEnd(int page, int pageSize, String number, String beginTime, String endTime) {

        Page<Orders> orderPage = new Page<>(page, pageSize);

        Page<OrdersDto> orderDetailPage = new Page<>();

        //根据用户id查询出订单信息（一条或多数据，用户可能点很多次）
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.like(number != null, Orders::getNumber, number);
        lqw.orderByDesc(Orders::getOrderTime);
        lqw.gt(!StringUtils.isEmpty(beginTime), Orders::getOrderTime, beginTime)
                .lt(!StringUtils.isEmpty(endTime), Orders::getOrderTime, endTime);
        ordersService.page(orderPage, lqw);//page完成后自动赋值到orderPage中
        //拷贝
        BeanUtils.copyProperties(orderPage, orderDetailPage, "records");

        //从orderPage中获取records
        List<Orders> orderPageRecords = orderPage.getRecords();

        List<OrdersDto> ordersDtoList = new ArrayList<>();

        for (Orders orderPageRecord : orderPageRecords) {
            //创建新的ordersDto用于存放
            OrdersDto ordersDto = new OrdersDto();
            //获取order_id,然后用它去order_detail中查询出数据（一条或多条）
            Long orderPageRecordId = orderPageRecord.getId();
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, orderPageRecordId);
            //查询出该订单对应的详细信息
            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
            //设置ordersDto对象的orderDetails属性（订单明细属性）
            ordersDto.setOrderDetails(orderDetails);
            //将Orders的全部信息(订单信息)拷贝进去
            BeanUtils.copyProperties(orderPageRecord, ordersDto);
            ordersDtoList.add(ordersDto);
        }
        //给orderDetailPage的records设置值
        orderDetailPage.setRecords(ordersDtoList);
        return orderDetailPage;
    }

    /**
     * 再来一单
     * 点击再来一单，会直接将当前订单的数据添加到购物车，并且自动跳转到购物车详情页面
     *
     * @param orders
     */
    @Override
    public void again(Orders orders) {
        Long id = orders.getId();
        //根据order的id，从order_detail表中查询出订单详情（一条/多条）
        LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(id != null, OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailService.list(lqw);
        //创建个购物车对象，将订单详情信息填充进去
        List<ShoppingCart> shoppingCarts = new ArrayList<>();

        //生成新的购物车订单id
        long shoppingId = IdWorker.getId();//订单号
        //获取用户id，待会需要set操作
        Long userId = BaseContext.getCurrentId();
        if (orderDetailList != null) {
            for (OrderDetail orderDetail : orderDetailList) {
                ShoppingCart shoppingCart = new ShoppingCart();
                //Copy对应属性值
                BeanUtils.copyProperties(orderDetail, shoppingCart);
                //设置一下购物车id
                shoppingCart.setId(shoppingId);
                //设置一下userId
                shoppingCart.setUserId(userId);
                //设置一下创建时间为当前时间
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCarts.add(shoppingCart);
                //shoppingCartService.add(shoppingCart);
            }
            shoppingCartService.saveBatch(shoppingCarts);

        }

    }
}
