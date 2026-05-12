package com.zsn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsn.common.Result;
import com.zsn.entity.User;
import com.zsn.mapper.UserMapper;
import com.zsn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    //BCrypt加密
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User getUserById(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public Result<User> login(String username, String password) {
        // 1. 按用户名查询
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        // 2. 用户不存在
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        // 3. 核对密码（使用 BCrypt 加密比对）
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            throw new RuntimeException("密码错误");
//        }
        if (!(Objects.equals(password, user.getUsername()))) {
            return Result.fail("用户名或密码错误");
//            throw new RuntimeException("密码错误");
        }

        return Result.success(user);
    }

    @Override
    public Result<User> register(String username, String password) {

        return null;
    }


}
