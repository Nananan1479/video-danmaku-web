package com.cilicili.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cilicili.common.Result;
import com.cilicili.entity.Video;
import com.cilicili.entity.vo.VideoVO;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.service.admin.AdminVideoService;
import com.cilicili.util.OssUtil;
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

    @Value("${aliyun.oss.endpoint}")
    private String ENDPOINT;
    @Value("${aliyun.oss.accessKeyId}")
    private String ACCESS_KEY_ID;
    @Value("${aliyun.oss.accessKeySecret}")
    private String ACCESS_KEY_SECRET;
    @Value("${aliyun.oss.private-bucketName}")
    private String PRIVATE_BUCKET;
    @Value("${aliyun.oss.public-bucket}")
    private String PUBLIC_BUCKET;

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
        // 删除 OSS 上的视频和封面
        try {
            if (video.getVideoUrl() != null && !ENDPOINT.isEmpty()) {
                OssUtil.delete(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET,
                        PRIVATE_BUCKET, VIDEO_DIR + video.getVideoUrl());
            }
            if (video.getCoverUrl() != null && !ENDPOINT.isEmpty()) {
                OssUtil.delete(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET,
                        PUBLIC_BUCKET, COVER_DIR + video.getCoverUrl());
            }
            System.out.println("视频被删除: videoId= " + video.getId() + " ，videoUrl= " + video.getVideoUrl());
        } catch (Exception ignored) {}
        // 兜底：删除本地磁盘文件
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
