package com.yjh.platform.common.handler;

import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;

public class CustomCellWriteHeightConfig extends AbstractRowHeightStyleStrategy {
    /**
     * 默认高度
     */
    private static final Integer DEFAULT_HEIGHT = 300;

    @Override
    protected void setHeadColumnHeight(Row row, int relativeRowIndex) {
    }

    @Override
    protected void setContentColumnHeight(Row row, int relativeRowIndex) {
        Iterator<Cell> cellIterator = row.cellIterator();
        if (!cellIterator.hasNext()) {
            return;
        }

        // 默认为 1行高度
        Integer maxHeight = 1;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            switch (cell.getCellTypeEnum()) {
                case STRING:
                    if (cell.getStringCellValue().indexOf("\n") != -1) {
                        int length = cell.getStringCellValue().split("\n").length;
                        maxHeight = Math.max(maxHeight, length);
                    }
                    break;
                default:
                    break;
            }
        }

        row.setHeight((short) (maxHeight * DEFAULT_HEIGHT));
    }
}
