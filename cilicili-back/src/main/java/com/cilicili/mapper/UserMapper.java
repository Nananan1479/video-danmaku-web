package com.cilicili.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cilicili.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

//定义查询规则
@Mapper
public interface UserMapper extends BaseMapper<User> {
    public User selectByUsername(@Param("username") String name);
    public User selectByUserPhone(@Param("userPhone") String phone);
}
