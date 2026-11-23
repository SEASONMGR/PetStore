-- ============================================================
-- V2：product 表增加逻辑删除字段（需求裁定 R5）
-- 关联任务：T-20261118-24
-- 适用环境：MySQL 5.7（application-mysql.yaml 下 spring.sql.init.mode=never，不会自动执行本脚本）
--
-- ★★★ 用户必须手工执行本脚本 ★★★
--   方式一（推荐）：mysql -u root -p petstore < V2__add_logical_delete.sql
--   方式二：在 MySQL Workbench / Navicat 中打开本文件执行
--   方式三：或按 create-database.sql + schema.sql 整体重新导入（会清空现有数据，谨慎）
--
-- 不执行本脚本的后果：启动 mysql profile 后，所有商品查询会因缺少 deleted 列而报
--   "Unknown column 'deleted' in 'where clause'" —— 这是预期行为，便于尽早发现问题。
--
-- 回滚（如需）：ALTER TABLE `product` DROP INDEX idx_product_deleted, DROP COLUMN `deleted`;
-- ============================================================

ALTER TABLE `product`
    ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除';

CREATE INDEX idx_product_deleted ON `product` (`deleted`);
