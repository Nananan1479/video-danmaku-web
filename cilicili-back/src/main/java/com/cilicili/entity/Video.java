package com.cilicili.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("video")
public class Video {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private Integer duration;
    private Integer status;
    private Long playCount;
    private Long danmakuCount;
    private Long commentCount;
    private Long likeCount;
    private Long coinCount;
    private Long collectCount;
    private Long shareCount;
    private Long uploaderId;
    private String createdAt;
    private String updatedAt;
}
