package com.yjh.platform.common.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class ImageConverter implements Converter<ImageFile> {

    @Override
    public Class supportJavaTypeKey() {
        return ImageFile.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.IMAGE;
    }

    @Override
    public ImageFile convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        throw new UnsupportedOperationException("Cannot convert images to string");
    }

    /**
     * 图片压缩处理
     */
    @Override
    public CellData convertToExcelData(ImageFile img, ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) throws IOException {
        String value = img.getPath();
        if (FileUtil.hasEffective(value)) {
            //文件存在
            String picCompression = FileUtil.picCompression(value);
            if (StringUtils.isNotEmpty(picCompression)) {
                return new CellData(FileUtils.readFileToByteArray(new File(picCompression)));
            }
        }
        return new CellData("");
    }

}
