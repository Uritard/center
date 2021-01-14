package com.yjh.accessvqd.module.diagnose.entity;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author czh
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "数据服务器对象", description = "视频诊断-数据服务器")
public class DateServer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据服务器ID-String")
    private String serverId;
    @ApiModelProperty(value = "数据服务器IP-String")
    private String serverIp;
    @ApiModelProperty(value = "数据服务器端口-Integer")
    private String serverPort;
    @ApiModelProperty(value = "是否只需要发送告警信息-Integer-0：诊断结果全部发送" +
            "1：只发送异常的诊断结果到报警客户端" +
            "2：只发送异常的诊断结果到平台")
    private String alarmFlag;
}

