package com.example.gumeng.service.impl;

import com.example.gumeng.dao.UserDao;
import com.example.gumeng.entily.User;
import com.example.gumeng.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public void insert(User user) {
        userDao.insert(user);
    }

    @Override
    public void update(User user) {
        userDao.update(user);
    }

    @Override
    public void delete(Integer[] ids) {
        for (Integer id : ids) {
            userDao.delete(id);
        }
    }

}
