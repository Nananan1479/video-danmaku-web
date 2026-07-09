package com.cilicili.mapper.interaction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cilicili.entity.VideoCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoCollectMapper extends BaseMapper<VideoCollect> {

    /** 检查用户是否已收藏 */
    boolean existsByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 获取视频收藏数 */
    long countByVideoId(@Param("videoId") Long videoId);

    /** 收藏 */
    int insertCollect(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 取消收藏 */
    int deleteByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 获取用户收藏的视频ID列表 */
    List<Long> selectCollectedVideoIds(@Param("userId") Long userId);
}
