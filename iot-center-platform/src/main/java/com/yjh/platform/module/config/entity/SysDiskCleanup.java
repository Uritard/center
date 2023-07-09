package com.yjh.platform.module.config.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 磁盘清理记录
 * </p>
 *
 * @author Chenfei
 * @since 2023-06-30
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class SysDiskCleanup {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 是否删除临时文件，0: 否 1: 是  2: 清理完成  -1: 清理失败
     */
    @ApiModelProperty(value = "是否删除临时文件，0: 否 1: 是  2: 清理完成  -1: 清理失败")
    private Integer delTmp;

    /**
     * 是否删除任务文件，0: 否 1: 是  2: 清理完成  -1: 清理失败
     */
    @ApiModelProperty(value = "是否删除任务文件，0: 否 1: 是  2: 清理完成  -1: 清理失败")
    private Integer delTaskFile;

    /**
     * 是否删除任务报告，0: 否 1: 是  2: 清理完成  -1: 清理失败
     */
    @ApiModelProperty(value = "是否删除任务报告，0: 否 1: 是  2: 清理完成  -1: 清理失败")
    private Integer delTaskReport;

    /**
     * 是否删除任务数据库记录，0: 否 1: 是  2: 清理完成  -1: 清理失败
     */
    @ApiModelProperty(value = "是否删除任务数据库记录，0: 否 1: 是  2: 清理完成  -1: 清理失败")
    private Integer delTaskData;

    /**
     * 是否删除日志记录，0: 否 1: 是  2: 清理完成  -1: 清理失败
     */
    @ApiModelProperty(value = "是否删除日志记录，0: 否 1: 是  2: 清理完成  -1: 清理失败")
    private Integer delLogs;

    /**
     * 是否备份数据库，0: 否 1: 是，如果有删除数据库记录则必须备份
     */
    @ApiModelProperty(value = "是否备份数据库，0: 否 1: 是，如果有删除数据库记录则必须备份")
    private Integer backDatabase;

    /**
     * 是否备份数据库，0: 否 1: 是，如果有删除数据库记录则必须备份
     */
    @ApiModelProperty(value = "是否备份文件，0: 否 1: 是，数据库中根据 backFilePath 判断是否备份")
    @TableField(exist=false)
    private Integer backFile;

    /**
     * 备份文件路径
     */
    @ApiModelProperty(value = "备份文件路径")
    private String backFilePath;

    /**
     * 备份数据库文件
     */
    @ApiModelProperty(value = "备份数据库文件")
    private String backDataPath;

    /**
     * 备份过期状态，0: 未过期 1: 过期失效  2: 手动删除 3: 未备份
     */
    @ApiModelProperty(value = "备份过期状态，0: 未过期 1: 过期失效  2: 手动删除")
    private Integer backExpire;
    /**
     * 备份过期状态名称
     */
    @ApiModelProperty(value = "备份过期状态名称")
    @TableField(exist=false)
    private String backExpireName;

    /**
     * 清理具体内容说明
     */
    @ApiModelProperty(value = "清理具体内容说明")
    private String cleanContent;

    /**
     * 清理状态，0: 未完成 1: 待确认  2: 已确认
     */
    @ApiModelProperty(value = "清理状态，0: 未完成 1: 待确认  2: 已确认")
    private Integer cleanStatus;

    /**
     * 清理时限
     */
    @ApiModelProperty(value = "清理时限")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date expiryDate;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String creator;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
