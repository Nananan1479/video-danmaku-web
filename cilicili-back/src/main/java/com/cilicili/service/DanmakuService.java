package com.cilicili.service;

import com.cilicili.entity.Danmaku;

import java.util.List;

public interface DanmakuService {
    Danmaku saveDanmaku(Danmaku danmaku);
    List<Danmaku> getDanmakuByVideoId(Long videoId);
}
