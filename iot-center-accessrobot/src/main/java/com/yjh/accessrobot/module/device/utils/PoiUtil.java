package com.yjh.accessrobot.module.device.utils;



import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * @author tt
 */
public class PoiUtil {
    /**
     * 创建表格标题样式：无border，字体大小fontHeight，不加粗，无背景色，垂直居中
     */
    public static HSSFCellStyle createHeadStyle(HSSFWorkbook hb, short fontHeight) {
        Assert.notNull(hb);
        // 设置字体
        HSSFFont font = hb.createFont();
        font.setFontHeightInPoints(fontHeight);            // 字体高度
        font.setColor(HSSFFont.COLOR_NORMAL);            // 字体颜色

        // 设置单元格类型
        HSSFCellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);    // 垂直布局：居中
        style.setWrapText(true);
        return style;
    }

    /**
     * 背景 蓝色
     *
     * @param hb
     * @return
     */
    public static HSSFCellStyle createTableHeadStyle(HSSFWorkbook hb) {
        Assert.notNull(hb);
        // 设置字体
        HSSFFont font = hb.createFont();
        font.setFontHeightInPoints((short) 10);            // 字体高度
        font.setColor(HSSFFont.COLOR_NORMAL);                // 字体颜色
        font.setBold(true);//设置是否加粗



        // 设置单元格类型
        HSSFCellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);            // 水平布局：居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);    // 垂直布局：居中
        style.setWrapText(true);
        setBorder(style);


        HSSFPalette palette = hb.getCustomPalette();
        final short colorIndex = 9;                                //自定义颜色索引取值9—64

        palette.setColorAtIndex(colorIndex, (byte) 0x99, (byte) 0xcc, (byte) 0xff);
        style.setFillForegroundColor(colorIndex);

        return style;
    }

    /**
     * 全边框
     *
     * @param hb
     * @return
     */
    public static HSSFCellStyle createTableCellStyle(HSSFWorkbook hb) {
        Assert.notNull(hb);
        // 设置字体
        HSSFFont font = hb.createFont();
        font.setFontHeightInPoints((short) 11);            // 字体高度
        font.setColor(HSSFFont.COLOR_NORMAL);                // 字体颜色

        // 设置单元格类型
        HSSFCellStyle style = hb.createCellStyle();
        style.setFont(font);
        setBorder(style);

        return style;
    }

    public static HSSFCellStyle createTableCellStyleTime(HSSFWorkbook hb) {
        Assert.notNull(hb);
        // 设置字体
        HSSFFont font = hb.createFont();
        font.setFontHeightInPoints((short) 20);            // 字体高度
        font.setColor(HSSFFont.COLOR_NORMAL);                // 字体颜色

        // 设置单元格类型
        HSSFCellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);//水平居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        setBorder(style);

        return style;
    }

    public static void setTimeCell(HSSFWorkbook hb, HSSFSheet hs, HSSFRow row, int firstRow, int lastRow, int firstCol, int lastCol, String val, HSSFCellStyle style) {
        CellRangeAddress cra = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol); // 起始行, 终止行, 起始列, 终止列
        row.setHeightInPoints(30);
        hs.addMergedRegion(cra);
        style.setBorderBottom(BorderStyle.THIN);//下边框
        style.setBorderLeft(BorderStyle.THIN);//左边框
        style.setBorderRight(BorderStyle.THIN);//右边框
        style.setBorderTop(BorderStyle.THIN); //上边框

        PoiUtil.createCell(row, firstCol, val, style);
    }

    public static HSSFCellStyle createTableCellStyleRed(HSSFWorkbook hb) {
        Assert.notNull(hb);
        // 设置字体
        HSSFFont font = hb.createFont();
        font.setFontHeightInPoints((short) 11);            // 字体高度
        font.setColor(HSSFFont.COLOR_RED);                // 字体颜色

        // 设置单元格类型
        HSSFCellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);

        return style;
    }

    public static HSSFCell createCell(HSSFRow row, int index, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    public static HSSFCell createCell(HSSFRow row, int index, Double value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    public static HSSFCell createCell(HSSFRow row, int index, int value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    public static HSSFCell createCell(HSSFRow row, int index, Date value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    /**
     * 给样式设置border
     */
    private static void setBorder(CellStyle style) {
        Assert.notNull(style);
        style.setBorderLeft(BorderStyle.valueOf((short)1));
        style.setBorderRight(BorderStyle.valueOf((short)1));
        style.setBorderTop(BorderStyle.valueOf((short)1));
        style.setBorderBottom(BorderStyle.valueOf((short)1));
    }

    /**
     * 百万级数据的导出样式
     *
     */

    /**
     * 创建表格标题样式：无border，字体大小fontHeight，不加粗，无背景色，垂直居中
     */
    public static CellStyle createHeadStyle(Workbook hb, short fontHeight) {
        Assert.notNull(hb);
        // 设置字体
        Font font = hb.createFont();
        font.setFontHeightInPoints(fontHeight);            // 字体高度
        font.setColor(Font.COLOR_NORMAL);            // 字体颜色

        // 设置单元格类型
        CellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);    // 垂直布局：居中
        style.setWrapText(true);
        return style;
    }

    /**
     * 背景 蓝色
     *
     * @param hb
     * @return
     */
    public static CellStyle createTableHeadStyle(Workbook hb) {
        Assert.notNull(hb);
        // 设置字体
        Font font = hb.createFont();
        font.setFontHeightInPoints((short) 10);            // 字体高度
        font.setColor(Font.COLOR_NORMAL);                // 字体颜色
        font.setBold(true);//设置是否加粗


        // 设置单元格类型
        CellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);            // 水平布局：居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);    // 垂直布局：居中
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());

        return style;
    }

    /**
     * 全边框
     *
     * @param hb
     * @return
     */
    public static CellStyle createTableCellStyle(Workbook hb) {
        Assert.notNull(hb);
        // 设置字体
        Font font = hb.createFont();
        font.setFontHeightInPoints((short) 11);            // 字体高度
        font.setColor(Font.COLOR_NORMAL);                // 字体颜色

        // 设置单元格类型
       CellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);

        return style;
    }

    public static CellStyle createTableCellStyleTime(Workbook hb) {
        Assert.notNull(hb);
        // 设置字体
        Font font = hb.createFont();
        font.setFontHeightInPoints((short) 20);            // 字体高度
        font.setColor(Font.COLOR_NORMAL);                // 字体颜色

        // 设置单元格类型
        CellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);

        return style;
    }



    public static CellStyle createTableCellStyleRed(Workbook hb) {
        Assert.notNull(hb);
        // 设置字体
        Font font = hb.createFont();
        font.setFontHeightInPoints((short) 11);            // 字体高度
        font.setColor(Font.COLOR_RED);                // 字体颜色

        // 设置单元格类型
        CellStyle style = hb.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);

        return style;
    }

    public static Cell createCell(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }



}
