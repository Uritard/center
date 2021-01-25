package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.TRobotInspectionTree;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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
    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TRobotInfoService.class);
    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInfo tRobotInfo,Long userId) {
        String userName = tRobotInfoDao.selectUserName(userId);
        tRobotInfo.setCreateBy(userName);
        tRobotInfo.setCreateDate(new Date());
        tRobotInfo.setRobotStatus("离线");
        return this.tRobotInfoDao.insert(tRobotInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.deleteByPrimaryId(robotId)+this.tRobotInfoDao.deleteInstance(robotId)+this.tRobotInfoDao.deleteInspection(robotId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInfo tRobotInfo,Long userId) {
        String userName = tRobotInfoDao.selectUserName(userId);
        tRobotInfo.setUpdateBy(userName);
        tRobotInfo.setUpdateDate(new Date());

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
                                   Date commissionDateString, Long upRegionId, String robotPosition, String robotSource,
                                   String address,String buildingUser,String appearanceNumber,String defectRecord,String repairRecord,
                                   String exitPutIntoRecord,String remarks) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.select(robotId, robotCode, robotName, robotStatus, robotType, robotIp, robotPort,
                upRegionName, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword,
                photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDateString, upRegionId, robotPosition,
                robotSource,address,buildingUser,appearanceNumber,defectRecord,repairRecord,exitPutIntoRecord,remarks);
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
    public boolean synchronizeFromPMS(String robotCode,Long userId) throws Exception {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        String userName = tRobotInfoDao.selectUserName(userId);

        Map<String,String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
//        String filePathAndName = resMap.get("content") +  "/PMS/RobotPMS.xml";
        String filePathAndName = "D:/testform/PMS/机器人PMS系统.xml";
        log.info("路径是==="+filePathAndName);

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
                            .setUpdateBy(userName)
                            .setUpdateDate(new Date())
                            .setRobotFactory(robotFactory)
                            .setIsUse(isUse)
                            .setCommissionDate(sf.parse(map.get("commissionDate").toString()))
                            .setRobotSource(map.get("robotSource").toString())
                            .setAddress(map.get("address").toString())
                            .setBuildingUser(map.get("buildingUser").toString())
                            .setRobotPosition(robotPosition)
                            .setAppearanceNumber(map.get("appearanceNumber").toString())
                            .setDefectRecord(map.get("defectRecord").toString())
                            .setRepairRecord(map.get("repairRecord").toString())
                            .setExitPutIntoRecord(map.get("exitPutIntoRecord").toString())
                            .setRemarks(map.get("remarks").toString());
                    log.info("tRobotInfo信息是==="+tRobotInfo);
                    tRobotInfoDao.update(tRobotInfo);
                    return true;
                }
            }
        }
        return false;
    }
    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int lookRobotRecord(List<TRobotInfo> list) {
        return this.tRobotInfoDao.batchInsert(list);
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
    @Logs(title = "从PMS系统同步机器人信息2", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int synchronizeFromPMS2() throws IOException{
        BufferedReader br = null;
        InputStreamReader in = null;
        String filePath = "D:/testform/PMS/PMS.txt";
        try  {
            in = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8");
            br = new BufferedReader(in);
            String line;
            String[] strArray = null;
            List<TRobotInfo> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if(line.contains("#")){
                    strArray = line.split("\\s+");
                    if("".equals(strArray[0])){
                        strArray= Arrays.copyOfRange(strArray,1,strArray.length);
                    }
                    //取数据
                    String robotFactory = tRobotInfoDao.selectDictCode("robot_factory",strArray[3]);
                    String isUse = tRobotInfoDao.selectDictCode("robot_use",strArray[4]);
                    Integer robotType = Integer.valueOf(tRobotInfoDao.selectDictCode("robot_type",strArray[2]));

                    TRobotInfo tRobotInfo = new TRobotInfo();
                    tRobotInfo.setRobotCode(strArray[1]);
                    tRobotInfo.setRobotType(robotType);
                    tRobotInfo.setRobotFactory(robotFactory);
                    tRobotInfo.setIsUse(isUse);
                    list.add(tRobotInfo);
                }
            }
            log.info("准备插库的结果是==="+list);
            br.close();
            in.close();
        }catch (IOException e) {
            log.info("读取文件错误: "+e);
        } finally {
            if(br != null ){
                br.close();
            }
            if(in != null ){
                in.close();
            }

        }
        return 1;
    }

    @Logs(title = "读取从PMS系统获取的文件再生成xml", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int generateXMLByFile() throws IOException {
        BufferedReader br = null;
        InputStreamReader in = null;
        String filePath = "D:/testform/PMS/PMS.txt";
        try {
            //读取文件
            in = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8");
            br = new BufferedReader(in);
            String line;
            String[] strArray = null;
            Document document = DocumentHelper.createDocument();
            Element rss = document.addElement("Robot");
            while ((line = br.readLine()) != null) {
                if (line.contains("#")) {
                    strArray = line.split("\\s+");
                    if ("".equals(strArray[0])) {
                        strArray = Arrays.copyOfRange(strArray, 1, strArray.length);
                    }
                    Element items = rss.addElement("items");
                    items.addAttribute("robotCode", strArray[1]);
                    Element robotType = items.addElement("robotType");
                    robotType.setText(strArray[2]);
                    Element robotFactory = items.addElement("robotFactory");
                    robotFactory.setText(strArray[3]);
                    Element isUse = items.addElement("isUse");
                    isUse.setText(strArray[4]);
                    Element commissionDate = items.addElement("commissionDate");
                    commissionDate.setText(strArray[5]);
                    Element robotSource = items.addElement("robotSource");
                    robotSource.setText(strArray[6]);
                    Element address = items.addElement("address");
                    address.setText(strArray[7]);
                    Element buildingUser = items.addElement("buildingUser");
                    buildingUser.setText(strArray[8]);
                    Element appearanceNumber = items.addElement("appearanceNumber");
                    appearanceNumber.setText(strArray[9]);
                    Element defectRecord = items.addElement("defectRecord");
                    defectRecord.setText(strArray[10]);
                    Element repairRecord = items.addElement("repairRecord");
                    repairRecord.setText(strArray[11]);
                    Element exitPutIntoRecord = items.addElement("exitPutIntoRecord");
                    exitPutIntoRecord.setText(strArray[12]);
                }
            }
            br.close();
            in.close();

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            //生成xml文件
            File file = new File("D:/testform/Robot_PMS_System.xml");
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            writer.setEscapeText(false);
            writer.write(document);
            writer.close();
            System.out.println("生成Robot_PMS_System.xml成功");

        } catch (IOException e) {
            log.info("读取文件错误: "+e);
        } finally {
            if(br != null ){
                br.close();
            }
            if(in != null ){
                in.close();
            }
        }
        return 1;
    }
}
