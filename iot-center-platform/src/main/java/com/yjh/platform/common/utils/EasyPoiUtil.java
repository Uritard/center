package com.yjh.platform.common.utils;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author YC
 * @date 2020/10/20 - 15:46
 */
public class EasyPoiUtil {

    public static void exportExcel(List<?> list, String title, String sheetName, String fileName,
                                   Class<?> pojoClass, boolean isCreateHeader, boolean isNeedSignature ) {
        ExportParams exportParams = new ExportParams(title, sheetName);
        exportParams.setCreateHeadRows(isCreateHeader);

        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        //页底签名
        if (isNeedSignature) {
            Sheet sheet = workbook.getSheet(sheetName);
            int lastRowNum = sheet.getLastRowNum();
            short lastCellNum = sheet.getRow(2).getLastCellNum();
            //合并单元格
            CellRangeAddress rangeAddress = new CellRangeAddress(lastRowNum + 1,lastRowNum + 2, 0, lastCellNum - 1);
            sheet.addMergedRegion(rangeAddress);
            //创建一个签名单元格
            Cell signatureCell = sheet.createRow(lastRowNum + 1).createCell(0);
            //创建单元格并设置样式
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.RIGHT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            signatureCell.setCellStyle(style);
            signatureCell.setCellValue("签名： 嬴政 日期:  公元前221年   ");
        }
        OutputStream out = null;//创建输出流
        try {
            File file = new File("../templateFile");
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName+".xls");
            workbook.write(fileOutputStream);
            System.out.println("<----------导出Excel成功---------->");
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("---------->导出Excel异常---------->");
        }
    }
//        Runtime.getRuntime().exec(transUrl);

}
