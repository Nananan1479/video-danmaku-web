package com.cilicili.service.impl;

import com.cilicili.entity.Danmaku;
import com.cilicili.mapper.DanmakuMapper;
import com.cilicili.service.DanmakuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DanmakuServiceImpl implements DanmakuService {

    @Autowired
    private DanmakuMapper danmakuMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // 注入消息模板

    @Override
    public Danmaku saveDanmaku(Danmaku danmaku) {
        danmaku.setSendTime(LocalDateTime.now());
        danmakuMapper.insert(danmaku);

        // 保存后广播给所有订阅了该视频弹幕的用户
        // 目标路径：/topic/danmaku/{videoId}
        String destination = "/topic/danmaku/" + danmaku.getVideoId();
        messagingTemplate.convertAndSend(destination, danmaku);

        return danmaku;
    }
}
