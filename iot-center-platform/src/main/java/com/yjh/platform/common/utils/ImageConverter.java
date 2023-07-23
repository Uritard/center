package com.yjh.platform.common.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.FileUtils;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ImageConverter implements Converter<String> {
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
        String isPath = "/home/yjh_iot_center/";
        if (!file.isDirectory() && file.exists()) {
            //文件存在
            String picCompression = FileUtil.picCompression(value);
            if (StringUtils.isEmpty(picCompression)) {
                return new CellData("");
            }
            // 判断文件大小是否为 0 kb
            if (!FileUtil.hasEffective(value)) {
                return new CellData("");
            }
            return new CellData(FileUtils.readFileToByteArray(new File(picCompression)));
        } else if (value.contains(isPath)) {
            return new CellData(FileUtils.readFileToByteArray(new File(Objects.requireNonNull(this.getClass().getResource("/")).getPath()
                    + "nopic2.png")));
        }
        return new CellData(value);
    }

}
