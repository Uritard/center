package com.yjh.imitator.common;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/1
 * @since [产品/模块版本] （可选）
 */
@Data
public class Result {

    /**
     * 默认返回状态码
     */
    private int code = 200;

    public Result(int code) {
        this.code = code;
    }

    public Result() {

    }

    public static Result ofError(int errorCode) {
        return new Result(errorCode);
    }

    public static Result ofSuccess() {
        return new Result();
    }
}
