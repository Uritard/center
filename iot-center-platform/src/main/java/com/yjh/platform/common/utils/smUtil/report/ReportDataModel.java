package com.yjh.platform.common.utils.smUtil.report;

import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 巡视报告模板--北京检测要求
 *
 * @author YC
 * @date 2020/10/29 - 15:18
 */
public class ReportDataModel {

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
            "变电站", "电压等级", "巡视日期", "变电站类别", "巡视任务", "环境信息", "审核人", "审核时间",
            "巡视开始时间", "巡视结束时间"
    };
    private static final String[] TASK_INFO2 = {
            "巡视统计", "巡视结论"
    };
    /**
     * 详细巡视记录标题
     */
    private static final String[] TASK_PORT_INFO = {
            "编号", "区域", "间隔", "设备", "部件", "点位", "数据来源", "采集时间", "巡视结果", "点位状态","巡视图像"
    };

    /**
     * 填入数据并添加表格样式
     * @param param 数据
     * @return ContentData
     */
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

//        TaskVO taskVoInfo = param.getTaskVO();
        TaskVO taskVoInfo = new TaskVO()
                .setStationName("1000kv泰州变")
                .setVoltageClasses("1000kv")
                .setCruiseDate(new Date())
                .setStationType("HGIS站")
                .setTaskName("室内外场地例行巡视")
                .setEnvInfo("气温5，气压101kpa")
//                .setReviewer("张三")
                .setReviewTime(new Date())
                .setCruiseStartTime(new Date())
                .setCruiseEndTime(new Date())
                .setCruiseStatistics("总点位 12000 个，已检点位 12000 个，未检点位 0 个，正常点位 11900 个，异常点位：50 个, 待人工确认点位 50 个")
                .setCruiseConclusion("1 号主变 A 相调补变呼吸器硅胶变色超过 1/2，注意定期观察");

        // 变电站
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{taskVoInfo.getStationName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowCount = rowCount + 2;
        // 电压等级
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{taskVoInfo.getVoltageClasses()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        // 巡视日期
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[2]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        if (null != taskVoInfo.getCruiseDate()){
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{dataFormat(taskVoInfo.getCruiseDate())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }else{
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }
        rowCount = rowCount + 2;
        // 变电站类别
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[3]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{taskVoInfo.getStationType()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        // 巡视任务
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[4]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{taskVoInfo.getTaskName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowCount = rowCount + 2;
        // 环境信息
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[5]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{taskVoInfo.getEnvInfo()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        // 审核人
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[6]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{Optional.ofNullable(taskVoInfo.getReviewer()).orElse("")},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowCount = rowCount + 2;
        // 审核时间
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[7]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        if (null != taskVoInfo.getReviewTime()){
            elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{dataFormat(taskVoInfo.getReviewTime())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }else{
            elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }
        rowIndex++;
        // 巡视开始时间
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO[8]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        if (null != taskVoInfo.getCruiseStartTime()){
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{dataFormat(taskVoInfo.getCruiseStartTime())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }else{
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }
        rowCount = rowCount + 2;
        // 巡视结束时间
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 7, new String[]{TASK_INFO[9]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        if (null != taskVoInfo.getCruiseEndTime()){
            elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{dataFormat(taskVoInfo.getCruiseEndTime())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }else{
            elements.add(new TableCellElement(rowIndex, rowIndex, 8, 10, new String[]{},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        }
        rowIndex++;
        // 巡视统计
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO2[0]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 10, new String[]{taskVoInfo.getCruiseStatistics()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        // 巡视结论
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_INFO2[1]},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setBold(true));
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 10, new String[]{taskVoInfo.getCruiseConclusion()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        rowIndex++;
        rowCount++;


        return rowIndex;
    }

    /**
     * 填入详细巡视记录数据
     *
     * @param elements 样式
     * @param rowStart 开始行
     * @param param 数据
     * @return int
     */
    private static int prepareReportTaskPortInfo(List<TableCellElement> elements, int rowStart, ReportData param) {
        int rowIndex = rowStart;
        int rowCount = 0;

        List<TCruiseDataResultDetail> tCDRDList = new ArrayList<>();

        TCruiseDataResultDetail detail = new TCruiseDataResultDetail()
                .setPx(1)
                .setRegionName("主变区域")
                .setInterval("1号主变")
                .setDeviceName("1号主变A相调压补偿变本体")
                .setComponent("呼吸器")
                .setInstanceName("1号主变A相调补变呼吸器外观")
                .setDataType("高清视频")
                .setCruiseTime(new Date())
                .setResultNum("呼吸器硅胶变色")
                .setCruiseResultName("正常")
                .setPicPath("home");
        tCDRDList.add(detail);
        TCruiseDataResultDetail detail1 = new TCruiseDataResultDetail()
                .setPx(1)
                .setRegionName("主变区域")
                .setInterval("2号主变")
                .setDeviceName("2号主变A相调压补偿变本体")
                .setComponent("呼吸器")
                .setInstanceName("2号主变A相调补变呼吸器外观")
                .setDataType("高清视频")
                .setCruiseTime(new Date())
                .setResultNum("呼吸器硅胶变色")
                .setCruiseResultName("异常")
                .setPicPath("home");
        tCDRDList.add(detail1);
        TCruiseDataResultDetail detail2 = new TCruiseDataResultDetail()
                .setPx(1)
                .setRegionName("主变区域")
                .setInterval("3号主变")
                .setDeviceName("3号主变A相调压补偿变本体")
                .setComponent("呼吸器")
                .setInstanceName("3号主变A相调补变呼吸器外观")
                .setDataType("高清视频")
                .setCruiseTime(new Date())
                .setResultNum("呼吸器硅胶变色")
                .setCruiseResultName("异常")
                .setPicPath("home");
        tCDRDList.add(detail2);
        TCruiseDataResultDetail detail3 = new TCruiseDataResultDetail()
                .setPx(1)
                .setRegionName("主变区域")
                .setInterval("4号主变")
                .setDeviceName("4号主变A相调压补偿变本体")
                .setComponent("呼吸器")
                .setInstanceName("4号主变A相调补变呼吸器外观")
                .setDataType("高清视频")
                .setCruiseTime(new Date())
                .setResultNum("呼吸器硅胶变色")
                .setCruiseResultName("异常")
                .setPicPath("home");
        tCDRDList.add(detail3);

        List<TCruiseDataResultDetail> list1 = new ArrayList<>();
        List<TCruiseDataResultDetail> list2 = new ArrayList<>();
        List<TCruiseDataResultDetail> list3 = new ArrayList<>();
        for (TCruiseDataResultDetail cbsInspectionResultVo : tCDRDList) {
            if (Objects.equals("异常", cbsInspectionResultVo.getCruiseResultName())){
                list1.add(cbsInspectionResultVo);
            }else if (Objects.equals("正常", cbsInspectionResultVo.getCruiseResultName())){
                list2.add(cbsInspectionResultVo);
            }else {
                list3.add(cbsInspectionResultVo);
            }

        }

        System.out.println("list1=="+ list1);
        System.out.println("list2=="+ list2);
        System.out.println("list3=="+ list3);


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

//        List<TCruiseDataResultDetail> tCDRDList = param.getTCDRDList();

            Map<String, Integer> rowCountw = new HashMap<>();
            if (i == 0){
                rowCountw = getRowCount(elements, rowIndex, rowCount, list1);
            }else if (i == 1){
                rowCountw = getRowCount(elements, rowIndex, rowCount, list3);
            }else  if (i == 2){
                rowCountw = getRowCount(elements, rowIndex, rowCount, list2);
            }

            rowIndex = rowCountw.get("rowIndex");
            rowCount = rowCountw.get("rowCount");
        }


        return rowCount;
    }

    private static Map<String, Integer> getRowCount(List<TableCellElement> elements, int rowIndex, int rowCount, List<TCruiseDataResultDetail> tCDRDList) {
        Map<String, Integer> map = new HashMap<>(16);
        for (TCruiseDataResultDetail cbsInspectionResultVo : tCDRDList) {
            int index = tCDRDList.indexOf(cbsInspectionResultVo);
            int colorIndex = IndexedColors.BLACK.index;
            boolean bold = false;
            // 编号
            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 0, new String[]{String.valueOf(index + 1)},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 区域
            elements.add(new TableCellElement(rowIndex, rowIndex, 1, 1, new String[]{cbsInspectionResultVo.getRegionName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 间隔
            elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{cbsInspectionResultVo.getInterval()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 设备
            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 3, new String[]{cbsInspectionResultVo.getDeviceName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 部件
            elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{cbsInspectionResultVo.getComponent()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 点位
            elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{cbsInspectionResultVo.getInstanceName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 数据来源
            elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{cbsInspectionResultVo.getDataType()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 采集时间
            elements.add(new TableCellElement(rowIndex, rowIndex, 7, 7, new String[]{dataFormat(cbsInspectionResultVo.getCruiseTime())},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 巡视结果
            elements.add(new TableCellElement(rowIndex, rowIndex, 8, 8, new String[]{cbsInspectionResultVo.getResultNum()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 点位状态
            elements.add(new TableCellElement(rowIndex, rowIndex, 9, 9, new String[]{cbsInspectionResultVo.getCruiseResultName()},
                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
            // 巡视图像
            String file = StringUtils.isEmpty(cbsInspectionResultVo.getPicPath()) ? null : cbsInspectionResultVo.getPicPath();
            elements.add(new TableCellElement(rowIndex, rowIndex, 10, 10, new String[]{file},
                    TableCellElement.TYPE_PICTURE));

            rowIndex++;
            rowCount++;
        }
        map.put("rowIndex", rowIndex);
        map.put("rowCount", rowCount);
        return map;
    }

    private static void extracted(List<TableCellElement> elements, int rowIndex, TCruiseDataResultDetail cbsInspectionResultVo, int index, int colorIndex, boolean bold) {
        // 编号
        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 0, new String[]{String.valueOf(index + 1)},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 区域
        elements.add(new TableCellElement(rowIndex, rowIndex, 1, 1, new String[]{cbsInspectionResultVo.getRegionName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 间隔
        elements.add(new TableCellElement(rowIndex, rowIndex, 2, 2, new String[]{cbsInspectionResultVo.getInterval()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 设备
        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 3, new String[]{cbsInspectionResultVo.getDeviceName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 部件
        elements.add(new TableCellElement(rowIndex, rowIndex, 4, 4, new String[]{cbsInspectionResultVo.getComponent()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 点位
        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 5, new String[]{cbsInspectionResultVo.getInstanceName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 数据来源
        elements.add(new TableCellElement(rowIndex, rowIndex, 6, 6, new String[]{cbsInspectionResultVo.getDataType()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 采集时间
        elements.add(new TableCellElement(rowIndex, rowIndex, 7, 7, new String[]{dataFormat(cbsInspectionResultVo.getCruiseTime())},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 巡视结果
        elements.add(new TableCellElement(rowIndex, rowIndex, 8, 8, new String[]{cbsInspectionResultVo.getResultNum()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 点位状态
        elements.add(new TableCellElement(rowIndex, rowIndex, 9, 9, new String[]{cbsInspectionResultVo.getCruiseResultName()},
                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER).setColorIndex(colorIndex).setBold(bold));
        // 巡视图像
        String file = StringUtils.isEmpty(cbsInspectionResultVo.getPicPath()) ? null : cbsInspectionResultVo.getPicPath();
        elements.add(new TableCellElement(rowIndex, rowIndex, 10, 10, new String[]{file},
                TableCellElement.TYPE_PICTURE));
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

//    private static int prepareReportTaskItem(List<TableCellElement> elements, int rowStart, ReportData param) {
//        int rowIndex = rowStart, rowCount = 0;
//
//        elements.add(new TableCellElement(rowIndex, rowIndex, 0, TASK_PORT_INFO.length-1, new String[]{TASK_TITLE_CONTENT[1]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_LEFT).setBold(true));
//
//        rowIndex++;
//        rowCount++;
//
//        elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{TASK_ITEM[0]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{TASK_ITEM[1]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        elements.add(new TableCellElement(rowIndex, rowIndex, 5, 10, new String[]{TASK_ITEM[2]},
//                TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//        rowIndex++;
//        rowCount++;
//
//        List<CheckPointType> cpTypeItems = param.getCpTypeItems();
//
//        for (CheckPointType cpTypeItem : cpTypeItems) {
//            elements.add(new TableCellElement(rowIndex, rowIndex, 0, 2, new String[]{cpTypeItem.getMeteType()},
//                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//            elements.add(new TableCellElement(rowIndex, rowIndex, 3, 4, new String[]{cpTypeItem.getMeteNum().toString()},
//                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//            elements.add(new TableCellElement(rowIndex, rowIndex, 5, 10, new String[]{cpTypeItem.getRegularNum().toString()},
//                    TableCellElement.TYPE_TEXT_STRING, (short) 10, 20, -1, TableCellElement.ALIGN_CENTER));
//            rowIndex++;
//            rowCount++;
//
//        }
//
//        return rowCount;
//    }

//    private static int prepareReportTitle(List<TableCellElement> elements) {
//        elements.add(new TableCellElement(0, 0, 0, TASK_PORT_INFO.length-1, TASK_REPORT_TITLE,
//                TableCellElement.TYPE_TEXT_STRING, (short) 15, 30, -1, TableCellElement.ALIGN_CENTER).setBold(true));
//        return 1;
//    }

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
