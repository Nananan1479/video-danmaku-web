package com.cilicili.controller;

import com.cilicili.common.Result;
import com.cilicili.entity.dto.interaction.CoinDTO;
import com.cilicili.entity.dto.interaction.InteractionDTO;
import com.cilicili.entity.vo.interaction.VideoInteractionVO;
import com.cilicili.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/videoInteraction")
public class VideoInteractionController {

    @Autowired
    private InteractionService interactionService;

    /** 点赞/取消点赞 */
    @PostMapping("/like")
    public Result<VideoInteractionVO> toggleLike(@RequestBody InteractionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        boolean liked = interactionService.toggleLike(userId, dto.getVideoId());
        Map<String, Object> status = interactionService.getInteractionStatus(userId, dto.getVideoId());
        return Result.success(200, buildVO(status, liked ? "点赞成功" : "已取消点赞"));
    }

    /** 投币 */
    @PostMapping("/coin")
    public Result<VideoInteractionVO> coin(@RequestBody CoinDTO body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        Long videoId = Long.valueOf(body.getVideoId().toString());
        Byte num = Byte.valueOf(body.getNum().toString());
        Result<Object> result = interactionService.coin(userId, videoId, num);
        if (result.getCode() != 200) return Result.fail(result.getCode(), result.getMessage());

        Map<String, Object> status = interactionService.getInteractionStatus(userId, videoId);
        return Result.success(200, buildVO(status, "投币成功"));
    }

    /** 收藏/取消收藏 */
    @PostMapping("/collect")
    public Result<VideoInteractionVO> toggleCollect(@RequestBody InteractionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        boolean collected = interactionService.toggleCollect(userId, dto.getVideoId());
        Map<String, Object> status = interactionService.getInteractionStatus(userId, dto.getVideoId());
        return Result.success(200, buildVO(status, collected ? "收藏成功" : "已取消收藏"));
    }

    /** 获取视频互动状态 */
    @GetMapping("/status")
    public Result<VideoInteractionVO> getStatus(@RequestParam Long videoId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        Map<String, Object> status = interactionService.getInteractionStatus(userId, videoId);
        return Result.success(200, buildVO(status, "ok"));
    }

    private VideoInteractionVO buildVO(Map<String, Object> status, String msg) {
        VideoInteractionVO vo = new VideoInteractionVO();
        vo.setLiked((Boolean) status.get("liked"));
        vo.setLikeCount((Long) status.get("likeCount"));
        vo.setCoined((Boolean) status.get("coined"));
        vo.setCoinCount((Long) status.get("coinCount"));
        vo.setCollected((Boolean) status.get("collected"));
        vo.setCollectCount((Long) status.get("collectCount"));
        vo.setMessage(msg);
        return vo;
    }
}
