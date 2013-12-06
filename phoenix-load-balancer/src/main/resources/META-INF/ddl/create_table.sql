delimiter ;

CREATE TABLE `deploy_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '发布任务的备注名，仅用于方便查看',
  `deploy_policy` varchar(32) DEFAULT NULL COMMENT '部署策略, 可选值: one-by-one, two-by-two, three-by-three',
  `error_policy` varchar(32) DEFAULT NULL COMMENT '出错处理策略, 可选值: 1 - abort on error, 2 - fall through',
  `auto_continue` tinyint(1) DEFAULT NULL COMMENT '是否自动继续下一个Rollout，可选值: 1 - auto continue, 2 - mannul continue',
  `deploy_interval` int(1) DEFAULT NULL COMMENT '自动deploy的间隔时间，单位:分钟',
  `status` varchar(32) NOT NULL DEFAULT 'CREATED' COMMENT '部署状态，可选值: 1 - pending, 2 - deploying, 3 - completed with all successful, 4 - completed with partial failures, 5 - failed, 8 - aborted, 9 - cancelled',
  `deployed_by` varchar(64) DEFAULT NULL COMMENT '部署者',
  `agent_id` bigint(20) DEFAULT NULL COMMENT '发布agent时的流水号，用于通知agent发布后，不断从agent获取rawlog，一次task发布时，所有agent都使用该agentId。\nagentId用于正在发布的task，一次task发布结束后，就可以废弃该流水号，因为rawlog已经存储到数据库。\n再次发布时，使用新的agentId。',
  `begin_date` datetime DEFAULT NULL COMMENT '部署开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '部署结束时间',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_modified_date` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8 COMMENT='软负载部署-task表';

delimiter ;

CREATE TABLE `deploy_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deploy_vs_id` bigint(20) NOT NULL COMMENT '部署ID',
  `ip_address` varchar(32) NOT NULL COMMENT '业务主机IP地址',
  `status` varchar(32) NOT NULL DEFAULT 'CREATED' COMMENT '部署状态，可选值: 1 - pending, 2 - deploying, 3 - completed with all successful, 4 - completed with partial failures, 5 - failed, 8 - aborted, 9 - cancelled',
  `raw_log` mediumtext COMMENT '原始日志',
  `begin_date` datetime DEFAULT NULL COMMENT '部署开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '部署结束时间',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_modified_date` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='软负载部署详细表';

delimiter ;

CREATE TABLE `agent_id_sequence` (
  `agent_id` bigint(20) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于产生agent_id流水号';



CREATE TABLE `deploy_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deploy_id` bigint(20) NOT NULL COMMENT '部署ID',
  `ip_address` varchar(32) DEFAULT NULL COMMENT '业务主机IP地址',
  `status` varchar(32) DEFAULT 'CREATED' COMMENT '部署状态，可选值: 1 - pending, 2 - deploying, 3 - completed with all successful, 4 - completed with partial failures, 5 - failed, 8 - aborted, 9 - cancelled',
  `raw_log` mediumtext COMMENT '原始日志',
  `begin_date` datetime DEFAULT NULL COMMENT '部署开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '部署结束时间',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_modified_date` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='软负载部署详细表';

