package com.gumeng.mapper;

import com.gumeng.domain.Policy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.entity.vo.PolicyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【policy(非遗政策表)】的数据库操作Mapper
* @createDate 2025-04-11 08:25:04
* @Entity com.gumeng.domain.Policy
*/
@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {

    //根据id查询政策
    Policy findById(Integer id);

}




