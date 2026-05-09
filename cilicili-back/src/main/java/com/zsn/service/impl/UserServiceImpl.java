package com.zsn.service.impl;

import com.zsn.entity.User;
import com.zsn.mapper.UserMapper;
import com.zsn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserByName(String name) {
        return null;
    }

    @Override
    public User getUserById(int id) {
        return userMapper.selectById(id);
    }
}
