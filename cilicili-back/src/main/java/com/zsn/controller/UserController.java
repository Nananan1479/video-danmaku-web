package com.zsn.controller;

import com.zsn.common.Result;
import com.zsn.entity.User;
import com.zsn.service.UserService;
import com.zsn.util.JwtUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@CrossOrigin //跨域
@MapperScan("com.zsn.mapper")
@RestController
@RequestMapping("/api/users/")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

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
            User user = result.getData();
            user.setPassword(null);
            user.setPhone(null);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            result.setToken(token);
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
            return Result.fail(400, "手机号格式不正确");
        }

        if (username == null || password == null) {
            return Result.fail(400, "注册信息不完整");
        }
        Result<User> result = userService.register(username, phone, password);
        if (result.getCode() == 200) {
            User user = result.getData();
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            result.setToken(token);
        }
        return result;
    }

    @PostMapping("currentUser")
    public Result<User> currentUser(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
//        System.out.println("request"+request);
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(200, user);
    }

}
