/*
Navicat MySQL Data Transfer

Source Server         : 192.168.1.66
Source Server Version : 50728
Source Host           : 192.168.1.66:3306
Source Database       : intelligenceelec

Target Server Type    : MYSQL
Target Server Version : 50728
File Encoding         : 65001

Date: 2022-10-19 17:29:46
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(200) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  KEY `SCHED_NAME` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `qrtz_job_details` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for sys_info_backup
-- ----------------------------
DROP TABLE IF EXISTS `sys_info_backup`;
CREATE TABLE `sys_info_backup` (
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `user_name` varchar(20) DEFAULT '' COMMENT '用户名',
  `password` varchar(256) DEFAULT '' COMMENT '密码',
  `true_name` varchar(20) DEFAULT '' COMMENT '真实姓名',
  `user_type` int(1) DEFAULT '1' COMMENT '账号性质',
  `sex` int(1) DEFAULT '1' COMMENT '性别0女，1 男，2未知',
  `e_mail` varchar(128) DEFAULT '' COMMENT 'email',
  `work_no` varchar(30) DEFAULT '' COMMENT '工号',
  `face_id` varchar(32) DEFAULT '' COMMENT '人脸ID',
  `finger_id` varchar(50) DEFAULT '' COMMENT '指纹ID',
  `voice_id` varchar(32) DEFAULT '' COMMENT '声纹ID',
  `state` int(1) DEFAULT '0' COMMENT '0 正常，1 异常',
  `creator_id` bigint(20) DEFAULT '1' COMMENT '创建人',
  `user_title` varchar(32) DEFAULT '' COMMENT '用户职称',
  `appkey` varchar(20) DEFAULT '' COMMENT '用户appkey 建议使用10位随机数——用户ID',
  `image_url` varchar(255) DEFAULT '' COMMENT '头像路径',
  `role_id` bigint(20) DEFAULT '1' COMMENT '角色ID',
  `org_id` bigint(20) DEFAULT '1' COMMENT '组织机构ID',
  `user_status` tinyint(4) DEFAULT '1' COMMENT '员工状态',
  `verfi_code` varchar(256) DEFAULT '' COMMENT '摘要',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改日期',
  `invalid_time` int(9) DEFAULT NULL COMMENT '注释',
  `last_login` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后登录时间',
  `lock_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '锁定时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户备份表';

-- ----------------------------
-- Table structure for sys_key
-- ----------------------------
DROP TABLE IF EXISTS `sys_key`;
CREATE TABLE `sys_key` (
  `key_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `bind_type` int(11) NOT NULL COMMENT '绑定类型，1-Ukey 2-ip',
  `pub_key` varchar(512) DEFAULT '' COMMENT '公钥',
  `serial_num` varchar(32) DEFAULT '' COMMENT '序列号',
  `remark` varchar(32) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户绑定关联表';

-- ----------------------------
-- Table structure for sys_logs
-- ----------------------------
DROP TABLE IF EXISTS `sys_logs`;
CREATE TABLE `sys_logs` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `log_type` varchar(50) DEFAULT '' COMMENT '操作类型(0-保存、1-查询、2-新增、3-修改、4-删除、5-确认、6-登录、7-登出、8-导入、9-导出、10-任务下发、11-任务暂停、12-任务恢复、13-任务终止、14-全部)',
  `ip` varchar(50) DEFAULT '' COMMENT '请求者IP',
  `title` varchar(256) DEFAULT '' COMMENT '标题',
  `state` int(1) DEFAULT '1' COMMENT '结果，1成功，2失败',
  `content` text COMMENT '内容',
  `user_id` bigint(20) DEFAULT '99999' COMMENT '操作人ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '操作者名称',
  `request_origin` varchar(125) DEFAULT '' COMMENT '请求源 http://localhost:18711',
  `request_path` varchar(125) DEFAULT '' COMMENT '请求路径 /api/v1/sysOpLog/recordLog',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式(1-GET;2-HEAD;3-POST;4-PUT;5-DELETE;6-CONNECT;7-OPTIONS;8-TRACE;9-PATCH)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='审计日志表';

-- ----------------------------
-- Records of sys_logs
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `menu_name` varchar(20) DEFAULT '' COMMENT '菜单名称',
  `menu_code` varchar(20) NOT NULL COMMENT '菜单编码',
  `up_id` bigint(20) DEFAULT '1' COMMENT '父级ID',
  `icon_code` varchar(20) DEFAULT '' COMMENT '字体图标编码',
  `icon_url` varchar(255) DEFAULT '' COMMENT '图标地址',
  `menu_type` int(1) DEFAULT '1' COMMENT '菜单类型',
  `menu_level` int(2) DEFAULT '1' COMMENT '菜单级别',
  `element_code` varchar(255) DEFAULT '' COMMENT '页面元素 add|edit',
  `state` int(1) DEFAULT '1' COMMENT '状态（0 无效，1有效）',
  `sort` int(9) DEFAULT '1' COMMENT '排序',
  `link_type` int(1) DEFAULT '1' COMMENT '连接类型',
  `url` varchar(225) DEFAULT '' COMMENT '连接地址',
  `creator_id` bigint(20) DEFAULT '1' COMMENT '创建人',
  `sys_state` int(1) DEFAULT '1' COMMENT '系统状态（0系统，1 非系统）',
  `role_id`  bigint(20) NULL COMMENT '默认所属权限组',
  PRIMARY KEY (`menu_id`) USING BTREE,
  UNIQUE KEY `idx_sys_menu_code` (`menu_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='菜单表';

-- ----------------------------
-- Table structure for sys_org
-- ----------------------------
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org` (
  `org_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_name` varchar(50) DEFAULT '' COMMENT '组织名称',
  `org_code` varchar(50) DEFAULT '' COMMENT '组织编码',
  `up_id` bigint(20) DEFAULT '1' COMMENT '上级组织ID',
  `sort` int(9) DEFAULT '1' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator_id` bigint(20) DEFAULT '1',
  `org_level` int(9) DEFAULT '1' COMMENT '层级',
  `org_path` varchar(255) DEFAULT '',
  `dept_name` varchar(64) DEFAULT '' COMMENT '部门名称',
  PRIMARY KEY (`org_id`) USING BTREE,
  UNIQUE KEY `idx_org_code` (`org_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=500001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='组织机构表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(20) DEFAULT '' COMMENT '角色名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator_id` bigint(20) DEFAULT '1' COMMENT '创建人',
  `sys_state` int(1) DEFAULT '0' COMMENT '1系统权限，0非系统权限',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE KEY `idx_sys_role_name` (`role_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1237 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色表';

-- ----------------------------
-- Table structure for sys_role_camera
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_camera`;
CREATE TABLE `sys_role_camera` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像机ID',
  `is_checked` varchar(12) DEFAULT '',
  PRIMARY KEY (`role_id`,`camera_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色和摄像机关联表';

-- ----------------------------
-- Table structure for sys_role_device
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_device`;
CREATE TABLE `sys_role_device` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `device_id` bigint(32) NOT NULL COMMENT '设备ID',
  `is_checked` varchar(12) DEFAULT '',
  PRIMARY KEY (`role_id`,`device_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色和设备关联表';

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `rp_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_code` varchar(20) NOT NULL COMMENT '菜单编码',
  `sort` int(9) DEFAULT '1' COMMENT '菜单排序',
  `element_code` varchar(255) DEFAULT '' COMMENT '元素编码列表',
  `role_id` bigint(20) DEFAULT '1' COMMENT '角色id',
  PRIMARY KEY (`rp_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=210000 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色菜单表';

-- ----------------------------
-- Table structure for sys_role_monitor_device
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_monitor_device`;
CREATE TABLE `sys_role_monitor_device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色Id',
  `monitor_device_id` bigint(20) NOT NULL COMMENT '监视设备ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-监视设备关系表';

-- ----------------------------
-- Table structure for sys_role_region
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_region`;
CREATE TABLE `sys_role_region` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `region_id` bigint(32) NOT NULL COMMENT '机器人ID',
  `is_checked` varchar(12) DEFAULT '',
  PRIMARY KEY (`role_id`,`region_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='权限区域表';

-- ----------------------------
-- Table structure for sys_route
-- ----------------------------
DROP TABLE IF EXISTS `sys_route`;
CREATE TABLE `sys_route` (
  `route_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_id` varchar(50) DEFAULT '' COMMENT '服务id',
  `url` varchar(255) DEFAULT '' COMMENT '跳转url',
  `strip_prefix` varchar(20) DEFAULT '' COMMENT '前缀',
  `retry_able` int(1) DEFAULT '1' COMMENT '重试(0不重试，1 重试)',
  `api_name` varchar(20) DEFAULT '' COMMENT '微服务名称',
  `enabled` int(1) DEFAULT '1' COMMENT '是否可用（0不可用，1 可用）',
  `path` varchar(50) DEFAULT '' COMMENT '过滤路径',
  `remark` varchar(500) DEFAULT '',
  PRIMARY KEY (`route_id`),
  UNIQUE KEY `idx_sys_route` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路由表';

-- ----------------------------
-- Table structure for sys_unique_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_unique_user`;
CREATE TABLE `sys_unique_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `unique_user` varchar(128) DEFAULT '' COMMENT '用户名SM3码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='唯一用户名表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(20) DEFAULT '' COMMENT '用户名',
  `password` varchar(256) DEFAULT '' COMMENT '密码',
  `true_name` varchar(20) DEFAULT '' COMMENT '真实姓名',
  `user_type` int(1) DEFAULT '1' COMMENT '账号1-在线，0-离线',
  `sex` int(1) DEFAULT '2' COMMENT '性别0女，1 男，2未知',
  `e_mail` varchar(128) DEFAULT '' COMMENT 'email',
  `work_no` varchar(30) DEFAULT '' COMMENT '工号',
  `face_id` varchar(32) DEFAULT '' COMMENT '人脸ID',
  `finger_id` varchar(50) DEFAULT '' COMMENT '指纹ID',
  `voice_id` varchar(32) DEFAULT '' COMMENT '声纹ID',
  `state` int(1) DEFAULT '1' COMMENT '1 正常，0 删除，2 锁定',
  `user_title` varchar(32) DEFAULT '' COMMENT '用户职称',
  `creator_id` bigint(20) DEFAULT '1' COMMENT '创建人',
  `appkey` varchar(20) DEFAULT '' COMMENT '用户appkey 建议使用10位随机数——用户ID',
  `image_url` varchar(255) DEFAULT '' COMMENT '头像路径',
  `role_id` bigint(20) DEFAULT '1' COMMENT '角色ID',
  `org_id` bigint(20) DEFAULT '1' COMMENT '组织机构ID',
  `user_status` tinyint(4) DEFAULT '1' COMMENT '员工状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改日期',
  `invalid_time` int(9) DEFAULT '1' COMMENT '注释',
  `lock_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '锁定时间',
  `last_login` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `idx_username` (`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10004 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统用户表';

-- ----------------------------
-- Table structure for three_dimensional_relevancy
-- ----------------------------
DROP TABLE IF EXISTS `three_dimensional_relevancy`;
CREATE TABLE `three_dimensional_relevancy` (
  `id` bigint(32) NOT NULL COMMENT '台账Id',
  `name` varchar(64) DEFAULT '' COMMENT '台账名称',
  `model_name` varchar(64) DEFAULT '' COMMENT '模型名称',
  `category` int(11) DEFAULT '0' COMMENT '0-辅助设备，1-一次设备，2-二次设备',
  `is_camera` int(11) DEFAULT '0' COMMENT '0-不是，1-是',
  `type` varchar(64) DEFAULT NULL,
  `device_type` int(11) DEFAULT '0' COMMENT '设备类型',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for t_algorithm_conf
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_conf`;
CREATE TABLE `t_algorithm_conf` (
  `preset_id` bigint(68) NOT NULL COMMENT '摄像头预置位ID或者机器人巡检点ID',
  `algorithm_id` bigint(32) NOT NULL,
  `config_name` varchar(68) DEFAULT '' COMMENT '算法配置名称',
  `status` int(11) DEFAULT '1' COMMENT '状态',
  `if_del` int(11) DEFAULT '1' COMMENT '是否删除',
  `if_show` int(11) DEFAULT '1' COMMENT '是否展示1展示，2不展示',
  `pic_url` varchar(255) DEFAULT '' COMMENT '图标路径',
  `apply_module` int(11) DEFAULT '1' COMMENT '0不应用，1应用到日常巡视，2..待定',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`preset_id`,`algorithm_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='算法配置表';

-- ----------------------------
-- Table structure for t_algorithm_info
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_info`;
CREATE TABLE `t_algorithm_info` (
  `algorithm_id` bigint(32) NOT NULL AUTO_INCREMENT,
  `algorithm_name` varchar(64) DEFAULT '',
  `alias_name` varchar(64) DEFAULT '' COMMENT '算法别名',
  `describel` varchar(255) DEFAULT '',
  `algorithm_code` varchar(20) DEFAULT '' COMMENT '算法编码',
  `analyse_type` varchar(11) DEFAULT '' COMMENT '算法类型',
  `is_ai` int(1) DEFAULT '1' COMMENT '是否AI算法 0是1否',
  `defect_type` int(11) DEFAULT '1' COMMENT '缺陷类型',
  `defect_level` int(11) DEFAULT '1' COMMENT '缺陷等级',
  PRIMARY KEY (`algorithm_id`) USING BTREE,
  UNIQUE KEY `idx_algorithm_name` (`algorithm_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='算法表';

-- ----------------------------
-- Table structure for t_algorithm_mete
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_mete`;
CREATE TABLE `t_algorithm_mete` (
  `device_mete_id` bigint(68) NOT NULL COMMENT '设备标准测点ID',
  `algorithm_id` bigint(32) NOT NULL,
  `config_name` varchar(68) DEFAULT '' COMMENT '算法配置名称',
  `status` int(11) DEFAULT '1' COMMENT '状态',
  `if_del` int(11) DEFAULT '1' COMMENT '是否删除',
  `if_show` int(11) DEFAULT '1' COMMENT '是否展示1展示，2不展示',
  `pic_url` varchar(255) DEFAULT '' COMMENT '图标路径',
  `apply_module` int(11) DEFAULT '1' COMMENT '0不应用，1应用到日常巡视，2..待定',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  PRIMARY KEY (`device_mete_id`,`algorithm_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='算法测点配置表';

-- ----------------------------
-- Table structure for t_camera_alarm
-- ----------------------------
DROP TABLE IF EXISTS `t_camera_alarm`;
CREATE TABLE `t_camera_alarm` (
  `camera_alarm_id` bigint(48) NOT NULL AUTO_INCREMENT COMMENT '可视设备本体告警告警ID',
  `alarm_name` varchar(125) DEFAULT '' COMMENT '可视设备本体告警名称',
  `camera_id` bigint(20) DEFAULT '1' COMMENT '可视设备ID',
  `station_id` varchar(32) DEFAULT '' COMMENT '站所id',
  `alarm_type` int(2) DEFAULT '1' COMMENT '告警类型',
  `alarm_level` int(1) DEFAULT '1' COMMENT '1普通告警，2严重告警，3紧急告警，4致命告警',
  `alarm_info` varchar(256) DEFAULT '' COMMENT '告警具体信息',
  `alarm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  `is_alarm` int(1) DEFAULT '1' COMMENT '是否告警',
  `deal_type` int(1) DEFAULT '1' COMMENT '处理方式：0自动，1手动',
  `deal_info` varchar(256) DEFAULT '' COMMENT '处理信息',
  `deal_person_id` varchar(32) DEFAULT '' COMMENT '处理人ID',
  `deal_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
  `alarm_state` int(1) DEFAULT '0' COMMENT '0 未处理  1 处理',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`camera_alarm_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='可视设备本体告警表';

-- ----------------------------
-- Table structure for t_camera_group
-- ----------------------------
DROP TABLE IF EXISTS `t_camera_group`;
CREATE TABLE `t_camera_group` (
  `group_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `group_name` varchar(128) DEFAULT '' COMMENT '分组名称',
  `camera_ids` text COMMENT '相机ID',
  `remarks` varchar(200) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='相机分组表';

-- ----------------------------
-- Table structure for t_camera_info
-- ----------------------------
DROP TABLE IF EXISTS `t_camera_info`;
CREATE TABLE `t_camera_info` (
  `camera_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `camera_name` varchar(128) DEFAULT '' COMMENT '摄像机名称',
  `camera_model` int(11) DEFAULT '1' COMMENT '摄像机型号',
  `pms_id` varchar(32) DEFAULT '' COMMENT 'PMS ID',
  `alias_name` varchar(128) DEFAULT '' COMMENT '摄像机别名',
  `record_id` bigint(20) DEFAULT '1' COMMENT '录像机ID',
  `up_region_id` varchar(32) DEFAULT '' COMMENT '上级区域ID',
  `channel_num` int(11) DEFAULT '1' COMMENT '通道号',
  `camera_num` int(2) DEFAULT '1' COMMENT '相机本身通道',
  `sms_id` int(11) DEFAULT '1' COMMENT '主流媒体服务器',
  `rms_id` int(11) DEFAULT '1' COMMENT '主录像媒体服务器',
  `monitor_id` varchar(32) DEFAULT '' COMMENT '检测点ID',
  `vendor_id` varchar(64) DEFAULT '' COMMENT '生产厂家',
  `stream_type` int(11) DEFAULT '1' COMMENT '码流类型',
  `protocol_type` int(11) DEFAULT '1' COMMENT '接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG',
  `url` varchar(128) DEFAULT '' COMMENT '码流地址',
  `camera_ip` varchar(38) DEFAULT '' COMMENT '相机IP',
  `port` int(11) DEFAULT '1' COMMENT '相机端口',
  `infread_port` int(8) DEFAULT '1' COMMENT '红外测温端口',
  `camera_manager` varchar(68) DEFAULT '' COMMENT '相机用户名',
  `camera_code` varchar(28) DEFAULT '' COMMENT '相机密码',
  `camera_type` int(11) DEFAULT '1' COMMENT '0可见光摄像机, 1红外摄像机',
  `is_control` int(11) DEFAULT '1' COMMENT '是否可控(0-可控球机，1-不可控枪机)',
  `latitude` varchar(32) DEFAULT '',
  `longitude` varchar(32) DEFAULT '',
  `address` varchar(32) DEFAULT '',
  `unit` varchar(255) DEFAULT '' COMMENT '单位',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `commission_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投运日期',
  `camera_channel_id` VARCHAR (50)  DEFAULT NULL COMMENT '相机通道id',
  PRIMARY KEY (`camera_id`) USING BTREE,
  KEY `edge_code` (`edge_code`,`origin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='摄像头信息表';

-- ----------------------------
-- Table structure for t_camera_preset
-- ----------------------------
DROP TABLE IF EXISTS `t_camera_preset`;
CREATE TABLE `t_camera_preset` (
  `preset_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '预置位id',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `camera_id` bigint(20) DEFAULT '1' COMMENT '摄像头id',
  `preset_num` int(11) DEFAULT '1' COMMENT '预置位号',
  `preset_name` varchar(128) DEFAULT '' COMMENT '预置位名称',
  `preset_type` int(11) DEFAULT 1 COMMENT '预置位类型',
  `creator_user` varchar(64) DEFAULT '',
  `creator_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_use` int(11) DEFAULT '1' COMMENT '是否使用',
  `preset_img` varchar(255) DEFAULT '',
  `inspection_postion` int(10) DEFAULT '1' COMMENT '检测点位置，0-室外 1-室内',
  `collect_status` int(10) DEFAULT '1' COMMENT '采集状态，0-未采集 1-已采集',
  `preset_ptz` varchar(255) DEFAULT NULL COMMENT '相机预置位PTZ值',
  `calibration_status` int(10) DEFAULT '1' COMMENT '标定状态，0-未标定 1-已标定',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`preset_id`) USING BTREE,
  UNIQUE KEY `idx_camera_num` (`camera_id`,`preset_num`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21000000001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='摄像机预位置表';

-- ----------------------------
-- Table structure for t_camera_recorder
-- ----------------------------
DROP TABLE IF EXISTS `t_camera_recorder`;
CREATE TABLE `t_camera_recorder` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '录像机ID',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始录像机ID',
  `record_name` varchar(128) DEFAULT '' COMMENT '服务器名称',
  `recorder_model` int(11) DEFAULT '1' COMMENT '录像机型号',
  `recorder_type` varchar(32) DEFAULT '' COMMENT '录像机类型',
  `vendor_id` int(11) DEFAULT '1' COMMENT '生产厂家',
  `pms_id` varchar(32) DEFAULT '' COMMENT 'PMS ID',
  `alias_name` varchar(128) DEFAULT '' COMMENT '备用名称',
  `record_ip` varchar(32) DEFAULT '' COMMENT '服务器地址',
  `protocol` varchar(16) DEFAULT '' COMMENT '传输协议',
  `http_port` int(10) DEFAULT '1' COMMENT 'http端口',
  `trans_port` int(10) DEFAULT '1' COMMENT '传输端口',
  `rtsp_port` int(10) DEFAULT '1' COMMENT '控制端口',
  `identity_manager` varchar(128) DEFAULT '' COMMENT '用户名',
  `identity_code` varchar(50) DEFAULT '' COMMENT '密码',
  `protocol_url` varchar(255) DEFAULT '' COMMENT '协议路径',
  `max_channel` int(11) DEFAULT '1' COMMENT '最大通道数',
  `hdd_size` int(11) DEFAULT '1' COMMENT '缓存磁盘空间',
  `buffer_day` int(11) DEFAULT '1' COMMENT '缓存天数',
  `time_long` int(32) DEFAULT '1' COMMENT '录制文件时长 单位秒',
  `unit` varchar(255) DEFAULT '' COMMENT '单位',
  `device_channel` VARCHAR (50)  DEFAULT NULL COMMENT '设备id',
  PRIMARY KEY (`record_id`) USING BTREE,
  KEY `edge_code` (`edge_code`,`origin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='录像服务器表';

-- ----------------------------
-- Table structure for t_camera_screen
-- ----------------------------
DROP TABLE IF EXISTS `t_camera_screen`;
CREATE TABLE `t_camera_screen` (
  `user_id` bigint(20) NOT NULL,
  `screen_num` varchar(50) DEFAULT '',
  `camera_ids` varchar(200) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建日期',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='分屏配置表';

-- ----------------------------
-- Table structure for t_cfg_alarm_current
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_alarm_current`;
CREATE TABLE `t_cfg_alarm_current` (
  `alarm_no` bigint(62) NOT NULL AUTO_INCREMENT COMMENT '告警流水号',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备编号',
  `cunstom_id` varchar(32) DEFAULT '' COMMENT '部位ID',
  `mete_id` bigint(48) DEFAULT '1' COMMENT '监控量编号',
  `alarm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  `alarm_level` int(11) DEFAULT '1',
  `alarm_value` varchar(100) DEFAULT '' COMMENT '告警值',
  `alarm_desc` varchar(128) DEFAULT '' COMMENT '告警描述',
  `confirm_state` int(11) DEFAULT '1' COMMENT '确认状态',
  `confirm_people` varchar(50) DEFAULT '' COMMENT '确认人',
  `confirm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `confirm_remark` varchar(128) DEFAULT '' COMMENT '确认说明',
  `defect` int(11) DEFAULT '1' COMMENT '是否缺陷（0：是，1：否）',
  `defect_level` int(11) DEFAULT '1' COMMENT '缺陷等级：0-一级，1-二级，2-三级',
  `mete_code` varchar(20) DEFAULT '' COMMENT '信号标准化编码',
  `is_clear` int(11) DEFAULT '1' COMMENT '告警状态',
  `show_type` varchar(20) DEFAULT '' COMMENT '显示类型',
  `update_time` datetime NOT NULL COMMENT '告警插入时间',
  PRIMARY KEY (`alarm_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='活动告警表';

-- ----------------------------
-- Table structure for t_cfg_alarm_history
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_alarm_history`;
CREATE TABLE `t_cfg_alarm_history` (
  `alarm_no` bigint(62) NOT NULL COMMENT '告警流水号current表获取',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备编号',
  `cunstom_id` varchar(32) DEFAULT '' COMMENT '部位ID',
  `mete_id` bigint(48) DEFAULT '1' COMMENT '监控量编号',
  `alarm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  `alarm_level` int(11) DEFAULT '1',
  `alarm_value` varchar(100) DEFAULT '' COMMENT '告警值',
  `alarm_desc` varchar(128) DEFAULT '' COMMENT '告警描述',
  `clear_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '消除时间',
  `clear_value` decimal(10,4) DEFAULT '0.0000' COMMENT '消除值',
  `confirm_state` int(11) DEFAULT '1' COMMENT '确认状态',
  `confirm_people` varchar(50) DEFAULT '' COMMENT '确认人',
  `confirm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `confirm_remark` varchar(128) DEFAULT '' COMMENT '确认说明',
  `defect` int(11) DEFAULT '1' COMMENT '是否缺陷（0：是，1：否）',
  `defect_level` int(11) DEFAULT '0' COMMENT '缺陷等级：0-一级，1-二级，2-三级',
  `force_clear_reason` varchar(128) DEFAULT '' COMMENT '强制消除原因',
  `mete_code` varchar(20) DEFAULT '' COMMENT '信号标准化编码',
  `is_clear` varchar(20) DEFAULT '' COMMENT '告警状态',
  `show_type` varchar(20) DEFAULT '' COMMENT '显示类型',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '告警插入时间',
  PRIMARY KEY (`alarm_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='历史告警表';

-- ----------------------------
-- Table structure for t_cfg_data_current
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_data_current`;
CREATE TABLE `t_cfg_data_current` (
  `mete_id` bigint(48) NOT NULL COMMENT '监控量编号',
  `device_id` bigint(32) NOT NULL COMMENT '设备编号',
  `cunstom_id` varchar(32) DEFAULT '' COMMENT '部位ID',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '数值时间',
  `mete_kind` int(11) DEFAULT '1' COMMENT '监控量种类',
  `region_id` varchar(20) DEFAULT '' COMMENT '区域编号',
  `mete_comment` varchar(100) DEFAULT NULL COMMENT '监控量描述',
  `mete_value` varchar(100) DEFAULT '',
  `last_mete_value` varchar(100) DEFAULT '' COMMENT '上一次值',
  PRIMARY KEY (`mete_id`,`device_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='实时数据表';

-- ----------------------------
-- Table structure for t_cfg_device
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_device`;
CREATE TABLE `t_cfg_device` (
  `device_id` varchar(20) NOT NULL COMMENT '设备编号',
  `device_name` varchar(128) NOT NULL COMMENT '设备名称',
  `device_type` varchar(20) DEFAULT '' COMMENT '设备类型',
  `device_code` varchar(40) DEFAULT '' COMMENT '设备编码+设备测点地址',
  `station_id` varchar(20) DEFAULT '' COMMENT '变电站id',
  `relation_code` varchar(32) DEFAULT '' COMMENT '关联设备编码',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `remark` varchar(512) DEFAULT '' COMMENT '描述',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  PRIMARY KEY (`device_id`) USING BTREE,
  KEY `index_device_type` (`device_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备表';

-- ----------------------------
-- Table structure for t_cfg_mete
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_mete`;
CREATE TABLE `t_cfg_mete` (
  `mete_id` varchar(20) NOT NULL COMMENT '监控量编码',
  `mete_type` varchar(20) DEFAULT '' COMMENT '测点标准化编码',
  `mete_kind` int(11) NOT NULL COMMENT '四遥类型 0：遥信，1：遥测，2：遥控，3：遥调，4：遥脉',
  `mete_name` varchar(128) NOT NULL COMMENT '测点标准名',
  `mete_code` varchar(20) DEFAULT '' COMMENT '测点编码',
  `unit` varchar(50) DEFAULT '' COMMENT '单位',
  `mete_explain_type` text COMMENT '测点解释',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `modulus` int(11) DEFAULT '1' COMMENT '系数',
  `station_name` varchar(64) DEFAULT '',
  `station_id` varchar(28) DEFAULT '',
  `up_effect` int(11) DEFAULT '1',
  `down_effect` int(11) DEFAULT '1',
  `alarmlevel` int(11) DEFAULT '1',
  `alarmthresbhold` int(11) DEFAULT '1',
  `describer` varchar(64) DEFAULT '',
  `mete_precision` int(11) DEFAULT '1',
  `change_limit` float(7,3) DEFAULT '1',
  `hilimit1` float(7,3) DEFAULT '1',
  `lolimit1` float(7,3) DEFAULT '1',
  `hilimit2` float(7,3) DEFAULT '1',
  `lolimit2` float(7,3) DEFAULT '1',
  `hilimit3` float(7,3) DEFAULT '1',
  `lolimit3` float(7,3) DEFAULT '1',
  `hilimit4` float(7,3) DEFAULT '1',
  `stander` float(7,3) DEFAULT '1',
  `controlenable` int(11) DEFAULT '1',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  PRIMARY KEY (`mete_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统测点信息表';

-- ----------------------------
-- Table structure for t_cfg_teleadjust
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_teleadjust`;
CREATE TABLE `t_cfg_teleadjust` (
  `device_id` varchar(20) NOT NULL COMMENT '设备编号',
  `mete_id` varchar(20) NOT NULL COMMENT '监控量编号',
  `mete_name` varchar(256) DEFAULT '' COMMENT '监控量名称',
  `up_effect` float(7,3) DEFAULT '1' COMMENT '有效上限',
  `down_effect` float(7,3) DEFAULT '1' COMMENT '有效下限',
  `mete_precision` int(11) DEFAULT '1' COMMENT '小数点后的有效位数',
  `unit` varchar(16) DEFAULT '' COMMENT '单位',
  `mete_index` int(11) DEFAULT '1' COMMENT '同一设备下的监控量序号',
  `mete_cid` int(11) DEFAULT '1' COMMENT 'CID',
  `adjust_kind` int(11) DEFAULT '1' COMMENT '遥调量类型',
  `last_value` float(7,3) DEFAULT '1' COMMENT '当前值',
  `last_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `mete_code` varchar(20) DEFAULT '' COMMENT '信号标准化编码',
  `device_type` varchar(20) DEFAULT '' COMMENT '设备类型',
  `description` varchar(512) DEFAULT '' COMMENT '监控量描述',
  `stander` float(7,3) DEFAULT '1' COMMENT '标称值',
  `controlenable` int(11) DEFAULT '1' COMMENT '是否可控',
  PRIMARY KEY (`device_id`,`mete_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='遥调量表';

-- ----------------------------
-- Table structure for t_cfg_telecontrol
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_telecontrol`;
CREATE TABLE `t_cfg_telecontrol` (
  `device_id` varchar(20) NOT NULL COMMENT '设备编号',
  `mete_id` varchar(20) NOT NULL COMMENT '监控量编号',
  `mete_name` varchar(256) DEFAULT '' COMMENT '监控量名称',
  `mete_index` int(11) DEFAULT '1' COMMENT '同一设备下的监控量序号',
  `mete_cid` int(11) DEFAULT '1' COMMENT 'CID',
  `control_status` int(11) DEFAULT '1' COMMENT '可控状态',
  `enable_string` varchar(256) DEFAULT '' COMMENT '控制使能条件表达式',
  `succeed_string` varchar(256) DEFAULT '' COMMENT '控制成功条件表达式',
  `trigger_string` varchar(256) DEFAULT '' COMMENT '触发条件表达式',
  `control_value` int(11) DEFAULT '1' COMMENT '控制参数',
  `mete_code` varchar(20) DEFAULT '' COMMENT '信号标准化编码',
  `device_type` varchar(20) DEFAULT '' COMMENT '设备类型',
  `description` varchar(512) DEFAULT '' COMMENT '监控量描述',
  `describer` varchar(512) DEFAULT '' COMMENT '态值描述',
  PRIMARY KEY (`device_id`,`mete_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='遥控量表';

-- ----------------------------
-- Table structure for t_cfg_telemeter
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_telemeter`;
CREATE TABLE `t_cfg_telemeter` (
  `device_id` varchar(20) NOT NULL COMMENT '设备编号',
  `mete_id` varchar(20) NOT NULL COMMENT '监控量编号',
  `mete_name` varchar(256) DEFAULT '' COMMENT '监控量名称',
  `up_effect` float(7,3) DEFAULT '1' COMMENT '有效上限',
  `down_effect` float(7,3) DEFAULT '1' COMMENT '有效下限',
  `mete_precision` int(11) DEFAULT '1' COMMENT '小数点后的有效位数',
  `unit` varchar(16) DEFAULT '' COMMENT '单位',
  `mete_index` int(11) DEFAULT '1' COMMENT '同一设备下的监控量序号',
  `mete_cid` int(11) DEFAULT '1' COMMENT 'CID',
  `limit_band` float(7,3) DEFAULT '1' COMMENT '上下限带宽',
  `change_limit` float(7,3) DEFAULT '1' COMMENT '变化幅度门限',
  `valid_mid` varchar(256) DEFAULT '' COMMENT '有效性表达式变量串',
  `valid_string` varchar(256) DEFAULT '' COMMENT '有效性判断表达式',
  `invalid_value` float(7,3) DEFAULT '1' COMMENT '无效时的值',
  `last_value` float(7,3) DEFAULT '1' COMMENT '当前值',
  `last_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '当前值更新时间',
  `mete_code` varchar(20) DEFAULT '' COMMENT '信号标准化编码',
  `device_type` varchar(20) DEFAULT '' COMMENT '设备类型',
  `description` varchar(512) DEFAULT '' COMMENT '监控量描述',
  `hilimit1` float(7,3) DEFAULT '1' COMMENT '一级告警上限',
  `lolimit1` float(7,3) DEFAULT '1' COMMENT '一级告警下限',
  `hilimit2` float(7,3) DEFAULT '1' COMMENT '二级告警上限',
  `lolimit2` float(7,3) DEFAULT '1' COMMENT '二级告警下限',
  `hilimit3` float(7,3) DEFAULT '1' COMMENT '三级告警上限',
  `lolimit3` float(7,3) DEFAULT '1' COMMENT '三级告警下限',
  `hilimit4` float(7,3) DEFAULT '1' COMMENT '四级告警上限',
  `lolimit4` float(7,3) DEFAULT '1' COMMENT '四级告警下限',
  `stander` float(7,3) DEFAULT '1' COMMENT '标称值',
  `isshield` int(11) DEFAULT '1' COMMENT '是否屏蔽',
  `storageperiod` bigint(20) DEFAULT '1' COMMENT '存储周期',
  `link_mete_id` varchar(512) DEFAULT '' COMMENT '关联的遥信量',
  PRIMARY KEY (`device_id`,`mete_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='遥测量表';

-- ----------------------------
-- Table structure for t_cfg_telesignal
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_telesignal`;
CREATE TABLE `t_cfg_telesignal` (
  `device_id` varchar(20) NOT NULL COMMENT '设备编号',
  `mete_id` varchar(20) NOT NULL COMMENT '监控量编号',
  `mete_name` varchar(256) DEFAULT '' COMMENT '监控量名称',
  `up_effect` int(11) DEFAULT '1' COMMENT '有效上限',
  `down_effect` int(11) DEFAULT '1' COMMENT '有效下限',
  `mete_index` int(11) DEFAULT '1' COMMENT '同一设备下的监控量序号',
  `mete_cid` int(11) DEFAULT '1' COMMENT 'CID',
  `signal_kind` int(11) DEFAULT '1' COMMENT '遥信量种类',
  `last_value` int(11) DEFAULT '1' COMMENT '当前值',
  `last_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '当前值更新时间',
  `explain_type` int(11) DEFAULT '0' COMMENT '解释类型',
  `report_type` int(11) DEFAULT '1' COMMENT '上报模式',
  `report_level` int(11) DEFAULT '1' COMMENT '上报级别',
  `mask_type` int(11) DEFAULT '1' COMMENT '屏蔽类型',
  `mask_mid` varchar(256) DEFAULT '' COMMENT '屏蔽表达式变量串',
  `mask_string` varchar(256) DEFAULT '' COMMENT '屏蔽表达式',
  `mask_value` int(11) DEFAULT '1' COMMENT '屏蔽值',
  `delay_time` int(11) DEFAULT '1' COMMENT '防抖延时门限',
  `mete_code` varchar(20) DEFAULT '' COMMENT '信号标准化编码',
  `device_type` varchar(20) DEFAULT '' COMMENT '设备类型',
  `description` varchar(512) DEFAULT '' COMMENT '监控量描述',
  `isshield` int(11) DEFAULT '1' COMMENT '是否屏蔽',
  `storageperiod` bigint(20) DEFAULT '1' COMMENT '存储周期',
  `describer` varchar(512) DEFAULT '' COMMENT '态值描述',
  `alarmthresbhold` int(11) DEFAULT '1' COMMENT '告警触发值',
  `alarmlevel` int(11) DEFAULT '1' COMMENT '告警等级',
  `link_mete_id` varchar(512) DEFAULT '' COMMENT '关联的遥信量',
  PRIMARY KEY (`device_id`,`mete_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='遥信量表';

-- ----------------------------
-- Table structure for t_cfg_union_rule
-- ----------------------------
DROP TABLE IF EXISTS `t_cfg_union_rule`;
CREATE TABLE `t_cfg_union_rule` (
  `rule_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '规则编号',
  `plan_id` bigint(32) DEFAULT NULL COMMENT '预案id',
  `rule_name` varchar(50) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(20) DEFAULT '' COMMENT '是否生成联动监控弹窗0.不生成1.生成',
  `rule_content` varchar(500) DEFAULT '' COMMENT '具体治理规则',
  `rule_delay` int(11) DEFAULT '1' COMMENT '延时发送时间',
  `description` varchar(500) DEFAULT '' COMMENT '描述',
  `input_param` text COMMENT '入参数据,meteId,meteName',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `camera_id` bigint(32) DEFAULT '1' COMMENT '联动监控全景摄像机Id',
  `preset_id` bigint(32) DEFAULT '1' COMMENT '联动监控全景摄像机对应预置位Id',
  PRIMARY KEY (`rule_id`) USING BTREE,
  UNIQUE KEY `rule_name` (`rule_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='联动规则表';

-- ----------------------------
-- Table structure for t_cruise_data_result
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_data_result`;
CREATE TABLE `t_cruise_data_result` (
  `cruise_data_id` bigint(68) NOT NULL AUTO_INCREMENT COMMENT '巡视点数据id',
  `cruise_result_id` varchar(82) DEFAULT '' COMMENT '巡视任务结果id',
  `cruise_id` bigint(48) DEFAULT '1' COMMENT '巡检点ID',
  `cruise_name` varchar(50) DEFAULT '' COMMENT '巡检点名称',
  `cruise_type` int(11) DEFAULT '1' COMMENT '巡检点类型 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹',
  `result_desc` varchar(512) DEFAULT '' COMMENT '巡检结果文字描述',
  `result_num` varchar(512) DEFAULT '' COMMENT '巡检结果数值',
  `modify_num` varchar(100) DEFAULT '' COMMENT '审核结果数值',
  `picpath` varchar(256) DEFAULT '' COMMENT '巡检分析图片',
  `confirm_pic_path` varchar(255) DEFAULT '' COMMENT '操作前结果图片,相对',
  `pic_path_anl` varchar(255) DEFAULT '' COMMENT '机器人巡检图片,相对',
  `person_check` varchar(512) DEFAULT '' COMMENT '人工校核结果',
  `origpic` varchar(256) DEFAULT '' COMMENT '算法原始图片/红外可见光',
  `orig_confirm_pic_path` varchar(255) DEFAULT '' COMMENT '操作前结果图片,绝对',
  `orig_pic_anl` varchar(255) DEFAULT '' COMMENT '机器人巡检图片,绝对',
  `cruise_abnormal` int(11) DEFAULT '1' COMMENT '巡视异常原因 -抓图失败、数据异常、异常告警、算法超时',
  `evaluation_state` int(11) DEFAULT '1' COMMENT '审核状态1-审核0-未审核',
  `identify_state` int(11) DEFAULT '1' COMMENT '识别状态 1识别正常 2识别异常',
  `identify_result` int(11) DEFAULT '1' COMMENT '实际结果 1正常 2异常',
  `createtime` datetime DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(256) DEFAULT '' COMMENT '备用字段3',
  `check_user` varchar(32) DEFAULT '' COMMENT '审核人',
  `check_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审核时间',
  `is_warn` int(11) DEFAULT '1' COMMENT '是否产生告警1.是0.否',
  `cruise_result` int(11) DEFAULT '1' COMMENT '巡视执行结果-正常、异常',
  `fir_name` varchar(60) DEFAULT '' COMMENT '红外FIR文件名称',
  `fir_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '红外FIR文件生成时间',
  `result_pic` varchar(255) DEFAULT '' COMMENT 'FIR文件存储路径',
  `points` varchar(125) DEFAULT '' COMMENT '图片坐标点',
  `voice_path` varchar(512) DEFAULT NULL COMMENT '声纹文件地址',
  PRIMARY KEY (`cruise_data_id`) USING BTREE,
  KEY `cruiseid` (`cruise_data_id`) USING BTREE,
  KEY `cid` (`cruise_result_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检点数据表';

-- ----------------------------
-- Table structure for t_cruise_nonhomologous_point_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_nonhomologous_point_instance`;
CREATE TABLE `t_cruise_nonhomologous_point_instance` (
  `instance_id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '非同源告警规则ID',
  `device_mete_id` bigint(48) DEFAULT '1' COMMENT '测点实例ID',
  `device_id` bigint(32) DEFAULT '1' COMMENT '关联设备id',
  `custom_id` varchar(32) DEFAULT '' COMMENT '关联部位表id',
  `instance_id_one` bigint(32) DEFAULT '1' COMMENT '巡视点id 1',
  `one_cruise_device_name` varchar(50) DEFAULT '' COMMENT '巡视点1巡视设备',
  `instance_id_two` bigint(32) DEFAULT '1' COMMENT '巡视点id 2',
  `two_cruise_device_name` varchar(50) DEFAULT '' COMMENT '巡视点2巡视设备',
  `identify_type` int(11) DEFAULT '1' COMMENT '点位识别类型 1. 表计读数，2位置状态识别，3外观缺陷识别，4红外测温，5声音检测',
  `identify_son_type` int(11) DEFAULT '1' COMMENT '点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表',
  `cruise_type` int(11) DEFAULT '1' COMMENT '非同源类型 1：红外 2：位置 3：数显表计 4：指针表计 5：三相 6：区间 7：5次',
  `cruise_name` varchar(64) DEFAULT '' COMMENT '巡检点名称',
  `warn_threshold` varchar(256) DEFAULT NULL COMMENT '告警阈值',
  `warn_level` int(11) DEFAULT '1' COMMENT '告警等级：1-预警，2-一般告警，3-严重告警，4-危急告警，',
  `sy_type` int(11) DEFAULT '1' COMMENT '四遥类型 1：遥测 2：摇信 3：遥控 4：遥调',
  `interval_type` int(11) DEFAULT '0' COMMENT '时间趋势类型 1天2周3月',
  PRIMARY KEY (`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='非同源告警配置实例表';

-- ----------------------------
-- Table structure for t_cruise_nonhomologous_warn
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_nonhomologous_warn`;
CREATE TABLE `t_cruise_nonhomologous_warn` (
  `warn_id` varchar(64) NOT NULL COMMENT '告警ID',
  `instance_id` bigint(64) DEFAULT NULL COMMENT '非同源告警规则ID',
  `device_mete_id` bigint(48) DEFAULT '1' COMMENT '测点实例ID',
  `warn_type` int(11) DEFAULT '1' COMMENT '非同源类型 1：红外 2：位置 3：数显表计 4：指针表计 5：三相 6：区间 7：5次',
  `warn_content` varchar(256) DEFAULT NULL COMMENT '告警内容',
  `warn_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  `warn_level` int(11) DEFAULT '1' COMMENT '告警等级：1-预警，2-一般告警，3-严重告警，4-危急告警，',
  `one_cruise_device_name` varchar(50) DEFAULT '' COMMENT '巡视点1巡视设备',
  `two_cruise_device_name` varchar(50) DEFAULT '' COMMENT '巡视点2巡视设备',
  `three_cruise_device_name` varchar(50) DEFAULT '' COMMENT '巡视点3巡视设备名称',
  `device_name` varchar(256) DEFAULT NULL COMMENT '设备名称',
  `device_type_name` varchar(256) DEFAULT NULL COMMENT '设备类型',
  `device_mete_name` varchar(256) DEFAULT NULL COMMENT '测点名称',
  `region_name` varchar(256) DEFAULT NULL COMMENT '区域名称',
  `custom_name` varchar(256) DEFAULT NULL COMMENT '部件名称',
  PRIMARY KEY (`warn_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='非同源告警表';

-- ----------------------------
-- Table structure for t_cruise_nonhomologous_warn_inspection
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_nonhomologous_warn_inspection`;
CREATE TABLE `t_cruise_nonhomologous_warn_inspection` (
  `warn_id` varchar(64) NOT NULL COMMENT '告警ID',
  `inspection_id` bigint(58) NOT NULL COMMENT '巡检点实例ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='非同源告警-巡视点关联表';

-- ----------------------------
-- Table structure for t_cruise_plan
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_plan`;
CREATE TABLE `t_cruise_plan` (
  `plan_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '预案ID',
  `plan_code` varchar(50) DEFAULT NULL COMMENT '预案编码',
  `device_id` varchar(50) DEFAULT NULL COMMENT '主设备编码',
  `up_region_id` bigint(32) DEFAULT '1' COMMENT '上层区域id',
  `robot_id` bigint(11) DEFAULT NULL COMMENT '机器人id',
  `plan_name` varchar(32) DEFAULT '' COMMENT '预案名称',
  `type` int(11) DEFAULT '1' COMMENT '任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义',
  `sub_type` int(11) DEFAULT '1' COMMENT '任务子类型',
  `plan_point_types` varchar(32) DEFAULT '' COMMENT '表计读数，位置状态识别，外观缺陷识别，红外测温，声音检测',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plan_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检预案属性表';

-- ----------------------------
-- Table structure for t_cruise_plan_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_plan_attr`;
CREATE TABLE `t_cruise_plan_attr` (
  `plan_id` bigint(32) NOT NULL COMMENT '预案ID',
  `instance_id` bigint(58) NOT NULL COMMENT '关联巡检点定义实例表id',
  `point_type` int(11) DEFAULT '1' COMMENT '巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹',
  `area_id` varchar(32) DEFAULT '' COMMENT '区域ID',
  `cruise_region_ids` varchar(256) DEFAULT '' COMMENT '巡检区域id',
  `exception_type` int(11) DEFAULT '1' COMMENT '巡视异常类型:0无，1.外观缺陷异常，2.多源对比异常，3.数值越限异常',
  `sub_type` int(11) DEFAULT '1' COMMENT '任务子类型',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `position` varchar(32) DEFAULT '' COMMENT '机器人点位或预置位点位或红外预置位',
  `algorithm_id` int(11) DEFAULT '1' COMMENT '算法实例ID',
  `inferad_analyze` varchar(256) DEFAULT '' COMMENT '红外诊断公式id',
  `ir_temp_box` varchar(32) DEFAULT '' COMMENT '红外预置位温度框',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plan_id`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检预案属性表';

-- ----------------------------
-- Table structure for t_cruise_point_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_point_attr`;
CREATE TABLE `t_cruise_point_attr` (
  `instance_id` bigint(48) NOT NULL COMMENT '关联巡检点定义实例表id',
  `instance_name` varchar(32) DEFAULT '' COMMENT '巡检点实例名称',
  `attr_name` varchar(64) DEFAULT '' COMMENT '属性名',
  `attr_value` varchar(64) DEFAULT '' COMMENT '属性值',
  `remark1` varchar(256) DEFAULT '' COMMENT '备用1',
  PRIMARY KEY (`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检点属性表';

-- ----------------------------
-- Table structure for t_cruise_point_info
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_point_info`;
CREATE TABLE `t_cruise_point_info` (
  `instance_id` bigint(58) NOT NULL COMMENT '巡检点实例ID',
  `device_mete_id` bigint(48) DEFAULT '1' COMMENT '测点实例ID',
  `remark` varchar(32) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检点实例信息表';

-- ----------------------------
-- Table structure for t_cruise_point_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_point_instance`;
CREATE TABLE `t_cruise_point_instance` (
  `instance_id` bigint(58) NOT NULL AUTO_INCREMENT COMMENT '巡检点实例ID',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `device_mete_id` bigint(48) DEFAULT '1' COMMENT '测点实例ID',
  `station_id` varchar(32) DEFAULT '' COMMENT '变电站id',
  `station_name` varchar(64) DEFAULT '' COMMENT '变电站名称',
  `device_id` bigint(32) DEFAULT '1' COMMENT '关联设备id',
  `custom_id` varchar(32) DEFAULT '' COMMENT '关联部位表id',
  `data_format` varchar(32) DEFAULT '' COMMENT '数据格式 1：数值结果，2：可见光图片，3：红外图谱，4：音频',
  `identify_type` int(11) DEFAULT '1' COMMENT '点位识别类型 1. 表计读数，2位置状态识别，3外观缺陷识别，4红外测温，5声音检测',
  `identify_son_type` int(11) DEFAULT '1' COMMENT '点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表',
  `cruise_type` int(11) DEFAULT '1' COMMENT '巡检点类型 4001：摄像头 4002：机器人 4003：红外',
  `cruise_id` bigint(48) NOT NULL COMMENT '巡检点ID',
  `cruise_name` varchar(64) DEFAULT '' COMMENT '巡检点名称',
  `cruise_content` varchar(256) DEFAULT '' COMMENT '巡检内容',
  `position_type` varchar(32) DEFAULT '' COMMENT 'inside-内部设备，outside-外部设备',
  `unit` varchar(32) DEFAULT '' COMMENT '单位',
  `if_sy` int(11) DEFAULT '1' COMMENT '是否四遥ID：0-是，1-否',
  `sy_type` int(11) DEFAULT '1' COMMENT '四遥类型 1：遥测 2：摇信 3：遥控 4：遥调',
  `if_videotape` int(11) DEFAULT '1' COMMENT '是否手动录像 0否1是',
  `videotape_time` varchar(32) DEFAULT '' COMMENT '录像时长 单位ms',
  `text_desc` varchar(128) DEFAULT '' COMMENT '文本描述',
  `sort` varchar(32) DEFAULT '' COMMENT '排序序号',
  PRIMARY KEY (`instance_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100000000001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检点实例表';

-- ----------------------------
-- Table structure for t_cruise_result
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_result`;
CREATE TABLE `t_cruise_result` (
  `task_result_id` varchar(50) NOT NULL COMMENT '任务结果UUID',
  `task_id` varchar(50) DEFAULT '' COMMENT '巡检任务ID',
  `task_name` varchar(50) DEFAULT '' COMMENT '巡检任务名称',
  `area_id` varchar(32) DEFAULT '' COMMENT '区域id',
  `c_type` int(11) DEFAULT '1' COMMENT '巡检类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义',
  `c_state` int(11) DEFAULT '1' COMMENT '当前状态  -1.数据异常 0.正在执行 1.执行完成 2.任务暂停 3.任务终止 4任务异常终止5. 任务超期',
  `modify_state` int(11) DEFAULT '1' COMMENT '状态修正值',
  `task_count` int(11) DEFAULT '1' COMMENT '检测点数',
  `task_wait` int(11) DEFAULT '1' COMMENT '待检测点数',
  `check_user` varchar(128) DEFAULT '' COMMENT '审核人',
  `check_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `weather` varchar(255) DEFAULT '' COMMENT '微气象',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡检时间',
  `execute_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `task_code` varchar(32) DEFAULT '' COMMENT '任务编码',
  `remark` varchar(10) DEFAULT '' COMMENT '巡视点是否全部审核完成，1-是0-否',
  PRIMARY KEY (`task_result_id`) USING BTREE,
  KEY `areaid` (`area_id`) USING BTREE,
  KEY `index_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检任务结果表';

-- ----------------------------
-- Table structure for t_cruise_task
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_task`;
CREATE TABLE `t_cruise_task` (
  `task_id` varchar(50) NOT NULL COMMENT '巡检任务UUID',
  `task_code` varchar(50) DEFAULT '' COMMENT '任务编码',
  `task_name` varchar(50) DEFAULT '' COMMENT '任务名称',
  `plan_id` bigint(32) DEFAULT '1' COMMENT '所属预案id',
  `area_id` varchar(32) DEFAULT '' COMMENT '所属厂站',
  `type` int(11) DEFAULT '1' COMMENT '任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义',
  `if_run` int(11) DEFAULT '1' COMMENT '是否立即执行（172.周期，173.立即，174.定期）',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `date_type` varchar(255) DEFAULT '' COMMENT '定时时间类型（1.周，2.日）',
  `task_type` int(11) DEFAULT '1' COMMENT '任务来源：1 日常巡视 2红外普测 3地电波 4机器人监控 5机器人本体任务',
  `task_level` int(2) DEFAULT '1' COMMENT '任务等级(从高到低):4级,3级,2级,1级',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡视时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `end_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '结束时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户id',
  PRIMARY KEY (`task_id`) USING BTREE,
  KEY `areaid` (`area_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检任务表';

-- ----------------------------
-- Table structure for t_cruise_task_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_task_attr`;
CREATE TABLE `t_cruise_task_attr` (
  `task_id` varchar(50) NOT NULL COMMENT '关联任务表id',
  `instance_id` bigint(48) NOT NULL COMMENT '巡检点实例ID',
  `device_mete_id` bigint(48) DEFAULT '1' COMMENT '测点实例ID',
  `device_id` bigint(32) DEFAULT '1' COMMENT '关联设备id',
  `custom_id` varchar(32) DEFAULT '' COMMENT '关联部位表id',
  `point_task_id` varchar(32) DEFAULT '' COMMENT '关联巡视点表id',
  `if_robot` int(11) DEFAULT '1' COMMENT '是否支持机器人巡视',
  `if_video` int(11) DEFAULT '1' COMMENT '是否支持视频巡视',
  `if_inferad` int(11) DEFAULT '1' COMMENT '是否支持红外巡视',
  `if_artificial` int(11) DEFAULT '1' COMMENT '是否支持人工巡视',
  PRIMARY KEY (`task_id`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='任务关联表';

-- ----------------------------
-- Table structure for t_cruise_task_del
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_task_del`;
CREATE TABLE `t_cruise_task_del` (
  `task_id` varchar(50) NOT NULL COMMENT '巡检任务UUID',
  `del_time` datetime NOT NULL COMMENT '巡视时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`task_id`,`del_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='周期任务删除记录表';

-- ----------------------------
-- Table structure for t_cruise_task_result
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_task_result`;
CREATE TABLE `t_cruise_task_result` (
  `task_result_id` varchar(50) NOT NULL COMMENT '任务结果UUID',
  `task_id` varchar(50) DEFAULT '' COMMENT '巡检任务ID',
  `task_name` varchar(50) DEFAULT '' COMMENT '巡检任务名称',
  `task_abnormal` int(11) DEFAULT '0' COMMENT '异常数量',
  `task_alarm` int(11) DEFAULT '0',
  `run_execute` varchar(30) DEFAULT '0' COMMENT '执行类型',
  `cruise_task_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '巡检时间',
  `task_status` int(11) DEFAULT '0' COMMENT '状态:0-进行中 1-已完成 2-未完成 3-任务中断',
  `cruise_result` int(11) DEFAULT '0' COMMENT '巡视结果：0-正常 1-异常',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`task_result_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='任务点状态表';

-- ----------------------------
-- Table structure for t_cruise_task_result_detail
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_task_result_detail`;
CREATE TABLE `t_cruise_task_result_detail` (
  `cruise_result_id` varchar(82) NOT NULL COMMENT '巡检点结果task_result_id+cruise_id',
  `task_result_id` varchar(50) DEFAULT '' COMMENT '巡检任务结果ID',
  `device_id` bigint(32) DEFAULT '0' COMMENT '设备ID',
  `device_name` varchar(50) DEFAULT '' COMMENT '设备名称',
  `instance_id` bigint(48) DEFAULT '0' COMMENT '巡检点ID',
  `instance_name` varchar(255) DEFAULT '' COMMENT '巡检点名称',
  `cruise_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡检时间',
  `end_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡检结束时间',
  `cruise_status` int(11) DEFAULT '0' COMMENT '状态:0-已执行 1-未执行 2-执行失败 3-未知',
  `remark` varchar(255) DEFAULT '',
  PRIMARY KEY (`cruise_result_id`) USING BTREE,
  KEY `task_result_id` (`task_result_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='任务点状态详细表';

-- ----------------------------
-- Table structure for t_cruise_type
-- ----------------------------
DROP TABLE IF EXISTS `t_cruise_type`;
CREATE TABLE `t_cruise_type` (
  `sub_type` int(11) NOT NULL COMMENT '巡视类型',
  `instance_id` bigint(20) NOT NULL COMMENT '巡检点id',
  `remark` int(12) DEFAULT '1' COMMENT '标记删除：1-有效 0-无效',
  PRIMARY KEY (`sub_type`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡视类型关联实例点表';

-- ----------------------------
-- Table structure for t_defect_info
-- ----------------------------
DROP TABLE IF EXISTS `t_defect_info`;
CREATE TABLE `t_defect_info` (
  `defect_id` bigint(58) NOT NULL AUTO_INCREMENT COMMENT '缺陷ID',
  `defect_level` int(11) DEFAULT '0' COMMENT '缺陷等级',
  `defect_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '缺陷时间',
  `defect_type` int(11) DEFAULT '0' COMMENT '缺陷类型',
  `defect_name` varchar(125) DEFAULT '' COMMENT '缺陷名称',
  `defect_content` varchar(512) DEFAULT '' COMMENT '缺陷内容',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备Id',
  `device_name` varchar(255) NULL COMMENT '设备名称',
  `cunstom_id` varchar(32) DEFAULT '' COMMENT '部位ID',
  `instance_id` bigint(48) DEFAULT '0' COMMENT '巡检点ID',
  `std_mete_id` bigint(48) DEFAULT '0' COMMENT '标准测点ID',
  `device_mete_name` varchar(255) NULL COMMENT '测点名称',
  `conf_mode` int(11) DEFAULT '0' COMMENT '缺陷状态：1未处理 2已处理 3已确认 4已忽略',
  `is_defect` int(11) DEFAULT '0' COMMENT '是否缺陷',
  `deal_type` int(1) DEFAULT '0' COMMENT '处理方式：0自动，1手动',
  `deal_info` text COMMENT '处理意见',
  `deal_person_id` varchar(32) DEFAULT '' COMMENT '确认人ID',
  `deal_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `if_defect_disable` int(11) DEFAULT '0' COMMENT '是否缺陷抑制',
  `alarm_source` int(11) DEFAULT '0' COMMENT '缺陷来源',
  `defect_subtype` int(11) DEFAULT '0' COMMENT '缺陷子类型',
  `device_code` varchar(64) DEFAULT '' COMMENT '设备编码',
  `image_path` varchar(512) DEFAULT '' COMMENT '图片地址',
  `video_path` varchar(200) DEFAULT '' COMMENT '视频地址',
  `VALUE` varchar(100) DEFAULT '',
  `out_range` varchar(100) DEFAULT '',
  `link_message` varchar(512) DEFAULT '' COMMENT '联动信息',
  PRIMARY KEY (`defect_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='缺陷信息表';

-- ----------------------------
-- Table structure for t_device_maintenance
-- ----------------------------
DROP TABLE IF EXISTS `t_device_maintenance`;
CREATE TABLE `t_device_maintenance` (
  `maintenance_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '检修ID',
  `maintenance_name` varchar(256) NOT NULL COMMENT '检修名称',
  `device_ids` text COMMENT '设备ID',
  `is_valid` int(2) DEFAULT '1' COMMENT '是否使用，0-不使用，1-使用',
  `maintenance_start` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '开始检修时间',
  `maintenance_stop` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '结束检修时间',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '边缘节点编码（从哪个边缘节点同步上来的）',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `coordinate_pixel` varchar(50) NOT NULL COMMENT '设备层级（1 = 间隔 2 = 主设备 3 = 设备点位 4 = 部件）',
  `device_level` varchar(50) NOT NULL COMMENT '设备层级（1 = 间隔 2 = 主设备 3 = 设备点位 4 = 部件）',
  `instance_ids` text COMMENT '巡视点id',
  PRIMARY KEY (`maintenance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备区域检修表';

-- ----------------------------
-- Table structure for t_device_type_img
-- ----------------------------
DROP TABLE IF EXISTS `t_device_type_img`;
CREATE TABLE `t_device_type_img` (
  `type_id` varchar(30) NOT NULL COMMENT '类型',
  `pic_abs_path` varchar(255) DEFAULT NULL COMMENT '图片绝对路径',
  `pic_real_path` varchar(255) DEFAULT NULL COMMENT '图片相对路径',
  `remake` varchar(255) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for t_diagnose_plan
-- ----------------------------
DROP TABLE IF EXISTS `t_diagnose_plan`;
CREATE TABLE `t_diagnose_plan` (
  `diagnose_plan_id` varchar(100) NOT NULL COMMENT '诊断任务ID',
  `plan_name` varchar(255) DEFAULT '' COMMENT '诊断任务名称',
  `plan_type` varchar(255) DEFAULT '' COMMENT '诊断类型(-1即时计划 0星期计划)',
  `user_id` bigint(20) DEFAULT '1' COMMENT '用户ID',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '任务下发时间',
  `end_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '任务结束时间',
  PRIMARY KEY (`diagnose_plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for t_diagnose_plan_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_diagnose_plan_attr`;
CREATE TABLE `t_diagnose_plan_attr` (
  `diagnose_plan_id` varchar(100) NOT NULL COMMENT '诊断任务ID',
  `channel_id` varchar(50) DEFAULT '' COMMENT '检测点ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for t_diagnose_plan_detail
-- ----------------------------
DROP TABLE IF EXISTS `t_diagnose_plan_detail`;
CREATE TABLE `t_diagnose_plan_detail` (
  `diagnose_plan_id` varchar(100) NOT NULL,
  `signal_opt` varchar(10) DEFAULT '',
  `blur_opt` varchar(10) DEFAULT '',
  `contrast_opt` varchar(10) DEFAULT '',
  `bright_opt` varchar(10) DEFAULT '',
  `dark_opt` varchar(10) DEFAULT '',
  `chroma_opt` varchar(10) DEFAULT '',
  `mono_opt` varchar(10) DEFAULT '',
  `noise_opt` varchar(10) DEFAULT '',
  `streak_opt` varchar(10) DEFAULT '',
  `freeze_opt` varchar(10) DEFAULT '',
  `shake_opt` varchar(10) DEFAULT '',
  `flash_opt` varchar(10) DEFAULT '',
  `scene_opt` varchar(10) DEFAULT '',
  `cover_opt` varchar(10) DEFAULT '',
  `ptz_opt` varchar(10) DEFAULT '',
  PRIMARY KEY (`diagnose_plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for t_diagnose_result
-- ----------------------------
DROP TABLE IF EXISTS `t_diagnose_result`;
CREATE TABLE `t_diagnose_result` (
  `diagnose_result_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '诊断结果ID',
  `channel_id` varchar(30) NOT NULL COMMENT '监测点ID',
  `ip` varchar(255) DEFAULT '' COMMENT '监测点IP',
  `chan_index` varchar(255) DEFAULT '' COMMENT '通道号',
  `check_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `channel_result` int(10) DEFAULT '1' COMMENT '监测点结果(0-未检测 1-正常 2-异常 3-登录失败 4-取流异常 5-解码失败 6-取流延迟)',
  `signal_result` varchar(30) DEFAULT '' COMMENT '视频丢失',
  `blur_result` varchar(30) DEFAULT '' COMMENT '图像模糊',
  `contrast_result` varchar(30) DEFAULT '' COMMENT '对比度',
  `bright_result` varchar(30) DEFAULT '' COMMENT '图像过亮',
  `dark_result` varchar(30) DEFAULT '' COMMENT '图像过暗',
  `chroma_result` varchar(30) DEFAULT '' COMMENT '图像偏色',
  `mono_result` varchar(30) DEFAULT '' COMMENT '黑白图像',
  `noise_result` varchar(30) DEFAULT '' COMMENT '噪声干扰',
  `streak_result` varchar(30) DEFAULT '' COMMENT '条纹干扰',
  `freeze_result` varchar(30) DEFAULT '' COMMENT '画面冻结',
  `shake_result` varchar(30) DEFAULT '' COMMENT '视频抖动',
  `flash_result` varchar(30) DEFAULT '' COMMENT '视频剧变',
  `scene_result` varchar(30) DEFAULT '' COMMENT '场景变换',
  `cover_result` varchar(30) DEFAULT '' COMMENT '视频遮挡',
  `ptz_result` varchar(30) DEFAULT '' COMMENT '云台检测',
  `snapshot_url` varchar(100) DEFAULT '' COMMENT '诊断抓图数据URL路径',
  `result_content` varchar(255) DEFAULT '' COMMENT '诊断结果-内容',
  `width` varchar(30) DEFAULT '' COMMENT '图像宽度',
  `height` varchar(30) DEFAULT '' COMMENT '图像高度',
  `diagnose_plan_id` varchar(40) DEFAULT '' COMMENT '诊断任务ID',
  `status` varchar(30) DEFAULT '' COMMENT '结果点状态(正常、故障、未检测、信任)',
  PRIMARY KEY (`diagnose_result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for t_dict_business
-- ----------------------------
DROP TABLE IF EXISTS `t_dict_business`;
CREATE TABLE `t_dict_business` (
  `dict_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_code` varchar(20) DEFAULT '' COMMENT '字典编码',
  `col_name` varchar(50) DEFAULT '' COMMENT '字典列名',
  `dict_note` varchar(128) DEFAULT '' COMMENT '字典描述',
  `up_dict` int(11) DEFAULT '1' COMMENT '上级字典ID',
  `remark` varchar(255) DEFAULT '' COMMENT '描述',
  `sort` bigint(8) DEFAULT '1' COMMENT '排序',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE KEY `idx_dictcode` (`dict_code`,`col_name`) USING BTREE,
  KEY `colName` (`col_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=300707 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='业务字典表';

-- ----------------------------
-- Table structure for t_env_warning
-- ----------------------------
DROP TABLE IF EXISTS `t_env_warning`;
CREATE TABLE `t_env_warning` (
  `env_warn_id` varchar(32) NOT NULL COMMENT '环控告警ID',
  `robot_name` varchar(32) DEFAULT NULL COMMENT '机器人名称',
  `robot_code` varchar(32) DEFAULT NULL COMMENT '机器人id',
  `time` datetime DEFAULT NULL COMMENT '时间',
  `type` varchar(255) DEFAULT NULL COMMENT '类型 1.温度 2.湿度 3.风速，4.水泵 5.防盗 6.灯7.空调8.门禁 9.SF6 10.O3 11.烟雾 12.液位传感器 13.风机 ',
  `value` varchar(255) DEFAULT NULL COMMENT '值',
  `unit` varchar(255) DEFAULT NULL COMMENT '单位',
  `value_unit` varchar(255) DEFAULT NULL COMMENT '值+单位',
  `sn` varchar(255) DEFAULT NULL COMMENT '环境设备SN编码',
  `value_type` varchar(255) DEFAULT NULL COMMENT '取值类型 1.状态型（正常或异常）2.数值型 3. 控制型 （开关）',
  `alarm_time` datetime DEFAULT NULL COMMENT '告警时间',
  `device_name` varchar(255) DEFAULT NULL COMMENT '环境设备名称',
  PRIMARY KEY (`env_warn_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_his_signal_data
-- ----------------------------
DROP TABLE IF EXISTS `t_his_signal_data`;
CREATE TABLE `t_his_signal_data` (
  `id` bigint(52) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mete_id` bigint(48) DEFAULT '1' COMMENT '监控量编号',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备编号',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '数值时间',
  `mete_kind` int(11) DEFAULT '1' COMMENT '监控量种类',
  `mete_value` varchar(100) DEFAULT '' COMMENT '值',
  `last_mete_value` varchar(100) DEFAULT '' COMMENT '上一次值',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_mete_id` (`mete_id`) USING BTREE,
  KEY `index_device_id` (`device_id`) USING BTREE,
  KEY `index_mete_kind` (`mete_kind`) USING BTREE,
  KEY `index_record_time` (`record_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='遥信历史数据表';

-- ----------------------------
-- Table structure for t_his_telemeter_data
-- ----------------------------
DROP TABLE IF EXISTS `t_his_telemeter_data`;
CREATE TABLE `t_his_telemeter_data` (
  `id` bigint(52) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mete_id` bigint(48) DEFAULT '1' COMMENT '监控量编号',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备编号',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '数值时间',
  `mete_kind` int(4) DEFAULT '1' COMMENT '监控量种类',
  `mete_value` varchar(100) DEFAULT '' COMMENT '值',
  `last_mete_value` varchar(100) DEFAULT '' COMMENT '上一次值',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_mete_id` (`mete_id`) USING BTREE,
  KEY `index_device_id` (`device_id`) USING BTREE,
  KEY `index_mete_kind` (`mete_kind`) USING BTREE,
  KEY `index_record_time` (`record_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='遥测历史数据表';

-- ----------------------------
-- Table structure for t_period_model
-- ----------------------------
DROP TABLE IF EXISTS `t_period_model`;
CREATE TABLE `t_period_model` (
  `period_id` bigint(30) NOT NULL AUTO_INCREMENT COMMENT '周期ID',
  `cron_expression` varchar(255) DEFAULT '' COMMENT '分',
  `remark` varchar(512) DEFAULT '' COMMENT '备注',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`period_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='周期任务模版表';

-- ----------------------------
-- Table structure for t_point_type_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_point_type_attr`;
CREATE TABLE `t_point_type_attr` (
  `id` bigint(20) NOT NULL COMMENT '点位类型id',
  `rule_id` bigint(20) NOT NULL COMMENT '规则id',
  `remark` varchar(255) DEFAULT '' COMMENT '规则描述',
  PRIMARY KEY (`id`,`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='点位类型关联相机规则表';

-- ----------------------------
-- Table structure for t_record_file
-- ----------------------------
DROP TABLE IF EXISTS `t_record_file`;
CREATE TABLE `t_record_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `camera_id` bigint(20) NOT NULL COMMENT '相机id',
  `up_region_id` varchar(32) DEFAULT '' COMMENT '上级区域ID',
  `file_name` varchar(128) DEFAULT NULL COMMENT '文件名称',
  `file_path` varchar(255) DEFAULT NULL COMMENT '录制文件路径',
  `absolute_file_path` varchar(255) DEFAULT NULL COMMENT '录制文件绝对路径',
  `start_time` datetime DEFAULT NULL COMMENT '开始录制时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束录制时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='录制文件表';

-- ----------------------------
-- Table structure for t_report_info
-- ----------------------------
DROP TABLE IF EXISTS `t_report_info`;
CREATE TABLE `t_report_info` (
  `report_id` varchar(50) NOT NULL COMMENT '报表id',
  `report_name` varchar(64) NOT NULL COMMENT '报表名称',
  `report_type` varchar(32) NOT NULL COMMENT '报表类型',
  `generate_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成日期',
  `report_env_id` varchar(64) NOT NULL COMMENT '报表在服务器上的唯一标识',
  `remarks` varchar(64) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`report_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='报表信息表';

-- ----------------------------
-- Table structure for t_robot_alarm
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_alarm`;
CREATE TABLE `t_robot_alarm` (
  `robot_alarm_id` bigint(48) NOT NULL AUTO_INCREMENT COMMENT '机器人告警ID',
  `alarm_name` varchar(125) DEFAULT '' COMMENT '机器人本体告警名称',
  `robot_name` varchar(255) DEFAULT '' COMMENT '机器人名称',
  `robot_id` bigint(20) DEFAULT '1',
  `station_id` varchar(32) DEFAULT '' COMMENT '站所id',
  `alarm_type` int(2) DEFAULT '1' COMMENT '告警类型 ',
  `alarm_level` int(1) DEFAULT '1' COMMENT '1普通告警，2严重告警，3紧急告警，4致命告警',
  `alarm_info` varchar(256) DEFAULT '' COMMENT '告警具体信息',
  `alarm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  `deal_type` int(1) DEFAULT '1' COMMENT '处理方式：0自动，1手动',
  `deal_info` varchar(256) DEFAULT '' COMMENT '处理信息',
  `deal_person_id` varchar(32) DEFAULT '' COMMENT '处理人ID',
  `deal_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
  `position_station_num` varchar(100) DEFAULT '' COMMENT '机器人告警时坐标X',
  `position_offset` varchar(100) DEFAULT '' COMMENT '机器人告警时坐标Y',
  `alarm_state` int(1) DEFAULT '0' COMMENT '0 未处理  1 处理',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`robot_alarm_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='机器人本体告警表';

-- ----------------------------
-- Table structure for t_robot_camera_preset
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_camera_preset`;
CREATE TABLE `t_robot_camera_preset` (
  `preset_id` bigint(48) NOT NULL AUTO_INCREMENT COMMENT '预置位id',
  `preset_num` int(20) DEFAULT NULL COMMENT '预置位点号',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `robot_code` varchar(50) DEFAULT '1' COMMENT '机器人编码',
  `preset_name` varchar(128) DEFAULT '' COMMENT '预置位名称',
  `creator_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `camera_type` int(10) DEFAULT '1' COMMENT '1=机器人可见光，2=机器人红外',
  PRIMARY KEY (`preset_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2100000001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='机器人预位置表';

-- ----------------------------
-- Table structure for t_robot_camera_rule
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_camera_rule`;
CREATE TABLE `t_robot_camera_rule` (
  `rule_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则id',
  `rule_code` varchar(20) DEFAULT '' COMMENT '规则编码',
  `rule_name` varchar(128) DEFAULT '' COMMENT '规则名称',
  `remark` varchar(255) DEFAULT '' COMMENT '规则描述',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='机器人相机规则表';

-- ----------------------------
-- Table structure for t_robot_info
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_info`;
CREATE TABLE `t_robot_info` (
  `robot_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '机器人id',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `robot_code` varchar(32) DEFAULT '',
  `robot_num` varchar(32) DEFAULT NULL COMMENT '机器人/无人机编号编码',
  `robot_name` varchar(64) DEFAULT '' COMMENT '机器人名称',
  `nest_name` varchar(64) DEFAULT NULL COMMENT '机巢名称',
  `nest_code` varchar(32) DEFAULT NULL COMMENT '机巢编码',
  `robot_status` varchar(6) DEFAULT '' COMMENT '机器人在线状态，1-在线0-离线',
  `robot_type` int(6) DEFAULT '1' COMMENT '机器人型号',
  `drone_type` int(6) DEFAULT NULL COMMENT '无人机型号',
  `robot_ip` varchar(32) DEFAULT '' COMMENT '机器人ip',
  `robot_port` int(8) DEFAULT '1' COMMENT '机器人端口',
  `light_ip` varchar(32) DEFAULT '' COMMENT '可见光IP',
  `light_port` varchar(8) DEFAULT '' COMMENT '可见光端口号',
  `identity_manager` varchar(32) DEFAULT '' COMMENT '可见光-用户名',
  `identity_code` varchar(32) DEFAULT '' COMMENT '可见光-密码',
  `lnferad_IP` varchar(32) DEFAULT '' COMMENT '红外IP',
  `Inferad_Port` int(8) DEFAULT '1' COMMENT '红外端口号',
  `Inferad_username` varchar(32) DEFAULT '' COMMENT '红外-用户名',
  `Inferad_password` varchar(32) DEFAULT '' COMMENT '红外-密码',
  `phote_path` varchar(255) DEFAULT '' COMMENT '照片路径',
  `create_by` varchar(64) DEFAULT '',
  `create_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `robot_factory` varchar(28) DEFAULT '' COMMENT '机器人厂家',
  `made_in` varchar(32) DEFAULT '中国' COMMENT '生成国家',
  `made_date` datetime DEFAULT NULL COMMENT '出厂日期',
  `is_use` varchar(10) DEFAULT '' COMMENT '使用状态，1-已报废2-使用中3-未使用',
  `commission_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投运日期',
  `up_region_id` bigint(32) DEFAULT '1' COMMENT '上层区域id',
  `robot_position` varchar(32) DEFAULT '' COMMENT '机器人类型.1-室内0-室外-2-轨道',
  `drone_position` varchar(32) DEFAULT '' COMMENT '无人机类型',
  `robot_source` varchar(255) DEFAULT '' COMMENT '设备来源',
  `address` varchar(255) DEFAULT '' COMMENT '安装位置',
  `building_user` varchar(255) DEFAULT '' COMMENT '使用单位',
  `appearance_number` varchar(255) DEFAULT '' COMMENT '出场编号',
  `defect_record` varchar(512) DEFAULT '' COMMENT '缺陷记录',
  `repair_record` varchar(512) DEFAULT '' COMMENT '大修记录',
  `exit_putInto_record` varchar(512) DEFAULT '' COMMENT '退出再重放记录',
  `remarks` varchar(255) DEFAULT '',
  `record_id` bigint(20) DEFAULT NULL COMMENT '录像机id',
  `channel_num_light` int(10) DEFAULT NULL COMMENT '通道号可见光',
  `channel_num_inferad` int(10) DEFAULT NULL COMMENT '通道号红外',
  `last_online_time` bigint(20) DEFAULT NULL COMMENT '上次登录时间(毫秒数)',
  `duration` bigint(20) DEFAULT NULL COMMENT '在线时长累积(毫秒)',
  `off_line_count` int(10) DEFAULT NULL COMMENT '离线次数',
  `image_size` varchar(20) DEFAULT '' COMMENT '机器人地图图片尺寸',
  `light_channel_id` varchar(50) DEFAULT NULL COMMENT '可见光设备通道号',
  `infrared_channel_id` varchar(50) DEFAULT NULL COMMENT '红外设备通道号',
  `light_vendor` varchar(64) DEFAULT NULL COMMENT '可见光厂家',
  `infrared_vendor` varchar(64) DEFAULT NULL COMMENT '红外厂家',
  PRIMARY KEY (`robot_id`) USING BTREE,
  KEY `edge_code` (`edge_code`,`origin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='机器人表';

-- ----------------------------
-- Table structure for t_robot_inspection
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_inspection`;
CREATE TABLE `t_robot_inspection` (
  `inspection_id` bigint(48) NOT NULL AUTO_INCREMENT COMMENT '机器人设备测点实例ID',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人ID',
  `inspection_code` varchar(255) DEFAULT '' COMMENT '机器人检测点编码',
  `inspection_name` varchar(255) DEFAULT '' COMMENT '测点名称',
  `inspection_type` int(11) DEFAULT '1' COMMENT '点位类型',
  `main_device_id` varchar(255) DEFAULT '' COMMENT '主设备id',
  `component_id` varchar(255) DEFAULT '' COMMENT '部件ID',
  `meter_type` int(11) DEFAULT '1' COMMENT '表计类型',
  `appearance_type` int(11) DEFAULT '1' COMMENT '外观类型',
  `main_operation_type` int(11) DEFAULT NULL COMMENT '主操作类型',
  `operation_type` int(11) DEFAULT NULL COMMENT '操作类型',
  `save_type_list` varchar(255) DEFAULT '' COMMENT '采集/保存文件类型列表',
  `recognition_type_list` varchar(255) DEFAULT '' COMMENT '识别类型列表',
  `phase` varchar(255) DEFAULT '' COMMENT '相位，A相B相C相',
  `device_info` varchar(255) DEFAULT '' COMMENT '备注信息',
  `property_pic_path` varchar(255) DEFAULT '' COMMENT '测点属性图',
  PRIMARY KEY (`inspection_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100000000 DEFAULT CHARSET=utf8mb4 COMMENT='机器人测点信息表';

-- ----------------------------
-- Table structure for t_robot_inspection_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_inspection_attr`;
CREATE TABLE `t_robot_inspection_attr` (
  `id` bigint(48) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人ID',
  `inspection_code` varchar(255) DEFAULT '' COMMENT '机器人检测点编码',
  `x` int(20) DEFAULT '0' COMMENT '左上角X坐标',
  `y` int(20) DEFAULT '0' COMMENT '左上角Y坐标',
  `width` int(20) DEFAULT '0' COMMENT '宽',
  `height` int(20) DEFAULT '0' COMMENT '高',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人测点属性表';

-- ----------------------------
-- Table structure for t_robot_region
-- ----------------------------
DROP TABLE IF EXISTS `t_robot_region`;
CREATE TABLE `t_robot_region` (
  `region_id` varchar(255) NOT NULL COMMENT '区域ID',
  `region_name` varchar(128) DEFAULT '' COMMENT '区域名称',
  `sort` int(1) DEFAULT '1' COMMENT '区域类型（1:国家,2:省份、直辖市,3:运维站,4:变电站,5:间隔,6:设备,7:部位）',
  `device_type` int(11) DEFAULT '1' COMMENT '如果是设备，为设备类型的值',
  `up_region_id` varchar(255) DEFAULT '' COMMENT '上级区域ID',
  `up_region_ids` varchar(255) DEFAULT '' COMMENT '区域ID层级',
  `region_type` int(9) DEFAULT '1' COMMENT '类型区域，标准测点区域类型：100；E机器人区域类型：101；相机区域类型：102',
  `station_id` varchar(32) DEFAULT '' COMMENT '变电站ID',
  `state` int(1) DEFAULT '0' COMMENT '0:非当前变电站 1：当前变电站',
  `robot_id` bigint(11) DEFAULT NULL COMMENT '机器人id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`region_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='机器人区域层级表';

-- ----------------------------
-- Table structure for t_sequential_conf
-- ----------------------------
DROP TABLE IF EXISTS `t_sequential_conf`;
CREATE TABLE `t_sequential_conf` (
  `cfg_device_id` varchar(20) NOT NULL COMMENT '设备编号',
  `cfg_mete_id` varchar(20) DEFAULT '' COMMENT '监控量编号',
  `camera_id` bigint(32) DEFAULT '1' COMMENT '摄像机id',
  `preset_id` bigint(32) DEFAULT '1' COMMENT '预置位id',
  `identify_result` varchar(20) DEFAULT '' COMMENT '识别结果',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='顺控配置表';

-- ----------------------------
-- Table structure for t_std_device
-- ----------------------------
DROP TABLE IF EXISTS `t_std_device`;
CREATE TABLE `t_std_device` (
  `device_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `device_code` varchar(40) DEFAULT '' COMMENT '设备编码',
  `device_name` varchar(128) DEFAULT '' COMMENT '设备名称',
  `alias_name` varchar(128) DEFAULT '' COMMENT '别名',
  `device_type` int(8) DEFAULT '1' COMMENT '设备类型',
  `position_type` varchar(20) DEFAULT '' COMMENT '点号位置，inside-内部设备，outside-外部设备',
  `model_id` bigint(20) DEFAULT '1' COMMENT '模版ID',
  `region_path` varchar(255) DEFAULT '' COMMENT '路径',
  `up_region_id` bigint(32) DEFAULT '1' COMMENT '上级区域id',
  `up_region_name` varchar(64) DEFAULT '' COMMENT '上级区域名称',
  `custom_type` int(8) DEFAULT '1' COMMENT '部位类型',
  `status` int(1) DEFAULT '1' COMMENT '设备状态(0：新建，1：在线，2：离线)',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `preset_id` bigint(32) DEFAULT NULL COMMENT '预置位id',
  `camera_id` bigint(32) DEFAULT NULL COMMENT '摄像机id',
  `real_code` varchar(32) DEFAULT '' COMMENT '实物编码',
  PRIMARY KEY (`device_id`) USING BTREE,
  KEY `index_create_date` (`create_time`) USING BTREE,
  KEY `index_device_type` (`device_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100000001 DEFAULT CHARSET=utf8mb4 COMMENT='标准化设备表';

-- ----------------------------
-- Table structure for t_std_devicemete
-- ----------------------------
DROP TABLE IF EXISTS `t_std_devicemete`;
CREATE TABLE `t_std_devicemete` (
  `device_mete_id` bigint(50) NOT NULL AUTO_INCREMENT COMMENT '设备测点实例ID',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备ID',
  `device_point_id` varchar(64) DEFAULT NULL COMMENT '设备点位ID',
  `custom_id` varchar(50) DEFAULT '' COMMENT '部位ID',
  `custom_name` varchar(64) DEFAULT '' COMMENT '部件名称',
  `mete_id` bigint(50) DEFAULT '1' COMMENT '标准测点ID',
  `mete_kind` varchar(20) DEFAULT '' COMMENT '测点类型:0-遥信，1-遥测',
  `mete_type` varchar(50) DEFAULT '' COMMENT '巡检类型',
  `meter_type` int(8) DEFAULT '1' COMMENT '表计类型',
  `device_type` int(8) DEFAULT '1' COMMENT '设备类型',
  `inspection_type` int(8) DEFAULT '1' COMMENT '点位类型',
  `mete_name` varchar(50) DEFAULT '' COMMENT '测点名称',
  `appearance_type` int(2) DEFAULT '1' COMMENT '外观类型',
  `position_type` varchar(20) DEFAULT '' COMMENT '点号位置，inside-内部设备，outside-外部设备',
  `analyse_type` int(11) DEFAULT '1' COMMENT '算法类型',
  `is_ai` varchar(20) DEFAULT 'off' COMMENT '是否缺陷 on-是 off-不是',
  `is_judge` varchar(20) DEFAULT 'off' COMMENT '是否判别--“on”是--“off”不是',
  `unit` varchar(50) DEFAULT '' COMMENT '单位',
  `alarm_note` varchar(50) DEFAULT '' COMMENT '是否生成告警提示 1-生成 0-不生成',
  `alarm_type` varchar(50) DEFAULT '' COMMENT '告警分类',
  `up_effect` float(7,3) DEFAULT '0.000' COMMENT '有效上限',
  `down_effect` float(7,3) DEFAULT '0.000' COMMENT '有效下限',
  `state_zero` varchar(20) DEFAULT '' COMMENT '状态一描述',
  `state_one` varchar(20) DEFAULT '' COMMENT '状态二描述',
  `alarm_state` int(8) DEFAULT '1' COMMENT '告警关联信号',
  `alarm_level` int(11) DEFAULT '1' COMMENT '告警级别',
  `high_limit1` float(7,3) DEFAULT '0.000' COMMENT '告警上限1',
  `low_limit1` float(7,3) DEFAULT '0.000' COMMENT '告警下限1',
  `high_limit2` float(7,3) DEFAULT '0.000' COMMENT '告警上限2',
  `low_limit2` float(7,3) DEFAULT '0.000' COMMENT '告警下限2',
  `high_limit3` float(7,3) DEFAULT '0.000' COMMENT '告警上限3',
  `low_limit3` float(7,3) DEFAULT '0.000' COMMENT '告警下限3',
  `high_limit4` float(7,3) DEFAULT '0.000' COMMENT '告警上限4',
  `low_limit4` float(7,3) DEFAULT '0.000' COMMENT '告警下限4',
  `alarm_delay` int(11) DEFAULT '1' COMMENT '告警延时',
  `alarm_cnt` int(11) DEFAULT '1' COMMENT '告警次数',
  `threshold_abs` decimal(10,4) DEFAULT '0.0000' COMMENT '绝对阀值',
  `threshold_per` decimal(8,4) DEFAULT '0.0000' COMMENT '百分比阀值',
  `modulus` int(11) DEFAULT '1' COMMENT '系数',
  `remark` varchar(128) DEFAULT '' COMMENT '信号说明',
  `redundant_type` varchar(50) NOT NULL COMMENT '测点级别（1 = Ⅰ类 2 = Ⅱ 类型）',
  `is_temdif` int(5) DEFAULT '0' COMMENT '是否温差任务（0-否；1-是）',
  `alarm_level_string` varchar(64) NULL  DEFAULT '' COMMENT '告警级别list',
  PRIMARY KEY (`device_mete_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000000001 DEFAULT CHARSET=utf8mb4 COMMENT='标准设备测点表';

-- ----------------------------
-- Table structure for t_std_devicemete_update
-- ----------------------------
DROP TABLE IF EXISTS `t_std_devicemete_update`;
CREATE TABLE `t_std_devicemete_update` (
  `device_mete_id` bigint(48) NOT NULL COMMENT '测点id',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡检时间',
  `cruise_result` int(11) DEFAULT '1' COMMENT '巡视执行结果',
  `identify_result` int(11) DEFAULT '1' COMMENT '实际结果',
  `pic_path` varchar(255) DEFAULT '' COMMENT '巡检图片',
  PRIMARY KEY (`device_mete_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100000001 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='测点巡检时间记录表';

-- ----------------------------
-- Table structure for t_std_device_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_std_device_attr`;
CREATE TABLE `t_std_device_attr` (
  `device_id` bigint(32) NOT NULL,
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `device_model` int(11) DEFAULT '1' COMMENT '设备型号',
  `pms_type` varchar(32) DEFAULT '' COMMENT 'PMS类型',
  `pms_id` varchar(32) DEFAULT '' COMMENT 'PMS ID',
  `device_vendor` varchar(64) DEFAULT '' COMMENT '生产厂家',
  `production_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `used_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投运时间',
  `disable_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_maintenance` datetime DEFAULT CURRENT_TIMESTAMP,
  `maintenance_count` varchar(32) DEFAULT '' COMMENT '维修次数',
  `organization` varchar(32) DEFAULT '' COMMENT '所属单位',
  `department` varchar(32) DEFAULT '' COMMENT '管理部门',
  `responsible_person` varchar(32) DEFAULT '' COMMENT '责任人',
  `latitude` varchar(32) DEFAULT '' COMMENT '纬度',
  `longitude` varchar(32) DEFAULT '' COMMENT '经度',
  `ip` varchar(32) DEFAULT '' COMMENT '设备IP地址',
  `port` int(11) DEFAULT '1' COMMENT '端口',
  `voltage_level` varchar(32) DEFAULT '' COMMENT '电压等级',
  `sequence_point` varchar(32) DEFAULT '' COMMENT '顺控点号',
  `real_code` varchar(32) DEFAULT '' COMMENT '实物编码',
  `address` varchar(68) DEFAULT '' COMMENT '安装地址',
  PRIMARY KEY (`device_id`) USING BTREE,
  KEY `deviceId` (`device_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='标准化设备参数表';

-- ----------------------------
-- Table structure for t_std_mete
-- ----------------------------
DROP TABLE IF EXISTS `t_std_mete`;
CREATE TABLE `t_std_mete` (
  `std_mete_id` bigint(50) NOT NULL AUTO_INCREMENT COMMENT '测点id',
  `device_type` int(8) DEFAULT '1' COMMENT '设备类型',
  `mete_type` varchar(20) DEFAULT '' COMMENT '识别类型',
  `mete_kind` int(11) DEFAULT '1' COMMENT '测点类型',
  `mete_name` varchar(128) DEFAULT '' COMMENT '测点标准名称',
  `alarm_note` varchar(256) DEFAULT '' COMMENT '信号说明',
  `alarm_explain` varchar(256) DEFAULT '' COMMENT '信号解释',
  `alarm_type` varchar(50) DEFAULT '' COMMENT '告警分类',
  `analyse_type` int(11) DEFAULT '1' COMMENT '识别算法',
  `unit` varchar(50) DEFAULT '' COMMENT '单位',
  `up_effect` float(7,3) DEFAULT '0.000' COMMENT '有效上限',
  `down_effect` float(7,3) DEFAULT '0.000' COMMENT '有效下限',
  `alarm_level` int(11) DEFAULT '1' COMMENT '告警级别',
  `alarm_limit` int(11) DEFAULT '1' COMMENT '告警门限',
  `alarm_delay` int(11) DEFAULT '1' COMMENT '告警延时',
  `state_zero` varchar(20) DEFAULT '' COMMENT '状态一',
  `state_one` varchar(20) DEFAULT '' COMMENT '状态二',
  `high_limit1` float(7,3) DEFAULT '0.000' COMMENT '告警上限1',
  `low_limit1` float(7,3) DEFAULT '0.000' COMMENT '告警下限1',
  `high_limit2` float(7,3) DEFAULT '0.000' COMMENT '告警上限2',
  `low_limit2` float(7,3) DEFAULT '0.000' COMMENT '告警下限2',
  `high_limit3` float(7,3) DEFAULT '0.000' COMMENT '告警上限3',
  `low_limit3` float(7,3) DEFAULT '0.000' COMMENT '告警下限3',
  `high_limit4` float(7,3) DEFAULT '0.000' COMMENT '告警上限4',
  `low_limit4` float(7,3) DEFAULT '0.000' COMMENT '告警下限4',
  `alarm_cnt` int(11) DEFAULT '1' COMMENT '告警次数',
  `threshold_abs` decimal(10,4) DEFAULT '0.0000' COMMENT '绝对阀值',
  `threshold_per` decimal(8,4) DEFAULT '0.0000' COMMENT '百分比阀值',
  `modulus` int(11) DEFAULT '1' COMMENT '系数',
  `remark` varchar(125) DEFAULT '' COMMENT '备注',
  `redundant_type` varchar(50) NOT NULL DEFAULT "1" COMMENT '测点级别（1 = Ⅰ类 2 = Ⅱ 类型）',
  PRIMARY KEY (`std_mete_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000020000 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统测点信息表';

-- ----------------------------
-- Table structure for t_std_metemodel
-- ----------------------------
DROP TABLE IF EXISTS `t_std_metemodel`;
CREATE TABLE `t_std_metemodel` (
  `model_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `model_name` varchar(128) NOT NULL COMMENT '模板名称',
  `device_type` int(8) NOT NULL COMMENT '所属设备类型',
  `remark` varchar(512) DEFAULT '' COMMENT '模板备注',
  PRIMARY KEY (`model_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统测点模版表';

-- ----------------------------
-- Table structure for t_std_metemodel_detail
-- ----------------------------
DROP TABLE IF EXISTS `t_std_metemodel_detail`;
CREATE TABLE `t_std_metemodel_detail` (
  `model_id` bigint(20) NOT NULL COMMENT '监控量模板ID',
  `mete_id` bigint(50) NOT NULL COMMENT '监控量ID',
  `custom_id` varchar(32) DEFAULT '' COMMENT '部位类型，默认为0-本体，',
  `mete_name` varchar(256) DEFAULT '' COMMENT '测点标准名',
  `mete_type` varchar(20) DEFAULT '' COMMENT '测点类型',
  `mete_kind` int(11) DEFAULT '1' COMMENT '测点种类',
  `analyse_type` int(11) DEFAULT '1' COMMENT '算法类型',
  `unit` varchar(50) DEFAULT '' COMMENT '单位',
  `alarm_note` varchar(256) DEFAULT '' COMMENT '信号说明',
  `alarm_explain` varchar(256) DEFAULT '' COMMENT '信号解释',
  `alarm_type` varchar(50) DEFAULT '' COMMENT '告警分类',
  `up_effect` float(7,3) DEFAULT '1' COMMENT '有效上限',
  `down_effect` float(7,3) DEFAULT '1' COMMENT '有效下限',
  `state_zero` varchar(20) DEFAULT '' COMMENT '状态一',
  `state_one` varchar(20) DEFAULT '' COMMENT '状态二',
  `alarm_level` int(11) DEFAULT '1' COMMENT '告警级别',
  `high_limit1` float(7,3) DEFAULT '1' COMMENT '告警上限1',
  `low_limit1` float(7,3) DEFAULT '1' COMMENT '告警下限1',
  `high_limit2` float(7,3) DEFAULT '1' COMMENT '告警上限2',
  `low_limit2` float(7,3) DEFAULT '1' COMMENT '告警下限2',
  `high_limit3` float(7,3) DEFAULT '1' COMMENT '告警上限3',
  `low_limit3` float(7,3) DEFAULT '1' COMMENT '告警下限3',
  `high_limit4` float(7,3) DEFAULT '1' COMMENT '告警上限4',
  `low_limit4` float(7,3) DEFAULT '1' COMMENT '告警下限4',
  `alarm_delay` int(11) DEFAULT '1' COMMENT '告警延时',
  `alarm_cnt` int(11) DEFAULT '1' COMMENT '告警次数',
  `threshold_abs` decimal(10,4) DEFAULT '0.0000' COMMENT '绝对阀值',
  `threshold_per` decimal(8,4) DEFAULT '0.0000' COMMENT '百分比阀值',
  `modulus` int(11) DEFAULT '1' COMMENT '系数',
  `redundant_type` varchar(50) NULL COMMENT '测点级别（1 = Ⅰ类 2 = Ⅱ 类型）',
  PRIMARY KEY (`mete_id`,`model_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统测点模版详细表';

-- ----------------------------
-- Table structure for t_std_meter_type_model
-- ----------------------------
DROP TABLE IF EXISTS `t_std_meter_type_model`;
CREATE TABLE `t_std_meter_type_model` (
  `id` int(32) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `meter_type` int(8) DEFAULT '1' COMMENT '表计类型',
  `device_type` int(8) DEFAULT '1' COMMENT '设备类型',
  `mete_kind` varchar(20) DEFAULT '' COMMENT '测点类型:0-遥信，1-遥测',
  `unit` varchar(50) DEFAULT '' COMMENT '单位',
  `alarm_note` varchar(50) DEFAULT '' COMMENT '是否生成告警提示 1-生成 0-不生成',
  `alarm_type` varchar(50) DEFAULT '' COMMENT '告警分类',
  `up_effect` float(7,3) DEFAULT '0.000' COMMENT '有效上限',
  `down_effect` float(7,3) DEFAULT '0.000' COMMENT '有效下限',
  `state_zero` varchar(20) DEFAULT '' COMMENT '状态一描述',
  `state_one` varchar(20) DEFAULT '' COMMENT '状态二描述',
  `alarm_state` int(8) DEFAULT '1' COMMENT '告警关联信号',
  `alarm_level` int(11) DEFAULT '1' COMMENT '告警级别',
  `high_limit1` float(7,3) DEFAULT '0.000' COMMENT '告警上限1',
  `low_limit1` float(7,3) DEFAULT '0.000' COMMENT '告警下限1',
  `high_limit2` float(7,3) DEFAULT '0.000' COMMENT '告警上限2',
  `low_limit2` float(7,3) DEFAULT '0.000' COMMENT '告警下限2',
  `high_limit3` float(7,3) DEFAULT '0.000' COMMENT '告警上限3',
  `low_limit3` float(7,3) DEFAULT '0.000' COMMENT '告警下限3',
  `high_limit4` float(7,3) DEFAULT '0.000' COMMENT '告警上限4',
  `low_limit4` float(7,3) DEFAULT '0.000' COMMENT '告警下限4',
  `alarm_delay` int(11) DEFAULT '1' COMMENT '告警延时',
  `alarm_cnt` int(11) DEFAULT '1' COMMENT '告警次数',
  `threshold_abs` decimal(10,4) DEFAULT '0.0000' COMMENT '绝对阀值',
  `threshold_per` decimal(8,4) DEFAULT '0.0000' COMMENT '百分比阀值',
  `modulus` int(11) DEFAULT '1' COMMENT '系数',
  `remark` varchar(128) DEFAULT '' COMMENT '信号说明',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=150000 DEFAULT CHARSET=utf8mb4 COMMENT='告警设置模板表';

-- ----------------------------
-- Table structure for t_std_region
-- ----------------------------
DROP TABLE IF EXISTS `t_std_region`;
CREATE TABLE `t_std_region` (
  `region_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `region_name` varchar(128) DEFAULT '' COMMENT '区域名称',
  `sort` int(1) DEFAULT '1' COMMENT '区域类型（1:国家,2:省份、直辖市,3:运维站,4:变电站,5:间隔,6:设备,7:部位）',
  `up_region_id` bigint(20) DEFAULT NULL COMMENT '上级区域ID',
  `up_region_ids` varchar(255) DEFAULT '' COMMENT '区域ID层级',
  `region_code` varchar(32) NULL COMMENT '下级区域编码',
  `origin_region_id` varchar(64) DEFAULT NULL COMMENT '下级节点区域ID(state为0时有值)',
  `station_id` varchar(32) DEFAULT '' COMMENT '变电站ID',
  `station_name` varchar(32) DEFAULT '' COMMENT '场站名称',
  `state` int(1) DEFAULT '0' COMMENT '0:非当前变电站 1：当前变电站',
  `edge_status` varchar(8) DEFAULT NULL COMMENT '边缘节点在线状态',
  `region_path` varchar(255) DEFAULT NULL COMMENT '站所地图路径',
  `longitude` varchar(32) DEFAULT NULL COMMENT '经度',
  `latitude` varchar(32) DEFAULT NULL COMMENT '纬度',
  `voltage_level` varchar(32) DEFAULT NULL COMMENT '电压层级',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`region_id`) USING BTREE,
  KEY `region_code` (`region_code`,`origin_region_id`)
) ENGINE=InnoDB AUTO_INCREMENT=700002 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='标准区域表';

-- ----------------------------
-- Table structure for t_sys_param
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_param`;
CREATE TABLE `t_sys_param` (
  `param_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  `param_type` int(11) DEFAULT '1' COMMENT '参数编码',
  `param_code` varchar(50) DEFAULT '' COMMENT '参数类型',
  `param_name` varchar(50) DEFAULT '' COMMENT '参数名称',
  `content` text COMMENT '参数内容',
  `remark` varchar(255) DEFAULT '' COMMENT '描述',
  `rules` varchar(1024) DEFAULT NULL COMMENT '校验规则',
  PRIMARY KEY (`param_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100115 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统参数表';

-- ----------------------------
-- Table structure for t_task_statistics
-- ----------------------------
DROP TABLE IF EXISTS `t_task_statistics`;
CREATE TABLE `t_task_statistics` (
  `task_result_id` varchar(50) NOT NULL COMMENT '巡检任务编号',
  `cruise_point_id` bigint(48) NOT NULL COMMENT '巡检点编号',
  `cruise_point_status` varchar(32) DEFAULT '' COMMENT '巡检点状态:0未完成、1正常、2异常、3算法超时、4抓图失败、5未识别',
  `result_type` varchar(32) DEFAULT '' COMMENT '任务类型:0全面、1例行、2熄灯、3特殊、4专项、5自定义',
  `description` varchar(128) DEFAULT '' COMMENT '表对象描述',
  PRIMARY KEY (`task_result_id`,`cruise_point_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='任务统计表';

-- ----------------------------
-- Table structure for t_union_task
-- ----------------------------
DROP TABLE IF EXISTS `t_union_task`;
CREATE TABLE `t_union_task` (
  `union_id` varchar(50) NOT NULL COMMENT '巡检任务',
  `rule_id` bigint(32) DEFAULT '1' COMMENT '规则ID',
  `union_name` varchar(128) DEFAULT '' COMMENT '任务名称',
  `rule_delay` int(11) DEFAULT '1' COMMENT '延迟时间',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `mete_id` bigint(20) DEFAULT '1' COMMENT '联动量id',
  `triggering_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '联动触发时间',
  `rule_name` varchar(256) DEFAULT '' COMMENT '规则名称',
  `is_finish` int(11) DEFAULT '1' COMMENT '联动结果 0-失败 1-成功',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡视时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `param_values` text COMMENT '断面数据',
  `plan_name` varchar(255) DEFAULT '' COMMENT '预案名称',
  `rule_content` varchar(255) DEFAULT '' COMMENT '具体治理规则',
  PRIMARY KEY (`union_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='联合巡视记录表';

-- ----------------------------
-- Table structure for t_union_task_attr
-- ----------------------------
DROP TABLE IF EXISTS `t_union_task_attr`;
CREATE TABLE `t_union_task_attr` (
  `union_id` varchar(50) NOT NULL COMMENT '关联任务表UUID',
  `instance_id` bigint(48) NOT NULL,
  `device_mete_id` bigint(50) DEFAULT '1' COMMENT '测点实例ID',
  `device_custom_id` varchar(32) DEFAULT '' COMMENT '关联巡视设备部位表id',
  `if_robot` int(11) DEFAULT '1' COMMENT '是否支持机器人巡视',
  `if_video` int(11) DEFAULT '1' COMMENT '是否支持视频巡视',
  `if_inferad` int(11) DEFAULT '1' COMMENT '是否支持红外巡视',
  PRIMARY KEY (`union_id`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='联合巡视记录属性表';

-- ----------------------------
-- Table structure for t_version
-- ----------------------------
DROP TABLE IF EXISTS `t_version`;
CREATE TABLE `t_version` (
  `version_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `system_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1: 系统版本 2: 算法版本',
  `version_name` varchar(256) NOT NULL COMMENT '版本号',
  `remark` varchar(512) NOT NULL COMMENT '版本描述',
  `text` varchar(2000) NOT NULL COMMENT '版本详细说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`version_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统版本表';

-- ----------------------------
-- Table structure for t_video_algo_result
-- ----------------------------
DROP TABLE IF EXISTS `t_video_algo_result`;
CREATE TABLE `t_video_algo_result` (
  `id` bigint(58) NOT NULL AUTO_INCREMENT COMMENT '视频轮训任务结果ID',
  `point_id` bigint(48) DEFAULT '1' COMMENT '巡检点ID',
  `task_id` varchar(50) DEFAULT '' COMMENT '预案Id',
  `plan_id` bigint(32) DEFAULT '1' COMMENT '计划Id',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备Id',
  `preset_id` bigint(48) DEFAULT '1' COMMENT '预置位Id',
  `device_mete_id` bigint(50) DEFAULT '1' COMMENT '设备测点实例ID',
  `pic_url` varchar(255) DEFAULT '' COMMENT '图片地址',
  `cus_id` varchar(32) DEFAULT '' COMMENT '设备部位Id',
  `algorithm_id` bigint(32) DEFAULT '1' COMMENT '算法配置Id',
  `status` varchar(2) DEFAULT '' COMMENT '状态：-2数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别',
  `analyse_result` varchar(255) DEFAULT '' COMMENT '算法结果分析',
  `pic_orignal` varchar(255) DEFAULT '' COMMENT '算法分析原图',
  `evaluation_state` int(11) DEFAULT '1' COMMENT '评价状态 1误报 2漏报',
  `signpic` varchar(255) DEFAULT '' COMMENT '算法表记图片',
  `algorithm_type` varchar(32) DEFAULT '' COMMENT '算法大类型',
  `algorithm_son_type` varchar(32) DEFAULT '' COMMENT '算法小类型',
  `execute_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='视频轮训任务结果表';

-- ----------------------------
-- Table structure for t_video_analyse_result
-- ----------------------------
DROP TABLE IF EXISTS `t_video_analyse_result`;
CREATE TABLE `t_video_analyse_result` (
  `algorithm_result_id` bigint(58) NOT NULL AUTO_INCREMENT COMMENT '算法分析结果ID',
  `analyse_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '算法分析时间',
  `analyse_conf_id` varchar(32) DEFAULT '' COMMENT '算法配置ID',
  `algorithm_result` varchar(256) DEFAULT '' COMMENT '分析结果',
  `algorithm_picture` varchar(512) DEFAULT '' COMMENT '图片地址',
  `algorithm_status` int(11) DEFAULT '1' COMMENT '结果类型',
  `result_rate` varchar(128) DEFAULT '' COMMENT '算法分析结果评价（0-准确，1-错误）',
  `result_describe` varchar(256) DEFAULT '' COMMENT '算法分析结果描述',
  `reserver` int(11) DEFAULT '1' COMMENT '备用字段1',
  PRIMARY KEY (`algorithm_result_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='算法结果表';

-- ----------------------------
-- Table structure for t_video_intercom
-- ----------------------------
DROP TABLE IF EXISTS `t_video_intercom`;
CREATE TABLE `t_video_intercom` (
  `video_intercom_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '可视对讲id',
  `camera_name` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '可视对讲名称',
  `camera_ip` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT 'IP地址',
  `owner` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '登录用户名',
  `owner_code` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '登录密码',
  `channel_num` int(10) NOT NULL COMMENT '通道号',
  `camera_type` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '设备类型',
  `up_region_id` bigint(20) NOT NULL COMMENT '所属区域id',
  `vendor_id` int(20) NOT NULL COMMENT '厂家ID',
  `protocol_type` int(2) unsigned zerofill DEFAULT NULL COMMENT '接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG',
  `port` int(10) NOT NULL COMMENT '设备端口',
  `address` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '安装地址',
  `camera_model` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '可视对讲机型号',
  `remark` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '描述',
  `region_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '区域名称',
  `vendor` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '厂家名称',
  `rtsp_port` int(10) NOT NULL COMMENT 'rtsp端口',
  PRIMARY KEY (`video_intercom_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='可视对讲表';

-- ----------------------------
-- Table structure for t_voice_config
-- ----------------------------
DROP TABLE IF EXISTS `t_voice_config`;
CREATE TABLE `t_voice_config` (
  `config_id` bigint(32) NOT NULL AUTO_INCREMENT,
  `ftp_url` varchar(255) DEFAULT '' COMMENT 'ftp地址',
  `station_id` varchar(32) DEFAULT '' COMMENT '所属电站',
  `owner` varchar(255) DEFAULT '' COMMENT '用户名',
  `owner_code` varchar(255) DEFAULT '' COMMENT '登陆密码',
  `port` int(11) DEFAULT '1' COMMENT '端口号',
  `channel_num` varchar(32) DEFAULT '' COMMENT '通道',
  `absolu_path` varchar(255) DEFAULT '' COMMENT 'Ftp声纹绝对路径',
  `relative_path` varchar(255) DEFAULT '' COMMENT 'Ftp声纹相对路径',
  `db_value` varchar(11) DEFAULT '' COMMENT '分贝告警值',
  `f_value` varchar(11) DEFAULT '' COMMENT '频率限值',
  `mp_value` varchar(11) DEFAULT '' COMMENT '幅值限值',
  `file_path` varchar(255) DEFAULT '' COMMENT '算法配置文件路径',
  `pms_id` varchar(30) DEFAULT '' COMMENT 'pmsId',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='声纹ftp配置表';

-- ----------------------------
-- Table structure for t_voice_device
-- ----------------------------
DROP TABLE IF EXISTS `t_voice_device`;
CREATE TABLE `t_voice_device` (
  `voice_device_id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '声纹监控设备Id',
  `edge_code` varchar(32) DEFAULT NULL COMMENT '节点编码',
  `origin_id` varchar(64) DEFAULT NULL COMMENT '原始id(下级同步的id)',
  `voice_device_name` varchar(255) DEFAULT '' COMMENT '声纹监控设备名称（）',
  `std_device_id` bigint(32) DEFAULT '1' COMMENT '变压器下面换流变的设备Id',
  `device_type` varchar(64) DEFAULT '' COMMENT '被监测的设备类型',
  `config_id` bigint(32) NOT NULL DEFAULT '1',
  `up_region_id` bigint(32) DEFAULT '1' COMMENT '上级区域id',
  `state` varchar(30) DEFAULT '' COMMENT '在线状态',
  `voice_code` varchar(256) DEFAULT NULL COMMENT '声纹设备编码',
  `voice_type` varchar(32) DEFAULT NULL COMMENT '设备类型',
  `voice_model` varchar(32) DEFAULT NULL COMMENT '设备型号',
  `voice_factory` varchar(32) DEFAULT NULL COMMENT '生产厂家',
  PRIMARY KEY (`voice_device_id`) USING BTREE,
  KEY `edge_code` (`edge_code`,`origin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='声纹设备表';

-- ----------------------------
-- Table structure for t_voice_print
-- ----------------------------
DROP TABLE IF EXISTS `t_voice_print`;
CREATE TABLE `t_voice_print` (
  `voice_id` varchar(64) NOT NULL COMMENT '声纹Id',
  `voice_print_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '声纹名称',
  `voice_device_id` varchar(32) DEFAULT '' COMMENT '设备Id',
  `voice_url` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '声纹url',
  `voice_star_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '声纹开始时间',
  `voice_end_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '声纹结束时间',
  `magnetic_detect` varchar(1) DEFAULT '' COMMENT '0正常1异常',
  `tick_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '标记名称',
  `tick_type` varchar(255) DEFAULT '' COMMENT '标签类型',
  `create_by` varchar(255) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(0) DEFAULT '' COMMENT '修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `remarks` varchar(255) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) NOT NULL COMMENT '删除标记',
  PRIMARY KEY (`voice_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='声纹文件表';

-- ----------------------------
-- Table structure for t_warn_info
-- ----------------------------
DROP TABLE IF EXISTS `t_warn_info`;
CREATE TABLE `t_warn_info` (
  `warn_id` bigint(58) NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `warn_level` int(11) DEFAULT '1' COMMENT '告警等级',
  `warn_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  `warn_type` int(11) DEFAULT '1' COMMENT '告警类型',
  `warn_name` varchar(125) DEFAULT '' COMMENT '告警名称',
  `warn_content` varchar(512) DEFAULT '' COMMENT '告警内容',
  `device_id` bigint(32) DEFAULT '1' COMMENT '设备Id',
  `device_name` varchar(255) NULL COMMENT '设备名称',
  `cunstom_id` varchar(32) DEFAULT '' COMMENT '部位ID',
  `instance_id` bigint(48) DEFAULT '1' COMMENT '巡检点ID',
  `std_mete_id` bigint(48) DEFAULT '1' COMMENT '标准测点ID',
  `device_mete_name` varchar(255) NULL COMMENT '测点名称',
  `conf_mode` int(11) DEFAULT '1' COMMENT '处理状态1.已核查2.未核查',
  `is_warn` int(11) DEFAULT '1' COMMENT '是否告警',
  `deal_type` int(1) DEFAULT '1' COMMENT '是否属实1.属实2.不属实',
  `deal_info` text COMMENT '处理意见',
  `deal_person_id` varchar(32) DEFAULT '' COMMENT '确认人ID',
  `deal_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `defect_model` int(11) DEFAULT '1' COMMENT '缺陷类型',
  `alarm_source` int(11) DEFAULT '1' COMMENT '告警来源',
  `warn_subtype` int(11) DEFAULT '1' COMMENT '告警子类型',
  `device_code` varchar(64) DEFAULT '' COMMENT '设备编码',
  `image_path` varchar(512) DEFAULT '0' COMMENT '图片地址',
  `video_path` varchar(200) DEFAULT '0' COMMENT '视频地址',
  `value` varchar(100) DEFAULT '',
  `out_range` varchar(100) DEFAULT '',
  `task_id` varchar(512) DEFAULT '' COMMENT '任务ID',
  `alarm_owner` int(11) DEFAULT NULL COMMENT '告警是用谁的告警规则产生的',
  PRIMARY KEY (`warn_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='告警信息表';

-- 任务重构 --
-- ----------------------------
-- Table structure for u_patrol_task_attr
-- ----------------------------
DROP TABLE IF EXISTS `u_patrol_task_attr`;
CREATE TABLE `u_patrol_task_attr` (
  `task_id` varchar(128) NOT NULL COMMENT '关联任务表id',
  `instance_id` bigint NOT NULL COMMENT '巡检点实例ID',
  `device_mete_id` bigint DEFAULT '1' COMMENT '测点实例ID',
  `device_id` bigint DEFAULT '1' COMMENT '关联设备id',
  `custom_id` varchar(32) DEFAULT '' COMMENT '关联部位表id',
  `point_task_id` varchar(32) DEFAULT '' COMMENT '关联巡视点表id',
  `point_type` int DEFAULT '1' COMMENT '巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹',
  `device_type` int DEFAULT '1' COMMENT '设备类型',
  `mete_type` int DEFAULT '1' COMMENT '识别类型',
  `region_id` bigint DEFAULT NULL COMMENT '设备所属区域',
  PRIMARY KEY (`task_id`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='任务关联表';


-- ----------------------------
-- Table structure for u_patrol_data_result
-- ----------------------------
DROP TABLE IF EXISTS `u_patrol_data_result`;
CREATE TABLE `u_patrol_data_result` (
  `cruise_data_id` bigint(68) NOT NULL AUTO_INCREMENT COMMENT '巡视点数据id',
  `task_id` varchar(128) DEFAULT '' COMMENT '巡视任务id',
  `device_id` bigint(32) DEFAULT '0' COMMENT '设备ID',
  `device_name` varchar(50) DEFAULT '' COMMENT '设备名称',
  `device_mete_id` bigint(32) DEFAULT '0' COMMENT '测点ID',
  `device_mete_name` varchar(50) DEFAULT '' COMMENT '测点名称',
  `custom_id` varchar(50) DEFAULT '' COMMENT '部位id',
  `custom_name` varchar(50) DEFAULT '' COMMENT '测点名称',
  `device_point_id` varchar(50) DEFAULT '' COMMENT '设备点位id',
  `instance_id` bigint(48) DEFAULT '0' COMMENT '巡检点实例ID',
  `instance_name` varchar(255) DEFAULT '' COMMENT '巡检点实例名称',
  `cruise_id` bigint(48) DEFAULT '0' COMMENT '巡检点ID',
  `cruise_name` varchar(50) DEFAULT '' COMMENT '巡检点名称',
  `cruise_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡检时间',
  `cruise_status` int(11) DEFAULT '0' COMMENT '状态:0-已执行 1-未执行 2-执行失败 3-未知',
  `cruise_type` int(11) DEFAULT '1' COMMENT '巡检点类型 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹',
  `cruise_device_id` varchar(64) DEFAULT '' COMMENT '巡视设备ID',
  `cruise_device_name` varchar(128) DEFAULT '' COMMENT '巡视设备名称',
  `result_desc` varchar(512) DEFAULT '' COMMENT '巡检结果文字描述（暂时没用）',
  `result_num` varchar(512) DEFAULT '' COMMENT '巡检结果数值',
  `modify_num` varchar(100) DEFAULT '' COMMENT '审核结果数值',
  `unit` varchar(64) NULL DEFAULT '' COMMENT '单位',
  `picpath` varchar(256) DEFAULT '' COMMENT '巡检分析图片，相对路径',
  `confirm_pic_path` varchar(255) DEFAULT '' COMMENT '操作前结果图片,相对',
  `pic_path_anl` varchar(255) DEFAULT '' COMMENT '机器人巡检图片,相对（暂时没用）',
  `person_check` varchar(512) DEFAULT '' COMMENT '人工校核结果',
  `origpic` varchar(256) DEFAULT '' COMMENT '算法原始图片/红外可见光，绝对路径',
  `orig_confirm_pic_path` varchar(255) DEFAULT '' COMMENT '操作前结果图片,绝对',
  `orig_pic_anl` varchar(255) DEFAULT '' COMMENT '机器人巡检图片,绝对（暂时没用）',
  `cruise_abnormal` int(11) DEFAULT '1' COMMENT '巡视异常原因 -抓图失败、数据异常、异常告警、算法超时',
  `evaluation_state` int(11) DEFAULT '1' COMMENT '审核状态1-审核0-未审核',
  `identify_state` int(11) DEFAULT '1' COMMENT '识别状态 1识别正常 2识别异常',
  `identify_result` int(11) DEFAULT '1' COMMENT '实际结果 1正常 2异常',
  `createtime` datetime DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(256) DEFAULT '' COMMENT '备用字段3',
  `check_user` varchar(32) DEFAULT '' COMMENT '审核人',
  `check_date` datetime NULL COMMENT '审核时间',
  `is_warn` int(11) DEFAULT '1' COMMENT '是否产生告警1.是0.否',
  `cruise_result` int(11) DEFAULT '1' COMMENT '巡视执行结果-正常、异常',
  `fir_name` varchar(60) DEFAULT '' COMMENT '红外FIR文件名称',
  `fir_date` datetime NULL COMMENT '红外FIR文件生成时间',
  `result_pic` varchar(255) DEFAULT '' COMMENT 'FIR文件存储路径',
  `points` varchar(125) DEFAULT '' COMMENT '图片坐标点',
  `voice_path` varchar(512) DEFAULT NULL COMMENT '声纹文件地址',
  PRIMARY KEY (`cruise_data_id`) USING BTREE,
  KEY `task_id_index` (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检点数据表';


-- ----------------------------
-- Table structure for u_patrol_plan_attr
-- ----------------------------
DROP TABLE IF EXISTS `u_patrol_plan_attr`;
CREATE TABLE `u_patrol_plan_attr` (
  `plan_id` bigint(32) NOT NULL COMMENT '预案ID',
  `device_id` bigint(32) DEFAULT '0' COMMENT '设备ID',
  `device_name` varchar(50) DEFAULT '' COMMENT '设备名称',
  `device_mete_id` bigint(32) DEFAULT NULL COMMENT '测点id',
  `device_mete_name` varchar(255) DEFAULT NULL COMMENT '测点名称',
  `instance_id` bigint(48) NOT NULL DEFAULT '0' COMMENT '巡检点实例ID',
  `instance_name` varchar(255) DEFAULT '' COMMENT '巡检点实例名称',
  `position_id` bigint(48) DEFAULT '0' COMMENT '巡检点ID(机器人上报的点位id)',
  `position_name` varchar(50) DEFAULT '' COMMENT '巡检点名称',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `area_id` varchar(32) DEFAULT '' COMMENT '区域ID',
  `point_type` int(11) DEFAULT '1' COMMENT '巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹',
  `cruise_region_ids` varchar(256) DEFAULT '' COMMENT '巡检区域id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plan_id`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检预案属性表';


-- ----------------------------
-- Table structure for u_patrol_result
-- ----------------------------
DROP TABLE IF EXISTS `u_patrol_result`;
CREATE TABLE `u_patrol_result` (
  `task_id` varchar(128) NOT NULL DEFAULT '' COMMENT '巡检任务ID',
  `task_code` varchar(128) DEFAULT '' COMMENT '任务编码',
  `task_name` varchar(50) DEFAULT '' COMMENT '巡检任务名称',
  `area_id` varchar(32) DEFAULT '' COMMENT '区域id',
  `task_type` int(11) DEFAULT '1' COMMENT '任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义',
  `execute_type` int(11) DEFAULT '1' COMMENT '执行类型（172.周期，173.立即，174.定期）',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `task_source` int(11) DEFAULT '1' COMMENT '任务来源（暂时没用）',
  `task_level` int(2) DEFAULT '1' COMMENT '任务等级(从高到低):4级,3级,2级,1级',
  `task_state` int(11) DEFAULT '1' COMMENT '任务状态  -1.数据异常 0.正在执行 1.执行完成 2.任务暂停 3.任务终止 4任务异常终止5. 任务超期',
  `modify_state` int(11) DEFAULT '1' COMMENT '状态修正值（暂时没用）',
  `task_count` int(11) DEFAULT '1' COMMENT '总巡检点数',
  `task_wait` int(11) DEFAULT '1' COMMENT '待巡检点数',
  `check_user` varchar(128) DEFAULT '' COMMENT '审核人',
  `check_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `weather` varchar(255) DEFAULT '' COMMENT '微气象',
  `create_time` datetime DEFAULT NULL COMMENT '巡检开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '巡检结束时间',
  `execute_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `is_review` varchar(10) DEFAULT '0' COMMENT '巡视点是否全部审核完成，1-是0-否',
  `task_abnormal` int(11) DEFAULT '0' COMMENT '巡检点异常数量',
  `cruise_result` int(11) DEFAULT '0' COMMENT '巡视结果：0-正常 1-异常',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`task_id`),
  KEY `index_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检任务结果表';


-- ----------------------------
-- Table structure for u_patrol_task
-- ----------------------------
DROP TABLE IF EXISTS `u_patrol_task`;
CREATE TABLE `u_patrol_task` (
  `task_id` varchar(128) NOT NULL COMMENT '巡检任务UUID',
  `task_code` varchar(128) DEFAULT '' COMMENT '任务编码',
  `task_name` varchar(50) DEFAULT '' COMMENT '任务名称',
  `plan_id` bigint(32) DEFAULT '1' COMMENT '所属预案id',
  `area_id` varchar(32) DEFAULT '' COMMENT '所属厂站',
  `task_type` int(11) DEFAULT '1' COMMENT '任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义',
  `execute_type` int(11) DEFAULT '1' COMMENT '执行类型（172.周期，173.立即，174.定期）',
  `robot_id` bigint(20) DEFAULT '1' COMMENT '机器人id',
  `date_type` varchar(255) DEFAULT '' COMMENT '定时时间类型（1.周，2.日）',
  `task_source` int(11) DEFAULT '1' COMMENT '任务来源（暂时没用）',
  `task_level` int(2) DEFAULT '1' COMMENT '任务等级(从高到低):4级,3级,2级,1级',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '巡视时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `end_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '结束时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户id',
  PRIMARY KEY (`task_id`) USING BTREE,
  KEY `areaid` (`area_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='巡检任务表';

-- ----------------------------
-- Table structure for warn_sub
-- ----------------------------
DROP TABLE IF EXISTS `warn_sub`;
CREATE TABLE `warn_sub` (
  `warn_sub_id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `sub_warn_level` varchar(32) DEFAULT '' COMMENT '订阅的告警级别',
  `sub_warn_type` varchar(64) DEFAULT '' COMMENT '订阅的告警类型',
  PRIMARY KEY (`warn_sub_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='告警订阅信息';

DROP TABLE IF EXISTS `t_task_priority_config`;
CREATE TABLE `t_task_priority_config`(
    `prioritized_task_type` int(20) NOT NULL COMMENT '执行等级配置任务类型',
    `execute_level`         int(20) NOT NULL COMMENT '任务执行等级',
    `update_time`           datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`prioritized_task_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='任务优先级配置信息';


DROP TABLE IF EXISTS `device_statics_info_result`;
CREATE TABLE `device_statics_info_result`  (
    `device_code` varchar(255)  NOT NULL COMMENT '设备编码',
    `device_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
    `duration` varchar(255) DEFAULT NULL COMMENT '累积在线时长总和',
    `offLine_count` varchar(255) DEFAULT NULL COMMENT '累积离线次数总和',
    `normal_day` varchar(255) DEFAULT NULL COMMENT '累计连续正常运行天数',
    `commission_days` varchar(255) DEFAULT NULL COMMENT '正常巡检天数',
    `cruise_percent` varchar(255) DEFAULT NULL COMMENT '巡检出勤率',
    `intact_percent` varchar(255) DEFAULT NULL COMMENT '录像完整率',
    `device_run` varchar(1) DEFAULT NULL COMMENT '设备状态',
    `device_resume_date` datetime DEFAULT NULL COMMENT '设备恢复时间',
    `device_type` varchar(255) DEFAULT NULL COMMENT '设备类型',
    `region_code` varchar(255) DEFAULT NULL COMMENT '区域编码',
    PRIMARY KEY (`device_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4  ROW_FORMAT = Dynamic COMMENT='可靠性指标统计表';

DROP TABLE IF EXISTS `sys_user_device_permission`;
CREATE TABLE `sys_user_device_permission`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `monitor_device_id` bigint(20) NOT NULL COMMENT '监控设备id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0  DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户设备权限表';

DROP TABLE IF EXISTS `silent_conf`;
CREATE TABLE `silent_conf` (
  `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `preset_type` int(11) DEFAULT NULL COMMENT '预置位类型',
  `preset_type_name` varchar(255) DEFAULT NULL COMMENT '预置位类型名称',
  `recognize_type` varchar(255) DEFAULT NULL COMMENT '要识别的类型，以逗号隔开 如:wcanm,wcgz',
  `chill_time` int(64) DEFAULT -1 COMMENT '此类型识别的时间间隔，单位：秒',
  `editable` tinyint(1) NOT NULL COMMENT '是否可编辑',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='静默监视配置表';

DROP TABLE IF EXISTS `t_cruise_triphase_rule`;
CREATE TABLE `t_cruise_triphase_rule` (
  `triphase_id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '三相告警规则ID',
  `triphase_name` varchar(64) DEFAULT '' COMMENT '三相告警规则名称',
  `device_mete_id` bigint(48) DEFAULT '1' COMMENT '测点实例ID',
  `device_id` bigint(32) DEFAULT '1' COMMENT '关联设备id',
  `custom_id` varchar(32) DEFAULT '' COMMENT '关联部位表id',
  `instance_one_id` bigint(32) DEFAULT '1' COMMENT '巡视点id 1',
  `instance_one_name` varchar(128) DEFAULT '' COMMENT '巡视点id 1名称',
  `one_cruise_device_name` varchar(64) DEFAULT '' COMMENT '巡视点1巡视设备名称',
  `instance_two_id` bigint(32) DEFAULT '1' COMMENT '巡视点id 2',
  `instance_two_name` varchar(128) DEFAULT '' COMMENT '巡视点id 2名称',
  `two_cruise_device_name` varchar(64) DEFAULT '' COMMENT '巡视点2巡视设备名称',
  `instance_tri_id` bigint(32) DEFAULT '1' COMMENT '巡视点id 3',
  `instance_tri_name` varchar(128) DEFAULT '' COMMENT '巡视点id 3名称',
  `tri_cruise_device_name` varchar(64) DEFAULT '' COMMENT '巡视点3巡视设备名称',
  `identify_type` int(11) DEFAULT '1' COMMENT '点位识别类型 1. 表计读数，2红外测温',
  `identify_son_type` int(11) DEFAULT '1' COMMENT '点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表',
  `triphase_type` int(11) DEFAULT '1' COMMENT '三相告警类型 1：三相不平衡 2：三相温差',
  `warn_threshold` varchar(256) DEFAULT NULL COMMENT '告警阈值',
  `warn_level` int(11) DEFAULT '1' COMMENT '告警等级：1-预警，2-一般告警，3-严重告警，4-危急告警，',
  PRIMARY KEY (`triphase_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='三相告警配置实例表';

DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint(68) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `config_type` varchar(50) default NULL  COMMENT '配置类型',
  `config_name` varchar(50) default NULL  COMMENT '配置类型名称',
  `config_key` varchar(50) default NULL  COMMENT '配置键',
  `config_value` text default NULL  COMMENT '配置值',
  `config_remark` varchar(500) default NULL  COMMENT '配置建说明',
  `remark` varchar(500) default NULL  COMMENT '补充说明',
  `rules` varchar(500) default NULL  COMMENT '规则校验',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统配置表';

-- ----------------------------
-- Table structure for sys_disk_cleanup
-- ----------------------------
DROP TABLE IF EXISTS `sys_disk_cleanup`;
CREATE TABLE `sys_disk_cleanup` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `del_tmp` int DEFAULT '0' COMMENT '是否删除临时文件，0: 否 1: 是  2: 清理完成  -1: 清理失败',
  `del_task_file` int DEFAULT '0' COMMENT '是否删除任务文件，0: 否 1: 是  2: 清理完成  -1: 清理失败',
  `del_task_report` int DEFAULT '0' COMMENT '是否删除任务报告，0: 否 1: 是  2: 清理完成  -1: 清理失败',
  `del_task_data` int DEFAULT '0' COMMENT '是否删除任务数据库记录，0: 否 1: 是  2: 清理完成  -1: 清理失败',
  `del_logs` int DEFAULT '0' COMMENT '是否删除日志记录，0: 否 1: 是  2: 清理完成  -1: 清理失败',
  `back_database` int DEFAULT '0' COMMENT '是否备份数据库，0: 否 1: 是，如果有删除数据库记录则必须备份',
  `back_file_path` varchar(128) DEFAULT NULL COMMENT '备份文件路径',
  `back_data_path` varchar(128) DEFAULT NULL COMMENT '备份数据库文件',
  `back_expire` int DEFAULT '0' COMMENT '备份过期状态，0: 未备份 1: 未过期 2: 过期失效  3: 手动删除',
  `clean_content` varchar(512) DEFAULT NULL COMMENT '清理具体内容说明',
  `clean_status` int DEFAULT '0' COMMENT '清理状态，0: 未完成 1: 待确认  2: 已确认',
  `expiry_date` datetime DEFAULT NULL COMMENT '清理时限',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='磁盘清理记录';


DROP TABLE IF EXISTS `common_menu_conf`;
CREATE TABLE `common_menu_conf` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
   `menu_code` varchar(50) DEFAULT NULL COMMENT '菜单编码',
   `menu_name` varchar(50) DEFAULT NULL COMMENT '菜单名称',
   `path` varchar(255) DEFAULT NULL COMMENT '菜单路径',
   `img` varchar(255) DEFAULT NULL COMMENT '菜单图片',
   `sort` int DEFAULT NULL COMMENT '顺序值 ',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='常用菜单配置表';

DROP TABLE IF EXISTS `common_menu`;
CREATE TABLE `common_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id` bigint DEFAULT NULL COMMENT '用户id',
    `menu_code` varchar(50) DEFAULT NULL COMMENT '菜单编码',
    `menu_name` varchar(50) DEFAULT NULL COMMENT '菜单名称',
    `path` varchar(255) DEFAULT NULL COMMENT '菜单路径',
    `img` varchar(255) DEFAULT NULL COMMENT '菜单图片',
    `sort` int DEFAULT NULL COMMENT '顺序 小于4为常用默认',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='常用功能配置表';
-- Table structure for t_wiring_config
-- ----------------------------
DROP TABLE IF EXISTS `t_wiring_config`;
CREATE TABLE `t_wiring_config` (
`wiring_config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
`wiring_diagram_id` smallint DEFAULT NULL COMMENT '主接线图id',
`equipment_id` bigint DEFAULT NULL COMMENT '关联设备id',
`equipment_name` varchar(128) DEFAULT '' COMMENT '关联设备名称',
`equipment_type` int DEFAULT '1' COMMENT '关联设备类型',
`x_coordinate` float(7,3) DEFAULT '0.000' COMMENT '坐标位置x',
`y_coordinate` float(7,3) DEFAULT '0.000' COMMENT '坐标位置y',
`update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
`update_person` varchar(20) DEFAULT '' COMMENT '更新人',
PRIMARY KEY (`wiring_config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for t_wiring_diagram
-- ----------------------------
DROP TABLE IF EXISTS `t_wiring_diagram`;
CREATE TABLE `t_wiring_diagram` (
`wiring_diagram_id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
`region_id` bigint DEFAULT NULL COMMENT '区域id',
`region_name` varchar(128) DEFAULT NULL COMMENT '区域名称',
`pic_path` varchar(255) DEFAULT '' COMMENT '主接线图路径',
`pic_length` smallint DEFAULT '1' COMMENT '图片长度',
`pic_width` smallint DEFAULT '1' COMMENT '图片宽度',
`upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
`upload_person` varchar(20) DEFAULT '' COMMENT '上传人',
`update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
`update_person` varchar(20) DEFAULT '' COMMENT '更新人',
`delete_flag` tinyint DEFAULT '0' COMMENT '删除标记，1-是0-否',
PRIMARY KEY (`wiring_diagram_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5000 DEFAULT CHARSET=utf8mb3;
-- ----------------------------
-- Table structure for alarm_shield
-- ----------------------------
DROP TABLE IF EXISTS `alarm_shield`;
CREATE TABLE `alarm_shield` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
`shield_id` text COMMENT '屏蔽的设备id(巡视设备id，或者测点id)',
`shield_name` varchar(258) DEFAULT NULL COMMENT '名称',
`mete_type` int DEFAULT NULL COMMENT '屏蔽的测点类型',
`meter_type` int DEFAULT NULL COMMENT '屏蔽的测点子类型',
`enable` int DEFAULT NULL COMMENT '是否启用 1-已起用 0-未启用',
`create_user_id` bigint DEFAULT NULL COMMENT '创建屏蔽的用户id',
`end_time` datetime DEFAULT NULL COMMENT '屏蔽结束时间',
`shield_type` int DEFAULT NULL COMMENT '屏蔽类型：1-测点,2-巡视设备,3-巡视类型',
`warn_content` varchar(258) DEFAULT NULL COMMENT '屏蔽的告警内容',
`create_time` datetime DEFAULT NULL COMMENT '创建时间',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='告警屏蔽配置';

DROP TABLE IF EXISTS `dict_area`;
CREATE TABLE `dict_area` (
`id` int NOT NULL,
`name` varchar(48) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '名称',
`parent_id` int DEFAULT NULL COMMENT '父节点',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='区县行政编码字典表';

CREATE TABLE `a_interface_task_info` (
`type` varchar(5) DEFAULT NULL COMMENT ' 巡检类型 <1>: = 全面巡视 <2>: = 例行巡视\r\n<3>: = 专项巡视<4>: = 特殊巡视',
`task_code` varchar(256) NOT NULL COMMENT '任务编码',
`task_name` varchar(256) DEFAULT NULL COMMENT '任务名称',
`priority` varchar(5) DEFAULT NULL COMMENT '优先级 <1>: = 优先级1，优先级最低<2>: = 优先级2 <3>: = 优先级3<4>: = 优先级4，优先级最高',
`device_level` varchar(5) DEFAULT NULL COMMENT '设备层级 <1>: = 间隔 <2>: = 主设备 <3>: = 设备点位',
`device_list` text DEFAULT NULL COMMENT '设备列表',
`fixed_start_time` varchar(32) DEFAULT NULL COMMENT '定期开始时间',
`cycle_month` varchar(128) DEFAULT NULL COMMENT '周期（月）',
`cycle_week` varchar(128) DEFAULT NULL COMMENT '周期（周）',
`cycle_execute_time` varchar(32) DEFAULT NULL COMMENT '周期（执行时间）',
`cycle_start_time` varchar(32) DEFAULT NULL COMMENT '周期开始时间',
`cycle_end_time` varchar(32) DEFAULT NULL COMMENT '周期结束时间',
`interval_number` varchar(5) DEFAULT NULL COMMENT '间隔（数量）',
`interval_type` varchar(5) DEFAULT NULL COMMENT '间隔（类型）  <1>: = 小时<2>: = 天',
`interval_execute_time` varchar(32) DEFAULT NULL COMMENT '间隔（执行时间）  格式：HH:mm:ss',
`interval_start_time` varchar(32) DEFAULT NULL COMMENT '间隔开始时间',
`interval_end_time` varchar(32) DEFAULT NULL COMMENT '间隔结束时间',
`invalid_start_time` varchar(32) DEFAULT NULL COMMENT '不可用开始时间',
`invalid_end_time` varchar(32) DEFAULT NULL COMMENT '不可用结束时间',
`isenable` varchar(5) DEFAULT NULL COMMENT '是否可用 <0>: = 可用<1>: = 不可用 2 删除',
`creator` varchar(32) DEFAULT NULL COMMENT '编制人',
`create_time` varchar(32) DEFAULT NULL COMMENT '编制时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A接口任务信息';
