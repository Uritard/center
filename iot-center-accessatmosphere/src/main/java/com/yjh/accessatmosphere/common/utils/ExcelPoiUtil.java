package com.yjh.accessatmosphere.common.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class ExcelPoiUtil {

    /**
     *
     * @param fileName 文件名称
     * @param sheetName sheet页名称
     * @param titleList 列名
     * @param titleCodeList 列名对应的code
     * @param parpamtsList 可选择参数列(需与列名一比一)
     */
    public static HSSFWorkbook createExcel(String fileName,String sheetName,List<String> titleList,List<String> titleCodeList,List<List<String>> parpamtsList) {
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFSheet sheet = setSheetBaseInfoExcel(sheetName,36,30,wb);

        //设置前三行冻结
        sheet.createFreezePane(0,3,0,3);
        //此处是设置 第一行 合并单元格的个数(标题行)
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, (titleList.size()-1)));

        // 产生表格标题行
        HSSFRow rowm = sheet.createRow(0);
        HSSFCell cellTiltle = rowm.createCell(0);
        HSSFCellStyle columnTopStyle = getColumnTopStyle(wb);//获取列头样式对象
        cellTiltle.setCellStyle(columnTopStyle);
        cellTiltle.setCellValue(sheetName);
        rowm.setHeightInPoints(25);

        //隐藏第二行，并填写code值
        HSSFRow row_titleCode = sheet.createRow(1);
        for(int i=0;i<titleCodeList.size();i++) {
            HSSFCell cellTiltleCode = row_titleCode.createCell(i);
            cellTiltleCode.setCellValue(titleCodeList.get(i));
        }
        row_titleCode.setZeroHeight(true);

        //设置第三行标题 标题列
        HSSFRow row_titleName = sheet.createRow(2);
        HSSFCellStyle style = getStyle(wb);
        for (int i=0;i<titleList.size();i++){
            HSSFCell cell_titleName = row_titleName.createCell(i);
            cell_titleName.setCellValue(titleList.get(i));
            cell_titleName.setCellStyle(style);
        }

        //设置默认下拉内容
        if(parpamtsList!=null && parpamtsList.size()>0) {
            for(int i=0;i<parpamtsList.size();i++) {
                if(parpamtsList.get(i)==null || parpamtsList.get(i).size()<1) {
                    continue;
                }
                //设置下拉控制的范围
                CellRangeAddressList regions = new CellRangeAddressList(3, 999, i, i);
                // 生成下拉框内容
                String[] strings = new String[parpamtsList.get(i).size()];
                parpamtsList.get(i).toArray(strings);
                DVConstraint constraint = DVConstraint.createExplicitListConstraint(strings);
                // 绑定下拉框和作用区域
                HSSFDataValidation data_validation = new HSSFDataValidation(regions, constraint);
                // 对sheet页生效
                sheet.addValidationData(data_validation);
            }
        }

        return wb;

    }

    /**设计excel定义列数、列宽和标头信息*/
    private static HSSFSheet setSheetBaseInfoExcel(String excelName,int columWith,int rowHight,HSSFWorkbook wb){
        HSSFSheet sheet = wb.createSheet(excelName);
        sheet.setDefaultColumnWidth(columWith);
        sheet.setDefaultRowHeightInPoints(rowHight);
//        String columWiths[] =  columWithMsg.split(",");
//        for (int i=0;i<columWiths.length;i++){
//            sheet.setColumnWidth(i,Integer.valueOf(columWiths[i])*512);
//        }
        return sheet;
    }

    /*
     * 列表首页的大title样式
     */
    private static HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {

        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short)16);
        //字体加粗
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
//        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
//        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        //设置背景颜色
//        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        return style;
    }

    /*
     * 标题 列的样式
     */
    private static HSSFCellStyle getStyle(HSSFWorkbook workbook) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short)12);
        //字体加粗
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
//        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
//        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        //设置背景颜色
//        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        return style;
    }

    /**
     * 获取文档
     * @param sheetname 表单名
     * @param title 标题栏
     * @param content 内容
     * @return
     */
    public static XSSFWorkbook getWorkbook (String sheetname, String[] title, List<List<String>> content, List<Integer> mergeSize) {
        //新建文档实例
        XSSFWorkbook workbook = new XSSFWorkbook();

        //在文档中添加表单
        XSSFSheet sheet = workbook.createSheet(sheetname);
        //sheet.protectSheet("password");
        //创建单元格格式，并设置居中
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setLocked(false);
        XSSFCellStyle style2 = workbook.createCellStyle();
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        style2.setLocked(false);
        Map<Integer,Integer> maxWidth = new HashMap<Integer,Integer>();

        //创建第一行，用于填充标题
        XSSFRow titleRow = sheet.createRow(0);
        //填充标题
        for (int i=0 ; i<title.length ; i++) {
            //创建单元格
            XSSFCell cell = titleRow.createCell(i);
            //设置单元格内容
            cell.setCellValue(title[i]);
            //设置单元格样式
            cell.setCellStyle(style);
            maxWidth.put(i,cell.getStringCellValue().getBytes().length  * 256 + 200);
        }

        //填充内容
        if(content!=null&&content.size()>0){
            for (int i=0 ; i<content.size() ; i++) {
                //创建行
                XSSFRow row = sheet.createRow(i+1);
                //遍历某一行
                for (int j=0 ; j<content.get(i).size() ; j++) {
                    //创建单元格
                    XSSFCell cell = row.createCell(j);
                    //设置单元格内容
                    cell.setCellValue(content.get(i).get(j));
                    //设置单元格样式
                    if(j==6){
                        cell.setCellStyle(style2);
                    }else{
                        cell.setCellStyle(style);
                    }
                    int length = cell.getStringCellValue().getBytes().length  * 256 + 200;
                    //这里把宽度最大限制到15000
                    if (length>15000){
                        length = 15000;
                    }
                    maxWidth.put(j,Math.max(length,maxWidth.get(j)));
                }
            }
        }

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直
        int begin=1;
        if(mergeSize!=null&&mergeSize.size()>0){
            for(Integer size:mergeSize){
                int end=begin+size-1;
                for(int i=1;i<=3;i++){
                    CellRangeAddress region = new CellRangeAddress(begin, end, i, i);
                    sheet.addMergedRegion(region);
                    XSSFCell nowCell = sheet.getRow(begin).getCell(i);
                    nowCell.setCellStyle(style);
                }
                begin=end+1;
            }
        }

        for (int i = 0; i < title.length; i++) {
            sheet.setColumnWidth(i,maxWidth.get(i));
        }
        //返回文档实例
        return workbook;
    }


    public static String getValue(Cell hssfCell) {
        if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
            // 返回布尔类型的值
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
            // 返回数值类型的值
            Object inputValue = null;// 单元格值
            Long longVal = Math.round(hssfCell.getNumericCellValue());
            Double doubleVal = hssfCell.getNumericCellValue();
            if(Double.parseDouble(longVal + ".0") == doubleVal){   //判断是否含有小数位.0
                inputValue = longVal;
            }
            else{
                inputValue = doubleVal;
            }
            DecimalFormat df = new DecimalFormat("#.####");    //格式化为四位小数，按自己需求选择；
            return String.valueOf(df.format(inputValue));      //返回String类型
        } else {
            // 返回字符串类型的值
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }
}
