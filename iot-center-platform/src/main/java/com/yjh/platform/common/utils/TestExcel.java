package com.yjh.platform.common.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YC
 * @date 2020/8/31 - 21:21
 */
public class TestExcel {

    public static void main(String[] args) throws IOException {
//        String name = "yonghu.xls";
//        String fileSuffix = name.substring(name.lastIndexOf('.') + 1);
//        System.out.println("第一个fileSuffix是:"+fileSuffix);
//        fileSuffix = fileSuffix.toLowerCase();
//        System.out.println("第二个fileSuffix是:"+fileSuffix);
//        Map<String, String> arguments = new HashMap<String, String>();
//        arguments.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//xlsx对应的Content-type类型
//        arguments.put("xls", "application/vnd.ms-excel");//xls对应的Content-type类型
//        System.out.println("arguments是："+arguments);
//        String contentType = arguments.get(fileSuffix);
//        System.out.println("contentType："+contentType);
//
//    }

        String titleName = "算法模板";
        String[] title = null;
        String[] example = null;
        switch (titleName){
            case "测点模板":
                 title = new String[]{"模板名称", "标准测点id", "部位类型", "所属设备类型", "信号标准化编码",
                         "测点标准名", "测点类型","单位","信号说明","信号解释","告警分类","有效上限","有效下限",
                         "告警级别", "告警上限1","告警下限1","告警上限2","告警下限2","告警上限3","告警下限3",
                         "告警上限4", "告警下限4","告警延时","告警次数","绝对阀值","百分比阀值","系数","模板备注"};
                 example = new String[]{"123456", "2012/2/12  0:00:00", "zd001", "我的娃", "3.1415926", "192.164.0.1"};
                break;
            case "设备模板":
                title = new String[]{"部位ID", "设备编码", "设备名称","别名", "设备类型", "点号位置",
                        "模版ID", "路径", "上级区域id","上级区域名称", "部位名称", "部位类型", "设备状态",
                        "更新时间", "创建时间","设备型号", "PMS ID", "PMS类型", "生产厂家", "生产时间",
                        "投运时间","失效时间","最后一次维修时间","维修次数", "所属单位", "管理部门","责任人",
                        "纬度", "经度", "设备IP地址", "端口","电压等级", "顺控点号", "实物编码"};
                example = new String[]{"123456", "2012/2/12  0:00:00", "zd001"};
                break;
            case "设备测点模板":
                title = new String[]{"设备编码", "设备名称","别名", "设备类型","点号位置", "模版ID",
                        "路径", "上级区域id","上级区域名称", "设备状态","部位ID", "部位名称","部位类型",
                        "更新时间","创建时间", "标准测点ID","测点类型", "测点标准名","信号标准化编码",
                        "单位","信号说明", "信号解释","告警分类", "有效上限","有效下限", "告警级别","告警上限1",
                        "告警下限1","告警上限2", "告警下限2","告警上限3", "告警下限3","告警上限4", "告警下限4",
                        "告警延时", "告警次数","绝对阀值", "百分比阀值","系数","是否生成告警"};
                example = new String[]{"123456", "2012/2/12  0:00:00"};
                break;
            case "组织模板":
                title = new String[]{"组织名称", "组织编码", "上级组织ID", "排序", "创建时间", "创建者id",
                        "层级", "组织路径","部门名称", };
                example = new String[]{"123456"};
                break;
            case "标准区域模板":
                title = new String[]{"区域名称", "区域类型", "上级区域ID", "区域ID层级","类型区域",
                        "变电站ID","场站名称","标志位","创建时间","角色ID","是否选中"};
                example = new String[]{"123456", "2012/2/12  0:00:00","xxi","21kk"};
                break;
            case "机器人区域模板":
                title = new String[]{"区域ID", "区域名称", "区域类型", "上级区域ID","区域ID层级",
                        "类型区域","变电站ID","场站名称","标志位","创建时间"};
                example = new String[]{"123456", "2012/2/12  0:00:00","xxi","21kk"};
                break;
            case "用户模板":
                title = new String[]{"用户名", "密码", "真实姓名", "账号","性别", "email",
                        "手机号","工号", "人脸ID", "指纹ID","声纹ID", "标志位", "用户职称","用户级别", "创建人",
                        "校验码","用户appkey", "头像路径", "角色ID","组织机构ID", "员工状态", "创建日期","修改日期",
                        "失效时间", "最后登录时间"};
                example = new String[]{"123456", "2012/2/12  0:00:00","xxi","21kk","这波我的"};
                break;
            case "遥测量模板":
                title = new String[]{"设备编号", "监控量编号", "监控量名称", "有效上限","有效下限", "小数点后的有效位数",
                        "单位","同一设备下的监控量序号", "CID", "上下限带宽","变化幅度门限", "有效性表达式变量串",
                        "有效性判断表达式","无效时的值", "当前值","当前值更新时间","信号标准化编码", "设备类型", "监控量描述",
                        "一级告警上限", "一级告警下限", "二级告警上限", "二级告警下限", "三级告警上限", "三级告警下限",
                        "四级告警上限","四级告警下限","标称值","是否屏蔽","存储周期","关联的遥信量"};
                example = new String[]{"123456", "2012/2/12  0:00:00", "zd001"};
                break;
            case "遥信量模板":
                title = new String[]{"设备编号", "监控量编号", "监控量名称", "有效上限","有效下限", "同一设备下的监控量序号",
                        "CID", "遥信量种类","当前值", "当前值更新时间", "解释类型", "上报模式", "上报级别","屏蔽类型",
                        "屏蔽表达式变量串", "屏蔽表达式", "屏蔽值","防抖延时门限", "信号标准化编码", "设备类型", "监控量描述",
                        "是否屏蔽", "存储周期","态值描述","告警触发值","告警等级","关联的遥信量"};
                example = new String[]{"123456", "2012/2/12  0:00:00", "zd001"};
                break;
            case "算法模板":
                title = new String[]{"算法id","算法名称","算法类型","描述","算法编码","分析类型", "摄像头预置位ID或者机器人巡检点ID",
                        "预置点名称","巡检设备ID","状态","是否删除","是否展示","图标路径","应用情况","创建时间","修改时间"};
                example = new String[]{"123456", "2012/2/12  0:00:00", "zd001"};
                break;
        }
        InputStream is = ExcelFormatUtil.export(title,titleName,example);
        System.out.println(is);
    }

}
