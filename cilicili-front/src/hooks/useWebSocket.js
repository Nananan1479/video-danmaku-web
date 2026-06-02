/**
 * useWebSocket - WebSocket 连接管理 Hook
 * 使用 STOMP 协议订阅服务端弹幕推送，新弹幕到达时通过回调通知调用方。
 */
import { ref, onUnmounted } from 'vue'
import Stomp from 'stompjs'

/**
 * @param {import('vue').Ref<number|null>} videoIdRef 当前视频 ID 的响应式引用
 * @returns {{ connect: Function, disconnect: Function, connected: import('vue').Ref<boolean>, onDanmakuReceived: Function }}
 */
export function useWebSocket(videoIdRef) {
    /** STOMP 客户端实例 */
    const stompClient = ref(null)
    /** 是否已连接 */
    const connected = ref(false)
    /** 弹幕到达时的回调函数 */
    let onDanmakuCallback = null

    /**
     * 建立 WebSocket 连接并订阅当前视频的弹幕频道
     */
    const connect = () => {
        if (!videoIdRef.value) return
        const url = import.meta.env.VITE_WS_URL
        const client = Stomp.client(url)
        // 关闭 STOMP 调试日志
        client.debug = () => {}
        stompClient.value = client

        client.connect({}, () => {
            connected.value = true
            // 订阅对应视频的弹幕频道
            const topic = `/topic/danmaku/${videoIdRef.value}`
            client.subscribe(topic, (message) => {
                const danmaku = JSON.parse(message.body)
                // 触发回调，将弹幕数据传递给调用方
                if (onDanmakuCallback) {
                    onDanmakuCallback(danmaku)
                }
            })
        }, (error) => {
            console.error('WebSocket 连接失败', error)
            connected.value = false
        })
    }

    /**
     * 断开 WebSocket 连接
     */
    const disconnect = () => {
        if (stompClient.value && stompClient.value.connected) {
            stompClient.value.disconnect()
            connected.value = false
        }
    }

    /**
     * 注册弹幕到达时的回调
     * @param {Function} callback 接收解析后的弹幕对象作为参数
     */
    const onDanmakuReceived = (callback) => {
        onDanmakuCallback = callback
    }

    // 组件卸载时自动断开连接
    onUnmounted(() => {
        disconnect()
    })

    return {
        connect,
        disconnect,
        connected,
        onDanmakuReceived
    }
}
