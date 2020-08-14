package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author tt
 * @since 2020-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "OrgInfo", description = "组织机构实体")
public class OrgInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    private Long Id;

    @ApiModelProperty(value = "名称")
    private String label;

    @ApiModelProperty(value = "上级机构ID")
    private Long upId;

    @ApiModelProperty(value = "上级机构名称")
    private String upName;

    @ApiModelProperty(value = "组织层级")
    private Integer level;

    @ApiModelProperty(value = "组织编码ID")
    private String orgCode;

    @ApiModelProperty(value = "子类")
    private List<OrgInfo> children;

}
