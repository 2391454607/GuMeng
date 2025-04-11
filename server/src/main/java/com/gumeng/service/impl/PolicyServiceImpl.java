package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.Policy;
import com.gumeng.entity.vo.PolicyVO;
import com.gumeng.service.PolicyService;
import com.gumeng.mapper.PolicyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【policy(非遗政策表)】的数据库操作Service实现
* @createDate 2025-04-11 08:25:04
*/
@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy>
    implements PolicyService{

    @Autowired
    private PolicyMapper policyMapper;

    @Override
    public List<Policy> getPolicyList() {
        return policyMapper.getPolicyList();
    }

    @Override
    public Policy findById(Integer id) {
        return policyMapper.findById(id);
    }

    @Override
    public void addPolicy(Policy policy) {
        policyMapper.addPolicy(policy);
    }

    @Override
    public void updatePolicy(Policy policy) {
        policyMapper.updatePolicy(policy);
    }

    @Override
    public void deletePolicy(Integer id) {
        policyMapper.deletePolicy(id);
    }
}




