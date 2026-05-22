package com.zsn.controller;

import com.zsn.entity.Video;
import com.zsn.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class VideoController {

    @Autowired
    private VideoMapper videoMapper;

    // 按视频 ID 从数据库查出 video_url ，再把磁盘文件以流的形式返回给浏览器
    @GetMapping("/api/videos/{id}")
    public ResponseEntity<byte[]> getVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        Video video = videoMapper.selectById(id);
        if (video == null || video.getVideoUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(video.getVideoUrl());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 读取磁盘
            long fileSize = Files.size(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            if (rangeHeader == null) {
                byte[] data = Files.readAllBytes(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileSize)
                        .body(data);
            }

            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;
            if (end >= fileSize) {
                end = fileSize - 1;
            }

            long contentLength = end - start + 1;

            byte[] data = new byte[(int) contentLength];
            try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
                raf.seek(start);
                raf.readFully(data);
            }

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .contentLength(contentLength)
                    .body(data);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 返回标题、时长、播放量等元数据
    @GetMapping("/api/videos/{id}/info")
    public ResponseEntity<Map<String, Object>> getVideoInfo(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> info = new HashMap<>();
        info.put("id", video.getId());
        info.put("title", video.getTitle());
        info.put("duration", video.getDuration());
        info.put("playCount", video.getPlayCount());

        return ResponseEntity.ok(info);
    }
}
