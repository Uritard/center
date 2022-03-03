package com.yjh.platform.common.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class MyUrlImageConverter implements Converter<String> {
    private static final int CONNECT_TIME = 5;

    private static final int READ_BYTE = 1024;

    @Override
    public Class supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.IMAGE;
    }

    @Override
    public String convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        throw new UnsupportedOperationException("Cannot convert images to string");
    }

    //图片失效处理
    @Override
    public CellData convertToExcelData(String value, ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) throws IOException {
        File file = new File(value);
        if (file.exists()) {
            //文件存在
            return new CellData(FileUtils.readFileToByteArray(new File(value)));
        }

        return new CellData(FileUtils.readFileToByteArray(new File(this.getClass().getResource("/").getPath()
                + "nopic2.png")));
    }

}
