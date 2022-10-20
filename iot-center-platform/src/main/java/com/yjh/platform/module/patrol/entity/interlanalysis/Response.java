package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/4/11
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    /**
     * 200：成功;
     * 400：表示客户端请求有语法错误，不能被服务器所理解;
     * 500：服务端异常
     */
    private int code;

    public static Response ok() {
        return new Response(200);
    }

    public static Response basRequest() {
        return new Response(400);
    }

    public static Response serverError() {
        return new Response(500);
    }
}
