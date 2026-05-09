package com.zsn.controller;

import com.zsn.entity.User;
import com.zsn.mapper.UserMapper;
import com.zsn.service.UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin //跨域
@MapperScan("com.zsn.mapper")
@RestController
@RequestMapping("/api/users/")
public class UserController {
    @Autowired
    private UserService userService;
    private UserMapper userMapper;

    @PostMapping("getById/{id}")
    public User getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

}
