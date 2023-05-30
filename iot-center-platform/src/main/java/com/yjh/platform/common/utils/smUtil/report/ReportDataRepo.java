package com.yjh.platform.common.utils.smUtil.report;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 巡视报告模板--自定义格式
 *
 * @author YC
 * @date 2020/10/29 - 15:18
 */
public class ReportDataRepo {
    /**
     * 详细巡视记录大标题
     */
    private static final String[] TASK_TITLE_CONTENT = {
            "异常点位汇总", "待人工确认点位汇总", "正常点位汇总"
    };
    /**
     * 表头标题
     */
    private static final String[] TASK_INFO = {
            "变电站", "电压等级", "变电站类别", "环境信息"
    };
    private static final String[] TASK_INFO2 = {
            "巡视统计"
    };
    /**
     * 详细巡视记录标题
     */
    private static final String[] TASK_PORT_INFO = {
            "编号", "区域", "间隔", "设备", "部件", "点位", "数据来源", "采集时间", "巡视结果", "点位状态","巡视图像"
    };

    public static  ContentData getData(ReportData param) {
        final int columnCount = TASK_PORT_INFO.length;

        List<TableCellElement> elements = new LinkedList<>();

        final int rowCount = prepareReportElements(elements, param);

        return new ContentData(rowCount, columnCount, elements);
    }

    /**
     * 根据样式填入数据
     * @param elements 样式
     * @param param 数据
     * @return int
     */
    private static int prepareReportElements(List<TableCellElement> elements, ReportData param) {
        int rowCount = 0;

        rowCount += prepareReportTaskInfo(elements, rowCount, param);

        rowCount += prepareReportTaskPortInfo(elements, rowCount, param);

        return rowCount;
    }

    private static int prepareReportTaskPortInfo(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart;
        int rowCount = 0;

        List<TCruiseDataResultDetail> tCDRDList = param.getTCDRDList();

        List<TCruiseDataResultDetail> abnormalList = new ArrayList<>();
        List<TCruiseDataResultDetail> normalList = new ArrayList<>();
        List<TCruiseDataResultDetail> unReviewList = new ArrayList<>();
        for (TCruiseDataResultDetail cbsInspectionResultVo : tCDRDList) {
            if (Objects.nonNull(cbsInspectionResultVo.getIdentifyResultName()) && !Objects.equals("正常", cbsInspectionResultVo.getIdentifyResultName())){
                cbsInspectionResultVo.setIdentifyResultName("异常");
                abnormalList.add(cbsInspectionResultVo);
            }else if (Objects.nonNull(cbsInspectionResultVo.getIdentifyResultName()) && Objects.equals("正常", cbsInspectionResultVo.getIdentifyResultName())){
                normalList.add(cbsInspectionResultVo);
            }else if (Objects.nonNull(cbsInspectionResultVo.getEvaluationStateName()) && Objects.equals("未审核", cbsInspectionResultVo.getEvaluationStateName())){
                cbsInspectionResultVo.setIdentifyResultName("待人工确认");
                unReviewList.add(cbsInspectionResultVo);
            }

        }

        for (int i = 0; i < TASK_TITLE_CONTENT.length; i++) {
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, TASK_PORT_INFO.length-1, new String[]{TASK_TITLE_CONTENT[i]},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            rowIndex++;
            rowCount++;
            for (int j = 0; j < TASK_PORT_INFO.length; j++){
                elements.add(new TableCellElement(rowIndex, rowIndex, j, j, new String[]{TASK_PORT_INFO[j]},
                        TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
            }
            rowIndex++;
            rowCount++;

            Map<String, Integer> rowCountw = new HashMap<>();
            if (i == 0){
                rowCountw = getRowCount(elements, rowIndex, rowCount, abnormalList);
            }else if (i == 1){
                rowCountw = getRowCount(elements, rowIndex, rowCount, unReviewList);
            }else  if (i == 2){
                rowCountw = getRowCount(elements, rowIndex, rowCount, normalList);
            }

            rowIndex = rowCountw.get("rowIndex");
            rowCount = rowCountw.get("rowCount");
        }

        return rowCount;
    }

    private static Map<String, Integer> getRowCount(List<TableCellElement> elements, int rowIndex, int rowCount, List<TCruiseDataResultDetail> tCDRDList) {
        Map<String, Integer> map = new HashMap<>(16);
        for (TCruiseDataResultDetail detail : tCDRDList) {
            int index = tCDRDList.indexOf(detail);
            int colorIndex = IndexedColors.BLACK.index;
            boolean bold = false;
            // 编号
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 0, new String[]{String.valueOf(index + 1)},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 区域
            elements.add(new TableCellElement(rowIndex, rowIndex, 1, 1, new String[]{Optional.ofNullable(detail.getRegionName()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 间隔
            elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{Optional.ofNullable(detail.getIntervalName()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 设备
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 3, new String[]{Optional.ofNullable(detail.getDeviceName()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 部件
            elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{Optional.ofNullable(detail.getComponentName()).orElse("本体")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 点位
            elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{Optional.ofNullable(detail.getInstanceName()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 数据来源
            elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{Optional.ofNullable(detail.getDataType()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 采集时间
            if (null != detail.getCruiseTime()){
                elements.add(new TableCellElement(rowIndex, rowIndex, 7, 7, new String[]{dataFormat(detail.getCruiseTime())},
                        TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            }else{
                elements.add(new TableCellElement(rowIndex, rowIndex, 7, 7, new String[]{},
                        TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            }
            // 巡视结果
            elements.add(new TableCellElement(rowIndex, rowIndex, 8, 8, new String[]{Optional.ofNullable(detail.getResultDesc()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 点位状态
            elements.add(new TableCellElement(rowIndex, rowIndex, 9, 9, new String[]{Optional.ofNullable(detail.getIdentifyResultName()).orElse("")},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 巡视图像
            String file = StringUtils.isEmpty(detail.getPicPath()) ? null : detail.getPicPath();
            elements.add(new TableCellElement(rowIndex, rowIndex, 10, 10, new String[]{file},
                    TableCellElement.TYPE_PICTURE));

            rowIndex++;
            rowCount++;
        }
        map.put("rowIndex", rowIndex);
        map.put("rowCount", rowCount);
        return map;
    }

    /**
     * 根据样式填入表头数据
     *
     * @param elements 样式
     * @param rowStart 开始行
     * @param param 数据
     * @return int
     */
    private static int prepareReportTaskInfo(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart;
        int rowCount = 0;
        int colorIndex = IndexedColors.BLACK.index;
        boolean bold = false;

        TaskVO taskVoInfo = param.getTaskVO();

        // 变电站
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{Optional.ofNullable(taskVoInfo.getStationName()).orElse("")},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowCount = rowCount + 2;
        // 电压等级
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{Optional.ofNullable(taskVoInfo.getVoltageClasses()).orElse("")},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        // 变电站类别
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[2]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{Optional.ofNullable(taskVoInfo.getStationType()).orElse("")},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowCount = rowCount + 2;
        // 环境信息
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[3]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{Optional.ofNullable(taskVoInfo.getEnvInfo()).orElse("")},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        // 巡视统计
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO2[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 10, new String[]{Optional.ofNullable(taskVoInfo.getCruiseStatistics()).orElse("")},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        rowCount++;

        return rowIndex;
    }

    /**
     * 时间格式化
     *
     * @param time 时间
     * @return 格式化后的时间
     */
    private static String dataFormat(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(time);
    }
}
