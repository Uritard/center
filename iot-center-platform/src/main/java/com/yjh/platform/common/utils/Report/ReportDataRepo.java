package com.yjh.platform.common.utils.Report;

import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author YC
 * @date 2020/10/29 - 15:18
 */
public class ReportDataRepo {
    private static final String[] TASK_TITLE_CONTENT = {
            "一、总体情况", "二、分项预览", "三、明细"
    };

    private static final String[] TASK_REPORT_TITLE = {"巡检记录报告"};
    //任务
    private static final String[] TASK_INFO = {
            "站所名称", "巡检任务名称", "测点数", "关联测点数", "巡检时间"
    };
    //分项预览
    private static final String[] TASK_ITEM = {
            "类别", "测点数", "未处理异常数"
    };
//    //环境设备
//    private static final String[] TASK_ENV = {
//            "环境设备名称", "设备类型名称", "检测值", "状态", "位置"
//    };
//    //关联设备
//    private static final String[] TASK_REPORT_INFO = {
//            "类别", "检测值", "状态"
//    };
    //明细
    private static final String[] TASK_PORT_INFO = {
            "序号", "设备名称", "检测内容", "巡视值", "图片", "识别状态", "巡检时间"
    };

    public static  ContentData getData(ReportData param) {
        final int columnCount = TASK_PORT_INFO.length;

        List<TableCellElement> elements = new LinkedList<>();

        final int rowCount = prepareReportElements(elements, param);

        return new ContentData(rowCount, columnCount, elements);
    }

    private static int prepareReportElements(List<TableCellElement> elements, ReportData param) {
        int rowCount = 0;

        rowCount += prepareReportTitle(elements);

        rowCount += prepareReportTaskInfo(elements, rowCount, param);

        rowCount += prepareReportTaskItem(elements, rowCount, param);

//        rowCount += prepareReportTaskEnv(elements, rowCount, param);

//        rowCount += prepareReportTaskReportInfo(elements, rowCount, param);

        rowCount += prepareReportTaskPortInfo(elements, rowCount, param);

        return rowCount;
    }

    private static int prepareReportTaskPortInfo(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart, rowCount = 0;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 6, new String[]{TASK_TITLE_CONTENT[2 ]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_LEFT).setBold(true));

        rowIndex++;
        rowCount++;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 0, new String[]{TASK_PORT_INFO[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 1, 1, new String[]{TASK_PORT_INFO[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{TASK_PORT_INFO[2]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 3, new String[]{TASK_PORT_INFO[3]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{TASK_PORT_INFO[4]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{TASK_PORT_INFO[5]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{TASK_PORT_INFO[6]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        rowIndex++;
        rowCount++;

        List<TCruiseDataResultDetail> tCDRDList = param.getTCDRDList();

        for (TCruiseDataResultDetail cbsInspectionResultVo : tCDRDList) {
            int index = tCDRDList.indexOf(cbsInspectionResultVo);
            int colorIndex = IndexedColors.BLACK.index;
            boolean bold = false;
//            if ("1".equals(cbsInspectionResultVo.getHighestTmp())) {
//                colorIndex = IndexedColors.RED.index;
//                bold = true;
//            }
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 0, new String[]{String.valueOf(index + 1)},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            elements.add(new TableCellElement(rowIndex, rowIndex, 1, 1, new String[]{cbsInspectionResultVo.getDeviceName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));

            elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{cbsInspectionResultVo.getInstanceName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 3, new String[]{cbsInspectionResultVo.getResultNum()},//巡视值
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            String file = StringUtils.isEmpty(cbsInspectionResultVo.getPicPath()) ?null
                    : cbsInspectionResultVo.getPicPath();
            elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{file},
                    TableCellElement.TYPE_PICTURE));
            elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{cbsInspectionResultVo.getCruiseResultName()},//识别状态
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{dataFormat(cbsInspectionResultVo.getCruiseTime())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            rowIndex++;
            rowCount++;
        }

        return rowCount;
    }

    private static int prepareReportTaskReportInfo(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart, rowCount = 0;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 6, new String[]{TASK_TITLE_CONTENT[3]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_LEFT).setBold(true));
        rowIndex++;
        rowCount++;

//        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{TASK_REPORT_INFO[0]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 3, new String[]{TASK_REPORT_INFO[1]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 4, 6, new String[]{TASK_REPORT_INFO[2]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        rowIndex++;
//        rowCount++;

        List<RelationDevice> rcpRecordList = param.getRcpRecordList();

        for (RelationDevice cbsRelationCheckpointResultVo : rcpRecordList) {
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{cbsRelationCheckpointResultVo.getRcpName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 2, 3, new String[]{cbsRelationCheckpointResultVo.getAnalysisResult()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 4, 6, new String[]{cbsRelationCheckpointResultVo.getJudgment()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            rowIndex++;
            rowCount++;
        }

        return rowCount;
    }

    private static int prepareReportTaskEnv(List<TableCellElement> elements, int rowStart, ReportData param) {

        int rowIndex = rowStart, rowCount = 0;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 6, new String[]{TASK_TITLE_CONTENT[2]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_LEFT).setBold(true));

        rowIndex++;
        rowCount++;

//        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{TASK_ENV[0]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 3, new String[]{TASK_ENV[1]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{TASK_ENV[2]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{TASK_ENV[3]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{TASK_ENV[4]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        rowIndex++;
//        rowCount++;

        List<EnvironmentResult> evnRecordList = param.getEvnRecordList();

        for (EnvironmentResult cbsEnvdeviceInfoResult : evnRecordList) {
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{cbsEnvdeviceInfoResult.getEnvDeviceName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 2, 3, new String[]{cbsEnvdeviceInfoResult.getEnvDeviceTypeName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{cbsEnvdeviceInfoResult.getEnvDeviceVal()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{cbsEnvdeviceInfoResult.getStatusName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{cbsEnvdeviceInfoResult.getOutInName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            rowIndex++;
            rowCount++;

        }

        return rowCount;
    }

    private static int prepareReportTaskItem(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart, rowCount = 0;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 6, new String[]{TASK_TITLE_CONTENT[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_LEFT).setBold(true));

        rowIndex++;
        rowCount++;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{TASK_ITEM[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 3, new String[]{TASK_ITEM[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 4, 6, new String[]{TASK_ITEM[2]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        rowIndex++;
        rowCount++;

        List<CheckPointType> cpTypeItems = param.getCpTypeItems();

        for (CheckPointType cpTypeItem : cpTypeItems) {
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{cpTypeItem.getMeteType()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 2, 3, new String[]{cpTypeItem.getMeteNum().toString()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            elements.add(new TableCellElement(rowIndex, rowIndex, 4, 6, new String[]{cpTypeItem.getRegularNum().toString()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
            rowIndex++;
            rowCount++;

        }

        return rowCount;
    }

    //任务信息
    private static int prepareReportTaskInfo(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart, rowCount = 0;

        TaskVO taskVoInfo = param.getTaskVO();

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 6, new String[]{TASK_TITLE_CONTENT[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_LEFT).setBold(true));

        rowIndex++;
        rowCount++;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{TASK_INFO[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{TASK_INFO[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{TASK_INFO[2]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{TASK_INFO[3]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{TASK_INFO[4]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        /*elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{TASK_INFO[5]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));*/
        rowIndex++;
        rowCount++;

        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 1, new String[]{taskVoInfo.getStationName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{taskVoInfo.getTaskName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{taskVoInfo.getMeteNum().toString()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{taskVoInfo.getMeteRelationNum().toString()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        /*elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{taskVoInfo.getAbnormalNum().toString()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));*/
        if (null != taskVoInfo.getCruiseDate()){
            elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{dataFormat(taskVoInfo.getCruiseDate())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        }else{
            elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
        }
        rowIndex++;
        rowCount++;

        return rowCount;
    }

    private static int prepareReportTitle(List<TableCellElement> elements) {
        elements.add(new TableCellElement(0, 0, 0, 6, TASK_REPORT_TITLE,
                TableCellElement.TYPE_TEXT_STRING, (short) 15, 30, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        return 1;
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
