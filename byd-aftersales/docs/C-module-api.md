# C 模块接口说明（工单 / 备件 / 结算）

基础地址：`http://localhost:8080`

## 维修工单

- `POST /api/work-orders` — 创建工单
- `GET /api/work-orders` — 工单列表
- `GET /api/work-orders/{id}` — 工单详情
- `GET /api/work-orders/my?technicianId=` — 技师自己的工单
- `PUT /api/work-orders/{id}/assign` — 指派技师
- `PUT /api/work-orders/{id}/start` — 开始维修
- `POST /api/work-orders/{id}/complete` — 完工（触发事务）

## 备件管理

- `POST /api/parts` — 新增备件
- `GET /api/parts` — 备件列表
- `GET /api/parts/{id}` — 备件详情
- `PUT /api/parts/{id}` — 更新备件
- `DELETE /api/parts/{id}` — 删除备件
- `POST /api/parts/{id}/stock` — 补货
- `GET /api/parts/alerts` — 低库存预警（Redis）

## 备件申请

- `POST /api/part-usages` — 技师申请备件
- `GET /api/part-usages?workOrderId=` — 查看工单备件申请
- `PUT /api/part-usages/{id}/approve` — 审批通过
- `PUT /api/part-usages/{id}/reject` — 审批拒绝

## 结算

- `GET /api/settlements/work-order/{workOrderId}` — 按工单查结算单
- `GET /api/settlements/{id}` — 结算单详情
- `PUT /api/settlements/{id}/pay` — 确认收款

## 事务说明

`POST /api/work-orders/{id}/complete` 在 `@Transactional` 中原子执行：

1. 扣减已审批备件库存
2. 更新 part_usage 状态为 USED
3. 生成 settlement 结算单
4. 更新 work_order 状态为 COMPLETED

事务提交后异步写入 Redis 低库存预警和 MongoDB 操作日志。
