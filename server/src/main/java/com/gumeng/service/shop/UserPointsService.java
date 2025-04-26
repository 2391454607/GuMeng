package com.gumeng.service.shop;

import com.gumeng.domain.shop.UserPoints;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_points(用户积分表)】的数据库操作Service
* @createDate 2025-04-26 23:23:52
*/
@Service
public interface UserPointsService extends IService<UserPoints> {


    UserPoints getUserPoints(Integer userId);
}
