package com.yjh.accessrobot.common.enumeration;

public enum MeteTypeEnum{
    /**
     * 声音检测
     */
    METE_TYPE_223(223, "声音检测", 5),


    /**
     * 局放特高频图谱
     */
    METE_TYPE_692(692, "局放特高频图谱", 13),


    /**
     * 局放超声波检测
     */
    METE_TYPE_691(691, "局放超声波检测", 11),


    /**
     * 外观缺陷识别
     */
    METE_TYPE_220(220, "外观缺陷识别", 3),


    /**
     * 表计读数
     */
    METE_TYPE_221(221, "表计读数", 1),


    /**
     * 闪烁检测
     */
    METE_TYPE_433(433, "闪烁检测", 6),


    /**
     * 位置状态识别
     */
    METE_TYPE_219(219, "位置状态识别", 2),


    /**
     * 氧气浓度检测
     */
    METE_TYPE_816(816, "氧气浓度检测", 103),


    /**
     * 环境湿度检测
     */
    METE_TYPE_815(815, "环境湿度检测", 102),


    /**
     * 录像
     */
    METE_TYPE_523(523, "录像", 0),


    /**
     * SF6浓度检测
     */
    METE_TYPE_817(817, "SF6浓度检测", 104),


    /**
     * 红外测温
     */
    METE_TYPE_222(222, "红外测温", 4),


    /**
     * 三相检测
     */
    METE_TYPE_996(996, "三相检测", 8),


    /**
     * 环境温度检测
     */
    METE_TYPE_814(814, "环境温度检测", 101),


    /**
     * 局放地电压检测
     */
    METE_TYPE_690(690, "局放地电压检测", 12);




    Integer dictCode;
    String dictNode;
    Integer upDict;



    MeteTypeEnum(Integer dictCode, String dictNode, Integer upDict) {
        this.dictCode = dictCode;
        this.dictNode = dictNode;
        this.upDict = upDict;
    }

    public Integer getDictCode() {
        return this.dictCode;
    }

    public String getDictNote() {
        return this.dictNode;
    }

    public Integer getUpDict() {
        return this.upDict;
    }


    public static MeteTypeEnum getEnumByUpDict(Integer upDict) {
        for (MeteTypeEnum meteTypeEnum:MeteTypeEnum.values()) {
            if (meteTypeEnum.getUpDict().equals(upDict)) {
                return meteTypeEnum;
            }
        }
        return null;
    }
    public static String getNodeByUpDict(Integer upDict) {
        for (MeteTypeEnum meteTypeEnum:MeteTypeEnum.values()) {
            if (meteTypeEnum.getUpDict().equals(upDict)) {
                return meteTypeEnum.getDictNote();
            }
        }
        return null;
    }

    public static Integer getUpDictByDictCode(Integer dictCode) {
        for (MeteTypeEnum meteTypeEnum:MeteTypeEnum.values()) {
            if (meteTypeEnum.getDictCode().equals(dictCode)) {
                return meteTypeEnum.getUpDict();
            }
        }
        return null;
    }
}


