package com.cilicili.controller;

import com.cilicili.common.Result;
import com.cilicili.entity.Danmaku;
import com.cilicili.entity.dto.DanmakuSendDTO;
import com.cilicili.service.DanmakuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
        return Result.success(Result.RESULT_OK,danmaku); // 返回完整弹幕对象（含生成id）
    }
}
