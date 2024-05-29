package com.yjh.imitator.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
@Data
@Schema(name = "配置文件", description = "配置文件修改")
public class VoiceProp {

    @Schema(description = "音频文件位置")
    private String voicePath;

    @Schema(description = "返回结果中是否增加错误")
    private Boolean retError;

    @Schema(description = "结果批量返回，多个相同采集结束时间的点位在一个请求中返回")
    private Boolean batchResponse;

    @Schema(description = "返回音频文件URL http://172.24.39.9/voice-files/")
    private String fileUrlPath;

    @Schema(description = "采集返回接口")
    private String collectNotify;

    @Schema(description = "分析返回接口")
    private String analyseNotify;
}
