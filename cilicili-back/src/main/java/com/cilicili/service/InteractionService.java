package com.cilicili.service;

public interface InteractionService {
    boolean toggleLike(Long userId, Long videoId) throws InterruptedException;
    boolean coin(Long userId, Long videoId, Byte num);
    boolean toggleCollect(Long userId, Long videoId);
}
