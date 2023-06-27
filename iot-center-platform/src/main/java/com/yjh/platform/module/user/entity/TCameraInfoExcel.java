package com.yjh.platform.module.user.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.yjh.platform.common.annotation.ExcelExtend;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/5/25
 * @since [产品/模块版本] （可选）
 */
@Data
public class TCameraInfoExcel implements Serializable {

    @ExcelProperty(value = "所属区域")
    private String regionName;

    @ExcelProperty(value = "PMS编码")
    @ExcelExtend
    private String pmsId;

    private Integer cameraType;

    @ExcelProperty(value = "设备类型")
    @ExcelExtend(dictType = "cameraType", convent = "Str")
    private String cameraTypeStr;

    private Integer cameraModel;

    @ExcelProperty(value = "设备型号")
    @ExcelExtend(dictType = "cameraModel", convent = "Str")
    private String cameraModelStr;

    @ExcelProperty(value = "设备名称")
    private String cameraName;

    @ExcelProperty(value = "设备别名")
    private String aliasName;

    @ExcelProperty(value = "IP")
    private String cameraIp;

    @ExcelProperty(value = "端口")
    private Integer port;

    private Integer recordId;

    @ExcelProperty(value = "录像机")
    private String recordName;

    @ExcelProperty(value = "红外测温端口")
    @ExcelExtend
    private Integer infreadPort;

    @ExcelProperty(value = "用户名")
    private String cameraManager;

    @ExcelProperty(value = "密码")
    private String cameraCode;

    @ExcelProperty(value = "录像机通道号")
    private Integer channelNum;

    @ExcelProperty(value = "使用单位")
    @ExcelExtend
    private String unit;

    @ExcelProperty(value = "安装位置")
    @ExcelExtend
    private String address;

    @ExcelProperty(value = "生产厂家")
    @ExcelExtend(dictType = "cameraVendor", convent = "Str")
    private String vendorIdStr;

    private Integer vendorId;

    @ExcelProperty(value = "经度")
    @ExcelExtend
    private String longitude;

    @ExcelProperty(value = "纬度")
    @ExcelExtend
    private String latitude;

    private Integer isControl;

    @ExcelProperty(value = "是否可控")
    @ExcelExtend(require = true, convent = "Str")
    private String isControlStr;

    @ExcelProperty(value = "投运时间")
    @ExcelExtend
    private Date commissionDate;

}
