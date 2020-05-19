CREATE TABLE `teachplan_media_pub` (
  `teachplan_id` varchar(32) NOT NULL COMMENT '课程计划id',
  `media_id` varchar(32) NOT NULL COMMENT '媒资文件id',
  `media_fileoriginalname` varchar(128) NOT NULL COMMENT '媒资文件的原始名称',
  `media_url` varchar(256) NOT NULL COMMENT '媒资文件访问地址',
  `courseid` varchar(32) NOT NULL COMMENT '课程Id',
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'logstash使用',
  PRIMARY KEY (`teachplan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8