package com.cilicili.service;

import com.cilicili.common.Result;
import com.cilicili.entity.User;


public interface UserService  {
    public User getUserById(int id);
    public Result<User> login(String username, String password);
    public Result<User> register(String username, String phone, String password);
//    public Result<Boolean> selectByUsername();
}
