package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/9/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWarnInfo对象扩展", description = "告警信息表扩展")
public class TWarnInfoDetail extends TWarnInfo {
    private String warnLevelName;

    private String deviceName;

    private String alarmSourceName;

    private String confModeName;

}
