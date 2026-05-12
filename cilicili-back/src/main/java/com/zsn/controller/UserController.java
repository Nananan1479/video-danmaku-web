package com.zsn.controller;

import com.zsn.common.Result;
import com.zsn.entity.User;
import com.zsn.service.UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin //跨域
@MapperScan("com.zsn.mapper")
@RestController
@RequestMapping("/api/users/")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("getById/{id}")
    public User getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PostMapping("login")
    public Result<User> login(@RequestBody Map<String, String> userMap) {
        Result<User> result = userService.login(userMap.get("username"), userMap.get("password"));
        if (result.getCode() == 200) {
            result.getData().setPassword(null);
            return result;
        }
        return result;
    }

}
