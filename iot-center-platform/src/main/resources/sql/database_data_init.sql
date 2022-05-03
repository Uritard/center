/*
Navicat MySQL Data Transfer

Source Server         : 10.151
Source Server Version : 50734
Source Host           : 192.168.10.151:3306
Source Database       : intelligenceelec

Target Server Type    : MYSQL
Target Server Version : 50734
File Encoding         : 65001

Date: 2022-04-27 18:37:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Records of t_sys_param
-- ----------------------------
INSERT INTO `t_sys_param` VALUES ('72', '402', 'nvrFreeMin', '录像机最小空闲报警', '70', '单位 %');
INSERT INTO `t_sys_param` VALUES ('100001', '403', 'videoRunningTime', '视频轮巡视频播放时间', '1', '单位 分钟');
INSERT INTO `t_sys_param` VALUES ('100003', '400', 'systemUDPServices', 'udp服务路径和端口', 'http://192.168.10.151:18714', 'udp服务');
INSERT INTO `t_sys_param` VALUES ('100004', '400', 'systemVideoServices', '视频服务路径和端口', 'http://192.168.10.151:18715', '视频服务');
INSERT INTO `t_sys_param` VALUES ('100005', '400', 'systemLogServices', '日志服务路径和端口', 'http://192.168.10.151:18712', '日志服务');
INSERT INTO `t_sys_param` VALUES ('100006', '400', 'systemPlatFromServices', '系统平台服务路径和端口', 'http://192.168.10.151:18711', '系统平台服务');
INSERT INTO `t_sys_param` VALUES ('100007', '400', 'systemGateWayServices', '网关服务路径和端口', 'http://192.168.10.151:18701', '网关服务');
INSERT INTO `t_sys_param` VALUES ('100008', '400', 'systemManagerServices', '服务管理服务路径和端口', 'http://192.168.10.151:18710', '服务管理服务');
INSERT INTO `t_sys_param` VALUES ('100009', '400', 'systemRobotServices', '机器人服务路径和端口', 'http://192.168.10.151:18716', '机器人服务');
INSERT INTO `t_sys_param` VALUES ('100015', '401', 'picModelPath', '模板图片路径', '/home/yjh_iot_center/iot-picture/model-picture/sync/Template/BigImg', '模板图片路径');
INSERT INTO `t_sys_param` VALUES ('100016', '401', 'reportReflect', '报表绝对路径', '/home/yjh_iot_center/iot-files/reportFiles', '报表绝对路径');
INSERT INTO `t_sys_param` VALUES ('100017', '401', 'tempReflect', '临时文件绝对路径', '/home/yjh_iot_center/iot-files/temporaryFiles', '临时文件绝对路径');
INSERT INTO `t_sys_param` VALUES ('100018', '401', 'reportRelative', '报表相对路径', '/files/reportFiles', '报表相对路径');
INSERT INTO `t_sys_param` VALUES ('100019', '401', 'unionDeviceInfoPath', '联动设备配置文件路径', '/home/yjh_iot_center/iot-files/UDPFiles', '联动设备配置文件路径');
INSERT INTO `t_sys_param` VALUES ('100020', '401', 'meteModelPath', '临时文件相对路径', '/files/temporaryFiles', '临时文件相对路径');
INSERT INTO `t_sys_param` VALUES ('100021', '401', 'ftpsFilePath', 'ftps服务器存储文件路径', '/home/yjh_iot_center/ftps', 'ftps服务器存储文件路径');
INSERT INTO `t_sys_param` VALUES ('100022', '401', 'ftpImageAbsolute', '机器人巡检结果图片绝对路径', '/home/yjh_iot_center/iot-picture/ftpImg', '机器人巡检结果图片绝对路径');
INSERT INTO `t_sys_param` VALUES ('100023', '401', 'ftpImageRelative', '机器人巡检结果图片相对路径', 'http://192.168.10.151:10086/imgs/ftpImg', '机器人巡检结果图片相对路径');
INSERT INTO `t_sys_param` VALUES ('100024', '401', 'zipPath', '压缩包文件路径', '/home/yjh_iot_center/iot-picture/zip', '预置位图片集压缩相对路径');
INSERT INTO `t_sys_param` VALUES ('100025', '401', 'zipRealPath', '压缩包绝对路径', 'http://192.168.10.151:10086/imgs/zip', '预置位图片集压缩绝对路径');
INSERT INTO `t_sys_param` VALUES ('100026', '401', 'zipTargetPath', '标定文件导入路径', '/home/yjh_iot_center/iot-picture/zip', '标定文件导入路径');
INSERT INTO `t_sys_param` VALUES ('100027', '404', 'waitTime', '等待时间', '10000', '摄像头转到预置位等待的时间 单位 毫秒');
INSERT INTO `t_sys_param` VALUES ('100028', '404', 'tasksAreTime', '任务超期', '1', '任务超期时间 单位 小时');
INSERT INTO `t_sys_param` VALUES ('100030', '404', 'isIntelAnalysis', '是否开启调用智能分析主机接口', 'false', '是否开启调用智能分析主机接口');
INSERT INTO `t_sys_param` VALUES ('100031', '404', 'confirmExpireTime', '二次确认超时时间', '60000', '单位:毫秒');
INSERT INTO `t_sys_param` VALUES ('100032', '404', 'stationVoltageGrade', '首页-变电站电压等级', '220', '单位 KV');
INSERT INTO `t_sys_param` VALUES ('100033', '404', 'fromWhatClass', '首页-所属班所', '1', '所属班所');
INSERT INTO `t_sys_param` VALUES ('100034', '404', 'allPeople', '首页-所有的人员', '66', '所有的人');
INSERT INTO `t_sys_param` VALUES ('100035', '404', 'workPeople', '首页-工作的人', '50', '在工作的人');
INSERT INTO `t_sys_param` VALUES ('100036', '404', 'allCar', '首页-所有的车', '20', '所有的车');
INSERT INTO `t_sys_param` VALUES ('100037', '404', 'workCar', '首页-工作的车', '10', '在工作的车');
INSERT INTO `t_sys_param` VALUES ('100038', '404', 'serialPort', '微气象-串口', 'COM3', '微气象数据传输串口');
INSERT INTO `t_sys_param` VALUES ('100039', '404', 'baudRate', '微气象-波特率', '19200', '微气象波特率');
INSERT INTO `t_sys_param` VALUES ('100040', '404', 'weatherInfoService', '微气象系统服务路径和端口', 'http://192.168.10.100:18717', '获取微气象数据的服务');
INSERT INTO `t_sys_param` VALUES ('100041', '404', 'heartbeatInterval', '机器人心跳间隔', '6', '机器人心跳间隔（单位：s）');
INSERT INTO `t_sys_param` VALUES ('100042', '404', 'runDataInterval', '机器人运行数据间隔', '30', '机器人运行数据间隔（单位：s）');
INSERT INTO `t_sys_param` VALUES ('100043', '404', 'weatherDataInterval', '机器人微气象数据间隔', '100', '微气象数据间隔（单位：s） ');
INSERT INTO `t_sys_param` VALUES ('100044', '402', 'logoutTime', '用户不操作退出登录时间', '30', '用户不操作退出登录时间 单位：分钟');
INSERT INTO `t_sys_param` VALUES ('100047', '404', 'upSystemReceiveCode', '上级系统-上级系统唯一标识', 'Server02', '上级系统唯一标识');
INSERT INTO `t_sys_param` VALUES ('100048', '404', 'cruiseHostSendCode', '巡视主机-巡视主机唯一标识', 'Client02', '巡视主机唯一标识');
INSERT INTO `t_sys_param` VALUES ('100049', '402', 'cpuFreeMin', 'CPU最小空闲报警', '20', '单位:%');
INSERT INTO `t_sys_param` VALUES ('100050', '402', 'DiskFreeMin', '硬盘最小空闲报警', '20', '单位:%');
INSERT INTO `t_sys_param` VALUES ('100051', '402', 'MemoryFreeMin', '内存最小空闲报警', '20', '单位:%');
INSERT INTO `t_sys_param` VALUES ('100052', '401', 'modelAbsolutePath', '模型文件绝对路径', '/home/yjh_iot_center/iot-files/tcpFiles', '模型文件的绝对路径');
INSERT INTO `t_sys_param` VALUES ('100053', '401', 'modelRelativePath', '模型文件的相对路径', 'http://192.168.10.151:10086/files/tcpFiles', '模型文件的相对路径');
INSERT INTO `t_sys_param` VALUES ('100054', '402', 'secureVerify', '安全参数配置', 'MemoryFreeMin,cpuFreeMin,DiskFreeMin', '安全标记');
INSERT INTO `t_sys_param` VALUES ('100055', '404', 'PlatformServer', '巡视主机唯一标识', 'Server01', '巡视主机与机器人通信唯一标识');
INSERT INTO `t_sys_param` VALUES ('100057', '404', 'commissioningTime', '首页-投运时间', '2020-07-06', '系统的投运时间');
INSERT INTO `t_sys_param` VALUES ('100058', '401', 'robotPicModelPath', '机器人标定文件路径', '/home/yjh_iot_center/iot-picture/model-picture/sync/RobotTemplate/BigImg', '机器人标定文件路径');
INSERT INTO `t_sys_param` VALUES ('100059', '401', 'absVoicePath', '音频文件相对路径', '/home/yjh_iot_center/iot-files/voiceFile', '音频文件相对路径');
INSERT INTO `t_sys_param` VALUES ('100060', '401', 'relativeVoicePath', '音频文件绝对路径', 'http://192.168.10.151:10086/files/voiceFile', '音频文件绝对路径');
INSERT INTO `t_sys_param` VALUES ('100061', '404', 'cleanTime', '顺控步骤清除时间', '120', '单位：秒');
INSERT INTO `t_sys_param` VALUES ('100062', '402', 'isDecode', '是否拦截参数篡改', 'true', '是否拦截参数篡改');
INSERT INTO `t_sys_param` VALUES ('100063', '402', 'isEncryption', '是否开启加解密', 'false', '是否开启加解密');
INSERT INTO `t_sys_param` VALUES ('100064', '402', 'isUkey', '是否开启Ukey', 'false', '是否开启Ukey');
INSERT INTO `t_sys_param` VALUES ('100065', '401', 'defectResultImg', '缺陷识别算法图片绝对路径', '/home/yjh_iot_center/iot-picture/analyseResultImg/defect', '缺陷识别算法图片绝对路径');
INSERT INTO `t_sys_param` VALUES ('100066', '401', 'defectResultRealImg', '缺陷算法图片存储相对路径', 'http://192.168.10.151:10086/imgs/analyseResultImg/defect', '缺陷算法图片存储相对路径');
INSERT INTO `t_sys_param` VALUES ('100067', '402', 'voiceFileRecordTime', '音频文件分段时长', '5', '单位：分钟');
INSERT INTO `t_sys_param` VALUES ('100068', '402', 'loginErrorNum', '登录错误次数', '5', '登录错误次数');
INSERT INTO `t_sys_param` VALUES ('100069', '402', 'lockTime', '登录锁定时间', '20', '登录锁定时间');
INSERT INTO `t_sys_param` VALUES ('100070', '402', 'isIp', '是否开启ip验证', 'false', '是否开启ip验证');
INSERT INTO `t_sys_param` VALUES ('100071', '402', 'isLogin', '账户是否只能一处登录', 'false', '账户是否只能一处登录');
INSERT INTO `t_sys_param` VALUES ('100072', '404', 'noLogTime', '不写入日志-操作时间', '2021-03-03 00:00:00~2021-03-04 00:00:00', '在这个时间段内，用户的操作将补写入日志。（格式：yyyy-MM-dd HH:mm:ss~yyyy-MM-dd HH:mm:ss 示例:2021-03-03 00:00:00~2021-03-04 00:00:00）');
INSERT INTO `t_sys_param` VALUES ('100073', '404', 'noLogType', '不写入日志-操作类型', '查询', '用户执行此操作时，将不写入日志。（操作类型包括：查询，新增，修改，删除，执行等）（多个操作类型以-隔开。示列1：查询。示例2：查询-修改）');
INSERT INTO `t_sys_param` VALUES ('100077', '401', 'meterResultImg', '表计识别算法结果图片存储绝对路径', '/home/yjh_iot_center/iot-picture/analyseResultImg/meter', '');
INSERT INTO `t_sys_param` VALUES ('100078', '401', 'meterResultRealImg', '表计识别算法结果图片存储相对路径', 'http://192.168.10.151:10086/imgs/analyseResultImg/meter', '');
INSERT INTO `t_sys_param` VALUES ('100079', '401', 'deviceTypeImgAbsPath', '设备类型图片存储绝对路径', '/home/yjh_iot_center/iot-files/deviceTypeImage', '设备类型图片存储绝对路径');
INSERT INTO `t_sys_param` VALUES ('100080', '401', 'deviceTypeImgReaPath', '设备类型图片存储相对路径', 'http://192.168.10.151:10086/files/deviceTypeImage', '设备类型图片存储相对路径');
INSERT INTO `t_sys_param` VALUES ('100081', '404', 'isCheckByFour', '检测-是否开启', 'false', '专为检测使用（4个一组）');
INSERT INTO `t_sys_param` VALUES ('100082', '404', 'checkWaitTimeByFour', '检测-等待时间', '30', '检测专用，等待时间（单位秒）');
INSERT INTO `t_sys_param` VALUES ('100083', '404', 'isCheckByOne', '检测-是否开启（1个点）', 'false', '是否开启，一个点');
INSERT INTO `t_sys_param` VALUES ('100084', '404', 'checkWaitTimeByOne', '检测-等待时间（1个点）', '10', '等待时间，单位：秒');
INSERT INTO `t_sys_param` VALUES ('100085', '401', 'judgeResultImg', '判别算法图片存储绝对路径', '/home/yjh_iot_center/iot-picture/analyseResultImg/panbie/', '');
INSERT INTO `t_sys_param` VALUES ('100086', '401', 'judgeResultRealImg', '判别算法图片存储相对路径', 'http://192.168.10.151:10086/imgs/analyseResultImg/panbie', '');
INSERT INTO `t_sys_param` VALUES ('100087', '404', 'webSocketUrl', '调用其他服务的webSocket', 'http://192.168.10.151:18701/route/syncWebsocket', '调用其他服务的webSocket');
INSERT INTO `t_sys_param` VALUES ('100088', '401', 'infraredStorePath', '红外文件存储绝对路径', '/home/yjh_iot_center/iot-picture/infrared/', '');
INSERT INTO `t_sys_param` VALUES ('100089', '401', 'infraredRealPath', '红外文件存储相对路径', 'http://192.168.10.151:10086/imgs/infrared/', '');
INSERT INTO `t_sys_param` VALUES ('100090', '401', 'resultImgPath', '相机抓图存储绝对路径', '/home/yjh_iot_center/iot-picture/resultImg/', '');
INSERT INTO `t_sys_param` VALUES ('100091', '401', 'resultImgRealPath', '相机抓图存储相对路径', 'http://192.168.10.151:10086/imgs/resultImg/', '');
INSERT INTO `t_sys_param` VALUES ('100092', '401', 'presetImgPath', '预置位图片存储相对路径', '/home/yjh_iot_center/iot-picture/specimens', '');
INSERT INTO `t_sys_param` VALUES ('100093', '401', 'presetRealImgPath', '预置位图片存储绝对路径', 'http://192.168.10.151:10086/imgs/specimens', '');
INSERT INTO `t_sys_param` VALUES ('100094', '404', 'zipFileSize', '采集文件大小限制', '20', '采集文件大小超过此限制，禁止下载。（单位：M）(默认值为20)');
INSERT INTO `t_sys_param` VALUES ('100095', '404', 'keepWatchTime', '摄相机回到守望位置时间', '2', '检测专用，摄像机在时间内未收到控制摄像机请求， 应自动回归守望位（默认10），单位分钟');
INSERT INTO `t_sys_param` VALUES ('100096', '404', 'stationName', '变电站名称', '江苏变电站', '变电站名称');
INSERT INTO `t_sys_param` VALUES ('100097', '402', 'isIpLogin', '是否开启用户登录ip验证', 'false', '是否开启用户登录ip验证');
INSERT INTO `t_sys_param` VALUES ('100098', '404', 'voiceDeviceTime', '声纹设备录音时长', '60', '单位：秒');


INSERT INTO `t_version`(`version_id`, `version_name`, `remark`, `text`, `create_time`) VALUES (1, '1.0.1.200806_relase', '巡视主机初始版本，具备巡视主机主要功能。', '巡视主机初始版本，具备巡视主机主要功能。', '2020-08-06 08:08:08');
INSERT INTO `t_version`(`version_id`, `version_name`, `remark`, `text`, `create_time`) VALUES (2, '1.5.6.210412_relase', '巡视主机特高压检测版本，满足特高压检测要求。', '修复若干BUG\n巡视主机特高压检测版本，满足特高压检测要求。', '2021-04-12 14:08:27');
INSERT INTO `t_version`(`version_id`, `version_name`, `remark`, `text`, `create_time`) VALUES (3, '1.5.8.210608_relase', '巡视主机商用版本，增加用户定制需求。', '修复若干BUG\n巡视主机商用版本，增加用户定制需求。', '2021-06-18 10:08:38');
INSERT INTO `t_version`(`version_id`, `version_name`, `remark`, `text`, `create_time`) VALUES (4, '2.0.1.220506_RC', '巡视主机新特高压检测版本，添加新特高压检测要求。', '1.修复个别BUG\n2.巡检功能逻辑优化\n3.数据采集功能优化\n4.视频播放功能优化\n5.配置逻辑优化', '2022-05-06 09:09:11');
INSERT INTO `t_version`(`version_id`, `system_type`, `version_name`, `remark`, `text`, `create_time`) VALUES (5, 2, '1.1.0.210623_beta', '增加用户定制需求，性能优化。', '1.修复个别BUG\n2.增加用户定制需求\n3.性能优化，提高检测精度', '2022-04-18 14:09:11');
INSERT INTO `t_version`(`version_id`, `system_type`, `version_name`, `remark`, `text`, `create_time`) VALUES (6, 2, '1.1.1.220422_beta', '特高压检测版本，添加新特高压检测要求，增加检测类型。', '1.修复若干BUG\n2.检测接口优化修改\n3.图片传输方式修改\n4.性能优化，增加检测类型，提高检测精度\n5.配置逻辑优化', '2022-04-22 16:22:53');