package com.cilicili.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cilicili.common.Result;
import com.cilicili.entity.User;
import com.cilicili.mapper.UserMapper;
import com.cilicili.service.UserService;
import com.cilicili.util.OssUtil;
import com.cilicili.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Service
public class UserServiceImpl implements UserService {

    @Value("${file.avatar-dir}")
    private String AVATAR_DIR;

    @Value("${aliyun.oss.endpoint}")
    private String ENDPOINT;
    @Value("${aliyun.oss.accessKeyId}")
    private String ACCESS_KEY_ID;
    @Value("${aliyun.oss.accessKeySecret}")
    private String ACCESS_KEY_SECRET;
    @Value("${aliyun.oss.public-bucket}")
    private String PUBLIC_BUCKET;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 通过id获取用户信息
     *
     *
     * @param id
     *
     *
     * @return com.cilicili.entity.User
     */
    @Override
    public User getUserById(long id) {
        return userMapper.selectById(id);
    }

    /**
     * 用户登录。<br>
     * 返回查找后的用户资料，检查用户名是否存在，使用BCrypt解码进行密码比对
     *

     * @param username
     * @param password
     *
     * @author Nananan1479
     * @date 2026/5/25 13:39
     * @return com.cilicili.common.Result<com.cilicili.entity.User>
     */
    @Override
    public Result<User> login(String username, String password) {
        // 1. 按用户名查询
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        // 2. 用户不存在
        if (user == null) {
            return Result.fail(400,"用户名或密码错误");
        }

        // 确认用户状态
        int userState = user.getStatus();
        if (userState == 0 || userState == 2) {
            return Result.fail(400,"账户状态异常");
        }
        // 核对密码（使用 BCrypt 加密比对）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail(400,"用户名或密码错误");
        }
//        if (!(Objects.equals(password, user.getUsername()))) {
//            return Result.fail(400,"用户名或密码错误");
////            throw new RuntimeException("密码错误");
//        }

        return Result.success(200,user);
    }

    /**
     * 用户注册
     *

     * @param username
     * @param phone
     * @param password
     *
     * @author Nananan1479
     * @date 2026/5/25 13:44
     * @return com.cilicili.common.Result<com.cilicili.entity.User>
     */
    @Override
    public Result<User> register(String username, String phone, String password) {
//        User existUser = userMapper.selectOne(
//                new LambdaQueryWrapper<User>()
//                        .eq(User::getUsername, username)
//        );

        User existUserPhone = userMapper.selectByUserPhone(phone);
        if (existUserPhone != null) {
            return Result.fail(400, "手机号已被使用");
        }

        User existUsername = userMapper.selectByUsername(username);
        if (existUsername != null) {
            return Result.fail(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAvatar("D:\\CiliCili\\Avatars\\Akalin.webp");
        user.setPhone(phone);
        user.setNickname(username);
        user.setStatus((byte) 1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        user.setCreated_at(now);
        user.setUpdated_at(now);

        userMapper.insert(user);
        user.setPassword(null);
        return Result.success(200, user);
    }

    /**
     * 上传头像
     *
     * @param file 上传的文件路径
     * @param userId
     *
     * @author Nananan1479
     * @date 2026/5/26 14:06

     * @return java.lang.String
     */
    @Override
    public String uploadAvatar(MultipartFile file, long userId) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".") ?
                    originalName.substring(originalName.lastIndexOf(".")) : ".png";
            String avatarName = UUID.randomUUID().toString() + ext;

            // 上传到 OSS 公共桶
            if (ENDPOINT != null && !ENDPOINT.isEmpty()) {
                OssUtil.upload(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET, PUBLIC_BUCKET,
                        AVATAR_DIR + avatarName, file.getInputStream());
            } else {
                // 兜底：本地磁盘
                Files.createDirectories(Paths.get(AVATAR_DIR));
                Path avatarPath = Paths.get(AVATAR_DIR, avatarName);
                file.transferTo(avatarPath.toFile());
            }

            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            // 数据库只存文件名
            user.setAvatar(avatarName);
            userMapper.updateById(user);

            return avatarName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("头像上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新用户信息（昵称、签名、邮箱、电话）
     *
     * @param nickname 用户昵称
     * @param signature 用户个性签名
     *
     * @author Nananan1479
     * @date 2026/6/10
     * @return com.cilicili.common.Result&lt;com.cilicili.entity.User&gt;
     */
    @Override
    public Result<User> updateUser(long userId, String nickname, String signature, String email, String phone) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname.trim());
        }
        if (signature != null) {
            user.setSignature(signature.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            String phonePattern = "^1[3-9]\\d{9}$";
            if (!phone.matches(phonePattern)) {
                return Result.fail(400, "手机号格式不正确");
            }
            user.setPhone(phone.trim());
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        user.setUpdated_at(now);
        userMapper.updateById(user);
        user.setPassword(null);
        return Result.success(200, user);
    }

    /**
     * 增加硬币
     */
    @Override
    public int addCoin(long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("增加的硬币必须大于0");
        }
        return userMapper.addCoin(userId, amount);
    }

    /**
     * 减少硬币（含余额不足校验）
     */
    @Override
    public int subtractCoin(long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("扣减的硬币必须大于0");
        }
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getCoin() == null || user.getCoin().compareTo(amount) < 0) {
            throw new RuntimeException("硬币不足");
        }
        return userMapper.subtractCoin(userId, amount);
    }

    /**
     * 获取用户头像
     *
     * @param filename 需要的头像名称（仅支持png）
     *
     * @author Nananan1479
     * @date 2026/5/26 14:08

     * @return org.springframework.http.ResponseEntity<org.springframework.core.io.Resource>
     */
    @Override
    public ResponseEntity<Resource> getAvatar(String filename) {
        // OSS 公共桶：302 重定向到 OSS 公开 URL
        if (ENDPOINT != null && !ENDPOINT.isEmpty() && PUBLIC_BUCKET != null) {
            String avatarUrl = "https://" + PUBLIC_BUCKET + "." + ENDPOINT + "/"
                    + AVATAR_DIR + filename;
            return ResponseEntity.status(302)
                    .header("Location", avatarUrl)
                    .body(null);
        }

        // 兜底：本地磁盘
        try {
            File file = new File(AVATAR_DIR, filename);
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

//    private boolean updateUser()

}
