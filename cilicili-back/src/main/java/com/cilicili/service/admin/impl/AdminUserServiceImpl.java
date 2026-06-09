package com.cilicili.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cilicili.common.Result;
import com.cilicili.entity.User;
import com.cilicili.entity.vo.UserVO;
import com.cilicili.mapper.UserMapper;
import com.cilicili.service.admin.AdminUserService;
import com.cilicili.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录：验证用户名密码 + 检查 role=1
     */
    @Override
    public Result<?> login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        if (user == null) {
            return Result.fail(400, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail(400, "用户名或密码错误");
        }
        if (user.getRole() == null || user.getRole() != 1) {
            return Result.fail(403, "非管理员账号，无法登录后台");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.fail(403, "账号已被封禁");
        }

        // 清除敏感信息
        user.setPassword(null);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Result<User> result = Result.success(200, user);
        result.setToken(token);
        return result;
    }

    @Override
    public List<UserVO> getUserList() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .orderByDesc(User::getCreated_at)
        );
        return users.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 更新用户状态
     * status: 0=封禁，1=正常，2=申请注销
     */
    @Override
    public Result<?> updateUserStatus(Long userId, int status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        user.setStatus((byte) status);
        userMapper.updateById(user);
        String msg;
        switch (status) {
            case 0: msg = "已封禁"; break;
            case 1: msg = "已解封"; break;
            case 2: msg = "状态已更新"; break;
            default: msg = "操作成功";
        }
        return Result.success(200, msg);
    }

    /**
     * 处理用户注销申请（status=2）
     */
    @Override
    public Result<?> handleDeleteRequest(Long userId, boolean approve) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if (user.getStatus() == null || user.getStatus() != 2) {
            return Result.fail(400, "该用户未申请注销");
        }
        if (approve) {
            userMapper.deleteById(userId);
            return Result.success(200, "已注销用户账号");
        } else {
            user.setStatus((byte) 1);
            userMapper.updateById(user);
            return Result.success(200, "已拒绝注销申请");
        }
    }

    @Override
    public Result<?> deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        userMapper.deleteById(userId);
        return Result.success(200, "用户已删除");
    }
}
