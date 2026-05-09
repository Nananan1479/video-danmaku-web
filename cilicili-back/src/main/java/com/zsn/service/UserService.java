package com.zsn.service;

import com.zsn.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;


public interface UserService  {
    public User getUserByName(String name);
    public User getUserById(int id);
}
