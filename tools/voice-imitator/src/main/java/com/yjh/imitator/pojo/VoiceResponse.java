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
@Schema(name = "数据主动返回对象", description = "数据主动返回对象")
public class VoiceResponse<T> {
    @Schema(description = "请求数据唯一标识，UUID")
    private String requestId;

    @Schema(description = "数据列表，JSON格式的数组")
    private List<ResponseItem<T>> resultList;
}
