package com.cilicili.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import java.net.URL;
import java.util.Date;

public class OssUtil {
    /**
     * 生成带签名的临时访问URL（有效期1小时）
     *
     * @param endpoint
     * @param accessKeyId
     * @param accessKeySecret
     * @param bucketName
     * @param objectName
     *
     * @author Nananan1479
     * @date 2026/6/22 17:01

     * @return java.lang.String
     */
    public static String generatePresignedUrl(String endpoint, String accessKeyId,
                                              String accessKeySecret, String bucketName,
                                              String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 1小时
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }
}
