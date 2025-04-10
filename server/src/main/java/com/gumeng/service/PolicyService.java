package com.gumeng.service;

import com.gumeng.domain.Policy;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【policy(非遗政策表)】的数据库操作Service
* @createDate 2025-04-11 08:25:04
*/
@Service
public interface PolicyService extends IService<Policy> {

    //获取政策列表
    List<Policy> getPolicyList();
    //根据id查询政策
    Policy findById(Integer id);
    //添加
    void addPolicy(Policy policy);
    //修改
    void updatePolicy(Policy policy);
    //删除
    void deletePolicy(Integer id);

}
