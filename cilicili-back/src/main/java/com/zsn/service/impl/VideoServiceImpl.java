package com.zsn.service.impl;

import com.zsn.entity.Video;
import com.zsn.mapper.VideoMapper;
import com.zsn.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    private static final String VIDEO_DIR = "C:/Users/NANANAN/Videos/Captures";
    private static final String COVER_DIR = "C:/Users/NANANAN/Videos/Covers";

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public ResponseEntity<byte[]> getVideo(Long id, String rangeHeader) {
        Video video = videoMapper.selectById(id);
        // 未查询到返回 错误
        if (video == null || video.getVideoUrl() == null) {
            return ResponseEntity.notFound().build();
        }


        Path filePath = Paths.get(video.getVideoUrl());
        // 对应地址无视频返回错误
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 读取磁盘
            long fileSize = Files.size(filePath);
            // 探测文件的内容类型。
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            // 无 Range 头 → 返回整个视频
            if (rangeHeader == null) {
                byte[] data = Files.readAllBytes(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileSize)
                        .body(data);
            }

            // 有 Range 头 → 返回指定片段
            // 解析 Range 请求,提取起始和结束位置。
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = (ranges[0] != null && !ranges[0].isEmpty()) ? Long.parseLong(ranges[0]) : 0;
            long end = fileSize - 1;
            if (ranges.length > 1 && ranges[1] != null && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
            if (end >= fileSize) {
                end = fileSize - 1;
            }

            // 使用 RandomAccessFile 直接定位到文件的指定位置，只读取需要的那一段字节
            long contentLength = end - start + 1;

            byte[] data = new byte[(int) contentLength];
            try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
                raf.seek(start);
                raf.readFully(data);
            }

            // 返回 206(分段响应的标准状态码)
            // 设置 Content-Range 头,告知浏览器当前返回的是哪一段、总共多大。
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    // 例如 bytes 0-1048575/123456789
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .contentLength(contentLength)
                    .body(data);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public Video uploadVideo(MultipartFile videoFile, MultipartFile coverFile, String title, String description, Long uploaderId) {
        try {
            Files.createDirectories(Paths.get(VIDEO_DIR));
            Files.createDirectories(Paths.get(COVER_DIR));

            String videoExt = ".mp4";
            String videoName = UUID.randomUUID().toString() + videoExt;
            Path videoPath = Paths.get(VIDEO_DIR, videoName);
            videoFile.transferTo(videoPath.toFile());

            String coverName = null;
            if (coverFile != null && !coverFile.isEmpty()) {
                String originalName = coverFile.getOriginalFilename();
                String coverExt = originalName != null && originalName.contains(".") ?
                        originalName.substring(originalName.lastIndexOf(".")) : ".png";
                coverName = UUID.randomUUID().toString() + coverExt;
                Path coverPath = Paths.get(COVER_DIR, coverName);
                coverFile.transferTo(coverPath.toFile());
            }

            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setVideoUrl(videoPath.toString());
            video.setCoverUrl(coverName != null ? Paths.get(COVER_DIR, coverName).toString() : null);
            video.setStatus(1);
            video.setUploaderId(uploaderId);
            video.setPlayCount(0L);
            video.setDanmakuCount(0L);
            video.setCommentCount(0L);
            video.setLikeCount(0L);
            video.setCoinCount(0L);
            video.setCollectCount(0L);
            video.setShareCount(0L);
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            video.setCreatedAt(now);
            video.setUpdatedAt(now);

            videoMapper.insert(video);
            return video;

        } catch (IOException e) {
            throw new RuntimeException("上传失败", e);
        }
    }
}
