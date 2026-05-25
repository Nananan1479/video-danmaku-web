package com.cilicili.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cilicili.common.Result;
import com.cilicili.entity.User;
import com.cilicili.mapper.UserMapper;
import com.cilicili.service.UserService;
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

    /**
     * 用户登录。<br>
     * 返回查找后的用户资料，检查用户名是否存在，使用BCrypt解码进行密码比对
     *

     * @param username
     * @param password
     *
     * @author Nananan1479
     * @date 2026/5/25 13:39
     * @return com.cilicili.common.Result<com.cilicili.entity.User>
     */
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

    /**
     * 用户注册
     *

     * @param username
     * @param phone
     * @param password
     *
     * @author Nananan1479
     * @date 2026/5/25 13:44
     * @return com.cilicili.common.Result<com.cilicili.entity.User>
     */
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
