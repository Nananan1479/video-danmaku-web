package com.cilicili.common;

import lombok.Data;

// 向前端返回错误码
@Data
public class Result<T> {
    //常见的状态码
    //1xx（信息）：请求已接收，继续处理。
    //常见的有 101 Switching Protocols（切换协议）。

    //2xx（成功）：操作被成功接收。
    //200 OK：请求成功，最常见。
    //201 Created：新资源创建成功（如POST请求）。
    //204 No Content：成功但无返回内容（如DELETE请求）。

    //3xx（重定向）：需进一步操作。
    //301 Moved Permanently：资源永久移至新URL，以后用新地址。
    //302 Found：临时重定向，仍用原URL。
    //304 Not Modified：资源未修改，可直接用缓存。

    //4xx（客户端错误）：请求有误。
    //400 Bad Request：请求语法错误或参数无效。
    //401 Unauthorized：需身份认证（未登录）。
    //403 Forbidden：已认证但无权限访问。
    //404 Not Found：请求的资源不存在。
    //429 Too Many Requests：请求过于频繁。

    //5xx（服务端错误）：服务器处理失败。
    //500 Internal Server Error：通用服务器内部错误。
    //502 Bad Gateway：网关收到无效响应（如后端崩溃）。
    //503 Service Unavailable：服务器过载或维护中。
    //504 Gateway Timeout：网关超时。

    private int code;       // 状态码
    private String message; // 提示信息
    private T data;         // 实际数据，可以是任意类型
    private String token;   // JWT令牌（登录/注册时返回）

    // 成功时静态方法
    public static <T> Result<T> success(int Icode,T data) {
        Result<T> r = new Result<>();
        r.code = Icode;
        r.message = "成功";
        r.data = data;
        return r;
    }

    // 失败时静态方法
    public static <T> Result<T> fail(int Icode,String message) {
        Result<T> r = new Result<>();
        r.code = Icode;
        r.message = message;
        r.data = null;
        return r;
    }

}
