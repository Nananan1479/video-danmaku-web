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
        if (userMap.get("username") == null || userMap.get("password") == null) {
            return Result.fail(400, "用户名或密码为空");
        }
        Result<User> result = userService.login(userMap.get("username"), userMap.get("password"));
        if (result.getCode() == 200) {
            result.getData().setPassword(null);
        }
        return result;
    }

    @PostMapping("register")
    public Result<User> register(@RequestBody Map<String, String> userMap) {
        String username = userMap.get("username");
        String phone = userMap.get("phone");
        String password = userMap.get("password");

        String phonePattern = "^1[3-9]\\d{9}$";
        if (!phone.matches(phonePattern)) {
            return Result.fail(400,"手机号格式不正确");
        }

        if (username == null || password == null) {
            return Result.fail(400, "注册信息不完整");
        }
        return userService.register(username, phone, password);
    }

}
