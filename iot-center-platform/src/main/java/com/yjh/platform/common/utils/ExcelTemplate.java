package com.yjh.platform.common.utils;

import java.util.Set;

/**
 * @author YC
 * @date 2020/8/29 - 11:23

 * Excel模板
 */

public class ExcelTemplate {

    public String ColumnName;//列名
    public boolean IsRequire;//是否必填
    public String ColumnType;//列类型

    public ExcelTemplate(String columnName, boolean isRequire, String columnType) {
        ColumnName = columnName;
        IsRequire = isRequire;
        ColumnType = columnType;
    }

    @Override
    public String toString() {
        return "ExcelTemplate{" +
                "ColumnName='" + ColumnName + '\'' +
                ", IsRequire=" + IsRequire +
                ", ColumnType='" + ColumnType + '\'' +
                '}';
    }
}
