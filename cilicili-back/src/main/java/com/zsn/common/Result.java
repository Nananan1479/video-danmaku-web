package com.zsn.common;

import lombok.Data;

// 向前端返回错误码
@Data
public class Result<T> {
    private int code;       // 状态码，如 200 成功，400 失败
    private String message; // 提示信息
    private T data;         // 实际数据，可以是任意类型

    // 成功时静态方法
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "成功";
        r.data = data;
        return r;
    }

    // 失败时静态方法
    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.code = 400;
        r.message = message;
        r.data = null;
        return r;
    }

}
