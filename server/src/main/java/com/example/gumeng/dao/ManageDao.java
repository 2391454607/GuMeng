package com.example.gumeng.dao;

import com.example.gumeng.entily.Manage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ManageDao {

    @Select("SELECT * FROM admin")
    List<Manage> findAll();

}
