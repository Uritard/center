package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.StationVoltageData;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.patrol.entity.LineKeyValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author tt
* @since 2020-07-27
*/
@Service
public class TStdRegionService{

    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TStdRegionService.class);

    public final static String LOWER_UP_KEY = "region:lowerUpStation";
    public final static String UP_REF_KEY = "region:upRef:";
    public final static String LOW_REF_KEY = "region:lowRef:";

    private final static Map<Long,TStdRegion> REGION_MAP = new ConcurrentHashMap<>(32);

    private volatile boolean changed = false;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdRegion tStdRegion) {
        if (StringUtils.isNotBlank(tStdRegion.getRegionCode())) {
            // 判断当前区域编码是否与本级系统的区域编码重复
            Integer count = this.countByRegionCode(tStdRegion.getRegionCode(), null);
            if (count > 0) {
                throw new BusinessException(201, "区域编码" + tStdRegion.getRegionCode() + "已存在");
            }
        }
        changed = true;
        return this.tStdRegionDao.insert(tStdRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long regionId) {
        List<Long> list = tStdRegionDao.selectDownId(regionId);
        if(list.size()>1){
            return -1;
        }
        List<Long> deviceList = tStdRegionDao.selectDevice(list);
        if(deviceList != null && deviceList.size() > 0){
            return -1;
        }
        changed = true;
        return this.tStdRegionDao.deleteByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdRegion tStdRegion) {
        String regionCode = tStdRegion.getRegionCode();
        if (StringUtils.isNotBlank(regionCode)) {
            List<TStdRegion> list = tStdRegionDao.selectIsIn(tStdRegion);
            if (CollectionUtils.isNotEmpty(list)) {
                throw new BusinessException(209,"编码与其他厂站区域重复");
            }
        }
        TStdRegion oldStdRegion = tStdRegionDao.selectByPrimaryId(tStdRegion.getRegionId());
        if (!oldStdRegion.getRegionCode().equals(tStdRegion.getRegionCode()) && StringUtils.isNotEmpty(oldStdRegion.getEdgeStatus())) {
            tStdRegion.setEdgeStatus("离线");
        }
        changed = true;
        return this.tStdRegionDao.update(tStdRegion);
    }

//    @Transactional(rollbackFor = Exception.class)
//    public int update(Map<String, Object> map) {
//        if (map.get("regionCode").equals("")) map.replace("regionCode",0);
//        log.info("map: "+map);
//        return this.tStdRegionDao.updateByMap(map);
//    }

    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectByPrimaryId(Long regionId) {
        return this.tStdRegionDao.selectByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdRegion> select(Long regionId, String regionName, Integer sort, Long upRegionId, String upRegionIds, Integer regionCode, String stationId, Integer state, Date createTime) {
        List<TStdRegion> tStdRegionList = tStdRegionDao.select(regionId, regionName, sort, upRegionId, upRegionIds, regionCode, stationId, state, createTime);
        return tStdRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdRegion> selectByPage(TStdRegion tStdRegion) {
        List<TStdRegion> tStdRegionList = tStdRegionDao.selectByPage(tStdRegion);
        return tStdRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoRegionCode> selectRegTreeByRegName(String regionName) {

        List<AreaInfoRegionCode> listTree = new ArrayList<>();
        List<AreaInfoRegionCode> listTreeAll = this.tStdRegionDao.selectAreaTree();
        if (Objects.equals(null, regionName) || regionName.equals("")) {
            List<AreaInfoRegionCode> areaInfoRegionCodeList = new ArrayList<>();
            Integer level = 1;
            for (AreaInfoRegionCode areaInfoRegionCode:listTreeAll) {
                if (areaInfoRegionCode.getUpId()==null || areaInfoRegionCode.getUpId()==-1) {
                    areaInfoRegionCode.setLevel(level);
                    areaInfoRegionCodeList.add(areaInfoRegionCode);
                }
            }
            diGui(areaInfoRegionCodeList, listTreeAll, level);
            return areaInfoRegionCodeList;
        }

        List<AreaInfoRegionCode> listTreeByName = this.tStdRegionDao.selectRegTreeByRegName(regionName);;
        if (listTreeByName.size()>0) {
            for (AreaInfoRegionCode areaInfoRegionCode : listTreeByName) {
                listTree.add(areaInfoRegionCode);
                if (areaInfoRegionCode.getUpId() != null && areaInfoRegionCode.getUpId() != -1) {
                    Long areaInfoRegionCodeUpId = areaInfoRegionCode.getUpId();
                    System.out.println("areaInfoRegionCodeUpId: "+areaInfoRegionCodeUpId);
                    for (AreaInfoRegionCode areaInfoRegionCodeAll : listTreeAll) {
                        if (Objects.equals(areaInfoRegionCodeUpId, areaInfoRegionCodeAll.getId())) {
                            listTree.add(areaInfoRegionCodeAll);
                            diGuiMoHu(areaInfoRegionCodeAll, listTreeAll, listTree);
                        }
                    }
                }
            }
        }
        listTree = listTree.stream().distinct().collect(Collectors.toList());
        System.out.println("listTree: "+listTree);
        List<AreaInfoRegionCode> areaInfoRegionCodeStartList = new ArrayList<>();
        Integer level = 1;
        for (AreaInfoRegionCode areaInfoRegionCode:listTree) {
            if (areaInfoRegionCode.getUpId()==null || areaInfoRegionCode.getUpId()==-1) {
                areaInfoRegionCode.setLevel(level);
                areaInfoRegionCodeStartList.add(areaInfoRegionCode);
            }
        }
        diGui(areaInfoRegionCodeStartList, listTree, level);
        return areaInfoRegionCodeStartList;

    }

    private void diGui(List<AreaInfoRegionCode> areaInfoList, List<AreaInfoRegionCode> listTree, Integer level) {
        for(AreaInfoRegionCode areaInfo : areaInfoList){
            List<AreaInfoRegionCode> childrenList = new ArrayList<>();
            for (AreaInfoRegionCode areaInfoMap : listTree) {
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    areaInfoMap.setLevel(areaInfo.getLevel()+1);
                    childrenList.add(areaInfoMap);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree, level);
            }
        }
    }

    private void diGuiMoHu(AreaInfoRegionCode areaInfoRegionCodeAll, List<AreaInfoRegionCode> listTreeAll, List<AreaInfoRegionCode> listTree) {
        if (areaInfoRegionCodeAll.getUpId() != null && areaInfoRegionCodeAll.getUpId() != -1) {
            Long areaInfoRegionCodeAllUpId = areaInfoRegionCodeAll.getUpId();
            for (AreaInfoRegionCode areaInfoRegionCode : listTreeAll) {
                if (Objects.equals(areaInfoRegionCodeAllUpId, areaInfoRegionCode.getId())) {
                    listTree.add(areaInfoRegionCode);
                    diGuiMoHu(areaInfoRegionCode, listTreeAll, listTree);
                }
            }
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectDownId(Long  regionId) {
        return tStdRegionDao.selectDownId(regionId);
    }

    public List<TStdRegion> queryAll() {
        return tStdRegionDao.queryAll();
    }

    public int loadRegionIntoRedis() {
        List<TStdRegion> list = tStdRegionDao.select(null, null, null, null, null, null, null, null, null);
        Set<String> keys = redisTemplate.keys("region:*");
        // 删除所有区域信息重新加载
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        for (TStdRegion item : list) {
            if (StringUtils.isNotEmpty(item.getRegionCode()) && 1 == item.getState()) {
                Map<String, String> map = Object2Map.toStringMap(Object2Map.objectToMap(item, true));
                String str = "region:" + item.getRegionCode();
                redisTemplate.opsForHash().putAll(str, map);
            }
        }
        ThreadPoolUtil.COMMON_POOL.addThread(()->regionUpDownRef(list));
        stationDownId();
        return 1;
    }

    public void regionUpDownRef(List<TStdRegion> list) {
        log.info("开始加载区域对应上下级关系......");
        try {
            for (TStdRegion region : list) {
                String regionId = region.getRegionId().toString();
                List<Long> upRegionList = tStdRegionDao.selectAllUpRegion(regionId);
                List<Long> lowRegionList = tStdRegionDao.selectDownRegion(regionId);
                if (CollectionUtils.isNotEmpty(upRegionList)) {
                    redisTemplate.opsForSet().add(UP_REF_KEY + regionId, upRegionList.toArray(new Long[0]));
                }
                if (CollectionUtils.isNotEmpty(lowRegionList)) {
                    redisTemplate.opsForSet().add(LOW_REF_KEY + regionId, lowRegionList.toArray(new Long[0]));
                }
            }
            log.info("区域对应上下级关系加载完成");
        } catch (Exception e) {
            log.error("区域对应上下级关系加载失败", e);
        }
    }

    public Map<Long, KeyValue<Long, String>> stationDownId() {

        Map<Long, KeyValue<Long, String>> downToStationMap = redisStation();
        if (MapUtils.isNotEmpty(downToStationMap)) {
            return downToStationMap;
        }

        synchronized (this) {
            downToStationMap = redisStation();
            if (MapUtils.isNotEmpty(downToStationMap)) {
                return downToStationMap;
            }

            regionMaps(false);
            // 查询所有站所，即
            List<TStdRegion> stationList = tStdRegionDao.selectStations();

            downToStationMap = new HashMap<>(64);
            for (TStdRegion station : stationList) {
                Long stationId = station.getRegionId();
                List<Long> downRegionList = tStdRegionDao.selectDownRegion(String.valueOf(stationId));
                for (Long downId : downRegionList) {
                    downToStationMap.put(downId, new LineKeyValue<>(stationId, station.getRegionName()));
                }
            }
            redisTemplate.opsForHash().putAll(LOWER_UP_KEY, Object2Map.toStringMap(downToStationMap));
            changed = false;
            return downToStationMap;
        }
    }

    private Map<Long, KeyValue<Long, String>> redisStation() {
        if (changed) {
            return Collections.emptyMap();
        }
        Map<String, String> downStringMap = redisTemplate.opsForHash().entries(LOWER_UP_KEY);
        if (MapUtils.isNotEmpty(downStringMap)) {
            return toLongMap(downStringMap);
        } else {
            return Collections.emptyMap();
        }
    }

    public static Map<Long, KeyValue<Long, String>> toLongMap(Map<String, String> map) {
        Map<Long, KeyValue<Long, String>> longMap = new HashMap<>((int)(map.size() * 1.5));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            longMap.put(NumberUtils.toLong(k), LineKeyValue.parse(v, Long.class, String.class));
        }
        return longMap;
    }

    public List<TStdRegion> cruiseTree() {
       List<TStdRegion> tStdRegionList = new ArrayList<>();

       TStdRegion tStdRegion = tStdRegionDao.selectCruiseTree(-1L);

       treeToList(tStdRegionList,tStdRegion);

       return tStdRegionList;
    }


    public List<TStdRegion> queryStationPosition() {
        return tStdRegionDao.selectByState(Constant.STATE_LOCAL);
    }

    public List<StationVoltageData> queryStationVoltage() {
        return tStdRegionDao.selectStationVoltageData();
    }

    // 判断当前区域编码是否在本级已经存在
    public Integer countByRegionCode(String regionCode, Long upRegionId) {
        return tStdRegionDao.countByRegionCode(regionCode, upRegionId);
    }

    public List<Long> getRegionIdByLeafNode(Set<Long> regionParam) {
        Set<Long> regionTemp = new HashSet<>(regionParam);
        do {
            //通过regionList查询上层节点，后将结果放入插入参数继续查询，直到结果与入参一致
            regionParam.addAll(regionTemp);
            regionTemp.addAll(tStdRegionDao.selectRegionListByUpRegionId(regionParam));
        } while (!regionParam.containsAll(regionTemp));
        return  new ArrayList<>(regionTemp);
    }

    private void treeToList(List<TStdRegion> tStdRegionList, TStdRegion tStdRegion) {
        if (CollectionUtils.isNotEmpty(tStdRegion.getChildren())) {
            List<TStdRegion> childrenList = tStdRegion.getChildren();
            for (TStdRegion stdRegion : childrenList) {
                if (StringUtils.isNotBlank(stdRegion.getRegionCode())) {
                    stdRegion.setChildren(new ArrayList<>());
                    tStdRegionList.add(stdRegion);
                }
                else {
                    treeToList(tStdRegionList,stdRegion);
                }
            }
        }
    }

    public List<AreaInfoRegionCode> selectRegionByName(String regionName){
        List<AreaInfoRegionCode> allTree = tStdRegionDao.selectAreaTree();
        allTree = assembleTrees(allTree);
        if (StringUtils.isNotEmpty(regionName)){
            if (!matchName(allTree.get(0),regionName)){
                allTree.remove(0);
            }
        }
        return allTree;
    }

    private Boolean matchName(AreaInfoRegionCode node,String regionName){
        if (node.getLabel().contains(regionName)){
            return true;
        }else {
            List<AreaInfoRegionCode> child = node.getChildren();
            List<AreaInfoRegionCode> newChild = new ArrayList<>();
            if (child != null && child.size() > 0){
                for (AreaInfoRegionCode nodeItem : child){
                    if (matchName(nodeItem,regionName)){
                     newChild.add(nodeItem);
                    }
                }
            }
            node.setChildren(newChild);
            if (newChild.size() > 0){
                return true;
            }
            return false;
        }
    }

    public List<AreaInfoRegionCode> assembleTrees(Collection<AreaInfoRegionCode> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfoRegionCode> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfoRegionCode::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfoRegionCode parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
    }

    public Map<Long, TStdRegion> regionMaps() {
        return regionMaps(true);
    }

    private Map<Long, TStdRegion> regionMaps(boolean needChange) {
        if (REGION_MAP.isEmpty() || changed) {
            synchronized (REGION_MAP) {
                if (REGION_MAP.isEmpty() || changed) {
                    List<TStdRegion> stdRegionList = tStdRegionDao.selectAll();
                    Map<Long, TStdRegion> regionMaps =
                        stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getRegionId, Function.identity()));
                    REGION_MAP.putAll(regionMaps);
                }
            }

            if (needChange) {
                stationDownId();
            }
        }

        return REGION_MAP;
    }

    public TStdRegion getRegion(Long regionId) {
        return getRegion(regionId, null);
    }

    public TStdRegion getRegion(Long regionId, TStdRegion def) {
        if (regionId == null) {
            return null;
        }
        return regionMaps().getOrDefault(regionId, def);
    }

    public String getRegionName(Long regionId) {
        if (regionId == null) {
            return "";
        }
        TStdRegion region = regionMaps().get(regionId);
        return Optional.ofNullable(region).map(TStdRegion::getRegionName).orElse("");
    }

    /**
     * 返回不为空的对象（如果第一个对象为空，则返回第二个对象）
     *
     * @param object   目标对象
     * @param function 目标对象方法
     * @param <T>      目标对象类型泛型
     * @param <R>      返回对象类型泛型
     * @return 返回对象
     */
    public static <T, R> R ifNull(T object, Function<T, R> function) {
        return object == null || function == null ? null : function.apply(object);
    }
}

