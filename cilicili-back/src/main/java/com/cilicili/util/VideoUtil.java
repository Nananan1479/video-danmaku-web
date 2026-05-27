package com.cilicili.util;

import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;

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
}
