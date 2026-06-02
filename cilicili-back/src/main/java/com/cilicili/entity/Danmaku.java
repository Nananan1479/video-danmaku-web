package com.cilicili.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 弹幕实体类，映射数据库 danmaku 表
 */
@Data
@TableName("danmaku")
public class Danmaku {

    private Long id;

    private Long videoId;

    private Long userId;

    private String content;

    private Integer playTime;   // 弹幕在视频中出现的时间（秒）

    private String color;

    private Integer mode;       //播放方式 1:滚动 2:顶部 3:底部

    private Integer fontSize;

    private LocalDateTime sendTime;
}