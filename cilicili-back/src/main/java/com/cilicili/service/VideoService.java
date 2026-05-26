package com.cilicili.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cilicili.entity.Video;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

public interface VideoService {

    ResponseEntity<byte[]> getVideo(Long id, String rangeHeader);

    Video uploadVideo(MultipartFile videoFile, MultipartFile coverFile, String title, String description, Long uploaderId);

    ResponseEntity<Resource> getCover(String filename);

    Page<Video> getHomeRecommendedVideos(int pageNum, int pageSize);

    Page<Video> getRelatedVideos(int pageNum, int pageSize, Long currentVideoId);

    public Page<Video> getAllVideos(int pageNum, int pageSize);
}
