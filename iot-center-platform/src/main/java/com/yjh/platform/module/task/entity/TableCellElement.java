package com.yjh.platform.module.task.entity;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author YC
 * @date 2020/10/29 - 15:14
 */
public class TableCellElement {
    public static final int TYPE_TITLE_STRING = 0;
    public static final int TYPE_TEXT_STRING = 1;
    public static final int TYPE_NUMBER_STRING = 2;
    public static final int TYPE_PICTURE = 100;
    public static final int TYPE_AUDIO = 200;
    public static final int TYPE_VIDEO = 300;

    public static final int ALIGN_DEFAULT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_LEFT = 2;

    public static final short DEFAULT_FONT_HEIGHT_IN_POINT = 12;

    public static final int DEFAULT_IMAGE_HEIGHT = 300;
    public static final int DEFAULT_IMAGE_WIDTH = 60 * 256;

    private int mRowStart;
    private int mRowEnd;
    private int mColumnStart;
    private int mColumnEnd;
    private String[] mValue;
    private int mType;
    private short mFontHeightInPoint;
    private int mCellHeight;
    private int mCellWidth;
    private int mAlignment;
    private boolean mBold;
    private int mColorIndex;
    private int mTableIndex;
    private static DecimalFormat sDecimalFormat = new DecimalFormat("0.##");

    public TableCellElement(int rowStart, int rowEnd, int columnStart, int columnEnd, int tableIndex, String[] value, int type) {
        this(rowStart, rowEnd, columnStart, columnEnd, value, type, DEFAULT_FONT_HEIGHT_IN_POINT, -1, -1, ALIGN_DEFAULT, tableIndex);
    }

    // 图像
    public TableCellElement(int rowStart, int rowEnd, int columnStart, int columnEnd, String[] value, int type) {
        this(rowStart, rowEnd, columnStart, columnEnd, value, type, DEFAULT_FONT_HEIGHT_IN_POINT, -1, -1, ALIGN_DEFAULT, -1);
    }

    public TableCellElement(int rowStart, int rowEnd, int columnStart, int columnEnd, String[] value, int type, int alignment) {
        this(rowStart, rowEnd, columnStart, columnEnd, value, type, DEFAULT_FONT_HEIGHT_IN_POINT, -1, -1, alignment, -1);
    }

    public TableCellElement(int rowStart, int rowEnd, int columnStart, int columnEnd, String[] value, int type, short fontHeightInPoint) {
        this(rowStart, rowEnd, columnStart, columnEnd, value, type, fontHeightInPoint, -1, -1, ALIGN_DEFAULT, -1);
    }

    public TableCellElement(int rowStart, int rowEnd, int columnStart, int columnEnd, String[] value,
                            int type, short fontHeightInPoint, int cellHeight, int cellWidth, int alignment) {
        this(rowStart, rowEnd, columnStart, columnEnd, value,
                type, fontHeightInPoint, cellHeight, cellWidth, alignment, -1);
    }

    public TableCellElement(int rowStart, int rowEnd, int columnStart, int columnEnd, String[] value,
                            int type, short fontHeightInPoint, int cellHeight, int cellWidth, int alignment, int tableIndex) {
        mRowStart = rowStart;
        mRowEnd = rowEnd;
        mColumnStart = columnStart;
        mColumnEnd = columnEnd;
        mValue = value;
        mType = type;
        mFontHeightInPoint = fontHeightInPoint;
        mCellHeight = cellHeight;
        mCellWidth = cellWidth;
        mAlignment = alignment;
        mTableIndex = tableIndex;

        if (type == TYPE_NUMBER_STRING && mValue != null) {
            String text = mValue[0] != null ? mValue[0].trim() : "";
            if (!text.isEmpty()) {
                String unit = text.replaceAll("[0-9+.-]+", "");
                if (text.contains(".")) {
                    String numberVal = null;
                    String number = text.replaceAll("[^0-9+.-]+", "");

                    boolean validNumber = false;
                    try {
                        numberVal = sDecimalFormat.format(Double.parseDouble(number));
                        validNumber = true;
                    } catch (Exception e) {
                        mType = TYPE_TEXT_STRING;
                    }

                    if (validNumber) {
                        if (unit.length() != 0) {
                            mType = TYPE_TEXT_STRING;
                            mValue[0] = numberVal + unit;
                        } else {
                            if (numberVal != null)
                                mValue[0] = numberVal;
                            else
                                mType = TYPE_TEXT_STRING;
                        }
                    }
                } else {
                    if (unit.length() != 0)
                        mType = TYPE_TEXT_STRING;
                }
            } else {
                mType = TYPE_TEXT_STRING;
                mValue[0] = "";
            }
        }
    }

    public short getFontHeightInPoint() {
        return mFontHeightInPoint;
    }

    public void setFontHeightInPoint(short fontHeightInPoint) {
        mFontHeightInPoint = fontHeightInPoint;
    }

    public int getCellHeight() {
        return mCellHeight;
    }

    public void setCellHeight(int cellHeight) {
        mCellHeight = cellHeight;
    }

    public int getCellWidth() {
        return mCellWidth;
    }

    public void setCellWidth(int cellWidth) {
        mCellWidth = cellWidth;
    }

    public int getRowStart() {
        return mRowStart;
    }

    public void setRowStart(int rowStart) {
        mRowStart = rowStart;
    }

    public int getRowEnd() {
        return mRowEnd;
    }

    public void setRowEnd(int rowEnd) {
        mRowEnd = rowEnd;
    }

    public int getColumnStart() {
        return mColumnStart;
    }

    public void setColumnStart(int columnStart) {
        mColumnStart = columnStart;
    }

    public int getColumnEnd() {
        return mColumnEnd;
    }

    public void setColumnEnd(int columnEnd) {
        mColumnEnd = columnEnd;
    }

    public String[] getValue() {
        return mValue;
    }

    public void setValue(String[] value) {
        mValue = value;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getAlignment() {
        return mAlignment;
    }

    public void setAlignment(int alignment) {
        mAlignment = alignment;
    }

    public TableCellElement setBold(boolean bold) {
        mBold = bold;
        return this;
    }

    public boolean getBold() {
        return mBold;
    }

    public TableCellElement setColorIndex(int colorIndex) {
        mColorIndex = colorIndex;
        return this;
    }

    public int getColorIndex() {
        return mColorIndex;
    }

    public int getTableIndex() {
        return mTableIndex;
    }

    @Override
    public String toString() {
        return "TableCellElement{" +
                "mRowStart=" + mRowStart +
                ", mRowEnd=" + mRowEnd +
                ", mColumnStart=" + mColumnStart +
                ", mColumnEnd=" + mColumnEnd +
                ", mValue=" + Arrays.toString(mValue) +
                ", mType=" + mType +
                '}';
    }
}
