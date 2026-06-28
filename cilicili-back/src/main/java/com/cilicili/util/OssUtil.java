package com.cilicili.util;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class OssUtil {

    /** OSS 读取超时：10 分钟（大视频文件 + 网络慢时需要较长时间） */
    private static final int SOCKET_TIMEOUT = 10 * 60 * 1000;
    /** OSS 连接超时 */
    private static final int CONNECTION_TIMEOUT = 30 * 1000;

    private static OSS buildClient(String endpoint, String accessKeyId, String accessKeySecret) {
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();
        config.setSocketTimeout(SOCKET_TIMEOUT);
        config.setConnectionTimeout(CONNECTION_TIMEOUT);
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, config);
    }

    /**
     * 上传文件到 OSS
     */
    public static void upload(String endpoint, String accessKeyId, String accessKeySecret,
                               String bucketName, String objectName, InputStream inputStream) {
        OSS ossClient = buildClient(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream));
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从 OSS 删除文件
     */
    public static void delete(String endpoint, String accessKeyId, String accessKeySecret,
                               String bucketName, String objectName) {
        OSS ossClient = buildClient(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.deleteObject(bucketName, objectName);
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
        OSS ossClient = buildClient(endpoint, accessKeyId, accessKeySecret);
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从 OSS 流式读取视频并返回 ResponseEntity（支持 Range 分段，用于视频拖拽进度条）
     * <p>
     * 使用 InputStreamResource 流式传输，不再将整个视频读入内存，
     * 同时 OSS 客户端配置了 10 分钟读取超时，避免大文件超时。
     *
     * @param endpoint        OSS endpoint
     * @param accessKeyId     AK
     * @param accessKeySecret SK
     * @param bucketName      Bucket 名称
     * @param objectName      对象路径（如 Videos/xxx.mp4）
     * @param rangeHeader     前端 Range 请求头（可为 null）
     * @return ResponseEntity 视频流
     */
    public static ResponseEntity<Resource> serveOssVideo(String endpoint, String accessKeyId,
                                                          String accessKeySecret, String bucketName,
                                                          String objectName, String rangeHeader) {
        OSS ossClient = buildClient(endpoint, accessKeyId, accessKeySecret);
        try {
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

            OSSObject ossObject;
            long contentLength = fileSize;
            HttpStatus status = HttpStatus.OK;
            String contentRange = null;

            if (rangeHeader == null) {
                // 完整下载 → 流式传输
                ossObject = ossClient.getObject(bucketName, objectName);
            } else {
                // Range 分段请求
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                long start = (ranges[0] != null && !ranges[0].isEmpty()) ? Long.parseLong(ranges[0]) : 0;
                long end = fileSize - 1;
                if (ranges.length > 1 && ranges[1] != null && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                if (end >= fileSize) end = fileSize - 1;

                GetObjectRequest getRequest = new GetObjectRequest(bucketName, objectName);
                getRequest.setRange(start, end);
                ossObject = ossClient.getObject(getRequest);
                contentLength = end - start + 1;
                status = HttpStatus.PARTIAL_CONTENT;
                contentRange = "bytes " + start + "-" + end + "/" + fileSize;
            }

            InputStream inputStream = ossObject.getObjectContent();
            final long streamLength = contentLength;

            // 流关闭时由 Spring 自动释放 OSS 连接
            InputStreamResource resource = new InputStreamResource(inputStream) {
                @Override
                public long contentLength() {
                    return streamLength;
                }
            };

            ResponseEntity.BodyBuilder builder = ResponseEntity.status(status)
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(contentLength);

            if (contentRange != null) {
                builder.header(HttpHeaders.CONTENT_RANGE, contentRange);
                builder.header(HttpHeaders.ACCEPT_RANGES, "bytes");
            }

            return builder.body(resource);

        } catch (Exception e) {
            System.err.println("OSS 读取失败: " + objectName + " → " + e.getMessage());
            e.printStackTrace();
            ossClient.shutdown();
            return ResponseEntity.notFound().build();
        }
    }
}
