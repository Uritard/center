package com.yjh.platform.common.utils.smUtil.report;

import com.google.common.base.Strings;
import com.yjh.platform.module.task.entity.TableCellElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author YC
 * @date 2020/10/29 - 15:28
 */
public class ReportHelper {
    private static Logger logger = getLogger(ReportHelper.class);
    private static final IndexedColors DEFAULT_FRONT_COLOR = IndexedColors.BLACK;
    private static final short DEFAULT_BORDER_COLOR_INDEX = IndexedColors.BLACK.getIndex();
    private static final float EN_CHAR_WEIGHT = 0.76667f;
    private static final float CH_CHAR_WEIGHT = 2.7477f;
    private static final float CH_CHAR_BOLD_WEIGHT = 3;
    private static final float CH2EN = CH_CHAR_WEIGHT / EN_CHAR_WEIGHT;

    private final static IndexedColors[] sIndexedColors;

    static {//ported from POI3.16
        sIndexedColors = new IndexedColors[65];
        for (IndexedColors color : IndexedColors.values()) {
            sIndexedColors[color.index] = color;
        }
    }

    private static IndexedColors colorFromIndex(int index) {
        if (index < 0 || index >= sIndexedColors.length) {
            throw new IllegalArgumentException("Illegal IndexedColor index: " + index);
        }
        IndexedColors color = sIndexedColors[index];
        if (color == null) {
            throw new IllegalArgumentException("Illegal IndexedColor index: " + index);
        }
        return color;
    }

    private static String createCacheKey(short fontSize, boolean bold, IndexedColors color,
                                         HorizontalAlignment alignment) {
        return String.valueOf(fontSize) + ((bold) ? "1" : "0") + color.name() + alignment.name();
    }

    private static CellStyle getDefaultCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        final short BLACK_COLOR_INDEX = IndexedColors.BLACK.getIndex();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(BLACK_COLOR_INDEX);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(BLACK_COLOR_INDEX);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(BLACK_COLOR_INDEX);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(BLACK_COLOR_INDEX);
        return style;
    }

    private static CellStyle getCellStyle(
            Workbook wb,
            short fontSize,
            boolean bold,
            IndexedColors color,
            HorizontalAlignment alignment,
            Map<String, CellStyle> styleCache
    ) {
        String key = createCacheKey(fontSize, bold, color, alignment);
        CellStyle style = styleCache.get(key);
        if (style != null)
            return style;

        style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(DEFAULT_BORDER_COLOR_INDEX);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(DEFAULT_BORDER_COLOR_INDEX);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(DEFAULT_BORDER_COLOR_INDEX);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(DEFAULT_BORDER_COLOR_INDEX);
        style.setAlignment(alignment == HorizontalAlignment.LEFT ? HorizontalAlignment.LEFT : HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        Font font = wb.createFont();
        font.setFontHeightInPoints(fontSize);
        font.setFontName("宋体");
        font.setBold(bold);
        font.setColor(color.getIndex());
        style.setFont(font);
        styleCache.put(key, style);
        return style;
    }

    private static IndexedColors getColor(int colorIndex) {
        try {
            return colorIndex != 0 ? colorFromIndex(colorIndex) : DEFAULT_FRONT_COLOR;
        } catch (Exception e) {
            return DEFAULT_FRONT_COLOR;
        }
    }

    private static HorizontalAlignment getAlignment(int align) {
        switch (align) {
            case TableCellElement.ALIGN_LEFT:
                return HorizontalAlignment.LEFT;
            default:
                return HorizontalAlignment.CENTER;
        }
    }

    public static void mergeCell(Sheet sheet, int rowFrom, int rowTo, int columnFrom, int columnTo) {
        sheet.addMergedRegion(new CellRangeAddress(rowFrom, rowTo, columnFrom, columnTo));
    }

    private static int calculateCellWidth(int CHChars, int ENChars, boolean bold) {
        return ((int) (CHChars * (bold ? CH_CHAR_BOLD_WEIGHT : CH_CHAR_WEIGHT) + EN_CHAR_WEIGHT * ENChars)) * 256;
    }
    public static void appendHyperLink(Workbook wb, Cell cell, String url, String linkText) {
        CellStyle hlink_style = wb.createCellStyle();
        Font hlink_font = wb.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);
        hlink_style.setAlignment(HorizontalAlignment.CENTER);

        CreationHelper createHelper = wb.getCreationHelper();
        cell.setCellValue(linkText);

        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(url);
        cell.setHyperlink(link);
        cell.setCellStyle(hlink_style);
    }
    public static boolean createDocument(int rowNum, int columnNum, List<TableCellElement> elements, File dest,String taskId) {
        final SXSSFWorkbook wb = new SXSSFWorkbook(rowNum);
        final Sheet sheet = wb.createSheet("巡检报告");
        final CellStyle defaultCellStyle = getDefaultCellStyle(wb);
        Row row;
        Cell cell;

        Map<String, CellStyle> styleCache = new HashMap<>();
        for (int i = 0; i < rowNum; ++i) {
            row = sheet.createRow(i);
            for (int j = 0; j < columnNum; ++j) {
                row.createCell(j).setCellStyle(defaultCellStyle);
            }
        }

        int[] EnglishChars = new int[columnNum];
        int[] ChineseChars = new int[columnNum];
        int[] maxChars = new int[columnNum];
        boolean[] isBold = new boolean[columnNum];
        for (int i = 0; i < columnNum; ++i) {
            ChineseChars[i] = -1;
            EnglishChars[i] = -1;
            maxChars[i] = -1;
        }

        HashMap<Integer, Boolean> imageColumnMap = new HashMap<>();
        for (TableCellElement element : elements) {
            if (element == null)
                continue;

            int rowIndex = element.getRowStart();
            int colStart = element.getColumnStart();
            int cellType = element.getType();
            boolean crossColumn = colStart != element.getColumnEnd();
            boolean crossRow = rowIndex != element.getRowEnd();
            row = sheet.getRow(rowIndex);
//            if (isEmptyRow(row)){
//                continue;
//            }
            cell = row.getCell(colStart);
            if (cellType == TableCellElement.TYPE_TEXT_STRING ||
                    cellType == TableCellElement.TYPE_NUMBER_STRING) {
                String[] textValues = element.getValue();
                String text = (textValues != null && textValues.length > 0) ?
                        (textValues[0] != null ? textValues[0] : "") : "";
                CellStyle style = getCellStyle(
                        wb,
                        element.getFontHeightInPoint(),
                        element.getBold(),
                        getColor(element.getColorIndex()),
                        getAlignment(element.getAlignment()),
                        styleCache
                );

                cell.setCellStyle(style);
                if (cellType == TableCellElement.TYPE_NUMBER_STRING) {
                    cell.setCellValue(text.contains(".") ? Double.parseDouble(text) : Integer.parseInt(text));
                } else
                    cell.setCellValue(text);

                if (!crossColumn) {
                    String longestLine = text;
                    if (text.contains("\n")) {
                        String[] lines = text.split("\n");
                        if (lines.length > 1) {
                            int maxLengthLineIndex = -1;
                            int maxLineLength = -1;
                            for (int i = 0; i < lines.length; ++i) {
                                int length = lines[i].length();
                                if (length > maxLineLength) {
                                    maxLineLength = length;
                                    maxLengthLineIndex = i;
                                }
                            }

                            longestLine = lines[maxLengthLineIndex];
                        }
                    }

                    int textLength = longestLine.length();
                    int ENCharLength = longestLine.replaceAll("[^A-Za-z0-9.:-]+", "").length();
                    int CHCharLength = textLength - ENCharLength;
                    int length = ENCharLength + (int) (CH2EN * CHCharLength);
                    if (length > maxChars[colStart]) {
                        maxChars[colStart] = length;
                        ChineseChars[colStart] = CHCharLength;
                        EnglishChars[colStart] = ENCharLength;
                        isBold[colStart] = element.getBold();
                    }
                }
            } else if (cellType == TableCellElement.TYPE_PICTURE) {
                String[] values = element.getValue();
                if (values == null || values.length == 0) continue;

                final int ANCHOR_DX1 = PoiUtils.pixelToEMU(5);
                final int ANCHOR_DX2 = PoiUtils.pixelToEMU(245);
                final int ANCHOR_DY1 = PoiUtils.pixelToEMU(5);
                final int ANCHOR_DY2 = PoiUtils.pixelToEMU(106);
                final float ROW_HEIGHT = (float) PoiUtils.pixelToPoints(140);
                final int COLUMN_WIDTH = (int) (31.1f * 256);

                boolean[] imageColumnWidthSet = new boolean[columnNum];
                Drawing patriarch = sheet.createDrawingPatriarch();

                imageColumnMap.putIfAbsent(colStart, true);

                if (cell == null) {
                    logger.warn("createDocument: null cell to insert image!");
                    continue;
                }

                String imagePath = element.getValue()[0];

                if (Strings.isNullOrEmpty(imagePath) || !FileUtil.fileExists(imagePath)) continue;
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(imagePath));
                    int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
                    XSSFClientAnchor clientAnchor = new XSSFClientAnchor(
                            ANCHOR_DX1, ANCHOR_DY1,
                            ANCHOR_DX2, ANCHOR_DY2,
                            colStart, rowIndex,
                            colStart, rowIndex);
                    clientAnchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                    patriarch.createPicture(clientAnchor, pictureIdx);
                    row.setHeightInPoints(ROW_HEIGHT);
                    if (!imageColumnWidthSet[colStart]) {
                        sheet.setColumnWidth(colStart, COLUMN_WIDTH);
                        imageColumnWidthSet[colStart] = true;
                    }
                    if (StringUtils.isNotEmpty(taskId)){
                        //插入超链接
                        String oriImg = element.getValue()[1];
                        if (StringUtils.isNotEmpty(oriImg)){
                            String[] fileStr = oriImg.split("/");
                            if (fileStr.length > 1){
                                String fileUrl = fileStr[fileStr.length-1];

                                appendHyperLink(wb, cell, taskId+"/"+fileUrl, fileUrl);
                            }
                        }


                    }


                } catch (Exception e) {
                    logger.info("createDocument, error to insert img.", e);
                }

            }
            if (crossColumn || crossRow) {
                mergeCell(sheet, rowIndex, element.getRowEnd(), colStart, element.getColumnEnd());
            }

//            final int cellHeight = element.getCellHeight();
//            if (cellHeight > 0) {
//                row.setHeightInPoints(cellHeight);
//            }
        }

        boolean useAutoSize = true;//rowNum <= 1000;

        for (int i = 0; i < columnNum; ++i) {
            if (imageColumnMap.containsKey(i))
                continue;

            if (useAutoSize) {
                try {
                    sheet.autoSizeColumn(i,true);
                    continue;
                } catch (Exception e) {
                    logger.info("Failed to autoSize for column " + i);
                }
            }

            if (maxChars[i] == -1)
                continue;

                sheet.setColumnWidth(i, calculateCellWidth(ChineseChars[i], EnglishChars[i], isBold[i]));
        }
        logger.info("Set Column Size done. AutoSize ? " + useAutoSize);

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(dest);
            wb.write(fileOut);
            wb.dispose();
        } catch (Exception e) {
            logger.info("createDocument: ", e);
        } finally {
            IOUtils.closeQuietly(fileOut);
        }

        styleCache.clear();
        return true;
    }

    @SuppressWarnings("deprecation")
    public static boolean isEmptyRow(Row row) {
        if (row == null || row.toString().isEmpty()) {
            return true;
        } else {
            Iterator<Cell> it = row.iterator();
            boolean isEmpty = true;
            while (it.hasNext()) {
                Cell cell = it.next();
                if (cell != null || cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }
}
