-- ============================================================
-- PetStore —— 创建数据库（MySQL 5.7）
-- 用法（注意必须加 --default-character-set=utf8mb4）：
--   $env:MYSQL_PWD='<密码>'
--   & 'F:\mysqlshujuku\bin\mysql.exe' -u root --default-character-set=utf8mb4 -e "source F:/.../create-database.sql"
--
-- 为什么显式 utf8mb4：
--   本机 MySQL 5.7 的 @@character_set_server 实测为 latin1，
--   CREATE DATABASE 不指定字符集时会继承 latin1 → 中文写入变问号。
--   注意用 utf8mb4 而不是 utf8：MySQL 的 utf8 是 3 字节，存不下 emoji 等 4 字节字符。
-- ============================================================

CREATE DATABASE IF NOT EXISTS petstore
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 不在此处 USE / 建表：建表脚本事在 schema.sql（见文件头说明）。
-- 导入顺序：
--   1) 本文件（建库）
--   2) schema.sql（建表，开头有 DROP TABLE IF EXISTS，可重复执行）
--   3) data.sql（初始数据）
