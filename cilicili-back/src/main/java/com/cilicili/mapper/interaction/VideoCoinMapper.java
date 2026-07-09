package com.cilicili.mapper.interaction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cilicili.entity.VideoCoin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoCoinMapper extends BaseMapper<VideoCoin> {

    /** 检查用户是否已投币 */
    boolean existsByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 获取视频投币总数 */
    int sumCoinByVideoId(@Param("videoId") Long videoId);

    /** 投币 */
    int insertCoin(@Param("videoId") Long videoId, @Param("userId") Long userId, @Param("num") int num);

    /** 取消投币 */
    int deleteByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    /** 获取用户投币的视频ID列表 */
    List<Long> selectCoinedVideoIds(@Param("userId") Long userId);
}
