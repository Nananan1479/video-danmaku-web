package com.cilicili.config;

import com.cilicili.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * JWT鉴权拦截器<br>
     * 分析“Bearer”请求头并解析token是否无效或已过期。
     *
     * @param request
     * @param response
     * @param handler
     *
     * @author Nananan1479
     * @date 2026/5/25 14:28

     * @return boolean
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
//        System.out.println("request"+request.toString());
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 放行公开的视频接口（GET 请求，且路径匹配 /api/videos/*）
        if (path.startsWith("/api/videos/")) {
            // 获取视频流：GET /api/videos/{id}
            // 获取视频信息：GET /api/videos/{id}/info
            // 获取封面：GET /api/videos/cover/**
            // 推荐列表：GET /api/videos/recommend 等
            if ("GET".equalsIgnoreCase(method) && !path.endsWith("/upload")) {
                return true;
            }
            // 上传视频：POST /api/videos/upload 需要验证
        }

        // 放行公开的弹幕查询接口（GET 获取弹幕无需登录，POST 发送弹幕需要登录）
        if (path.startsWith("/api/danmaku/") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // 从请求头获取 Authorization 字段（以Bearer为开头）。
        String token = request.getHeader("Authorization");
        // 若不存在或不以 Bearer 开头，返回 JSON 错误信息
        if (token == null || !token.startsWith("Bearer ")) {
//            System.out.println("token:"+token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请求头格式错误或未登录\"}");
            return false;
        }

        token = token.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
            response.setContentType("application/json;charset=UTF-8");
            // 前端根据response.code
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }

        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("role", jwtUtil.getRoleFromToken(token));
        return true;
    }
}
