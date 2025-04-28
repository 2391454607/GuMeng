package com.gumeng.mapper.shop;

import com.gumeng.domain.shop.UserBalance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Chine
* @description 针对表【user_balance(用户金额账户表)】的数据库操作Mapper
* @createDate 2025-04-26 23:23:52
* @Entity com.gumeng.domain.shop.UserBalance
*/
@Mapper
public interface UserBalanceMapper extends BaseMapper<UserBalance> {


    UserBalance getBalance(Integer userId);

    void addBalance(UserBalance userBalance);

    void updateBalance(UserBalance userBalance);
}




