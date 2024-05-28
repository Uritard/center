package com.yjh.imitator.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Data
@Schema(name = "数据请求对象", description = "数据请求对象")
public class VoiceRequest<T> {

    @Schema(description = "请求返回ip地址")
    private String requestHostIp;

    @Schema(description = "请求返回端口")
    private String requestHostPort;

    @Schema(description = "请求数据唯一标识，UUID")
    private String requestId;

    @Schema(description = "数据列表，JSON格式的数组")
    private List<T> objectList;

    public RespSource getRespSource() {
        return new RespSource(requestHostIp, requestHostPort, requestId);
    }
}
