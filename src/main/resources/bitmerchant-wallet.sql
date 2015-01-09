



-- ---
-- Globals
-- ---

-- SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
-- SET FOREIGN_KEY_CHECKS=0;

-- ---
-- Table 'test'
-- 
-- ---

DROP TABLE IF EXISTS `test`;
		
CREATE TABLE `test` (
  `id` INTEGER NULL primary key AUTOINCREMENT NOT NULL,
  `mmmk` TINYINT NULL DEFAULT NULL,
  `new field` INTEGER NULL DEFAULT NULL,
  `id_kkkk` INTEGER NULL DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP

);

CREATE TABLE derp2 (
  `id` INTEGER primary key AUTOINCREMENT NOT NULL,
  `blarp` INTEGER NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
)

-- ---
-- Table 'kkkk'
-- 
-- ---

DROP TABLE IF EXISTS `kkkk`;
		
CREATE TABLE `kkkk` (
  `id` INTEGER NULL primary key AUTOINCREMENT DEFAULT NOT NULL,
  `test` INTEGER NULL DEFAULT NULL,

);

-- ---
-- Foreign Keys 
-- ---

ALTER TABLE `test` ADD FOREIGN KEY (id_kkkk) REFERENCES `kkkk` (`id`);

-- ---
-- Table Properties
-- ---

-- ALTER TABLE `test` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `kkkk` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ---
-- Test Data
-- ---

-- INSERT INTO `test` (`id`,`mmmk`,`new field`,`id_kkkk`) VALUES
-- ('','','','');
-- INSERT INTO `kkkk` (`id`,`test`) VALUES
-- ('','');

