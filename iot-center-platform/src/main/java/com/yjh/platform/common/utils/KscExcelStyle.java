package com.yjh.platform.common.utils;

import org.apache.poi.ss.usermodel.*;

/**
 * @author YC
 * @date 2020/10/20 - 15:49
 */
public class KscExcelStyle  {

    public KscExcelStyle(Workbook workbook) {
        super();
    }
    /**
     * 覆盖此方法实现自定义HeaderStyle
     * @param i
     * @return
     */
//    @Override
//    public CellStyle getHeaderStyle(short i) {
//        CellStyle style = getBaseCellStyle(workbook);
//        style.setFont(getFont(workbook, (short) 12, true));
//        return style;

        /**
         * 覆盖此方法实现自定义TitleStyle
         * @param i
         * @return
         */
//    }
//    @Override
//    public CellStyle getTitleStyle(short i) {
//        CellStyle style = getBaseCellStyle(workbook);
//        style.setFont(getFont(workbook, (short) 11, false));
//        //背景色
//        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        return style;
//    }

    private CellStyle getBaseCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        //下边框
        style.setBorderBottom(BorderStyle.THIN);
        //左边框
        style.setBorderLeft(BorderStyle.THIN);
        //上边框
        style.setBorderTop(BorderStyle.THIN);
        //右边框
        style.setBorderRight(BorderStyle.THIN);
        //水平居中
        style.setAlignment(HorizontalAlignment.CENTER);
        //上下居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置自动换行
        style.setWrapText(true);
        return style;
    }

    /**
     * 字体样式
     *
     * @param size   字体大小
     * @param isBold 是否加粗
     * @return
     */
    private Font getFont(Workbook workbook, short size, boolean isBold) {
        Font font = workbook.createFont();
        //字体样式
        font.setFontName("宋体");
        //是否加粗
        font.setBold(isBold);
        //字体大小
        font.setFontHeightInPoints(size);
        return font;
    }


}
