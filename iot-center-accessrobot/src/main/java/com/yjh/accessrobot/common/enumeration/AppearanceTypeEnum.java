package com.yjh.accessrobot.common.enumeration;

public enum AppearanceTypeEnum {
	/**
	 * 设备室
	 */
	APPEARANCE_TYPE_426(426, "设备室", 0),


	/**
	 * 消防室
	 */
	APPEARANCE_TYPE_425(425, "消防室", 0),


	/**
	 * 沉降监测点
	 */
	APPEARANCE_TYPE_431(431, "沉降监测点", 0),


	/**
	 * 电子围栏
	 */
	APPEARANCE_TYPE_420(420, "电子围栏", 0),


	/**
	 * 水位线
	 */
	APPEARANCE_TYPE_429(429, "水位线", 0),


	/**
	 * 排水泵
	 */
	APPEARANCE_TYPE_430(430, "排水泵", 0),


	/**
	 * 消防水泵
	 */
	APPEARANCE_TYPE_423(423, "消防水泵", 0),


	/**
	 * 消防栓
	 */
	APPEARANCE_TYPE_424(424, "消防栓", 0),


	/**
	 * 泡沫喷淋
	 */
	APPEARANCE_TYPE_422(422, "泡沫喷淋", 0),


	/**
	 * 摄像头
	 */
	APPEARANCE_TYPE_428(428, "摄像头", 0),


	/**
	 * 红外对射
	 */
	APPEARANCE_TYPE_421(421, "红外对射", 0),


	/**
	 * 照明灯
	 */
	APPEARANCE_TYPE_427(427, "照明灯", 0);




	Integer dictCode;
	String dictNode;
	Integer upDict;



	AppearanceTypeEnum(Integer dictCode, String dictNode, Integer upDict) {
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

	public static Integer getDictCodeByDictNote(String dictNote) {
		for (AppearanceTypeEnum appearanceTypeEnum:AppearanceTypeEnum.values()) {
			if (appearanceTypeEnum.getDictNote().equals(dictNote)) {
				return appearanceTypeEnum.getDictCode();
			}
		}
		return null;
	}
}
