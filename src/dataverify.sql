
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `filetag`
-- ----------------------------
DROP TABLE IF EXISTS `filetag`;
CREATE TABLE `filetag` (
  `name` varchar(10) NOT NULL,
  `value` binary(160) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `public`
-- ----------------------------
DROP TABLE IF EXISTS `public`;
CREATE TABLE `public` (
  `name` varchar(20) NOT NULL,
  `value` binary(160) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
