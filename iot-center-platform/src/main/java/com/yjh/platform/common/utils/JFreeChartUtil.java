/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.utils;

import com.yjh.commons.ValueUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import java.awt.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/8/22
 * @since [产品/模块版本] （可选）
 */
public class JFreeChartUtil {

    public static String NO_DATA_MSG = "数据加载失败";

    /**
     * 生成主题
     * @param fontName 字体名称（默认为宋体）
     * @return
     */
    public static StandardChartTheme createChartTheme(String fontName) throws Exception {
        StandardChartTheme theme = new StandardChartTheme(fontName) {
            @Override
            public void apply(JFreeChart chart) {
                chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                super.apply(chart);
            }
        };
        theme.setExtraLargeFont(getDefaultFont(Font.PLAIN, 20));
        theme.setLargeFont(getDefaultFont(Font.PLAIN, 14));
        theme.setRegularFont(getDefaultFont(Font.PLAIN, 12));
        theme.setSmallFont(getDefaultFont(Font.PLAIN, 10));
        return theme;
    }

    /**
     * 获取默认字体
     * @param style
     * @param size  字体大小
     * @return
     * @throws Exception
     */
    public static Font getDefaultFont(int style, int size) throws Exception {
        //如果不使用Font,中文将显示不出来
        Font font = new Font("新宋体", Font.BOLD, size);
        return font;
    }

    /**
     * 创建饼图数据集合
     * @param legendNameList 图例名称列表
     * @param dataList       数据列表
     * @return
     */
    public static DefaultPieDataset createDefaultPieDataset(List<String> legendNameList, List<Object> dataList) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        //图例名称列表或数据列表为空
        if (legendNameList == null || legendNameList.size() <= 0 || dataList == null || dataList.size() <= 0) {
            return dataset;
        }
        for (int i = 0; i < legendNameList.size() && legendNameList.size() == dataList.size(); i++) {
            String value = dataList.get(i).toString();
            dataset.setValue(legendNameList.get(i), Double.valueOf(value));
        }
        return dataset;
    }

    /**
     * 设置饼状图渲染
     */
    public static void setPieRender(Plot plot) {
        plot.setNoDataMessage(NO_DATA_MSG);
        plot.setInsets(new RectangleInsets(10, 10, 5, 10));
        PiePlot piePlot = (PiePlot) plot;
        piePlot.setInsets(new RectangleInsets(0, 0, 0, 0));
        piePlot.setCircular(true);// 圆形

        // 简单标签
        piePlot.setLabelGap(0.01);
        piePlot.setInteriorGap(0.05D);
        // 图例形状
        piePlot.setLegendItemShape(new Rectangle(10, 10));
        piePlot.setIgnoreNullValues(true);
        // 去掉标签背景色
        piePlot.setLabelBackgroundPaint(null);
        //去掉图表背景颜色
        piePlot.setBackgroundPaint(null);
        // 去掉阴影
        piePlot.setLabelShadowPaint(null);
        // 去掉边框
        piePlot.setLabelOutlinePaint(null);
        piePlot.setShadowPaint(null);
        // 显示标签数据
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}:{2}"));
    }

    /**
     * 创建类别数据集合(柱形图、折线图)
     * @param legendNameList 图例名称列表
     * @param xAxisNameList  x轴名称列表
     * @param dataList       数据列表
     * @return
     */
    public static DefaultCategoryDataset createDefaultCategoryDataset(List<String> legendNameList, List<String> xAxisNameList
        , List<List<?>> dataList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        //图例名称列表、x轴名称列表或数据列表为空
        if (xAxisNameList == null || xAxisNameList.size() <= 0 || legendNameList == null || legendNameList.size() <= 0
            || dataList == null || dataList.size() <= 0) {
            return dataset;
        }
        for (int yAxisIndex = 0; yAxisIndex < legendNameList.size() && legendNameList.size() == dataList.size(); yAxisIndex++) {
            String legendName = legendNameList.get(yAxisIndex);
            List<?> rowList = dataList.get(yAxisIndex);
            //该组数据不存在或该组数据总数不等于x轴数据数量
            if (rowList == null || rowList.size() != xAxisNameList.size()) {
                continue;
            }
            for (int xAxisIndex = 0; xAxisIndex < rowList.size(); xAxisIndex++) {
                dataset.setValue(ValueUtil.toDouble(rowList.get(xAxisIndex), null), legendName, xAxisNameList.get(xAxisIndex));
            }
        }
        return dataset;
    }

    public static XYDataset createTimeSeriesDataset(List<String> legendNameList, List<String> xAxisNameList
        , List<List<?>> dataList) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        //图例名称列表、x轴名称列表或数据列表为空
        if (xAxisNameList == null || xAxisNameList.size() <= 0 || legendNameList == null || legendNameList.size() <= 0
            || dataList == null || dataList.size() <= 0) {
            return dataset;
        }
        for (int yAxisIndex = 0; yAxisIndex < legendNameList.size() && legendNameList.size() == dataList.size(); yAxisIndex++) {
            String legendName = legendNameList.get(yAxisIndex);
            List<?> rowList = dataList.get(yAxisIndex);
            //该组数据不存在或该组数据总数不等于x轴数据数量
            if (rowList == null || rowList.size() != xAxisNameList.size()) {
                continue;
            }
            TimeSeries series = new TimeSeries(legendName);
            for (int xAxisIndex = 0; xAxisIndex < rowList.size(); xAxisIndex++) {
                Date time = DateTimeUtil.parse(xAxisNameList.get(xAxisIndex));
                series.add(new FixedMillisecond(time), ValueUtil.toDouble(rowList.get(xAxisIndex), 0D));
                // dataset.addSeries(ValueUtil.toDouble(rowList.get(xAxisIndex), 0D), legendName, xAxisNameList.get(xAxisIndex));
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    /**
     * 设置柱状图渲染
     * @param plot
     * @param isShowDataLabels 显示数据值标记
     */
    public static void setBarRenderer(CategoryPlot plot, boolean isShowDataLabels) {
        plot.setNoDataMessage(NO_DATA_MSG);
        plot.setInsets(new RectangleInsets(10, 10, 5, 10));
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // 设置柱子最大宽度
        renderer.setMaximumBarWidth(0.175);
        //设置图表背景颜色(透明)
        plot.setBackgroundPaint(null);
        //显示数据值标记
        if (isShowDataLabels) {
            renderer.setDefaultItemLabelsVisible(true);
        }
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        //注意：此句很关键，若无此句，那数字的显示会被覆盖，给人数字没有显示出来的问题
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
            ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));

        setXAixs(plot);
        setYAixs(plot);
    }

    /**
     * 设置折线图样式
     * @param plot
     * @param isShowDataLabels 是否显示数据标签
     * @param isShapesVisible  是否显示数据点
     */
    public static void setLineRender(CategoryPlot plot, boolean isShowDataLabels, boolean isShapesVisible) {
        plot.setNoDataMessage(NO_DATA_MSG);
        plot.setInsets(new RectangleInsets(10, 10, 0, 10), false);
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        //设置图表背景颜色(透明)
        plot.setBackgroundPaint(null);
        renderer.setDefaultStroke(new BasicStroke(1.5F));
        //显示数据标签
        if (isShowDataLabels) {
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING,
                NumberFormat.getInstance()));
            // 位置
            renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE1, TextAnchor.BOTTOM_CENTER));
        }
        // 数据点绘制形状
        renderer.setDefaultShapesVisible(isShapesVisible);
        setXAixs(plot);
        setYAixs(plot);
    }

    public static void setLineRender(XYPlot plot, boolean isShowDataLabels, boolean isShapesVisible) {
        plot.setNoDataMessage(NO_DATA_MSG);
        plot.setInsets(new RectangleInsets(10, 10, 0, 10), false);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        //设置图表背景颜色(透明)
        plot.setBackgroundPaint(null);
        renderer.setDefaultStroke(new BasicStroke(1.5F));
        //显示数据标签
        if (isShowDataLabels) {
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator(StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,
                new SimpleDateFormat("MM-dd HH:mm"), NumberFormat.getInstance()));
            // 位置
            renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE1, TextAnchor.BOTTOM_CENTER));
        }
        // 数据点绘制形状
        renderer.setDefaultShapesVisible(isShapesVisible);
        setXAixs(plot);
        setYAixs(plot);
    }

    /**
     * 设置散点图样式
     * @param plot
     */
    public static void setScatterRender(XYPlot plot) {
        plot.setNoDataMessage(NO_DATA_MSG);
        plot.setInsets(new RectangleInsets(10, 10, 0, 10), false);
        //设置图表背景颜色(透明)
        plot.setBackgroundPaint(null);
        setXAixs(plot);
        setYAixs(plot);
    }

    /**
     * 设置类别图表(CategoryPlot) X坐标轴线条颜色和样式
     * @param plot
     */
    public static void setXAixs(CategoryPlot plot) {
        Color lineColor = new Color(31, 121, 170);
        CategoryAxis axis = plot.getDomainAxis();
        // X坐标轴颜色
        axis.setAxisLinePaint(lineColor);
        // X坐标轴标记|竖线颜色
        axis.setTickMarkPaint(lineColor);
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
    }

    /**
     * 设置图表(XYPlot) X坐标轴线条颜色和样式
     * @param plot
     */
    public static void setXAixs(XYPlot plot) {
        Color lineColor = new Color(31, 121, 170);
        // X坐标轴颜色
        plot.getDomainAxis().setAxisLinePaint(lineColor);
        // X坐标轴标记|竖线颜色
        plot.getDomainAxis().setTickMarkPaint(lineColor);
        // x轴网格线条
        plot.setDomainGridlinePaint(new Color(192, 192, 192));
        plot.getDomainAxis().setAutoRange(true);
    }

    /**
     * 设置类别图表(CategoryPlot) Y坐标轴线条颜色和样式 同时防止数据无法显示
     * @param plot
     */
    public static void setYAixs(CategoryPlot plot) {
        Color lineColor = new Color(192, 208, 224);
        ValueAxis axis = plot.getRangeAxis();
        // Y坐标轴颜色
        axis.setAxisLinePaint(lineColor);
        // Y坐标轴标记|竖线颜色
        axis.setTickMarkPaint(lineColor);
        // 隐藏Y刻度
        axis.setAxisLineVisible(false);
        axis.setTickMarksVisible(false);
        // Y轴网格线条
        plot.setRangeGridlinePaint(new Color(192, 192, 192));
        plot.setRangeGridlineStroke(new BasicStroke(1));
        // 设置顶部Y坐标轴间距,防止数据无法显示
        plot.getRangeAxis().setUpperMargin(0.1);
        // 设置底部Y坐标轴间距
        plot.getRangeAxis().setLowerMargin(0.1);

    }

    /**
     * 设置图表(XYPlot) Y坐标轴线条颜色和样式 同时防止数据无法显示
     * @param plot
     */
    public static void setYAixs(XYPlot plot) {
        Color lineColor = new Color(192, 208, 224);
        ValueAxis axis = plot.getRangeAxis();
        // Y坐标轴颜色
        axis.setAxisLinePaint(lineColor);
        // Y坐标轴标记|竖线颜色
        axis.setTickMarkPaint(lineColor);
        // 隐藏Y刻度
        axis.setAxisLineVisible(false);
        axis.setTickMarksVisible(false);
        // Y轴网格线条
        plot.setRangeGridlinePaint(new Color(192, 192, 192));
        // 设置顶部Y坐标轴间距,防止数据无法显示
        plot.getRangeAxis().setUpperMargin(0.1);
        // 设置底部Y坐标轴间距
        plot.getRangeAxis().setLowerMargin(0.1);
    }
}
