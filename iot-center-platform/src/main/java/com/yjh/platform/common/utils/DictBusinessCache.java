package com.yjh.platform.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import com.yjh.platform.threadpool.TaskExecutePool;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/4/12
 * @since [产品/模块版本] （可选）
 */

public class DictBusinessCache {

    private static Logger log = LoggerFactory.getLogger(DictBusinessCache.class);

    private static Multimap<String,TDictBusiness> dictMap;

    private static Map<String,TDictBusiness> map;

    public static final String NAME_PREFIX = "%";

    public static void init(boolean autoRefresh) {
        if (autoRefresh) {
            TaskExecutePool.getInstance().execute(() -> {
                try {
                    Thread.sleep(3000);
                    init();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else {
            init();
        }
    }

    public static synchronized void init() {
        if (dictMap == null) {
            dictMap = HashMultimap.create();
        } else {
            dictMap.clear();
        }

        if (map == null) {
            map = new HashMap<>();
        }
        else  {
            map.clear();
        }
        TDictBusinessDao tDictBusinessDao = StaticContextAccessor.getBean(TDictBusinessDao.class);
        List<TDictBusiness> tDictBusinessList = tDictBusinessDao.selectAll();
        tDictBusinessList.forEach(tDictBusiness -> {
            dictMap.put(tDictBusiness.getColName(), tDictBusiness);
            map.put(tDictBusiness.getColName() + NAME_PREFIX + tDictBusiness.getDictCode(), tDictBusiness);
        });
        log.info("————————————————字典表缓存初始化完毕————————————————");
    }

    public static List<TDictBusiness> getDictList(String colName) {
        return new ArrayList<>(dictMap.get(colName));
    }

    public static TDictBusiness getDict(String colName,String dictCode) {
        TDictBusiness tDictBusiness = map.get(colName + NAME_PREFIX + dictCode);

        if (Objects.nonNull(tDictBusiness)) {
            return tDictBusiness;
        }
        return null;
    }

    public static String getDictNode(String colName,String dictCode) {
        TDictBusiness tDictBusiness = map.get(colName + NAME_PREFIX + dictCode);

        if (Objects.nonNull(tDictBusiness)) {
            return tDictBusiness.getDictNote();
        }
        return null;
    }

}
