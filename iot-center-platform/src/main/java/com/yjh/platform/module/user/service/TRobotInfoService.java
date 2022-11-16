package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.smUtil.ModelDecodeUtil;
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

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
    @Autowired
    private TCameraInfoService tCameraInfoService;
    @Autowired
    private LogsRecord logsRecord;

    private String ROBOT_REMOVE_LINK =  "http://iot-center-accessrobot/robot/v1/removeLink?robotCode={robotCode}&robotId={robotId}";

    private Logger log = LoggerFactory.getLogger(TRobotInfoService.class);
    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInfo tRobotInfo,Long userId) {
        String userName = tRobotInfoDao.selectUserName(userId);
        tRobotInfo.setCreateBy(userName);
        tRobotInfo.setCreateDate(new Date());
        tRobotInfo.setRobotStatus("离线");

        // 对加密口令进行解密
        ModelDecodeUtil.decodeField(tRobotInfo, "identityCode", "inferadPassword");

        int res = this.tRobotInfoDao.insert(tRobotInfo);
        if (res > 0){
            Long robotId = tRobotInfoDao.selectRobotIdByCode(tRobotInfo.getRobotCode());
            redisTemplate.opsForHash().put("AllRobotCode",robotId.toString(),tRobotInfo.getRobotCode());
        }
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotId, HttpServletRequest request) {
        //机器人有测点或巡视点
        List<Long> list = tRobotInfoDao.selectHaveIns(robotId);
        if(list != null && !list.isEmpty()){
            return -1;
        }

        TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
        int res = this.tRobotInfoDao.deleteByPrimaryId(robotId)
                + this.tRobotInfoDao.deleteInstance(robotId)
                + this.tRobotInfoDao.deleteInspection(robotId)
                + this.tRobotInfoDao.deleteCruisePlan(robotId);
        if (res > 0){
            redisTemplate.opsForHash().delete("AllRobotCode",tRobotInfo.getRobotId().toString());
        }
        //判断删除前的robotCode是否存在管道连接(在线),若存在，则断开连接
        if (Objects.equals("在线", tRobotInfo.getRobotStatus())){
            HashMap<String,String> map = new HashMap<>();
            map.put("robotCode",tRobotInfo.getRobotCode());
            map.put("robotId",tRobotInfo.getRobotId().toString());
            log.info("删除前的robotCode==="+map);
            sendPostRequest(ROBOT_REMOVE_LINK,map);
        }
        if (request != null) {
            String logName = "机器人";
            if (tRobotInfo.getDroneType() != null) {
                logName = "无人机";
            }
            logsRecord.LogsSend(request, "4", "删除" + logName + "信息", "根据用户传递的参数删除" + logName + "信息");
        }
        return tRobotInfo.getDroneType() != null ? 5 : 3;
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInfo tRobotInfo,Long userId) {
        String userName = tRobotInfoDao.selectUserName(userId);
        tRobotInfo.setUpdateBy(userName);
        tRobotInfo.setUpdateDate(new Date());
        TRobotInfo tRobotInfoPri = tRobotInfoDao.selectByPrimaryId(tRobotInfo.getRobotId());

        // 对加密口令进行解密
        ModelDecodeUtil.decodeField(tRobotInfo, "identityCode", "inferadPassword");

        int res = this.tRobotInfoDao.update(tRobotInfo);
        if (res > 0){
            redisTemplate.opsForHash().put("AllRobotCode",tRobotInfo.getRobotId().toString(),tRobotInfo.getRobotCode());
        }

        //判断修改前的robotCode是否存在管道连接(在线),若存在，则断开连接
        if (!tRobotInfoPri.getRobotCode().equals(tRobotInfo.getRobotCode()) && tRobotInfoPri.getRobotStatus().equals("在线")){
            HashMap<String,String> map = new HashMap<>();
            map.put("robotCode",tRobotInfoPri.getRobotCode());
            map.put("robotId",tRobotInfoPri.getRobotId().toString());
            log.info("修改前的robotCode==="+map);
            Result result = sendPostRequest(ROBOT_REMOVE_LINK,map);
            log.info("result==="+result);
            TRobotInfo tRobotInfoTemp = new TRobotInfo()
                    .setRobotStatus(result.getData().toString())
                    .setRobotId(tRobotInfoPri.getRobotId());
            //机器人状态改变给前端推送webSocket
            /*Map<String,Object> jasonMap=new HashMap<>();
            jasonMap.put("type","robotStatus");
            jasonMap.put("status",tRobotInfoTemp.getRobotStatus());
            String json= JSON.toJSONString(jasonMap);
            WebSocketServer.sendMsg(json);*/
            tRobotInfoDao.update(tRobotInfoTemp);
        }
        Long lightCameraId = Long.parseLong(String.valueOf(tRobotInfo.getRobotId())+ "9901");
        Long infraredCameraId = Long.parseLong(String.valueOf(tRobotInfo.getRobotId())+ "9902");
        tCameraInfoService.stopStream(lightCameraId);
        tCameraInfoService.stopStream(infraredCameraId);

        return res;
    }
    @Transactional(rollbackFor = Exception.class)
    public TRobotInfo selectByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.selectByPrimaryId(robotId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> select(Long robotId, String robotCode, String robotNum, String robotName, String robotStatus, Integer robotType, String robotIp, Integer robotPort,
                                    String upRegionName, String lightIp, String lightPort, String identityManager, String identityCode,
                                   String lnferadIp, Integer inferadPort, String inferadUsername, String inferadPassword, String photePath,
                                   String createBy, Date createDate, String updateBy, Date updateDate, String robotFactory,String isUse,
                                   Date commissionDateString, Long upRegionId, String robotPosition, String robotSource,
                                   String address,String buildingUser,String appearanceNumber,String defectRecord,String repairRecord,
                                   String exitPutIntoRecord,String remarks) {
        return tRobotInfoDao.select(robotId, robotCode, robotNum, robotName, robotStatus, robotType, robotIp, robotPort,
                upRegionName, lightIp, lightPort, identityManager, identityCode, lnferadIp, inferadPort, inferadUsername, inferadPassword,
                photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDateString, upRegionId, robotPosition,
                robotSource,address,buildingUser,appearanceNumber,defectRecord,repairRecord,exitPutIntoRecord,remarks);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> selectByPage(Integer robotPosition,String robotName,String buildingUser,Integer robotFactory,Integer robotType, Integer droneType,String robotSource,Integer isUse,
                                          String address,Integer type, List<Long> regionIdList) {
        List<TRobotInfo> tRobotInfoList = new ArrayList<>();
        if (type != null && type == 2){
            tRobotInfoList = tRobotInfoDao.selectDroneByPage(robotName,buildingUser,robotFactory,droneType,robotSource,isUse,address,regionIdList);
        }else {
            tRobotInfoList = tRobotInfoDao.selectRobotByPage(robotPosition,robotName,buildingUser,robotFactory,robotType,robotSource,isUse,address,regionIdList);
        }
        for (TRobotInfo tRobotInfo : tRobotInfoList){
            redisTemplate.opsForHash().put("AllRobotCode",tRobotInfo.getRobotId().toString(),tRobotInfo.getRobotCode());
        }
        return tRobotInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInfo> list) {
        int res = tRobotInfoDao.batchInsert(list);
        for (TRobotInfo tRobotInfo : list){
            redisTemplate.opsForHash().put("AllRobotCode", String.valueOf(tRobotInfo.getRobotId()), tRobotInfo.getRobotCode());
        }
        return res;
    }
    @Transactional(rollbackFor = Exception.class)
    public boolean synchronizeFromPMS(String robotCode,Long userId) throws Exception {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        String userName = tRobotInfoDao.selectUserName(userId);

        Map<String,String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String filePathAndName = resMap.get("content") +  "/PMS/RobotPMS.xml";
//        String filePathAndName = "D:/testform/PMS/RobotPMS.xml";
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

                    TRobotInfo tRobotInfo = new TRobotInfo()
                            .setRobotId(robotId);
                    if (map.containsKey("robotName") && (!"".equals(map.get("robotName")))) {
                        tRobotInfo.setRobotName(map.get("robotName").toString());
                    }
                    if (map.containsKey("robotStatus") && (!"".equals(map.get("robotStatus")))) {
                        tRobotInfo.setRobotStatus(map.get("robotStatus").toString());
                    }
                    if (map.containsKey("robotType") && (!"".equals(map.get("robotType")))) {
                        Integer robotType = Integer.valueOf(tRobotInfoDao.selectDictCode("robot_type",map.get("robotType").toString()));
                        tRobotInfo.setRobotType(robotType);
                    }
                    if (map.containsKey("robotIp") && (!"".equals(map.get("robotIp")))) {
                        tRobotInfo.setRobotIp(map.get("robotIp").toString());
                    }
                    if (map.containsKey("robotPort") && (!"".equals(map.get("robotPort")))) {
                        tRobotInfo.setRobotPort(Integer.valueOf(map.get("robotPort").toString()));
                    }
                    if (map.containsKey("lightIp") && (!"".equals(map.get("lightIp")))) {
                        tRobotInfo.setLightIp(map.get("lightIp").toString());
                    }
                    if (map.containsKey("lightPort") && (!"".equals(map.get("lightPort")))) {
                        tRobotInfo.setLightPort(map.get("lightPort").toString());
                    }
                    if (map.containsKey("identityManager") && (!"".equals(map.get("identityManager")))) {
                        tRobotInfo.setIdentityManager(map.get("identityManager").toString());
                    }
                    if (map.containsKey("identityCode") && (!"".equals(map.get("identityCode")))) {
                        tRobotInfo.setIdentityCode(map.get("identityCode").toString());
                    }
                    if (map.containsKey("lnferadIP") && (!"".equals(map.get("lnferadIP")))) {
                        tRobotInfo.setLnferadIp(map.get("lnferadIP").toString());
                    }
                    if (map.containsKey("InferadPort") && (!"".equals(map.get("InferadPort")))) {
                        tRobotInfo.setInferadPort(Integer.valueOf(map.get("InferadPort").toString()));
                    }
                    if (map.containsKey("InferadUsername") && (!"".equals(map.get("InferadUsername")))) {
                        tRobotInfo.setInferadUsername(map.get("InferadUsername").toString());
                    }
                    if (map.containsKey("InferadPassword") && (!"".equals(map.get("InferadPassword")))) {
                        tRobotInfo.setInferadPassword(map.get("InferadPassword").toString());
                    }
                    if (map.containsKey("photePath") && (!"".equals(map.get("photePath")))) {
                        tRobotInfo.setPhotePath(map.get("photePath").toString());
                    }
                    tRobotInfo.setUpdateBy(userName);
                    tRobotInfo.setUpdateDate(new Date());
                    if (map.containsKey("robotFactory") && (!"".equals(map.get("robotFactory")))) {
                        String robotFactory = tRobotInfoDao.selectDictCode("robot_factory",map.get("robotFactory").toString());
                        tRobotInfo.setRobotFactory(robotFactory);
                    }
                    if (map.containsKey("isUse") && (!"".equals(map.get("isUse")))) {
                        String isUse = tRobotInfoDao.selectDictCode("robot_use",map.get("isUse").toString());
                        tRobotInfo.setIsUse(isUse);
                    }
                    if (map.containsKey("commissionDate") && (!"".equals(map.get("commissionDate")))) {
                        tRobotInfo.setCommissionDate(sf.parse(map.get("commissionDate").toString()));
                    }
                    if (map.containsKey("robotSource") && (!"".equals(map.get("robotSource")))) {
                        tRobotInfo.setRobotSource(map.get("robotSource").toString());
                    }
                    if (map.containsKey("address") && (!"".equals(map.get("address")))) {
                        tRobotInfo.setAddress(map.get("address").toString());
                    }
                    if (map.containsKey("buildingUser") && (!"".equals(map.get("buildingUser")))) {
                        tRobotInfo.setBuildingUser(map.get("buildingUser").toString());
                    }
                    if (map.containsKey("robotPosition") && (!"".equals(map.get("robotPosition")))) {
                        String robotPosition = tRobotInfoDao.selectDictCode("robot_position",map.get("robotPosition").toString());
                        tRobotInfo.setRobotPosition(robotPosition);
                    }
                    if (map.containsKey("appearanceNumber") && (!"".equals(map.get("appearanceNumber")))) {
                        tRobotInfo.setAppearanceNumber(map.get("appearanceNumber").toString());
                    }
                    if (map.containsKey("buildingUser") && (!"".equals(map.get("buildingUser")))) {
                        tRobotInfo.setDefectRecord(map.get("buildingUser").toString());
                    }
                    if (map.containsKey("repairRecord") && (!"".equals(map.get("repairRecord")))) {
                        tRobotInfo.setRepairRecord(map.get("repairRecord").toString());
                    }
                    if (map.containsKey("exitPutIntoRecord") && (!"".equals(map.get("exitPutIntoRecord")))) {
                        tRobotInfo.setExitPutIntoRecord(map.get("exitPutIntoRecord").toString());
                    }
                    if (map.containsKey("remarks") && (!"".equals(map.get("remarks")))) {
                        tRobotInfo.setRemarks(map.get("remarks").toString());
                    }
                    if (map.containsKey("defectRecord") && (!"".equals(map.get("defectRecord")))) {
                        tRobotInfo.setDefectRecord(map.get("defectRecord").toString());
                    }
                    if (map.containsKey("repairRecord") && (!"".equals(map.get("repairRecord")))) {
                        tRobotInfo.setRepairRecord(map.get("repairRecord").toString());
                    }
                    if (map.containsKey("exitPutIntoRecord") && (!"".equals(map.get("exitPutIntoRecord")))) {
                        tRobotInfo.setExitPutIntoRecord(map.get("exitPutIntoRecord").toString());
                    }
                    log.info("tRobotInfo信息是==="+tRobotInfo);
                    tRobotInfoDao.update(tRobotInfo);
                    return true;
                }
            }
        }
        return false;
    }
    @Transactional(rollbackFor = Exception.class)
    public int lookRobotRecord(List<TRobotInfo> list) {
        return this.tRobotInfoDao.batchInsert(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectInspectionTree(Integer inspectionType, Long upRegionId, Integer type) {
        List<TRobotInspectionTree> robotList = tRobotInfoDao.selectInspectionTree(upRegionId, type);
        List<TRobotInspectionTree> tRobotInspectionTreeList = tRobotInfoDao.batchSelectInspection(inspectionType, upRegionId, type);
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
        if(inspectionType == null){
            robotInspectionTree.put("label", "机器人测点列表");
        }else if (inspectionType == 1){
            if (type == 1) {
                robotInspectionTree.put("label", "机器人巡检点列表");
            }else {
                robotInspectionTree.put("label", "无人机巡检点列表");
            }
        }else {
            robotInspectionTree.put("label", "机器人操作点列表");
        }
        robotInspectionTree.put("infoType", "tree");
        robotInspectionTree.put("id", "-1");
        List<Map<String, Object>> robotInspectionList = new ArrayList<>();
        robotInspectionList.add(robotInspectionTree);
        return robotInspectionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String robotIds) {
        int i = 0;
        List<String> robotIdList= Arrays.asList(robotIds.split(","));
        for (String item: robotIdList) {
            int j =this.deleteByPrimaryId(Long.valueOf(item), null);
            if(j == -1){
                return -1;
            }
            i = i+j;
        }
        return i;
    }
    @Transactional(rollbackFor = Exception.class)
    public int synchronizeFromPMS2() throws IOException{
        BufferedReader br = null;
        InputStreamReader in = null;
        String filePath = "D:/testform/PMS/PMS.txt";
        try  {
            in = new InputStreamReader(new FileInputStream(new File(filePath)), StandardCharsets.UTF_8);
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

    @Transactional(rollbackFor = Exception.class)
    public String generateXMLByFile() throws IOException {
        BufferedReader br = null;
        InputStreamReader in = null;
        String filePath = "D:/testform/PMS/PMS.txt";//从这读
        String xmlPath = "D:/testform/generateXML/Robot_PMS_System.xml";//往这写
        try {
            //读取文件
            in = new InputStreamReader(new FileInputStream(new File(filePath)), StandardCharsets.UTF_8);
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
            File file = new File(xmlPath);
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            writer.setEscapeText(false);
            writer.write(document);
            writer.close();
            log.info("生成Robot_PMS_System.xml成功");

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
        return xmlPath;
    }
    public Result sendPostRequest(String url,HashMap<String,String> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class,params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }
    @Transactional(rollbackFor = Exception.class)
    public String selectRobotCodeById(Long robotId) {
        return tRobotInfoDao.selectRobotCodeById(robotId);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllRobotCode2() {
        return tRobotInfoDao.selectAllRobotCode2();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Integer> selectAllRobotNum(){
        return tRobotInfoDao.selectAllRobotNum();
    }
}
