package com.cilicili.controller;

import com.cilicili.common.Result;
import com.cilicili.entity.Danmaku;
import com.cilicili.entity.dto.DanmakuSendDTO;
import com.cilicili.service.DanmakuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/danmaku")
public class DanmakuController {

    @Autowired
    private DanmakuService danmakuService;

    @PostMapping("/send")
    public Result<Object> sendDanmaku(@RequestBody DanmakuSendDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId"); // 从JWT拦截器注入
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        Danmaku danmaku = new Danmaku();
        danmaku.setUserId(userId);
        danmaku.setVideoId(dto.getVideoId());
        danmaku.setContent(dto.getContent());
        danmaku.setPlayTime(dto.getPlayTime());
        danmaku.setColor(dto.getColor());
        danmaku.setMode(dto.getMode());
        danmaku.setFontSize(dto.getFontSize());
        danmakuService.saveDanmaku(danmaku);
        return Result.success(Result.RESULT_OK,danmaku);
    }

    @GetMapping("/{videoId}")
    public Result<List<Danmaku>> getDanmakuByVideoId(@PathVariable Long videoId) {
        List<Danmaku> list = danmakuService.getDanmakuByVideoId(videoId);
        return Result.success(Result.RESULT_OK, list);
    }
}
