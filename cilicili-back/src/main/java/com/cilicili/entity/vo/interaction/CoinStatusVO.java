package com.cilicili.entity.vo.interaction;

import lombok.Data;

@Data
public class CoinStatusVO {
    private Integer coinCount;      // 当前用户投币数
    private Long coinTotal;         // 总投币数
}
