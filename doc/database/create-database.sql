-- ============================================================
-- 小米商城智能导购 Agent · 业务库初始化脚本
-- 用途：创建业务库 xiaomi_agent（schema.sql 在该库内建表）
-- 执行方式：连到 postgres 库执行（CREATE DATABASE 不能在事务块内，也不能连目标库自身）
--   psql -h 192.168.122.128 -U root -d postgres -f create-database.sql
-- 然后再连 xiaomi_agent 执行 schema.sql + init.sql：
--   psql -h 192.168.122.128 -U root -d xiaomi_agent -f schema.sql
--   psql -h 192.168.122.128 -U root -d xiaomi_agent -f init.sql
-- ============================================================

-- 业务库（如已存在则跳过，幂等）
SELECT 'CREATE DATABASE xiaomi_agent OWNER root'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'xiaomi_agent')\gexec

-- 授予 root 用户全部权限（OWNER 已是 root，此处显式再授权保险）
GRANT ALL PRIVILEGES ON DATABASE xiaomi_agent TO root;

\echo '业务库 xiaomi_agent 已就绪，请连接该库执行 schema.sql + init.sql'
