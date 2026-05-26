
import { getRecommendVideos, getVideoInfo } from '@/api/index'
/**
 * 获取视频数据
 * @param {*} pageNum 页码
 * @param {*} pageSize 每页数量
 * @returns 
 */
export const fetchVideos = async (pageNum, pageSize) => {
    try {
        const res = await getRecommendVideos({
            pageNum,
            pageSize
        })
        if (res.data.code === 200) {
            return {
                videos: res.data.data.records,
                total: res.data.data.total
            }
        }
    } catch (err) {
        console.error('加载失败', err)
        return {
            videos: [],
            total: 0
        }
    }
}

/**
 * 获取单个视频的详情数据
 * @param {*} videoId 视频ID
 * @returns {Object} { title, description, playCount, danmakuCount, ... }
 */
export const fetchVideoInfo = async (videoId) => {
    if (!videoId) {
        console.error('videoId 为空')
        return null
    }
    try {
        const res = await getVideoInfo(videoId)
        return res.data
    } catch (err) {
        console.error('获取视频详情失败', err)
        return null
    }
}

/**
 * 格式化播放量（万）
 * @param {*} num 
 * @returns {string} 格式化后的播放量字符串（例如：1234、1.3万）
 */
export function formatCount(num) {
    if (num == null) return '0'
    if (num >= 10000) {
        return (num / 10000).toFixed(num % 10000 === 0 ? 0 : 1) + '万'
    }
    return String(num)
}

/**
 * 格式化时长（秒 → mm:ss）
 * @param {*} seconds 
 * @returns {string} 格式化后的时长字符串（例如：01:23）
 */
export function formatDuration(seconds) {
    if (seconds == null) return '00:00'
    const m = Math.floor(seconds / 60)
    const s = Math.floor(seconds % 60)
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/**
 * 格式化日期为相对时间或简短日期
 * @param {*} dateStr 
 * @returns {string} 格式化后的日期字符串（例如：1分钟前、2小时前、2023-01-01）
 */
export function formatDate(dateStr) {
    if (!dateStr) return ''

    const date = new Date(dateStr.replace(' ', 'T'))
    const now = new Date()
    const diff = now - date

    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    const m = date.getMonth() + 1
    const d = date.getDate()
    
    return `${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}