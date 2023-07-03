package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.InspectedDevTreeCondition;
import com.yjh.platform.module.device.entity.PatrolDevTreeCondition;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.AreaInfoDetail;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.enums.UserStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备树统一接口查询
 *
 * @author 丫C
 * @date 2023/06/29
 * @since [产品/模块版本] （可选）
 */
@Service
public class TStdDeviceTreeService {

    private final SysUserDao sysUserDao;
    private final TCameraScreenDao tCameraScreenDao;
    private final TCameraInfoDao tCameraInfoDao;
    private final TRobotInfoDao tRobotInfoDao;
    private static final Logger log = LoggerFactory.getLogger(TStdDeviceTreeService.class);

    public TStdDeviceTreeService(SysUserDao sysUserDao, TCameraScreenDao tCameraScreenDao, TCameraInfoDao tCameraInfoDao, TRobotInfoDao tRobotInfoDao) {
        this.sysUserDao = sysUserDao;
        this.tCameraScreenDao = tCameraScreenDao;
        this.tCameraInfoDao = tCameraInfoDao;
        this.tRobotInfoDao = tRobotInfoDao;
    }

    public List<AreaInfoDetail> selectPatrolDevTree(PatrolDevTreeCondition condition, Long userIdTemp) {
        Long userId = updateUserId(userIdTemp);

        Map<String,String> map = getCameraStatus();

        String name = condition.getName();
        Integer flag = condition.getFlag();
        Long id = condition.getId();
        Integer type = condition.getType();

        switch (condition.getLevel()) {
            case "5":
                return areaTree(name, flag, "robotFlag", userId, map);
            case "6":
                return cameraTree(name, flag, "robotFlag", userId, id, map, type);
            case "66":
                return new ArrayList<>();
            default:
                throw new BusinessException("参数错误！");
        }
    }

    private List<AreaInfoDetail> areaTree(String cameraName, Integer flag, String robotFlag, Long userId, Map<String,String> map){
        List<AreaInfoDetail> areaTree = tCameraInfoDao.selectCameraTreeRegion();
        areaTree = assembleTrees(areaTree);
        areaTree = areaAddDeviceTree(areaTree, cameraName, flag, robotFlag, userId, map);
        return areaTree;
    }

    public List<AreaInfoDetail> assembleTrees(Collection<AreaInfoDetail> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfoDetail> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfoDetail::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfoDetail parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
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

    private List<AreaInfoDetail> areaAddDeviceTree(List<AreaInfoDetail> areaTree, String cameraName, Integer flag, String robotFlag, Long userId,Map<String,String> map){
        if (areaTree == null){
            return null;
        }
        areaTree.forEach(area ->{
            if ("region".equals(area.getInfoType())){
                if (area.getChildren() != null && area.getChildren().size() > 0){
                    area.getChildren().addAll(cameraTree(cameraName,flag,robotFlag,userId,area.getId(),map, null));
                    areaAddDeviceTree(area.getChildren(),cameraName,flag,robotFlag,userId,map);
                }
            }
        });
        return areaTree;
    }

    private List<AreaInfoDetail> cameraTree(String cameraName, Integer flag, String robotFlag, Long userId, Long upRegionId,
                                            Map<String, String> map, Integer cameraType) {
        List<AreaInfoDetail> childrenList = new ArrayList<>();
        List<AreaInfoDetail> child = tCameraInfoDao.selectCameraTreeWithRobotNew(cameraName, robotFlag, userId, upRegionId, cameraType);
        child.forEach(childs -> {
            if ("camera".equals(childs.getInfoType())) {
                if (map.get(childs.getId().toString()) != null) {
                    childs.setState(Integer.valueOf(map.get(childs.getId().toString())));
                } else {
                    childs.setState(0);
                }
                if (flag != null) {
                    if (flag.equals(childs.getState())) {
                        childrenList.add(childs);
                    } else if (flag == 2) {
                        childrenList.add(childs);
                    }
                }
            }
            if ("robot".equals(childs.getInfoType())) {
                //机器人
                TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(childs.getId());
                List<AreaInfoDetail> robotCameraList = new ArrayList<>();
                //可见光
                AreaInfoDetail lightCamera = new AreaInfoDetail();
                String light = String.valueOf(tRobotInfo.getRobotId()) + "9901";
                lightCamera.setId(Long.parseLong(light));
                lightCamera.setLabel("机器人可见光");
                lightCamera.setInfoType("robotCamera");
                lightCamera.setUpId(childs.getId());
                lightCamera.setUpName(tRobotInfo.getRobotCode());
                if ("在线".equals(tRobotInfo.getRobotStatus())) {
                    childs.setState(1);
                    lightCamera.setState(1);
                } else {
                    childs.setState(0);
                    lightCamera.setState(0);
                }

                if (flag != null) {
                    if (flag == lightCamera.getState() && (flag == 1 || flag == 0)) {
                        robotCameraList.add(lightCamera);
                    } else {
                        robotCameraList.add(lightCamera);
                    }
                }
                //红外
                AreaInfoDetail redCamera = new AreaInfoDetail();
                String infrared = String.valueOf(tRobotInfo.getRobotId()) + "9902";
                redCamera.setId(Long.parseLong(infrared));
                redCamera.setLabel("机器人红外");
                redCamera.setInfoType("robotCamera");
                redCamera.setUpId(childs.getId());
                redCamera.setUpName(tRobotInfo.getRobotCode());
                if ("在线".equals(tRobotInfo.getRobotStatus())) {
                    childs.setState(1);
                    redCamera.setState(1);
                } else {
                    childs.setState(0);
                    redCamera.setState(0);
                }

                if (flag != null) {
                    if (flag == redCamera.getState() && (flag == 1 || flag == 0)) {
                        robotCameraList.add(redCamera);
                    } else {
                        robotCameraList.add(redCamera);
                    }
                }
                childs.setChildren(robotCameraList);
                if (flag != null) {
                    if ((flag == 1 || flag == 0)) {
                        if (flag == childs.getState()) {
                            childrenList.add(childs);
                        }
                    } else {
                        childrenList.add(childs);
                    }
                }
            }
        });
        return childrenList;
    }

    private Long updateUserId(Long userId) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
        if (Objects.nonNull(sysUser) && UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        } else {
            if (1234L == sysUser.getRoleId()) {
                userId = null;
            }
            else if(1235L == sysUser.getRoleId()) {

            } else {
                throw  new BusinessException("当前用户角色不可查看");
            }
        }
        return userId;
    }

    /**
     * 获取相机的状态
     * map包含在离线的  不包含未知状态的
     *
     * @return Map<String, String>
     */
    private Map<String,String> getCameraStatus() {
        Map<String,String> map = new HashMap<>(8);
        try {
            List<Long> recordIdList = tCameraScreenDao.selectRecordId();
            for(Long recordId:recordIdList){
                HashMap<String, Object> recordIdMap = new HashMap<>();
                recordIdMap.put("recordId",recordId );
                Result re = cameraStates(recordIdMap);
                if(re == null){
                    continue;
                }
                map.putAll((Map<String,String>)re.getData());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }

    public List<AreaInfo> selectInspectedDevTree(InspectedDevTreeCondition condition) {
        List<AreaInfo> listTree = new ArrayList<>();
        switch (condition.getLevel()) {
            case "5":
                break;
            case "6":
                break;
            case "7":
                break;
            case "8":
                break;
            case "9":
                break;
            case "10":
                break;
            default:
                throw new BusinessException("设备树展示层级输入有误！");
        }
        return listTree;
    }

    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

}

