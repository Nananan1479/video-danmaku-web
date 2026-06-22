package com.cilicili.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cilicili.entity.Video;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.service.VideoService;
import com.cilicili.util.VideoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Service
public class VideoServiceImpl implements VideoService {

    /**
     *存储视频地址
     */
    @Value("${file.video-dir}")
    private String VIDEO_DIR ;
    /**
     *存储视频封面地址
     */
    @Value("${file.cover-dir}")
    private String COVER_DIR ;

    @Autowired
    private VideoMapper videoMapper;

//    @Autowired
//    private VideoUtil videoUtil;

    /**
     * 向前端返回视频流。通过rangeHeader头可向前端返回视频片段实现拖拽进度条时快速跳转到对应位置。
     *
     *
     * @param id
     * @param rangeHeader
     * 
     * @author Nananan1479 
     * @date 2026/5/25 13:51
     * @return org.springframework.http.ResponseEntity<byte[]>
     */
    @Override
    public ResponseEntity<byte[]> getVideo(Long id, String rangeHeader) {
        Video video = videoMapper.selectById(id);
        // 未查询到返回 错误
        if (video == null || video.getVideoUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        // status值不为1时错误
//        if (!(video.getStatus() == 1)) {
//            return ResponseEntity.badRequest().build();
//        }

        // 数据库只存文件名，用配置目录拼接完整路径
        Path filePath = Paths.get(VIDEO_DIR, video.getVideoUrl());
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

    /**
     * 上传视频和封面功能，视频仅支持MP4格式，图片仅支持png格式，视频大小不超500MB（可在application.yml里调整）。
     *

     * @param videoFile
     * @param coverFile
     * @param title
     * @param description
     * @param uploaderId
     *
     * @author Nananan1479
     * @date 2026/5/25 14:08

     * @return com.cilicili.entity.Video
     */
    @Override
    public Video uploadVideo(MultipartFile videoFile, MultipartFile coverFile, String title, String description, Long uploaderId) {
        try {
            Files.createDirectories(Paths.get(VIDEO_DIR));
            Files.createDirectories(Paths.get(COVER_DIR));

            String videoExt = ".mp4";
            // 视频
            String videoName = UUID.randomUUID().toString() + videoExt;
            Path videoPath = Paths.get(VIDEO_DIR, videoName);
            // 写入磁盘
            videoFile.transferTo(videoPath.toFile());

            // 封面写入磁盘
            String coverName = null;
            if (coverFile != null && !coverFile.isEmpty()) {
                String originalName = coverFile.getOriginalFilename();
                String coverExt = originalName != null && originalName.contains(".") ?
                        originalName.substring(originalName.lastIndexOf(".")) : ".png";
                // 为视频创建唯一的UUID
                coverName = UUID.randomUUID().toString() + coverExt;
                Path coverPath = Paths.get(COVER_DIR, coverName);
                // 写入磁盘
                coverFile.transferTo(coverPath.toFile());
            }

            // 存储视频信息(数据库)
            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            // 数据库只存文件名，读取时用 YAML 配置的目录拼接
            video.setVideoUrl(videoName);
            video.setCoverUrl(coverName);
            video.setStatus(2);
            video.setUploaderId(uploaderId);
            video.setPlayCount(0L);
            video.setDanmakuCount(0L);
            video.setCommentCount(0L);
            video.setLikeCount(0L);
            video.setCoinCount(0L);
            video.setCollectCount(0L);
            video.setShareCount(0L);
            video.setDuration((int) (VideoUtil.getDuration(videoPath.toFile())/1000));
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            video.setCreatedAt(now);
            video.setUpdatedAt(now);

            videoMapper.insert(video);
            return video;

        } catch (IOException e) {
            throw new RuntimeException("上传失败", e);
        }
    }


    /**
     * 根据文件名，从服务器固定目录读取图片，并以 Resource 形式返回给浏览器。<br>
     * 若文件夹不存在，则会创建文件
     *
     * @param filename 封面名字（包含后缀）
     *
     * @author Nananan1479
     * @date 2026/5/26 23:06

     * @return org.springframework.http.ResponseEntity<org.springframework.core.io.Resource>
     */
    @Override
    public ResponseEntity<Resource> getCover(String filename) {
        try {
            File file = new File(COVER_DIR, filename);
            if (!file.exists()) {
                Path dir = Paths.get(COVER_DIR);
                Files.createDirectories(dir);  // 自动创建所有不存在的父目录
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


    /**
     * 视频页侧边栏推荐：排除当前视频，按时间或播放量
     *
     * @param pageNum
     * @param pageSize
     *
     * @author Nananan1479
     * @date 2026/5/27 0:33

     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.cilicili.entity.Video>
     */
    @Override
    public Page<Video> getHomeRecommendedVideos(int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)                       // 仅正常状态
                .orderByDesc("play_count");            // 按播放量降序
        return videoMapper.selectPage(page, wrapper);
    }


    /**
     * 视频页侧边栏推荐：排除当前视频，按时间或播放量
     *
     * @param pageNum
     * @param pageSize
     * @param currentVideoId
     *
     * @author Nananan1479
     * @date 2026/5/27 0:32

     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.cilicili.entity.Video>
     */
    @Override
    public Page<Video> getRelatedVideos(int pageNum, int pageSize, Long currentVideoId) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .ne("id", currentVideoId)               // 排除当前视频
                .orderByDesc("created_at");            // 可按时间或热度
        return videoMapper.selectPage(page, wrapper);
    }


    /**
     * 通用分页查询（无条件，全部数据）
     *
     * @param pageNum
     * @param pageSize
     *
     * @author Nananan1479
     * @date 2026/5/27 0:32

     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.cilicili.entity.Video>
     */
    @Override
    public Page<Video> getAllVideos(int pageNum, int pageSize) {
        return videoMapper.selectPage(new Page<>(pageNum, pageSize), null);
    }

    /*
      解析MP4视频时长（使用isoparser文件解析库）

      @param videoPath
     *
     * @author Nananan1479
     * @date 2026/5/27 1:31

     * @return int
     */
//    private int extractDuration(Path videoPath) {
//        try {
//            IsoFile isoFile = new IsoFile(videoPath.toFile());
//            double durationSec = 0;
//            try {
//                MovieHeaderBox mvhd = isoFile.getMovieBox().getMovieHeaderBox();
//                durationSec = (double) mvhd.getDuration() / mvhd.getTimescale();
//            } finally {
//                isoFile.close();
//            }
//            return (int) Math.round(durationSec);
//        } catch (Exception e) {
//            System.err.println("获取视频时长失败: " + e.getMessage());
//            return 0;
//        }
//    }
}
