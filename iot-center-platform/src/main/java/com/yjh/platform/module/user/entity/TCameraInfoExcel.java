package com.yjh.platform.module.user.entity;

import com.alibaba.excel.annotation.ExcelProperty;
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
    private String pmsId;

    private Integer cameraType;

    @ExcelProperty(value = "设备类型")
    private String cameraTypeStr;

    private Integer cameraModel;

    @ExcelProperty(value = "设备型号")
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
    private Integer infreadPort;

    @ExcelProperty(value = "用户名")
    private String cameraManager;

    @ExcelProperty(value = "密码")
    private String cameraCode;

    @ExcelProperty(value = "录像机通道号")
    private Integer channelNum;

    @ExcelProperty(value = "使用单位")
    private String unit;

    @ExcelProperty(value = "安装位置")
    private String address;

    @ExcelProperty(value = "生产厂家")
    private String vendorIdStr;

    private Integer vendorId;

    @ExcelProperty(value = "经度")
    private String longitude;

    @ExcelProperty(value = "纬度")
    private String latitude;

    private Integer isControl;

    @ExcelProperty(value = "是否可控")
    private String isControlStr;

    @ExcelProperty(value = "投运时间")
    private Date commissionDate;

}
