package com.cilicili.controller.admin;

import com.cilicili.common.Result;
import com.cilicili.entity.Video;
import com.cilicili.entity.vo.VideoVO;
import com.cilicili.mapper.VideoMapper;
import com.cilicili.service.VideoService;
import com.cilicili.service.admin.AdminVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/videos")
public class AdminVideoController {

    @Autowired
    private AdminVideoService adminVideoService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoService videoService;

    /**
     * 获取视频列表
     * GET /api/admin/videos?status=0  只查待审核
     */
    @GetMapping
    public Result<List<VideoVO>> getVideoList(@RequestParam(required = false) Integer status,
                                               HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        List<VideoVO> list = adminVideoService.getVideoList(status);
        return Result.success(200, list);
    }


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
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");

        if (role == null || role != 1) {
            return ResponseEntity.notFound().build();
        }

        Video video = videoMapper.selectById(id);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }

        return videoService.getVideo(id, rangeHeader);
    }

    /**
     * 更新视频状态（审核通过/驳回）
     * PUT /api/admin/videos/{id}/status
     * body: { "status": 1|-1 }
     * 1=通过，-1=驳回
     */
    @PutMapping("/{id}/status")
    public Result<?> updateVideoStatus(@PathVariable Long id,
                                        @RequestBody Map<String, Integer> body,
                                        HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        Integer status = body.get("status");
        if (status == null) {
            return Result.fail(400, "status 参数无效");
        }
        return adminVideoService.updateVideoStatus(id, status);
    }

    /**
     * 删除视频
     * DELETE /api/admin/videos/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteVideo(@PathVariable Long id,
                                  HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        return adminVideoService.deleteVideo(id);
    }
}
