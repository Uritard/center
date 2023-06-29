package com.yjh.platform.module.user.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.yjh.platform.common.annotation.ExcelExtend;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/5/25
 * @since [产品/模块版本] （可选）
 */
@Data
public class TCameraRecorderExcel implements Serializable {

    @ExcelProperty(value = "设备名称")
    private String recordName;

    @ExcelProperty(value = "PMS编码")
    @ExcelExtend
    private String pmsId;

    @ExcelProperty(value = "设备别名")
    @ExcelExtend
    private String aliasName;

    @ExcelProperty(value = "设备类型")
    @ExcelExtend(dictType = "recorderType")
    private String recorderType;

    @ExcelProperty(value = "设备型号")
    @ExcelExtend(dictType = "recorderModel", convent = "Str")
    private String recorderModelStr;

    @ExcelProperty(value = "生产厂家")
    @ExcelExtend(dictType = "cameraVendor", convent = "Str")
    private String vendorIdStr;

    @ExcelProperty(value = "使用单位")
    @ExcelExtend
    private String unit;

    @ExcelProperty(value = "IP")
    private String recordIp;

    @ExcelProperty(value = "服务端口")
    private Integer httpPort;

    @ExcelProperty(value = "RTSP端口")
    private Integer rtspPort;

    @ExcelProperty(value = "传输协议")
    @ExcelExtend(dictType = "protocolType")
    private String protocol;

    @ExcelProperty(value = "协议路径")
    @ExcelExtend
    private String protocolUrl;

    @ExcelProperty(value = "用户名")
    private String identityManager;

    @ExcelProperty(value = "密码")
    private String identityCode;

    @ExcelProperty(value = "最大通道数")
    @ExcelExtend
    private Integer maxChannel;

    @ExcelProperty(value = "缓存天数")
    @ExcelExtend
    private Integer bufferDay;

    @ExcelProperty(value = "缓存大小")
    @ExcelExtend
    private Integer hddSize;

    @ExcelProperty(value = "录制时长")
    @ExcelExtend
    private Integer timeLong;

    @ApiModelProperty(value = "录像机型号")
    private Integer recorderModel;

    @ApiModelProperty(value = "生产厂家")
    private Integer vendorId;
}
