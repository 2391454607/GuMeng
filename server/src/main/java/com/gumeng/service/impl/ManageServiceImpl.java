package com.gumeng.service.impl;

import com.gumeng.mapper.ManageMapper;
import com.gumeng.entily.Manage;
import com.gumeng.service.ManageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/5 下午4:31
 */
@Service
public class ManageServiceImpl implements ManageService{
    @Resource
    private ManageMapper manageMapper;

    @Override
    public List<Manage> findAll() {
        return manageMapper.findAll();
    }
}
