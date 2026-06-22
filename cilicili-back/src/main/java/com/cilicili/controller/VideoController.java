package com.cilicili.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cilicili.common.Result;
import com.cilicili.entity.User;
import com.cilicili.entity.Video;
import com.cilicili.entity.vo.VideoVO;
import com.cilicili.mapper.UserMapper;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.service.VideoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos/")
public class VideoController {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserMapper userMapper;

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
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        Video video = videoMapper.selectById(id);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        if (video.getStatus() == null || video.getStatus() != 1) {
            return ResponseEntity.status(403).body(null);
        }

        return videoService.getVideo(id, rangeHeader);
    }

    /**
     * 根据Url中的id返回标题、时长、播放量等元数据，用于视频详情页的视频数据
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
        // status=1（正常）才返回视频详情
        if (video.getStatus() == null || video.getStatus() != 1) {
            return ResponseEntity.status(403).body(null);
        }

        Map<String, Object> info = new HashMap<>();
        info.put("id", video.getId());
        info.put("title", video.getTitle());
        info.put("description", video.getDescription());
        info.put("duration", video.getDuration());
        info.put("playCount", video.getPlayCount());
        info.put("danmakuCount", video.getDanmakuCount());
        info.put("likeCount", video.getLikeCount());
        info.put("coinCount", video.getCoinCount());
        info.put("collectCount", video.getCollectCount());
        info.put("shareCount", video.getShareCount());
        info.put("commentCount", video.getCommentCount());
        info.put("coverUrl", video.getCoverUrl());
        info.put("uploaderId", video.getUploaderId());
        info.put("createdAt", video.getCreatedAt());

        User uploader = userMapper.selectById(video.getUploaderId());
        info.put("uploaderName", uploader != null ? uploader.getUsername() : "");

        return ResponseEntity.ok(info);
    }


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
     * 根据文件名，从服务器固定目录读取图片，并以 Resource 形式返回给浏览器。
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
        return videoService.getCover(filename);
    }

    /**
     * 根据Id返回视频封面
     *
     * @param id
     *
     * @author Nananan1479
     * @date 2026/5/26 20:46

     * @return org.springframework.http.ResponseEntity<org.springframework.core.io.Resource>
     */
    @GetMapping("{id}/cover")
    public ResponseEntity<Resource> getVideoCoverById(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null || video.getCoverUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        // coverUrl 现在只存文件名，直接传入
        return videoService.getCover(video.getCoverUrl());
    }

    /**
     * 首页推荐视频（分页）
     *
     * @param pageNum 第几页
     * @param pageSize 该页需要的数据多少
     *
     * @author Nananan1479
     * @date 2026/5/25 22:20

     * @return com.cilicili.common.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.cilicili.entity.vo.VideoVO>>
     */
    @GetMapping("/recommend")
    public Result<Page<VideoVO>> getRecommendedVideos(
            @RequestParam(defaultValue = "1") int pageNum,     // 默认第1页
            @RequestParam(defaultValue = "12") int pageSize    // 默认每页12条
    ) {
        Page<Video> videoPage = videoService.getHomeRecommendedVideos(pageNum, pageSize);
        // 转换为 VO（过滤敏感字段，如删除 password、加密信息）
        Page<VideoVO> voPage = convertToVoPage(videoPage);
        return Result.success(Result.RESULT_OK,voPage);
    }

    /**
     * 视频页相关推荐视频（分页）
     *
     * @param pageNum 第几页
     * @param pageSize 该页需要的数据多少
     * @param currentVideoId
     *
     * @author Nananan1479
     * @date 2026/5/25 22:32

     * @return com.cilicili.common.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.cilicili.entity.vo.VideoVO>> VideoVO
     */
    @GetMapping("/related")
    public Result<Page<VideoVO>> getRelatedVideos(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "8") int pageSize,
            @RequestParam Long currentVideoId
    ) {
        Page<Video> page = videoService.getRelatedVideos(pageNum, pageSize, currentVideoId);
        Page<VideoVO> voPage = convertToVoPage(page);
        return Result.success(Result.RESULT_OK,voPage);
    }

    /**
     * 将 Video 分页转为 VideoVO 分页，并批量填充上传者用户名。
     *
     * 相比最初的纯 BeanUtils.copyProperties：
     *   - 新增了根据 uploaderId 批量查 user 表的逻辑
     *   - 将查询到的用户名写入 VideoVO.uploaderName
     * 这样前端 VideoCard 可以直接显示作者名，而无需额外请求。
     *
     * @param entityPage Video 实体的分页对象
     *
     * @author Nananan1479
     * @date 2026/5/25 22:33
     * @since 1.1 添加批量用户名填充

     * @return Page&lt;VideoVO&gt; 含用户名的 VO 分页
     */
    private Page<VideoVO> convertToVoPage(Page<Video> entityPage) {
        Page<VideoVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());

        // 收集当前页所有视频的上传者ID，去重
        Set<Long> uploaderIds = entityPage.getRecords().stream()
                // ::getUploaderId 是 Video 实体的方法引用，用于获取上传者ID
                .map(Video::getUploaderId)
                .collect(Collectors.toSet());

        // 批量查询用户表，构建 id -> username 映射（1次SQL）
        Map<Long, String> nameMap = new HashMap<>();
        if (!uploaderIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(uploaderIds);
            nameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        }

        // 逐一转换实体到VO，并填入对应的用户名
        Map<Long, String> finalNameMap = nameMap;
        List<VideoVO> voList = entityPage.getRecords().stream().map(video -> {
            VideoVO vo = new VideoVO();
            BeanUtils.copyProperties(video, vo);
            vo.setUploaderName(video.getUploaderId() != null
                    ? finalNameMap.getOrDefault(video.getUploaderId(), "")
                    : "");
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
