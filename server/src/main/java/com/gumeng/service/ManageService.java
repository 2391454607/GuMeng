package com.gumeng.service;

import com.gumeng.entity.Manage;

import java.util.List;

public interface ManageService {
    //根据 name 查询管理员
    Manage findByAdminName(String name);

    List<Manage> findAll();
}
