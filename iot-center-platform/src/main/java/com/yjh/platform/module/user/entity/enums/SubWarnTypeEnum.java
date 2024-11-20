package com.yjh.platform.module.user.entity.enums;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/2/24
 * @since [产品/模块版本] （可选）
 */
public enum SubWarnTypeEnum {
    DEVICE("2","设备告警"),
    INSPECTION("1","巡视告警");

    private String typeCode;
    private String typeName;

    SubWarnTypeEnum(String typeCode, String typeName) {
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public String getTypeCode() {
        return typeCode;
    }


    public String getTypeName() {
        return typeName;
    }

}
