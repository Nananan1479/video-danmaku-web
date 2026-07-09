package com.cilicili.entity.dto.interaction;

import lombok.Data;

/**
 * 点赞，收藏的数据传输对象，userId由JWT提供
 *
 */
@Data
public class InteractionDTO {
    private Long videoId;
    // userId 由后端从 JWT 中获取，前端不需传递
}
