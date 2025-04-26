package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.Order;
import com.gumeng.service.shop.OrderService;
import com.gumeng.mapper.shop.OrderMapper;
import org.springframework.stereotype.Service;

/**
* @author Chine
* @description 针对表【order(订单表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{

}




