package com.example.gumeng.service.impl;

import com.example.gumeng.dao.ManageDao;
import com.example.gumeng.entily.Manage;
import com.example.gumeng.service.ManageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/5 下午4:31
 */
@Service
public class ManageServiceImpl implements ManageService{
    @Resource
    private ManageDao manageDao;

    @Override
    public List<Manage> findAll() {
        return manageDao.findAll();
    }
}
