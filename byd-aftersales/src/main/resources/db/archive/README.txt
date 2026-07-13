归档说明
========

本目录存放历史 SQL 脚本，已由以下文件替代，请勿在新环境继续使用：

  schema.sql          — 建表（唯一结构入口，已包含全部 migration 变更）
  seed-production.sql — 生产级种子数据（整合 sample + demo + 扩充）
  reset-data.sql      — 清空业务数据后重导种子

归档文件对照：
  sample-data.sql                      → 已并入 seed-production.sql
  demo-data-20260709-rich.sql          → 已并入 seed-production.sql
  demo-data-20260709-chinese-labels.sql → 已并入 seed-production.sql（直接中文写入）
  migration-20260708-*.sql             → 已并入 schema.sql
  migration-20260709-vehicle-health.sql → 已并入 schema.sql
  module4-schema.sql                   → 历史模块脚本，以 schema.sql 为准
