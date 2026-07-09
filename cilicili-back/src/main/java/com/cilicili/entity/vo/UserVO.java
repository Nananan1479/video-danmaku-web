package com.cilicili.entity.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String signature;
    private String email;
    private String phone;
    private Integer coin;
    private Byte status;
    private Byte role;
    private String created_at;
}
