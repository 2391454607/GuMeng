package com.example.gumeng.service;

import com.example.gumeng.entily.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    void insert(User user);

    void update(User user);

    void delete(Integer[] ids);
}
