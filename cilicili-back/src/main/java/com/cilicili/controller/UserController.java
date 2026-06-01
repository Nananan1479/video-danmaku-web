package com.cilicili.controller;

import com.cilicili.common.Result;
import com.cilicili.entity.User;
import com.cilicili.entity.vo.UserVO;
import com.cilicili.service.UserService;
import com.cilicili.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

// @CrossOrigin //跨域
//@MapperScan("com.cilicili.mapper")
@RestController
@RequestMapping("/api/users/")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("getById/{id}")
    public UserVO getUserById(@PathVariable long id) {
        UserVO userVO = new UserVO();
        // 将User类里的值传给UserVO
        BeanUtils.copyProperties(userService.getUserById(id),userVO);
        return userVO;
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

    @GetMapping("check")
    public Result<Object> checkToken(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        System.out.println("当前登录的userId（by-checkToken()）:" + userId);
        if (userId == null) {
            return Result.fail(401, "token无效");
        }
        return Result.success(200, "ok");
    }

    @PostMapping("avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        if (file.isEmpty()) {
            return Result.fail(400, "头像文件不能为空");
        }
        try {
            String avatarName = userService.uploadAvatar(file, userId);
            return Result.success(200, avatarName);
        } catch (RuntimeException e) {
            return Result.fail(500, "头像上传失败: " + e.getMessage());
        }
    }

    @GetMapping("avatar/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        return userService.getAvatar(filename);
    }
}
