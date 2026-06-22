package com.cilicili.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cilicili.common.Result;
import com.cilicili.entity.Video;
import com.cilicili.entity.vo.VideoVO;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.service.admin.AdminVideoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminVideoServiceImpl implements AdminVideoService {

    @Value("${file.video-dir}")
    private String VIDEO_DIR;

    @Value("${file.cover-dir}")
    private String COVER_DIR;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public List<VideoVO> getVideoList(Integer status) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .orderByDesc(Video::getCreatedAt);
        if (status != null) {
            wrapper.eq(Video::getStatus, status);
        }
        List<Video> videos = videoMapper.selectList(wrapper);
        return videos.stream().map(v -> {
            VideoVO vo = new VideoVO();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Result<?> updateVideoStatus(Long videoId, int status) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return Result.fail(404, "视频不存在");
        }
        video.setStatus(status);
        videoMapper.updateById(video);
        String msg;
        if (status == 1) msg = "已通过审核";
        else if (status == 0) msg = "已下架";
        else msg = "状态已更新";
        return Result.success(200, msg);
    }

    @Override
    public Result<?> deleteVideo(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return Result.fail(404, "视频不存在");
        }
        // 删除磁盘文件（数据库只存文件名，用配置目录拼接）
        try {
            if (video.getVideoUrl() != null) {
                new java.io.File(VIDEO_DIR, video.getVideoUrl()).delete();
            }
            if (video.getCoverUrl() != null) {
                new java.io.File(COVER_DIR, video.getCoverUrl()).delete();
            }
        } catch (Exception ignored) {}
        videoMapper.deleteById(videoId);
        return Result.success(200, "视频已删除");
    }
}
