package com.yjh.platform.common.utils.Report;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import lombok.EqualsAndHashCode;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = false)
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ExportUtil {

    /**
     * 设置单元格居中
     *
     * @return 样式
     */
    public static HorizontalCellStyleStrategy getCellStyle() {
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        return new HorizontalCellStyleStrategy(new WriteCellStyle(), contentWriteCellStyle);
    }

    /**
     * 操作任务记录详情展示excel的列
     *
     * @return 列信息
     */
    public static Set<String> getOperationTaskDetailModel() {
        Set<String> OperationTaskDetailDataCol = new HashSet<>();
        OperationTaskDetailDataCol.add("cruiseName");
        OperationTaskDetailDataCol.add("resultNum");
        OperationTaskDetailDataCol.add("confirmPicpath");
        OperationTaskDetailDataCol.add("picpath");
        OperationTaskDetailDataCol.add("startTime");
        OperationTaskDetailDataCol.add("userName");
        OperationTaskDetailDataCol.add("userId");
        return OperationTaskDetailDataCol;
    }
}
