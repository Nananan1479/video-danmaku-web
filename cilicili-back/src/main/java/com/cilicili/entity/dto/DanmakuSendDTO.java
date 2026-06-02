package com.cilicili.entity.dto;

import lombok.Data;

@Data
public class DanmakuSendDTO {
    private Long videoId;
    private String content;
    private Integer playTime;  // 秒
    private String color = "#FFFFFF";
    private Integer mode = 1;
    private Integer fontSize = 16;
}
