package com.cilicili.service.admin;

import com.cilicili.common.Result;
import com.cilicili.entity.vo.UserVO;

import java.util.List;
import java.util.Map;

public interface AdminUserService {

    /** 管理员登录（仅 role=1 可登录） */
    Result<?> login(String username, String password);

    /** 获取用户列表 */
    List<UserVO> getUserList();

    /** 更新用户封禁状态（status：0=封禁，1=正常） */
    Result<?> updateUserStatus(Long userId, int status);

    /** 处理注销申请（status=2）：确认注销则删除用户，拒绝则恢复 status=1 */
    Result<?> handleDeleteRequest(Long userId, boolean approve);

    /** 硬删除用户 */
    Result<?> deleteUser(Long userId);
}
