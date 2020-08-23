package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDictBusiness对象", description = "业务字典表")
public class TDictBusiness implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典ID")
    @TableId(value = "dict_id", type = IdType.AUTO)
    private Integer dictId;

    @ApiModelProperty(value = "字典编码")
    private String dictCode;

    @ApiModelProperty(value = "字典列名")
    private String colName;

    @ApiModelProperty(value = "字典描述")
    private String dictNote;

    @ApiModelProperty(value = "上级字典ID")
    private Integer upDict;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty(value = "排序")
    private Long sort;


}
