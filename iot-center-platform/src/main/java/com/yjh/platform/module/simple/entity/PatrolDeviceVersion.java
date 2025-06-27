package com.yjh.platform.module.simple.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * <功能描述> 巡视设备版本信息表
 *
 * @author shaobinfen
 * @date 2025/6/25
 * @since [产品/模块版本](可选)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PatrolDeviceVersion {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 版本号
     */
    private String name;

    /**
     * 版本描述
     */
    @JsonIgnore
    private String remark;

    /**
     * 版本详细说明
     */
    private String text;

    /**
     * 版本文件路径
     */
    private String filePath;

    /**
     * 机器人型号
     */
    private Integer robotType;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    @JsonIgnore
    private String updateUser;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonIgnore
    private LocalDateTime updateTime;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField(exist = false)
    private Integer pageNum = 1;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField(exist = false)
    private Integer pageSize = 0;
}
