package com.cilicili.mapper.interaction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cilicili.entity.VideoLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoLikeMapper extends BaseMapper<VideoLike> {

    /** 检查用户是否已点赞 */
    boolean existsByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 获取视频点赞数 */
    long countByVideoId(@Param("videoId") Long videoId);

    /** 点赞 */
    int insertLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 取消点赞 */
    int deleteByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 获取用户点赞的视频ID列表 */
    List<Long> selectLikedVideoIds(@Param("userId") Long userId);
}
