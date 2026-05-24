package com.zsn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsn.common.Result;
import com.zsn.entity.User;
import com.zsn.mapper.UserMapper;
import com.zsn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
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
            return Result.fail(400,"用户名或密码错误");
        }
//         3. 核对密码（使用 BCrypt 加密比对）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail(400,"用户名或密码错误");
        }
//        if (!(Objects.equals(password, user.getUsername()))) {
//            return Result.fail(400,"用户名或密码错误");
////            throw new RuntimeException("密码错误");
//        }

        return Result.success(200,user);
    }

    @Override
    public Result<User> register(String username, String phone, String password) {
//        User existUser = userMapper.selectOne(
//                new LambdaQueryWrapper<User>()
//                        .eq(User::getUsername, username)
//        );

        User existUserPhone = userMapper.selectByUserPhone(phone);
        if (existUserPhone != null) {
            return Result.fail(400, "手机号已被使用");
        }

        User existUsername = userMapper.selectByUsername(username);
        if (existUsername != null) {
            return Result.fail(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setNickname(username);
        user.setStatus((byte) 1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        user.setCreated_at(now);
        user.setUpdated_at(now);

        userMapper.insert(user);
        user.setPassword(null);
        return Result.success(200, user);
    }


}
