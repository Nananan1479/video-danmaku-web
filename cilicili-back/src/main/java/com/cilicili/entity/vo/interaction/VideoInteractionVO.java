package com.cilicili.entity.vo.interaction;

import lombok.Data;

/**
 * 综合展示用户对该视频的互动状态(点赞投币收藏)
 */
@Data
public class VideoInteractionVO {
    private Boolean liked;
    private Boolean coined;
    private Boolean collected;
    private Long likeCount;
    private Long coinCount;
    private Long collectCount;
    private String message;
}
