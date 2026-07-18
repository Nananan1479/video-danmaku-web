package com.cilicili.service;

import com.cilicili.common.Result;
import com.cilicili.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


public interface UserService  {
    User getUserById(long id);
    Result<User> login(String username, String password);
    Result<User> register(String username, String phone, String password);
    String uploadAvatar(MultipartFile file, long userId);
    ResponseEntity<Resource> getAvatar(String filename);
    Result<User> updateUser(long userId, String nickname, String signature, String email, String phone);
    /** 增加硬币 */
    int addCoin(long userId, BigDecimal amount);
    /** 减少硬币（含余额不足校验） */
    int subtractCoin(long userId, BigDecimal amount);
}
