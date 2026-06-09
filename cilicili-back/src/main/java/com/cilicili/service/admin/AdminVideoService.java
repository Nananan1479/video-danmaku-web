package com.cilicili.service.admin;

import com.cilicili.common.Result;
import com.cilicili.entity.vo.VideoVO;

import java.util.List;

public interface AdminVideoService {

    /** 获取视频列表（可按状态筛选） */
    List<VideoVO> getVideoList(Integer status);

    /** 更新视频状态（审核通过/驳回） */
    Result<?> updateVideoStatus(Long videoId, int status);

    /** 删除视频 */
    Result<?> deleteVideo(Long videoId);
}
