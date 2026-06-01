package com.cilicili.service;

import com.cilicili.common.Result;
import com.cilicili.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


public interface UserService  {
    public User getUserById(long id);
    public Result<User> login(String username, String password);
    public Result<User> register(String username, String phone, String password);
    String uploadAvatar(MultipartFile file, int userId);
    ResponseEntity<Resource> getAvatar(String filename);
}
