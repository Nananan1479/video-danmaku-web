package com.cilicili.controller;

import com.cilicili.common.Result;
import com.cilicili.entity.Video;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.service.VideoService;
import com.cilicili.service.impl.VideoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/videos/")
public class VideoController {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoService videoService;

    /**
     *存储视频封面地址
     */
    @Value("${file.cover-dir}")
    private String COVER_DIR ;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;    // Spring 会自动解析并转换为 DataSize
    // 使用时需加上.toBytes()方法

    /**
     * 按视频 ID 从数据库查出 video_url ，再把磁盘文件以流的形式返回给浏览器
     *

     * @param id
     * @param rangeHeader
     *
     * @author Nananan1479
     * @date 2026/5/25 14:13

     * @return org.springframework.http.ResponseEntity<byte[]>
     */
    @GetMapping("{id}")
    public ResponseEntity<byte[]> getVideo(
            @PathVariable Long id,
            // 前端请求中视频拖拽进度条的需求的请求头（请求头为Range）（video标签自动完成发送）
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        return videoService.getVideo(id,rangeHeader);
    }

    /**
     * 根据Url中的id返回标题、时长、播放量等元数据
     *

     * @param id
     *
     * @author Nananan1479
     * @date 2026/5/25 14:14

     * @return org.springframework.http.ResponseEntity<java.util.Map<java.lang.String,java.lang.Object>>
     */
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

    /**
     *视频和封面上传功能，此处做条件检查
     *

     * @param videoFile
     * @param coverFile
     * @param title
     * @param description
     * @param uploaderId
     *
     * @author Nananan1479
     * @date 2026/5/25 14:17

     * @return com.cilicili.common.Result<com.cilicili.entity.Video>
     */
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

        if (videoFile.getSize() > maxFileSize.toBytes()) {
            return Result.fail(400, "视频文件过大，最大支持 500MB");
        }

        try {
            Video video = videoService.uploadVideo(videoFile, coverFile, title, description, uploaderId);
            return Result.success(200, video);
        } catch (RuntimeException e) {
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件名，从服务器固定目录 C:/Users/NANANAN/Videos/Covers 读取图片，并以 Resource 形式返回给浏览器。
     *
     * @param filename
     *
     * @author Nananan1479
     * @date 2026/5/25 15:10

     * @return org.springframework.http.ResponseEntity<org.springframework.core.io.Resource>
     */
    // .+为正则表达式，确保完整接收包含扩展名的文件名
    @GetMapping("cover/{filename:.+}")
    public ResponseEntity<Resource> getCover(@PathVariable String filename) {
        try {
            File file = new File(COVER_DIR, filename);
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
