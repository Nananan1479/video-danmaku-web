package com.cilicili.controller.admin;

import com.cilicili.common.Result;
import com.cilicili.entity.vo.UserVO;
import com.cilicili.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 管理员登录
     * POST /api/admin/login <br>
     * body: { "username": "...", "password": "..." } <br>
     * 仅 role=1 的管理员可登录成功。
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.fail(400, "用户名或密码为空");
        }
        return adminUserService.login(username, password);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public Result<List<UserVO>> getUserList(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        List<UserVO> list = adminUserService.getUserList();
        return Result.success(200, list);
    }

    /**
     * 封禁/解封用户
     * PUT /api/admin/users/{id}/status
     * body: { "status": 0|1 }
     * 0=封禁，1=正常
     */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id,
                                       @RequestBody Map<String, Integer> body,
                                       HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "status 参数无效，需为 0 或 1");
        }
        return adminUserService.updateUserStatus(id, status);
    }

    /**
     * 处理用户注销申请（用户 status=2 时）
     * PUT /api/admin/users/{id}/delete-request
     * body: { "approve": true|false }
     * approve=true  → 确认注销（删除用户）
     * approve=false → 拒绝注销（恢复 status=1）
     */
    @PutMapping("/users/{id}/delete-request")
    public Result<?> handleDeleteRequest(@PathVariable Long id,
                                          @RequestBody Map<String, Boolean> body,
                                          HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        Boolean approve = body.get("approve");
        if (approve == null) {
            return Result.fail(400, "approve 参数无效");
        }
        return adminUserService.handleDeleteRequest(id, approve);
    }

    /**
     * 硬删除用户
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id,
                                 HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.fail(403, "无管理员权限");
        }
        return adminUserService.deleteUser(id);
    }
}
