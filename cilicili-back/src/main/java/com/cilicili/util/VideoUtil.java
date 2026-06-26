package com.cilicili.util;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class VideoUtil {
    /*
      获取视频时长（毫秒）。<br>
      使用临时文件存储，适合小文件，不推荐使用
      @param file 视频文件
     * @return long 时长（毫秒），若出错返回0
     */
//    public static long getDuration(MultipartFile file) {
//        try {
//            // 1. 因为库需要 File 对象，对于小文件，直接转存为临时文件
//            File tempFile = File.createTempFile("video-", ".mp4");
//            file.transferTo(tempFile);
//
//            // 2. 用库解析视频信息
//            MultimediaObject multimediaObject = new MultimediaObject(tempFile);
//            MultimediaInfo info = multimediaObject.getInfo();
//
//            // 3. 清理临时文件
//            tempFile.delete();
//
//            // 4. 返回时长（毫秒）
//            return info.getDuration();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }

    /**
     * 获取视频时长（毫秒）。<br>
     * 使用视频文件路径解析，调用时请确保有File对象
     *
     * @param file 视频文件路径
     *
     * @author Nananan1479
     * @date 2026/5/27 14:21

     * @return long 时长（毫秒），若出错返回0
     */
    public static long getDuration(File file) {
        try {
            MultimediaObject instance = new MultimediaObject(file);
            MultimediaInfo result = instance.getInfo();
            return result.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 从磁盘读取视频文件，支持 Range 分段请求（拖拽进度条）。<br>
     * 项目转为使用阿里云oss读取视频，该方法不再适用
     *
     * @param filePath    视频文件完整路径
     * @param rangeHeader 前端 Range 请求头（可为 null）
     * @return ResponseEntity 包含视频字节流
     */
    public static ResponseEntity<Resource> serveVideoFile(Path filePath, String rangeHeader) {
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            long fileSize = Files.size(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "video/mp4";

            // 无 Range 头 → 返回完整视频（流式）
            if (rangeHeader == null) {
                FileSystemResource resource = new FileSystemResource(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileSize)
                        .body(resource);
            }

            // 解析 Range: bytes=start-end
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = (ranges[0] != null && !ranges[0].isEmpty()) ? Long.parseLong(ranges[0]) : 0;
            long end = fileSize - 1;
            if (ranges.length > 1 && ranges[1] != null && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
            if (end >= fileSize) end = fileSize - 1;

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
                    .body(new org.springframework.core.io.ByteArrayResource(data));

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
