package com.cilicili.service;

import com.cilicili.entity.Video;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {

    ResponseEntity<byte[]> getVideo(Long id, String rangeHeader);

    Video uploadVideo(MultipartFile videoFile, MultipartFile coverFile, String title, String description, Long uploaderId);


}
