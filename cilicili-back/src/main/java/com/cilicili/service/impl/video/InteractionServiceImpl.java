package com.cilicili.service.impl.video;

import com.cilicili.mapper.interaction.VideoCoinMapper;
import com.cilicili.mapper.interaction.VideoCollectMapper;
import com.cilicili.mapper.interaction.VideoLikeMapper;
import com.cilicili.service.InteractionService;
import com.cilicili.util.RedisUtil;
import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class InteractionServiceImpl implements InteractionService {

    @Autowired
    private VideoLikeMapper videoLikeMapper;
    @Autowired
    private VideoCoinMapper videoCoinMapper;
    @Autowired
    private VideoCollectMapper videoCollectMapper;
    @Autowired
    private RedisUtil redisUtil;

    // ---------- 常量定义（统一管理 Key 前缀，防止手滑写错） ----------
    // 给这个视频点赞的个数（视频点赞总数key）(like:count:
    private static final String LIKE_COUNT_PREFIX = "like:count:";
    // 谁给这个视频点赞key（like:users:VIDEOID "users:USERSID")
    private static final String LIKE_USERS_PREFIX = "like:users:";
    private static final String COIN_COUNT_PREFIX = "coin:count:";
    private static final String COIN_USERS_PREFIX = "coin:users:";
    private static final String FAV_COUNT_PREFIX = "fav:count:";
    private static final String FAV_USERS_PREFIX = "fav:users:";

    // 网络错误时的最大重试次数
    private static final int MAX_RETRIES = 3;
    // 网络错误时的重试间隔时间（ms）
    private static final int RETRY_COUNT = 50;


    /**
     * 点赞 / 是否点赞
     *
     * @param userId 当前用户ID
     * @param videoId 视频Id
     *

     * @return boolean
     */
    @Override
    public boolean toggleLike(Long userId, Long videoId) throws InterruptedException {
        String countKey = LIKE_COUNT_PREFIX + videoId;
        String usersKey = LIKE_USERS_PREFIX + videoId;

        // 查询用户是否有点赞视频
        Boolean isLiked = checkLikeStatus(usersKey, userId);

        // 处理因为网络波动而导致isLiked为null的情况
        if (isLiked == null) {
            throw new RuntimeException("网络繁忙，请稍后重试，");
        }

        if (Boolean.TRUE.equals(isLiked)) {
            // 如果已点赞
            redisUtil.delMember(usersKey, userId);
            redisUtil.decr(countKey);
            return false;
        } else  {
            // 如果没点赞
            redisUtil.addMember(usersKey, userId);
            redisUtil.incr(countKey);
            return true;
        }
    }


    @Override
    public boolean coin(Long userId, Long videoId, Byte num) {
        String countKey = COIN_COUNT_PREFIX + videoId;
        String usersKey = COIN_USERS_PREFIX + videoId;

        Boolean isCoined = checkLikeStatus(usersKey, userId);

        if (isCoined == null) {
            throw new RuntimeException("网络繁忙，请稍后重试，");
        }

        if (Boolean.TRUE.equals(isCoined)) {
            return false;
        } else {
            redisUtil.addMember(usersKey, userId);
            if (num == 1) {
                redisUtil.incr(countKey);
                return true;
            } else {
                redisUtil.incr(countKey);
                redisUtil.incr(countKey);
                return true;
            }
        }
    }

    @Override
    public boolean toggleCollect(Long userId, Long videoId) {
        String countKey = LIKE_COUNT_PREFIX + videoId;
        String usersKey = LIKE_USERS_PREFIX + videoId;

        Boolean isCollected = checkLikeStatus(usersKey, userId);

        if (isCollected == null) {
            throw new RuntimeException("网络繁忙，请稍后重试，");
        }

        if (Boolean.TRUE.equals(isCollected)) {
            redisUtil.delMember(usersKey, userId);
            redisUtil.decr(countKey);
            return false;
        } else  {
            redisUtil.addMember(usersKey, userId);
            redisUtil.incr(countKey);
            return true;
        }
    }

    /**
     * 使用 @Retryable 注释处理当网络波动导致值为null时自动重试（重试3次）
     *
     *
     * @param usersKey
     * @param userId
     *
     *
     * @return java.lang.Boolean
     */
    @Retryable(value = {RedisException.class}, maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = RETRY_COUNT))
    private Boolean checkLikeStatus(String usersKey, Long userId) {
        return redisUtil.isMember(usersKey, userId);
    }
}
