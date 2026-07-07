# B 模块接口说明

基础地址：`http://localhost:8080`

## 用户管理

- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{userId}`
- `PUT /api/users/{userId}`
- `DELETE /api/users/{userId}`

## 车辆档案管理

- `POST /api/vehicles`
- `GET /api/vehicles`
- `GET /api/vehicles/{vin}`
- `GET /api/vehicles/owner/{ownerId}`
- `PUT /api/vehicles/{vin}`
- `DELETE /api/vehicles/{vin}`

## 预约管理

- `POST /api/appointments`
- `GET /api/appointments`
- `GET /api/appointments/{appointmentNo}`
- `GET /api/appointments/vehicle/{vin}`
- `PUT /api/appointments/{appointmentNo}/status?status=CONFIRMED`
- `DELETE /api/appointments/{appointmentNo}`

## 故障登记管理

- `POST /api/fault-records`
- `GET /api/fault-records`
- `GET /api/fault-records/{faultNo}`
- `GET /api/fault-records/vehicle/{vin}`
- `PUT /api/fault-records/{faultNo}/status?status=DIAGNOSED`
- `DELETE /api/fault-records/{faultNo}`

## 状态取值

预约状态：

- `PENDING`：待确认
- `CONFIRMED`：已确认
- `CANCELLED`：已取消
- `ARRIVED`：已到店
- `COMPLETED`：已完成

故障状态：

- `REGISTERED`：已登记
- `DIAGNOSED`：已诊断
- `WORK_ORDER_CREATED`：已生成工单
- `CLOSED`：已关闭

## 示例请求

新增车辆：

```json
{
  "vin": "LC0CE4DB7N0000001",
  "ownerId": 1,
  "licensePlate": "YueB12345",
  "model": "Han EV",
  "batteryModel": "Blade Battery A1",
  "purchaseDate": "2024-05-01",
  "currentMileage": 18320.5,
  "vehicleStatus": "NORMAL"
}
```

新增预约：

```json
{
  "vin": "LC0CE4DB7N0000001",
  "ownerId": 1,
  "centerId": 1,
  "appointmentTime": "2026-07-08T09:30:00",
  "problemDescription": "车辆快充速度变慢，仪表提示电池温度异常"
}
```
