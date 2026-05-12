package com.zsn.service;

import com.zsn.common.Result;
import com.zsn.entity.User;


public interface UserService  {
    public User getUserById(int id);
    public Result<User> login(String username, String password);
    public Result<User> register(String username, String password);
}
