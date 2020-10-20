package com.yjh.platform.common.utils;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author YC
 * @date 2020/10/20 - 15:46
 */
public class EasyPoiUtil {

    public static void exportExcel(List<?> list, String title, String sheetName, String fileName,
                                   Class<?> pojoClass, boolean isCreateHeader, boolean isNeedSignature ,
                                   HttpServletResponse response)  {
        ExportParams exportParams = new ExportParams(title, sheetName);
        System.out.println("<----------exportParams---------->:"+exportParams);

        exportParams.setCreateHeadRows(isCreateHeader);

        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        System.out.println("<----------workbook---------->:"+workbook);
        //如果需页底要有签名，则需要合并单元格并为其设置样式和签名模板内容
        if (isNeedSignature) {
            Sheet sheet = workbook.getSheet(sheetName);
            int lastRowNum = sheet.getLastRowNum();
            short lastCellNum = sheet.getRow(2).getLastCellNum();
            CellRangeAddress rangeAddress = new CellRangeAddress(lastRowNum+1 , lastRowNum+2 , 0, lastCellNum-1);
            sheet.addMergedRegion(rangeAddress);
            Cell signatureCell = sheet.createRow(lastRowNum + 1).createCell(0);
            CellStyle signatureCellStyle = workbook.createCellStyle();
            signatureCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            signatureCell.setCellStyle(signatureCellStyle);
            signatureCell.setCellValue("签名：             日期:               ");
        }
        try {
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            System.out.println("导出Excel异常");
        }

//        Runtime.getRuntime().exec(transUrl);


    }

}
