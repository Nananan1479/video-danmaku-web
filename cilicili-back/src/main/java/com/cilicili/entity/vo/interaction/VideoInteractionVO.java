package com.cilicili.entity.vo.interaction;

import lombok.Data;

/**
 * 综合展示用户对该视频的互动状态(点赞投币收藏)
 */
@Data
public class VideoInteractionVO {
    private Boolean liked;          // 是否已点赞
    private Boolean collected;      // 是否已收藏
    private Integer coinCount;      // 当前用户已投币数量
    private Long likeTotal;         // 视频总点赞数
    private Long collectTotal;      // 视频总收藏数
    private Long coinTotal;         // 视频总投币数
}
