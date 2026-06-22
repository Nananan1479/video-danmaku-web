package com.cilicili.service.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class OssService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    // 上传文件的方法
    public String uploadFile(String fileName, MultipartFile file) throws IOException {
        // 1. 生成唯一的文件名，防止覆盖
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        // 2. 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 3. 上传文件
            ossClient.putObject(bucketName, fileName, file.getInputStream());
            // 4. 拼接文件访问URL并返回 (https://<bucketName>.<endpoint>/<fileName>)
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } finally {
            // 5. 关闭OSSClient，释放连接
            ossClient.shutdown();
        }
    }
}
