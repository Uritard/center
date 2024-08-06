package com.yjh.platform.module.devicemete.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreview;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreviewDetail;

import java.util.List;

/**
 * <p>
 * 自动审核配置表 服务类
 * </p>
 *
 * @author Chenfei
 * @since 2024-05-20
 */
public interface ITCfgAutoreviewService extends IService<TCfgAutoreview> {

    IPage<TCfgAutoreview> selectByPage(IPage<TCfgAutoreview> page, String autoreviewName, String autoreviewType, String defectType,
        String alarmType, String deviceType, String deviceName, String meteName, String labelAttri);

    IPage<TCfgAutoreview> selectByPageMini(IPage<TCfgAutoreview> page, String autoreviewName, String autoreviewType, int autoDetailType,
        String typeId, String typeName);

    List<TCfgAutoreviewDetail> selectDetailList(long autoreviewId, int autoDetailType, String typeId, String typeName);

    long addAutoreview(TCfgAutoreview autoreview);

    boolean updateAutoreview(TCfgAutoreview autoreview);

    boolean removeAutoreview(long id);
}
