package com.cilicili.entity.vo;

import lombok.Data;

@Data
public class UserVO {
    private int id;
    private String username;
//    private String password;
    private String nickname;
    //    头像URL
    private String avatar;
    private String signature;
    private String email;
//    private String phone;
    private byte status;
    private String created_at;
//    private String updated_at;
}
