package com.zsn.controller;

import com.zsn.common.Result;
import com.zsn.entity.Video;
import com.zsn.mapper.VideoMapper;
import com.zsn.service.impl.VideoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/videos/")
public class VideoController {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoServiceImpl videoService;

    @Value("{spring.servlet.multipart.max-file-size")
    private Long videoMaxMegaByte;

    // 按视频 ID 从数据库查出 video_url ，再把磁盘文件以流的形式返回给浏览器
    @GetMapping("{id}")
    public ResponseEntity<byte[]> getVideo(
            @PathVariable Long id,
            // 前端请求中视频拖拽进度条的需求的请求头（请求头为Range）（video标签自动完成发送）
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        return videoService.getVideo(id,rangeHeader);
    }

    // 返回标题、时长、播放量等元数据
    @GetMapping("{id}/info")
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

    // 返回所有视频的指定数据

    @PostMapping("upload")
    public Result<Video> uploadVideo(
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("uploaderId") Long uploaderId) {

        if (videoFile.isEmpty()) {
            return Result.fail(400, "视频文件不能为空");
        }

        String originalName = videoFile.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".mp4")) {
            return Result.fail(400, "仅支持MP4格式视频");
        }



        try {
            Video video = videoService.uploadVideo(videoFile, coverFile, title, description, uploaderId);
            return Result.success(200, video);
        } catch (RuntimeException e) {
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }

    @GetMapping("cover/{filename:.+}")
    public ResponseEntity<Resource> getCover(@PathVariable String filename) {
        try {
            File file = new File("C:/Users/NANANAN/Videos/Covers", filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "image/png";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
