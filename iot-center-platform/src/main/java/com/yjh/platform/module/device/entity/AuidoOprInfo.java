package com.yjh.platform.module.device.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author xmchen
 * @date 2022/4/16
 * @since [产品/模块版本] （可选）
 */
@NoArgsConstructor
@Data
public class AuidoOprInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("GLOBAL_MODE")
    private String globalMode = "ACTION";
    @JsonProperty("GLOBAL_CLIENTID")
    private String globalClientid;
    @JsonProperty("GLOBAL_GROUPID")
    private String globalGroupid = "admin";
    @JsonProperty("ACTION_POWER")
    private String actionPower;
}
