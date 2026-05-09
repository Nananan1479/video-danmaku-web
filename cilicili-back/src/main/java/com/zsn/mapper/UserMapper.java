package com.zsn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsn.entity.User;
import org.apache.ibatis.annotations.Mapper;

//定义查询规则
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
