package com.yjh.platform.common.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Excel样式工具类
 * @author YC
 * @date 2020/8/31 - 20:59
 */
public class  ExcelFormatUtil {
    /**
     * 设置表头样式
     */
    public static CellStyle headSytle(XSSFWorkbook workbook){
        // 设置style1的样式，此样式运用在第二行
        CellStyle style1 = workbook.createCellStyle();// cell样式
        Font font1 = workbook.createFont();// 创建一个字体对象
        font1.setFontHeightInPoints((short) 10);// 设置字体的高度
        font1.setBold(true);// 粗体显示
        style1.setFont(font1);// 设置style1的字体
        style1.setWrapText(true);// 设置自动换行
        style1.setAlignment(HorizontalAlignment.CENTER);// 设置单元格字体显示居中(左右)
        style1.setVerticalAlignment(VerticalAlignment.CENTER);// 设置单元格字体显示居中(上下)
        return style1;
    }
    /**
     *设置表头
     */
    public static void initTitleEX(Sheet sheet, CellStyle header, String title[]) {

        Row row0 = sheet.createRow(1);
        row0.setHeight((short) 550);
        for(int j = 0;j<title.length; j++) {
            XSSFCell cell = (XSSFCell) row0.createCell(j);
            //设置每一列的字段名
            cell.setCellValue(title[j]);
            cell.setCellStyle(header);
            sheet.setColumnWidth(j, 5000);
        }
    }
    /**
     *设置填写样例
     */
    public  static void initExample(Sheet sheet,CellStyle header,String example[] ){

        Row row0 = sheet.createRow(2);
        for (int j = 0;j < example.length;j++ ){
            XSSFCell cell = (XSSFCell) row0.createCell(j);
            cell.setCellValue(example[j]);
            cell.setCellStyle(header);
            sheet.setColumnWidth(j, 5000);

        }
    }
    /**
     * 设置填写样例样式
     */
    public static CellStyle exampleSytle(XSSFWorkbook workbook){
        // 设置style1的样式，此样式运用在第3行
        CellStyle style1 = workbook.createCellStyle();// cell样式
        Font font1 = workbook.createFont();// 创建一个字体对象
        font1.setColor(Font.COLOR_RED);
        style1.setFont(font1);// 设置style1的字体
        style1.setWrapText(true);// 设置自动换行
        style1.setAlignment(HorizontalAlignment.CENTER);// 设置单元格字体显示居中(左右)
        style1.setVerticalAlignment(VerticalAlignment.CENTER);// 设置单元格字体显示居中(上下)
        return style1;
    }
    /**
     *设置标题
     */
    public  static void initTitle(Sheet sheet,CellStyle header,String titleName ){

        Row row0 = sheet.createRow(0);
        row0.setHeight((short) 900);
        XSSFCell cell = (XSSFCell) row0.createCell(0);
        cell.setCellStyle(header);
        cell.setCellValue(titleName);
    }

    /**
     * 设置报表标题样式
     */
    public static CellStyle titleSytle(XSSFWorkbook workbook){
        // 设置style1的样式，此样式运用在第一行
        CellStyle style1 = workbook.createCellStyle();// cell样式
        Font font1 = workbook.createFont();// 创建一个字体对象
        font1.setFontHeightInPoints((short) 15);// 设置字体的高度
        font1.setBold(true);// 粗体显示
        style1.setFont(font1);// 设置style1的字体
        style1.setWrapText(true);// 设置自动换行
        style1.setAlignment(HorizontalAlignment.LEFT);// 设置单元格字体显示居左
        style1.setVerticalAlignment(VerticalAlignment.CENTER);// 设置单元格字体显示居中(上下)
        return style1;
    }
    /**
     * 判断文件夹是否存在，如果不存在则新建
     */
    public static void isChartPathExist(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /*
    * 生成excel
    * */
    public static InputStream export(String[] titleList, String titleName, String[] example) throws IOException {
        String filepath = "..//templateFile//";
        String fileUrl = "192.168.9.40:10086/Service/";
        String resultUrl = null;
        ByteArrayOutputStream output = null;
        InputStream inputStream1 = null;

            XSSFWorkbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename =  format.format(new Date())+ titleName + ".xlsx";

            //标题
            int titleListSize = titleList.length;
            //合并第一行
            CellRangeAddress cellRangeAddress0 = new CellRangeAddress(0,0,0,titleListSize-1);
            sheet.addMergedRegion(cellRangeAddress0);

            CellStyle hHeader = ExcelFormatUtil.headSytle(wb);// 设置报表头样式
            CellStyle tHeader = ExcelFormatUtil.titleSytle(wb);//设置报表标题样式
            CellStyle EHeader = ExcelFormatUtil.exampleSytle(wb);//设置填写样例样式

            // 设置表头
            ExcelFormatUtil.initTitleEX(sheet, hHeader, titleList);
            //设置标题
            String titleName1 = titleName+"(第三行为填写案例)";
            ExcelFormatUtil.initTitle(sheet,tHeader,titleName1);
            //设置填写样例
            ExcelFormatUtil.initExample(sheet,EHeader,example);

            //判断是否存在目录，不存在则创建
            ExcelFormatUtil.isChartPathExist(filepath);
            //输出Excel文件1
//            FileOutputStream output=new FileOutputStream(filepath+filename);
//            wb.write(output);//写入磁盘
//            output.close();
//            resultUrl = fileUrl+filename;
        try {
            output = new ByteArrayOutputStream();
            wb.write(output);
            inputStream1 = new ByteArrayInputStream(output.toByteArray());
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (output != null) {
                    output.close();
                    if (inputStream1 != null)
                        inputStream1.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return inputStream1;
    }

}
