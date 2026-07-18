package com.cilicili.service.impl.video;

import com.cilicili.common.Result;
import com.cilicili.entity.User;
import com.cilicili.entity.Video;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.mapper.interaction.VideoCoinMapper;
import com.cilicili.mapper.interaction.VideoCollectMapper;
import com.cilicili.mapper.interaction.VideoLikeMapper;
import com.cilicili.service.InteractionService;
import com.cilicili.service.UserService;
import com.cilicili.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 点赞 / 投币 / 收藏 —— 旁路缓存（Cache-Aside）模式
 * 写：DB 先写 → 再同步 Redis
 * 读：Redis 先读 → miss 则查 DB 回填
 */
@Service
@Slf4j
public class InteractionServiceImpl implements InteractionService {

    @Autowired private VideoLikeMapper videoLikeMapper;
    @Autowired private VideoCoinMapper videoCoinMapper;
    @Autowired private VideoCollectMapper videoCollectMapper;
    @Autowired private VideoMapper videoMapper;
    @Autowired private RedisUtil redisUtil;
    @Autowired private UserService userService;

    // ---------- Key 前缀 ----------
    private static final String LIKE_COUNT_KEY  = "like:count:";   // + videoId → Long
    private static final String LIKE_USER_KEY   = "like:user:";    // + videoId:userId → "1"
    private static final String COIN_COUNT_KEY  = "coin:count:";   // + videoId → Long
    private static final String COIN_USER_KEY   = "coin:user:";    // + videoId:userId → "1"
    private static final String COLLECT_COUNT_KEY = "fav:count:";  // + videoId → Long
    private static final String COLLECT_USER_KEY  = "fav:user:";   // + videoId:userId → "1"

    // ==================== 点赞 ====================
    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long videoId) {
        boolean exists = videoLikeMapper.existsByVideoIdAndUserId(videoId, userId);

        if (exists) {
            // ① 删 DB
            videoLikeMapper.deleteByVideoIdAndUserId(videoId, userId);
            // ② 更新 Redis：总数-1，清除用户状态
            redisUtil.decr(LIKE_COUNT_KEY + videoId);
            redisUtil.delValue(LIKE_USER_KEY + videoId + ":" + userId);
            // ③ 更新 video 表计数
            updateVideoLikeCount(videoId);
            return false;
        } else {
            // ① 写 DB
            videoLikeMapper.insertLike(videoId, userId);
            // ② 更新 Redis：总数+1，标记已点赞
            redisUtil.incr(LIKE_COUNT_KEY + videoId);
            redisUtil.setValue(LIKE_USER_KEY + videoId + ":" + userId, "1");
            // ③ 更新 video 表计数
            updateVideoLikeCount(videoId);
            return true;
        }
    }

    // ==================== 投币 ====================
    @Override
    @Transactional
    public Result<Object> coin(Long userId, Long videoId, Byte num) {
        if (num == null || num < 1 || num > 2) {
            log.error("用户{}投币{}个，数量异常", userId, num);
            return Result.fail(Result.RESULT_BAD_REQUEST, "投币数量只能是1或2");
        }

        // 检查余额
        User user = userService.getUserById(userId);
        if (user.getCoin() == null || user.getCoin().compareTo(new BigDecimal(num)) < 0) {
            return Result.fail(Result.RESULT_CONDITION_NOT_MET, "硬币不足");
        }

        // 检查是否已投过币
        String userCoinKey = COIN_USER_KEY + videoId + ":" + userId;
        Object cached = redisUtil.getValue(userCoinKey);
        if ("1".equals(cached) || videoCoinMapper.existsByVideoIdAndUserId(videoId, userId)) {
            return Result.fail(Result.RESULT_CANNOT_REPEAT_OPERATION, "已投过币，请勿重复投币");
        }

        // ① 写 DB：投币记录
        videoCoinMapper.insertCoin(videoId, userId, num.intValue());
        // ② 扣减用户硬币（原子更新 DB）
        userService.subtractCoin(userId, new BigDecimal(num.intValue()));
        // ③ 更新 Redis：投币总数 +num，标记已投
        for (int i = 0; i < num; i++) redisUtil.incr(COIN_COUNT_KEY + videoId);
        redisUtil.setValue(userCoinKey, "1");
        // ④ 更新 video 表计数
        updateVideoCoinCount(videoId);

        log.info("用户{}对视频{}投币{}枚", userId, videoId, num);
        return Result.success(200, "投币成功");
    }

    // ==================== 收藏 ====================
    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long videoId) {
        boolean exists = videoCollectMapper.existsByVideoIdAndUserId(videoId, userId);

        if (exists) {
            // ① 删 DB
            videoCollectMapper.deleteByVideoIdAndUserId(videoId, userId);
            // ② 更新 Redis
            redisUtil.decr(COLLECT_COUNT_KEY + videoId);
            redisUtil.delValue(COLLECT_USER_KEY + videoId + ":" + userId);
            // ③ 更新 video 表
            updateVideoCollectCount(videoId);
            return false;
        } else {
            // ① 写 DB
            videoCollectMapper.insertCollect(videoId, userId);
            // ② 更新 Redis
            redisUtil.incr(COLLECT_COUNT_KEY + videoId);
            redisUtil.setValue(COLLECT_USER_KEY + videoId + ":" + userId, "1");
            // ③ 更新 video 表
            updateVideoCollectCount(videoId);
            return true;
        }
    }

    // ==================== 获取互动状态 ====================
    @Override
    public Map<String, Object> getInteractionStatus(Long userId, Long videoId) {
        // 先从 Redis 读
        String likeKey = LIKE_USER_KEY + videoId + ":" + userId;
        String coinKey = COIN_USER_KEY + videoId + ":" + userId;
        String collectKey = COLLECT_USER_KEY + videoId + ":" + userId;

        Object likeCache = redisUtil.getValue(likeKey);
        Object coinCache = redisUtil.getValue(coinKey);
        Object collectCache = redisUtil.getValue(collectKey);

        // Redis miss → 查 DB 回填
        boolean liked = "1".equals(likeCache) ? true :
                (likeCache == null && videoLikeMapper.existsByVideoIdAndUserId(videoId, userId));
        boolean coined = "1".equals(coinCache) ? true :
                (coinCache == null && videoCoinMapper.existsByVideoIdAndUserId(videoId, userId));
        boolean collected = "1".equals(collectCache) ? true :
                (collectCache == null && videoCollectMapper.existsByVideoIdAndUserId(videoId, userId));

        // 回填 Redis
        if (!"1".equals(likeCache) && liked) redisUtil.setValue(likeKey, "1");
        if (!"1".equals(coinCache) && coined) redisUtil.setValue(coinKey, "1");
        if (!"1".equals(collectCache) && collected) redisUtil.setValue(collectKey, "1");

        // 获取计数（Redis → DB fallback）
        Long likeCount = getCount(LIKE_COUNT_KEY + videoId, videoLikeMapper.countByVideoId(videoId));
        Long coinCount = getCount(COIN_COUNT_KEY + videoId, (long) videoCoinMapper.sumCoinByVideoId(videoId));
        Long collectCount = getCount(COLLECT_COUNT_KEY + videoId, videoCollectMapper.countByVideoId(videoId));

        Map<String, Object> map = new HashMap<>();

        map.put("liked", liked);
        map.put("likeCount", likeCount);
        map.put("coined", coined);
        map.put("coinCount", coinCount);
        map.put("collected", collected);
        map.put("collectCount", collectCount);

        return map;
    }

    // ==================== 辅助方法 ====================
    private Long getCount(String redisKey, long dbCount) {
        Object val = redisUtil.getValue(redisKey);
        if (val != null) {
            try { return Long.parseLong(val.toString()); } catch (NumberFormatException ignored) {}
        }
        // miss → 回填
        redisUtil.setValue(redisKey, String.valueOf(dbCount));
        return dbCount;
    }

    private void updateVideoLikeCount(Long videoId) {
        Video v = videoMapper.selectById(videoId);
        if (v != null) {
            v.setLikeCount(videoLikeMapper.countByVideoId(videoId));
            videoMapper.updateById(v);
        }
    }
    private void updateVideoCoinCount(Long videoId) {
        Video v = videoMapper.selectById(videoId);
        if (v != null) {
            v.setCoinCount((long) videoCoinMapper.sumCoinByVideoId(videoId));
            videoMapper.updateById(v);
        }
    }
    private void updateVideoCollectCount(Long videoId) {
        Video v = videoMapper.selectById(videoId);
        if (v != null) {
            v.setCollectCount(videoCollectMapper.countByVideoId(videoId));
            videoMapper.updateById(v);
        }
    }
}
