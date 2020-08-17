package com.yjh.Manager.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-08-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgAccess对象", description = "接入配置表")
public class TCfgAccess implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键id")
    @TableId(value = "access_id", type = IdType.AUTO)
    private Long accessId;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "所属项目id")
    private Long projectId;

    @ApiModelProperty(value = "华为oc连接地址")
    private String url;

    @ApiModelProperty(value = "华为oc连接端口")
    private String port;

    @ApiModelProperty(value = "华为oc第三方应用的身份标识")
    private String appId;

    @ApiModelProperty(value = "华为oc第三方应用的密码")
    private String secret;

    @ApiModelProperty(value = "接入平台编码:oc、104")
    private String accessCode;

    @ApiModelProperty(value = "创建人id")
    private Long userId;

    @ApiModelProperty(value = "创建人名字")
    private String userName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "接入状态（ONLINE:正常 OFFLINE：物理异常 ABNORMAL：数据异常）")
    private String state;


}
