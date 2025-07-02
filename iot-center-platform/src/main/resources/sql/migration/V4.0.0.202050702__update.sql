ALTER TABLE t_patrol_device_version ADD robot_type int DEFAULT NULL COMMENT '机器人型号';
ALTER TABLE t_patrol_device_version modify column `file_path` varchar(256) DEFAULT NULL COMMENT '版本文件路径';
INSERT INTO `t_sys_param` (`param_type`, `param_code`, `param_name`, `content`, `remark`, `rules`) VALUES ('404', 'defaultReportExport', '默认导出报表', 'true', 'true:默认使用老的模板 false:使用新的模板', '{"rule":"^(true|false)$","msg":"只能填 true 或 false"}');
