DROP TABLE IF EXISTS `t_patrol_device_version`;
CREATE TABLE `t_patrol_device_version`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`        VARCHAR(64)   DEFAULT NULL COMMENT '版本号',
    `remark`      VARCHAR(512)  DEFAULT NULL COMMENT '版本描述',
    `text`        VARCHAR(1024) DEFAULT NULL COMMENT '版本详细说明',
    `file_path`   VARCHAR(256)  DEFAULT NULL COMMENT '版本文件路径',
    `robot_type`  int(6) DEFAULT '1' COMMENT '机器人型号',
    `create_user` VARCHAR(64)   DEFAULT '' COMMENT '创建人',
    `update_user` VARCHAR(64)   DEFAULT '' COMMENT '更新人',
    `create_time` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT = '巡视设备版本表';

INSERT INTO `t_sys_param` (`param_type`, `param_code`, `param_name`, `content`, `remark`, `rules`)
VALUES ('404', 'defaultReportExport', '默认导出报表', 'true', 'true:默认使用老的模板 false:使用新的模板', '{"rule":"^(true|false)$","msg":"只能填 true 或 false"}');
