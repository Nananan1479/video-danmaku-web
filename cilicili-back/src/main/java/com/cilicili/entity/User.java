package com.cilicili.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("user")
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;      // 头像URL
    private String signature;   // 签名
    private String email;
    private String phone;
    private BigDecimal coin;
    private Byte status;
    private Byte role;
    private String created_at;
    private String updated_at;
}
