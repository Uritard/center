package com.yjh.platform.module.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.module.device.dao.DictAreaMapper;
import com.yjh.platform.module.device.entity.DictArea;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YIJIAHE
 * @description 针对表【dict_area(区县行政编码字典表)】的数据库操作Service实现
 * @createDate 2023-09-07 10:46:59
 */
@Service
public class DictAreaService extends ServiceImpl<DictAreaMapper, DictArea> {

    @Resource
    private DictAreaMapper dictAreaMapper;

    public List<DictArea> getDictAreaByParentId(String parentId) {
        LambdaQueryWrapper<DictArea> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DictArea::getParentId, parentId);
        return dictAreaMapper.selectList(lambdaQueryWrapper);
    }
}




