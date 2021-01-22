package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.TRobotInspectionTree;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


/**
* @author tt
* @since 2020-08-05
*/
@Service
public class TRobotInfoService{

    @Autowired
    private TRobotInfoDao tRobotInfoDao;

    private Logger log = LoggerFactory.getLogger(TRobotInfoService.class);
    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.insert(tRobotInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.deleteByPrimaryId(robotId)+this.tRobotInfoDao.deleteInstance(robotId)+this.tRobotInfoDao.deleteInspection(robotId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.update(tRobotInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TRobotInfo selectByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.selectByPrimaryId(robotId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> select(Long robotId, String robotCode, String robotName, String robotStatus, Integer robotType, String robotIp, Integer robotPort,
                                    String upRegionName, String lightIp, String lightPort, String lightUsername, String lightPassword,
                                   String lnferadIp, Integer inferadPort, String inferadUsername, String inferadPassword, String photePath,
                                   String createBy, Date createDate, String updateBy, Date updateDate, String robotFactory,String isUse,
                                   Date commissionDateString, Long upRegionId, String robotPosition, String remarks,String robotSource,
                                   String address,String buildingUser,String appearanceNumber) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.select(robotId, robotCode, robotName, robotStatus, robotType, robotIp, robotPort,
                upRegionName, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword,
                photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDateString, upRegionId, robotPosition,
                robotSource,address,buildingUser,appearanceNumber,remarks);
        return tRobotInfoList;
    }

    @Logs(title = "分页模糊查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> selectByPage(String robotName,String buildingUser,Integer robotFactory,Integer robotType,String robotSource,Integer isUse,
                                          String address, List<Long> regionIdList) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.selectByPage(robotName,buildingUser,robotFactory,robotType,robotSource,isUse,address,regionIdList);
        return tRobotInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInfo> list) {
        return this.tRobotInfoDao.batchInsert(list);
    }
    @Logs(title = "从PMS系统同步机器人信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public boolean synchronizeFromPMS(String robotCode) throws Exception {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);

        String filePathAndName = "D:/testform/PMS/机器人PMS系统.xml";
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        //解析xml
        Element rootElement = document.getRootElement();

        Iterator iterator = rootElement.elementIterator();
        Map<String,Object> map = new HashMap<>();
        while (iterator.hasNext()){
            Element stu = (Element) iterator.next();
            List<Attribute> attributes = stu.attributes();

            for (Attribute attribute : attributes) {
                if (robotCode.equals(attribute.getValue())){
                    Iterator iterator1 = stu.elementIterator();
                    while (iterator1.hasNext()){
                        Element stuChild = (Element) iterator1.next();
                        map.put(stuChild.getName(),stuChild.getStringValue());
                    }
                    log.info("map的结果是==="+map);

                    String robotFactory = tRobotInfoDao.selectDictCode("robot_factory",map.get("robotFactory").toString());
                    String isUse = tRobotInfoDao.selectDictCode("robot_use",map.get("isUse").toString());
                    String robotPosition = tRobotInfoDao.selectDictCode("robot_position",map.get("robotPosition").toString());
                    Integer robotType = Integer.valueOf(tRobotInfoDao.selectDictCode("robot_type",map.get("robotType").toString()));

                    TRobotInfo tRobotInfo = new TRobotInfo()
                            .setRobotId(robotId)
                            .setRobotType(robotType)
                            .setRobotIp(map.get("robotIp").toString())
                            .setRobotPort(Integer.valueOf(map.get("robotPort").toString()))
                            .setLightIp(map.get("lightIp").toString())
                            .setLightPort(map.get("lightPort").toString())
                            .setLightUsername(map.get("lightUsername").toString())
                            .setLightPassword(map.get("lightPassword").toString())
                            .setLnferadIp(map.get("lnferadIP").toString())
                            .setInferadPort(Integer.valueOf(map.get("InferadPort").toString()))
                            .setInferadUsername(map.get("InferadUsername").toString())
                            .setInferadPassword(map.get("InferadPassword").toString())
                            .setCommissionDate(sf.parse(map.get("commissionDate").toString()))
                            .setRobotSource(map.get("robotStatus").toString())
                            .setAddress(map.get("address").toString())
                            .setBuildingUser(map.get("buildingUser").toString())
                            .setRobotFactory(robotFactory)
                            .setIsUse(isUse)
                            .setRobotPosition(robotPosition)
                            .setAppearanceNumber(map.get("appearanceNumber").toString());
                    log.info("tRobotInfo信息是==="+tRobotInfo);
                    tRobotInfoDao.update(tRobotInfo);
                    return true;
                }
            }
        }
        return false;
    }

    @Logs(title = "查询所有机器人巡检点信息树", code = "robotInspectionTree")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectInspectionTree() {
        List<TRobotInspectionTree> robotList = tRobotInfoDao.selectInspectionTree();
        List<TRobotInspectionTree> tRobotInspectionTreeList = tRobotInfoDao.batchSelectInspection();
        List<Map<String, Object>> robotPresetTreeTemList = new ArrayList<>();
        for (TRobotInspectionTree tRobot : robotList) {
            Map<String, Object> robotPresetTreeTem = new HashMap<>();
            List<Map<String, Object>> presetList = new ArrayList<>();
            Long robotId = tRobot.getRobotId();
            for (TRobotInspectionTree tRobotInspectionTree : tRobotInspectionTreeList) {
                Long robotIdTem = tRobotInspectionTree.getRobotId();
                if (Objects.nonNull(robotIdTem) && Objects.equals(robotId, robotIdTem)) {
                    Map<String, Object> inspectionMap = new HashMap<>();
                    inspectionMap.put("id", tRobotInspectionTree.getInspectionId());
                    inspectionMap.put("label", tRobotInspectionTree.getInspectionName());
                    inspectionMap.put("infoType", "inspection");
                    presetList.add(inspectionMap);
                }
            }
            robotPresetTreeTem.put("id", robotId);
            robotPresetTreeTem.put("label", tRobot.getRobotName());
            robotPresetTreeTem.put("position", tRobot.getRobotPosition());
            if(presetList == null || presetList.size()==0){
                continue;
            }
            robotPresetTreeTem.put("children", presetList);
            robotPresetTreeTem.put("infoType", "robot");
            robotPresetTreeTemList.add(robotPresetTreeTem);
        }
        Map<String, Object> robotInspectionTree = new HashMap<>();
        robotInspectionTree.put("children", robotPresetTreeTemList);
        robotInspectionTree.put("label", "机器人巡检点列表");
        robotInspectionTree.put("infoType", "tree");
        robotInspectionTree.put("id", "-1");
        List<Map<String, Object>> robotInspectionList = new ArrayList<>();
        robotInspectionList.add(robotInspectionTree);
        return robotInspectionList;
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String robotIds) {
        int i = 0;
        List<String> robotIdList= Arrays.asList(robotIds.split(","));
        for (String item: robotIdList) {
            i = i+ this.deleteByPrimaryId(Long.valueOf(item));
        }
        return i;
    }

}
