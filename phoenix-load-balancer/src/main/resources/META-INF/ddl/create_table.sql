delimiter ;

CREATE TABLE `deployment_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '发布任务的备注名，仅用于方便查看',
  `deploy_policy` varchar(32) DEFAULT NULL COMMENT '部署策略, 可选值: one-by-one, two-by-two, three-by-three',
  `error_policy` varchar(32) DEFAULT NULL COMMENT '出错处理策略, 可选值: 1 - abort on error, 2 - fall through',
  `auto_continue` tinyint(1) DEFAULT NULL COMMENT '是否自动继续下一个Rollout，可选值: 1 - auto continue, 2 - mannul continue',
  `deploy_interval` int(1) DEFAULT NULL COMMENT '自动deploy的间隔时间，单位:分钟',
  `status` varchar(32) DEFAULT 'CREATED' COMMENT '部署状态，可选值: 1 - pending, 2 - deploying, 3 - completed with all successful, 4 - completed with partial failures, 5 - failed, 8 - aborted, 9 - cancelled',
  `deployed_by` varchar(64) DEFAULT NULL COMMENT '部署者',
  `begin_date` datetime DEFAULT NULL COMMENT '部署开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '部署结束时间',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_modified_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8 COMMENT='软负载部署-task表';


delimiter ;

CREATE TABLE `deployment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `vs` varchar(32) NOT NULL COMMENT 'virtual server name，如: www',
  `tag` varchar(32) DEFAULT NULL COMMENT 'tag，如: www-1',
  `status` varchar(32) DEFAULT 'CREATED' COMMENT '部署状态，可选值: 1 - pending, 2 - deploying, 3 - completed with all successful, 4 - completed with partial failures, 5 - failed, 8 - aborted, 9 - cancelled',
  `task_id` int(11) NOT NULL COMMENT '所属的部署Task的ID',
  `begin_date` datetime DEFAULT NULL COMMENT '部署开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '部署结束时间',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_modified_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8 COMMENT='软负载部署主表';

delimiter ;

CREATE TABLE `deployment_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deploy_id` int(11) NOT NULL COMMENT '部署ID',
  `ip_address` varchar(32) DEFAULT NULL COMMENT '业务主机IP地址',
  `status` varchar(32) DEFAULT 'CREATED' COMMENT '部署状态，可选值: 1 - pending, 2 - deploying, 3 - completed with all successful, 4 - completed with partial failures, 5 - failed, 8 - aborted, 9 - cancelled',
  `raw_log` mediumtext COMMENT '原始日志',
  `begin_date` datetime DEFAULT NULL COMMENT '部署开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '部署结束时间',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_modified_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8 COMMENT='软负载部署详细表';


