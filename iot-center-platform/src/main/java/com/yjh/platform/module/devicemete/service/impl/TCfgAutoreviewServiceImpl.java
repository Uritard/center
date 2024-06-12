package com.yjh.platform.module.devicemete.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.devicemete.dao.TCfgAutoreviewMapper;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreview;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreviewDetail;
import com.yjh.platform.module.devicemete.service.ITCfgAutoreviewDetailService;
import com.yjh.platform.module.devicemete.service.ITCfgAutoreviewService;
import com.yjh.platform.module.patrol.service.AutoreviewHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 自动审核配置表 服务实现类
 * </p>
 *
 * @author Chenfei
 * @since 2024-05-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TCfgAutoreviewServiceImpl extends ServiceImpl<TCfgAutoreviewMapper, TCfgAutoreview> implements ITCfgAutoreviewService {
    private final ITCfgAutoreviewDetailService autoreviewDetailService;
    private final AutoreviewHandler autoreviewHandler;

    @Override
    public IPage<TCfgAutoreview> selectByPage(IPage<TCfgAutoreview> page, String autoreviewName, String autoreviewType, String defectType,
        String alarmType, String deviceType, String deviceName, String meteName, String labelAttri) {

        List<TCfgAutoreview> autoreviews =
            getBaseMapper().selectByPage(page, autoreviewName, autoreviewType, defectType, alarmType, deviceType, deviceName, meteName,
                labelAttri);
        page.setRecords(autoreviews);
        DictConvertUtil.optional("autoreviewType").covertToDict(autoreviews);
        autoreviews.forEach(v -> v.setAutoreviewTypes(StringUtils.split(v.getAutoreviewType(), ",")));
        return page;
    }

    @Override
    public IPage<TCfgAutoreview> selectByPageMini(IPage<TCfgAutoreview> page, String autoreviewName, String autoreviewType,
        int autoDetailType, String typeId, String typeName) {

        List<TCfgAutoreview> autoreviews =
            getBaseMapper().selectByPageMini(page, autoreviewName, autoreviewType, autoDetailType, typeId, typeName);
        page.setRecords(autoreviews);
        DictConvertUtil.optional("autoreviewType").covertToDict(autoreviews);
        autoreviews.forEach(v -> v.setAutoreviewTypes(StringUtils.split(v.getAutoreviewType(), ",")));
        return page;
    }

    @Override
    public List<TCfgAutoreviewDetail> selectDetailList(long autoreviewId, int autoDetailType, String typeId, String typeName) {

        LambdaQueryWrapper<TCfgAutoreviewDetail> queryWrapper = Wrappers.lambdaQuery(new TCfgAutoreviewDetail())
            .eq(TCfgAutoreviewDetail::getAutoreviewId, autoreviewId)
            .eq(autoDetailType > 0, TCfgAutoreviewDetail::getAutoDetailType, autoDetailType)
            .eq(StringUtils.isNotEmpty(typeId), TCfgAutoreviewDetail::getRefId, typeId)
            .like(StringUtils.isNotEmpty(typeName), TCfgAutoreviewDetail::getRefName, typeName);

        List<TCfgAutoreviewDetail> detailList = autoreviewDetailService.list(queryWrapper);
        DictConvertUtil.optional("autoDetailType").covertToDict(detailList);

        return detailList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addAutoreview(TCfgAutoreview autoreview) {
        List<TCfgAutoreviewDetail> detailList = autoreview.getDetail();
        // autoreview.setCreateTime(LocalDateTime.now());
        if (ArrayUtils.isNotEmpty(autoreview.getAutoreviewTypes())) {
            autoreview.setAutoreviewType(StringUtils.join(autoreview.getAutoreviewTypes(), ","));
        }
        super.save(autoreview);
        detailList.forEach(e -> e.setAutoreviewId(autoreview.getAutoreviewId()));
        autoreviewDetailService.saveBatch(detailList);
        autoreviewHandler.resetAutoreviewList();
        return autoreview.getAutoreviewId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAutoreview(TCfgAutoreview autoreview) {
        List<TCfgAutoreviewDetail> detailList = autoreview.getDetail();
        autoreview.setCreateTime(null).setCreateUser(null).setUpdateTime(LocalDateTime.now());
        if (ArrayUtils.isNotEmpty(autoreview.getAutoreviewTypes())) {
            autoreview.setAutoreviewType(StringUtils.join(autoreview.getAutoreviewTypes(), ","));
        }
        super.updateById(autoreview);

        long autoreviewId = autoreview.getAutoreviewId();
        LambdaQueryWrapper<TCfgAutoreviewDetail> delWrapper =
            Wrappers.lambdaQuery(new TCfgAutoreviewDetail()).eq(TCfgAutoreviewDetail::getAutoreviewId, autoreviewId);
        autoreviewDetailService.remove(delWrapper);
        detailList.forEach(e -> e.setAutoreviewId(autoreviewId));
        autoreviewDetailService.saveBatch(detailList);
        autoreviewHandler.resetAutoreviewList();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAutoreview(long id) {
        LambdaQueryWrapper<TCfgAutoreviewDetail> delWrapper =
            Wrappers.lambdaQuery(new TCfgAutoreviewDetail()).eq(TCfgAutoreviewDetail::getAutoreviewId, id);
        boolean del = autoreviewDetailService.remove(delWrapper);
        log.info("TCfgAutoreviewDetail deleted {} {}.", id, del);
        super.removeById(id);
        log.info("TCfgAutoreview deleted {} {}.", id, del);
        autoreviewHandler.resetAutoreviewList();
        return true;
    }
}
