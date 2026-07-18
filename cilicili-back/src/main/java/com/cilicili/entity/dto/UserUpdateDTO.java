package com.cilicili.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户信息更新请求 DTO
 */
@Data
public class UserUpdateDTO {
    private String nickname;
    private String signature;
    private String email;
    private String phone;
    private BigDecimal coin;
}
