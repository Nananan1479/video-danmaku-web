package com.cilicili.service;

import com.cilicili.common.Result;

import java.util.Map;

public interface InteractionService {
    boolean toggleLike(Long userId, Long videoId);
    Result<Object> coin(Long userId, Long videoId, Byte num);
    boolean toggleCollect(Long userId, Long videoId);
    Map<String, Object> getInteractionStatus(Long userId, Long videoId);
}
