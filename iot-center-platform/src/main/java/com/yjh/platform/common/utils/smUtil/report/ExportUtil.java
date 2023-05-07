package com.yjh.platform.common.utils.smUtil.report;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.util.*;

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
        OperationTaskDetailDataCol.add("origConfirmPicPath");
        OperationTaskDetailDataCol.add("origPic");
        OperationTaskDetailDataCol.add("startTime");
        OperationTaskDetailDataCol.add("userName");
        OperationTaskDetailDataCol.add("userId");
        return OperationTaskDetailDataCol;
    }

    public static HashMap<String, String> map = new HashMap<String, String>() {{
        put("巡视点名称", "instanceName");
        put("识别类型", "meteTypeName");
        put("识别结果", "cruiseResultName");
        put("巡视图片", "origpic");
        put("审核值", "personCheck");
        put("审核结果", "identifyResultName");
        put("采集信息", "resultDesc");
        put("表计类型", "meterTypeName");
        put("间隔名称", "upRegionName");
        put("设备名称", "deviceName");
        put("告警级别", "alarmLevelName");
        put("数据来源", "cruiseTypeName");
        put("设备区域", "regionName");
        put("设备类型", "deviceTypeName");
        put("巡视时间", "cruiseTime");
    }};

    /**
     *  巡视结果列信息
     *
     * @return 列信息
     */
    public static List<String> getCruiseDataReportModel(List<String> typeString) {
        List<String> includeCruiseDataNames = new ArrayList<>();
        for (String value : typeString) {
            includeCruiseDataNames.add(map.get(value));
        }
        return includeCruiseDataNames;
    }
}
