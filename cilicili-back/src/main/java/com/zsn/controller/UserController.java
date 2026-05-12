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
            // 通常这里生成 JWT 并返回，这里只返回用户信息（去掉密码）
        if (result.getCode() == 200){
            result.getData().setPassword(null);
        }// TODO 未完成

        return Result.success(user);

    }

}
