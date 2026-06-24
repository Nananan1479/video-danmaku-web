package com.cilicili.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class OssUtil {

    /**
     * 上传文件到 OSS
     */
    public static void upload(String endpoint, String accessKeyId, String accessKeySecret,
                               String bucketName, String objectName, InputStream inputStream) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream));
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成带签名的临时访问URL（有效期1小时）
     */
    public static String generatePresignedUrl(String endpoint, String accessKeyId,
                                              String accessKeySecret, String bucketName,
                                              String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从 OSS 读取视频并返回 ResponseEntity（支持 Range 分段，用于视频拖拽进度条）
     *
     * @param endpoint        OSS endpoint
     * @param accessKeyId     AK
     * @param accessKeySecret SK
     * @param bucketName      Bucket 名称
     * @param objectName      对象路径（如 Videos/xxx.mp4）
     * @param rangeHeader     前端 Range 请求头（可为 null）
     * @return ResponseEntity 视频字节流
     */
    public static ResponseEntity<byte[]> serveOssVideo(String endpoint, String accessKeyId,
                                                        String accessKeySecret, String bucketName,
                                                        String objectName, String rangeHeader) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 获取文件元信息（大小）
            long fileSize = ossClient.getObjectMetadata(bucketName, objectName).getContentLength();

            String contentType = "video/mp4";
            if (objectName.contains(".")) {
                String ext = objectName.substring(objectName.lastIndexOf(".")).toLowerCase();
                switch (ext) {
                    case ".mp4":  contentType = "video/mp4";  break;
                    case ".webm": contentType = "video/webm"; break;
                    case ".ogg":  contentType = "video/ogg";  break;
                }
            }

            // 无 Range → 完整下载
            if (rangeHeader == null) {
                OSSObject ossObject = ossClient.getObject(bucketName, objectName);
                byte[] data = readAllBytes(ossObject.getObjectContent());
                ossObject.close();
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileSize)
                        .body(data);
            }

            // 有 Range → 分段下载
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = (ranges[0] != null && !ranges[0].isEmpty()) ? Long.parseLong(ranges[0]) : 0;
            long end = fileSize - 1;
            if (ranges.length > 1 && ranges[1] != null && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
            if (end >= fileSize) end = fileSize - 1;

            GetObjectRequest getRequest = new GetObjectRequest(bucketName, objectName);
            getRequest.setRange(start, end);
            OSSObject ossObject = ossClient.getObject(getRequest);
            byte[] data = readAllBytes(ossObject.getObjectContent());
            ossObject.close();

            long contentLength = end - start + 1;
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .contentLength(contentLength)
                    .body(data);

        } catch (Exception e) {
            System.err.println("OSS 读取失败: " + objectName + " → " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } finally {
            ossClient.shutdown();
        }
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}
