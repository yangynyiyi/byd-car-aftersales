-- =============================================================================
-- 比亚迪售后系统 — 生产级种子数据（整合版）
-- -----------------------------------------------------------------------------
-- 整合并替代：sample-data.sql、demo-data-20260709-rich.sql、
--            demo-data-20260709-chinese-labels.sql
--
-- 【新库初始化】
--   mysql -uroot -p123456 < schema.sql
--   mysql -uroot -p123456 < seed-production.sql
--
-- 【已有库重置后导入】
--   mysql -uroot -p123456 < reset-data.sql
--   mysql -uroot -p123456 < seed-production.sql
--
-- 演示账号密码统一：12345678
-- 角色账号：owner001~005 / advisor001~002 / tech001~002 / part001 / manager001 / admin001
-- =============================================================================
USE byd_aftersales;

-- 清理重复门店（多次导入脚本时可能产生）
DELETE sc FROM service_center sc
JOIN (
    SELECT phone, MIN(center_id) AS keep_id
    FROM service_center
    WHERE deleted = 0
    GROUP BY phone
    HAVING COUNT(*) > 1
) dup ON sc.phone = dup.phone AND sc.center_id <> dup.keep_id;

-- -----------------------------------------------------------------------------
-- 1. 用户（10 个业务账号）
-- -----------------------------------------------------------------------------
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('owner001',   '12345678', '张三',       '13800000001', 'OWNER',           'ENABLED'),
('owner002',   '12345678', '陈宇',       '13800000007', 'OWNER',           'ENABLED'),
('owner003',   '12345678', '刘芳',       '13800000008', 'OWNER',           'ENABLED'),
('owner004',   '12345678', '王浩',       '13800000011', 'OWNER',           'ENABLED'),
('owner005',   '12345678', '赵敏',       '13800000012', 'OWNER',           'ENABLED'),
('advisor001', '12345678', '李顾问',     '13800000002', 'ADVISOR',         'ENABLED'),
('advisor002', '12345678', '孙顾问',     '13800000009', 'ADVISOR',         'ENABLED'),
('tech001',    '12345678', '王技师',     '13800000003', 'TECHNICIAN',      'ENABLED'),
('tech002',    '12345678', '周技师',     '13800000010', 'TECHNICIAN',      'ENABLED'),
('part001',    '12345678', '陈备件',     '13800000004', 'PART_ADMIN',      'ENABLED'),
('manager001', '12345678', '赵经理',     '13800000005', 'SERVICE_MANAGER', 'ENABLED'),
('admin001',   '12345678', '系统管理员', '13800000006', 'ADMIN',           'ENABLED')
ON DUPLICATE KEY UPDATE
    password = VALUES(password), real_name = VALUES(real_name), role = VALUES(role),
    status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 1.1 技师默认工时费（派工时自动带入人工费）
-- -----------------------------------------------------------------------------
INSERT INTO technician_rate (technician_id, hourly_rate, daily_rate, status)
SELECT u.user_id, 280.00, 350.00, 'ENABLED'
FROM sys_user u WHERE u.username = 'tech001'
  AND NOT EXISTS (SELECT 1 FROM technician_rate tr WHERE tr.technician_id = u.user_id);

INSERT INTO technician_rate (technician_id, hourly_rate, daily_rate, status)
SELECT u.user_id, 320.00, 400.00, 'ENABLED'
FROM sys_user u WHERE u.username = 'tech002'
  AND NOT EXISTS (SELECT 1 FROM technician_rate tr WHERE tr.technician_id = u.user_id);

-- -----------------------------------------------------------------------------
-- 2. 服务中心（3 家）
-- -----------------------------------------------------------------------------
INSERT INTO service_center (center_name, city, address, phone, status)
SELECT * FROM (
    SELECT '比亚迪深圳南山售后服务中心' center_name, '深圳' city, '深圳市南山区科技园示范路 1 号' address, '0755-88888888' phone, 'OPEN' status UNION ALL
    SELECT '比亚迪广州天河售后服务中心', '广州', '广州市天河区示范大道 88 号', '020-88886666', 'OPEN' UNION ALL
    SELECT '比亚迪上海浦东售后服务中心', '上海', '上海市浦东新区示范路 66 号', '021-88887777', 'OPEN'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM service_center sc WHERE sc.phone = seed.phone AND sc.deleted = 0
);

-- -----------------------------------------------------------------------------
-- 3. 车辆（10 辆，覆盖多车主/多状态）
-- -----------------------------------------------------------------------------
INSERT INTO vehicle
    (vin, owner_id, advisor_id, license_plate, model, battery_model, purchase_date,
     last_maintenance_date, next_maintenance_date, next_inspection_date, insurance_expire_date,
     current_mileage, vehicle_status)
SELECT * FROM (
    SELECT 'LC0CE4DB7N0000001' vin, (SELECT user_id FROM sys_user WHERE username='owner001') owner_id,
           (SELECT user_id FROM sys_user WHERE username='advisor001') advisor_id,
           '粤B·12345' license_plate, '汉 EV' model, '刀片电池 A1 型' battery_model, '2024-05-01' purchase_date,
           DATE_SUB(CURDATE(), INTERVAL 5 MONTH) last_maintenance_date, DATE_ADD(CURDATE(), INTERVAL 10 DAY) next_maintenance_date,
           DATE_ADD(CURDATE(), INTERVAL 25 DAY) next_inspection_date, DATE_ADD(CURDATE(), INTERVAL 40 DAY) insurance_expire_date,
           18320.5 current_mileage, 'NORMAL' vehicle_status
    UNION ALL SELECT 'LC0CE4DB7N0000002', (SELECT user_id FROM sys_user WHERE username='owner001'),
           (SELECT user_id FROM sys_user WHERE username='advisor001'),
           '粤B·23456', '海狮 07 EV', '刀片电池 B2 型', '2023-11-12',
           DATE_SUB(CURDATE(), INTERVAL 6 MONTH), DATE_SUB(CURDATE(), INTERVAL 5 DAY),
           DATE_ADD(CURDATE(), INTERVAL 18 DAY), DATE_ADD(CURDATE(), INTERVAL 32 DAY), 42880.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000003', (SELECT user_id FROM sys_user WHERE username='owner002'),
           (SELECT user_id FROM sys_user WHERE username='advisor001'),
           '粤A·34567', '宋 PLUS DM-i', '刀片电池 DM1 型', '2024-02-20',
           DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 35 DAY),
           DATE_ADD(CURDATE(), INTERVAL 65 DAY), DATE_ADD(CURDATE(), INTERVAL 90 DAY), 12650.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000004', (SELECT user_id FROM sys_user WHERE username='owner002'),
           (SELECT user_id FROM sys_user WHERE username='advisor001'),
           '沪A·45678', '唐 DM-i', '刀片电池 DM2 型', '2022-09-08',
           DATE_SUB(CURDATE(), INTERVAL 2 MONTH), DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
           DATE_ADD(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 56820.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000005', (SELECT user_id FROM sys_user WHERE username='owner003'),
           (SELECT user_id FROM sys_user WHERE username='advisor002'),
           '沪C·56789', '海豚', '刀片电池 C1 型', '2025-01-15',
           DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 70 DAY),
           DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 160 DAY), 6800.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000006', (SELECT user_id FROM sys_user WHERE username='owner003'),
           (SELECT user_id FROM sys_user WHERE username='advisor002'),
           '粤B·67890', '汉 EV', '刀片电池 A2 型', '2021-06-18',
           DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
           DATE_ADD(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 78200.0, 'REPAIRING'
    UNION ALL SELECT 'LC0CE4DB7N0000007', (SELECT user_id FROM sys_user WHERE username='owner004'),
           (SELECT user_id FROM sys_user WHERE username='advisor001'),
           '粤B·78901', '秦 PLUS EV', '刀片电池 A3 型', '2023-04-10',
           DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 20 DAY),
           DATE_ADD(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 80 DAY), 25600.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000008', (SELECT user_id FROM sys_user WHERE username='owner004'),
           (SELECT user_id FROM sys_user WHERE username='advisor002'),
           '粤B·89012', '元 PLUS', '刀片电池 C2 型', '2024-08-22',
           DATE_SUB(CURDATE(), INTERVAL 2 MONTH), DATE_ADD(CURDATE(), INTERVAL 50 DAY),
           DATE_ADD(CURDATE(), INTERVAL 100 DAY), DATE_ADD(CURDATE(), INTERVAL 150 DAY), 9800.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000009', (SELECT user_id FROM sys_user WHERE username='owner005'),
           (SELECT user_id FROM sys_user WHERE username='advisor001'),
           '沪B·90123', '海豹', '刀片电池 B3 型', '2023-12-05',
           DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
           DATE_ADD(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 250 DAY), 31200.0, 'NORMAL'
    UNION ALL SELECT 'LC0CE4DB7N0000010', (SELECT user_id FROM sys_user WHERE username='owner005'),
           (SELECT user_id FROM sys_user WHERE username='advisor002'),
           '沪B·01234', '护卫舰 07', '刀片电池 DM3 型', '2024-03-18',
           DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
           DATE_ADD(CURDATE(), INTERVAL 180 DAY), DATE_ADD(CURDATE(), INTERVAL 220 DAY), 18500.0, 'NORMAL'
) t
ON DUPLICATE KEY UPDATE
    owner_id=VALUES(owner_id), advisor_id=VALUES(advisor_id), license_plate=VALUES(license_plate), model=VALUES(model),
    battery_model=VALUES(battery_model), last_maintenance_date=VALUES(last_maintenance_date),
    next_maintenance_date=VALUES(next_maintenance_date), next_inspection_date=VALUES(next_inspection_date),
    insurance_expire_date=VALUES(insurance_expire_date), current_mileage=VALUES(current_mileage),
    vehicle_status=VALUES(vehicle_status), updated_at=CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 4. 预约（12 条，覆盖全部状态 + 四类服务）
-- -----------------------------------------------------------------------------
INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710001', 'LC0CE4DB7N0000001', v.owner_id, c.center_id, DATE_ADD(NOW(), INTERVAL 1 DAY), 'SCHEDULED_MAINTENANCE', '定期保养与电池检测', 'PENDING'
FROM vehicle v JOIN service_center c ON c.phone='0755-88888888' WHERE v.vin='LC0CE4DB7N0000001'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710001');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710002', 'LC0CE4DB7N0000002', v.owner_id, c.center_id, DATE_ADD(NOW(), INTERVAL 2 DAY), 'ANNUAL_INSPECTION', '车辆年检代办', 'CONFIRMED'
FROM vehicle v JOIN service_center c ON c.phone='0755-88888888' WHERE v.vin='LC0CE4DB7N0000002'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710002');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710003', 'LC0CE4DB7N0000004', v.owner_id, c.center_id, DATE_SUB(NOW(), INTERVAL 3 HOUR), 'FAULT_REPAIR', '制动异响并伴随踏板抖动', 'ARRIVED'
FROM vehicle v JOIN service_center c ON c.phone='021-88887777' WHERE v.vin='LC0CE4DB7N0000004'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710003');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710004', 'LC0CE4DB7N0000006', v.owner_id, c.center_id, DATE_ADD(NOW(), INTERVAL 4 HOUR), 'EMERGENCY_RESCUE', '无法启动，高压系统报警，需拖车', 'PENDING'
FROM vehicle v JOIN service_center c ON c.phone='0755-88888888' WHERE v.vin='LC0CE4DB7N0000006'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710004');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710005', 'LC0CE4DB7N0000005', v.owner_id, c.center_id, DATE_SUB(NOW(), INTERVAL 5 DAY), 'SCHEDULED_MAINTENANCE', '新车首保', 'COMPLETED'
FROM vehicle v JOIN service_center c ON c.phone='020-88886666' WHERE v.vin='LC0CE4DB7N0000005'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710005');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710006', 'LC0CE4DB7N0000001', v.owner_id, c.center_id, DATE_SUB(NOW(), INTERVAL 2 DAY), 'FAULT_REPAIR', '充电缓慢，续航下降', 'CANCELLED'
FROM vehicle v JOIN service_center c ON c.phone='021-88887777' WHERE v.vin='LC0CE4DB7N0000001'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710006');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710007', 'LC0CE4DB7N0000007', v.owner_id, c.center_id, DATE_ADD(NOW(), INTERVAL 3 DAY), 'FAULT_REPAIR', '快充变慢，仪表提示电池温度异常', 'CONFIRMED'
FROM vehicle v JOIN service_center c ON c.phone='0755-88888888' WHERE v.vin='LC0CE4DB7N0000007'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710007');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710008', 'LC0CE4DB7N0000003', v.owner_id, c.center_id, DATE_ADD(NOW(), INTERVAL 5 DAY), 'SCHEDULED_MAINTENANCE', '常规保养', 'PENDING'
FROM vehicle v JOIN service_center c ON c.phone='020-88886666' WHERE v.vin='LC0CE4DB7N0000003'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710008');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710009', 'LC0CE4DB7N0000008', v.owner_id, c.center_id, DATE_SUB(NOW(), INTERVAL 1 DAY), 'FAULT_REPAIR', '仪表偶发报警', 'ARRIVED'
FROM vehicle v JOIN service_center c ON c.phone='0755-88888888' WHERE v.vin='LC0CE4DB7N0000008'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710009');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710010', 'LC0CE4DB7N0000009', v.owner_id, c.center_id, DATE_SUB(NOW(), INTERVAL 10 DAY), 'SCHEDULED_MAINTENANCE', '半年保养', 'COMPLETED'
FROM vehicle v JOIN service_center c ON c.phone='021-88887777' WHERE v.vin='LC0CE4DB7N0000009'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710010');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710011', 'LC0CE4DB7N0000010', v.owner_id, c.center_id, DATE_ADD(NOW(), INTERVAL 7 DAY), 'ANNUAL_INSPECTION', '年检咨询', 'PENDING'
FROM vehicle v JOIN service_center c ON c.phone='021-88887777' WHERE v.vin='LC0CE4DB7N0000010'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710011');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260710012', 'LC0CE4DB7N0000002', v.owner_id, c.center_id, DATE_SUB(NOW(), INTERVAL 30 DAY), 'FAULT_REPAIR', '胎压传感器异常（已维修完成）', 'COMPLETED'
FROM vehicle v JOIN service_center c ON c.phone='0755-88888888' WHERE v.vin='LC0CE4DB7N0000002'
  AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no='APT20260710012');

-- -----------------------------------------------------------------------------
-- 5. 故障登记（8 条，覆盖各状态）
-- -----------------------------------------------------------------------------
INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710001', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor001'),
       '故障现象：其他\n【车主描述】制动异响并伴随踏板抖动，需检查制动片和悬挂\n【顾问判断】接车试车复现异响，初步判断制动片磨损或悬挂松旷，建议举升检查制动盘片与连接件', 'MEDIUM', 'WORK_ORDER_CREATED'
FROM appointment a WHERE a.appointment_no='APT20260710003'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710001');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710002', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor001'),
       '故障现象：仪表报警\n【车主描述】无法启动，高压系统报警，需拖车进站检测\n【顾问判断】远程确认高压系统报警，存在绝缘与启动风险，安排拖车进站并优先做高压安全检测', 'CRITICAL', 'WORK_ORDER_CREATED'
FROM appointment a WHERE a.appointment_no='APT20260710004'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710002');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710003', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor001'),
       '故障现象：电池温度异常\n【车主描述】快充变慢，仪表提示电池温度异常\n【顾问判断】接车时检查充电口温感与冷却回路，疑似热管理模块异常，安排高压检测与快充复现', 'HIGH', 'CLOSED'
FROM appointment a WHERE a.appointment_no='APT20260710007'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710003');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710004', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor002'),
       '故障现象：仪表报警\n【车主描述】仪表偶发报警，需读取故障码\n【顾问判断】仪表报警为间歇性，建议先读取全车故障码并检查网关与低压供电', 'LOW', 'CLOSED'
FROM appointment a WHERE a.appointment_no='APT20260710009'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710004');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710005', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor001'),
       '故障现象：其他\n【车主描述】胎压传感器异常\n【顾问判断】四轮胎压读数异常，逐个检测传感器信号并排除电磁干扰', 'LOW', 'CLOSED'
FROM appointment a WHERE a.appointment_no='APT20260710012'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710005');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710006', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor002'),
       '故障现象：其他\n【车主描述】半年保养及全车检查\n【顾问判断】按保养手册执行全车检查、油液核对与软件版本确认', 'LOW', 'CLOSED'
FROM appointment a WHERE a.appointment_no='APT20260710010'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710006');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710007', NULL, 'LC0CE4DB7N0000001', v.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor001'),
       '故障现象：续航下降\n【车主描述】车辆充电缓慢，续航明显下降\n【顾问判断】结合里程与充电习惯，优先检查电池SOH与压差，建议安排深度检测', 'HIGH', 'REGISTERED'
FROM vehicle v WHERE v.vin='LC0CE4DB7N0000001'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710007');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260710008', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username='advisor002'),
       '故障现象：其他\n【车主描述】新车首保检查\n【顾问判断】首保项目以全车检查、胎压核对与软件升级为主', 'LOW', 'DIAGNOSED'
FROM appointment a WHERE a.appointment_no='APT20260710005'
  AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no='FLT20260710008');

-- -----------------------------------------------------------------------------
-- 6. Agent 诊断
-- -----------------------------------------------------------------------------
INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '建议优先检查制动片磨损、制动液含水率和底盘悬挂连接件。', 'MEDIUM', '制动系统;底盘悬挂;试车复现', 0.86, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no='FLT20260710001'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id=f.fault_id AND d.agent_name='Qwen-Demo');

INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '高压系统异常风险较高，应先做绝缘检测、低压电源检测和 BMS 诊断。', 'HIGH', '高压安全;低压电池;BMS;拖车入站', 0.91, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no='FLT20260710002'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id=f.fault_id AND d.agent_name='Qwen-Demo');

INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '建议检查充电口温度传感器、电池热管理系统和快充继电器。', 'HIGH', '充电系统;热管理;电池压差', 0.88, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no='FLT20260710003'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id=f.fault_id AND d.agent_name='Qwen-Demo');

INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '建议读取整车故障码并检查低压供电与网关通信。', 'LOW', '故障码读取;低压电池;网关', 0.79, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no='FLT20260710004'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id=f.fault_id AND d.agent_name='Qwen-Demo');

INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '首保项目以全车检查和软件升级为主。', 'LOW', '全车检查;软件版本;胎压', 0.82, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no='FLT20260710008'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id=f.fault_id AND d.agent_name='Qwen-Demo');

-- -----------------------------------------------------------------------------
-- 7. 维修工单（6 条：进行中 2 + 已完工 4）
-- -----------------------------------------------------------------------------
INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, created_at, assigned_at, started_at, finished_at)
SELECT 'WO20260710001', f.fault_id, d.diagnosis_id, (SELECT user_id FROM sys_user WHERE username='tech001'),
       'IN_PROGRESS', 300.00, NULL, DATE_SUB(NOW(), INTERVAL 26 HOUR), DATE_SUB(NOW(), INTERVAL 24 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL
FROM fault_record f JOIN agent_diagnosis d ON d.fault_id=f.fault_id
WHERE f.fault_no='FLT20260710001'
  AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no='WO20260710001');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, created_at, assigned_at, started_at, finished_at)
SELECT 'WO20260710002', f.fault_id, d.diagnosis_id, (SELECT user_id FROM sys_user WHERE username='tech002'),
       'PART_WAITING', 500.00, NULL, DATE_SUB(NOW(), INTERVAL 28 HOUR), DATE_SUB(NOW(), INTERVAL 26 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), NULL
FROM fault_record f JOIN agent_diagnosis d ON d.fault_id=f.fault_id
WHERE f.fault_no='FLT20260710002'
  AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no='WO20260710002');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, created_at, assigned_at, started_at, finished_at)
SELECT 'WO20260710003', f.fault_id, d.diagnosis_id, (SELECT user_id FROM sys_user WHERE username='tech001'),
       'COMPLETED', 280.00, '更换充电口温度传感器并清理充电接口，快充恢复正常', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 84 HOUR), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM fault_record f JOIN agent_diagnosis d ON d.fault_id=f.fault_id
WHERE f.fault_no='FLT20260710003'
  AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no='WO20260710003');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, created_at, assigned_at, started_at, finished_at)
SELECT 'WO20260710004', f.fault_id, d.diagnosis_id, (SELECT user_id FROM sys_user WHERE username='tech002'),
       'COMPLETED', 150.00, '清除偶发故障码并紧固低压线束', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 60 HOUR), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM fault_record f JOIN agent_diagnosis d ON d.fault_id=f.fault_id
WHERE f.fault_no='FLT20260710004'
  AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no='WO20260710004');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, created_at, assigned_at, started_at, finished_at)
SELECT 'WO20260710005', f.fault_id, NULL, (SELECT user_id FROM sys_user WHERE username='tech001'),
       'COMPLETED', 200.00, '更换左前胎压传感器并做四轮换位', DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 684 HOUR), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY)
FROM fault_record f WHERE f.fault_no='FLT20260710005'
  AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no='WO20260710005');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, created_at, assigned_at, started_at, finished_at)
SELECT 'WO20260710006', f.fault_id, NULL, (SELECT user_id FROM sys_user WHERE username='tech002'),
       'COMPLETED', 180.00, '完成半年保养、更换空调滤芯', DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 300 HOUR), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)
FROM fault_record f WHERE f.fault_no='FLT20260710006'
  AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no='WO20260710006');

-- -----------------------------------------------------------------------------
-- 8. 备件（12 种）
-- -----------------------------------------------------------------------------
INSERT INTO part (part_no, part_name, category, stock_quantity, warning_threshold, unit, purchase_price, selling_price, status) VALUES
('P-BAT-001', '动力电池模组',     'BATTERY',       5,  3, '套', 12000.00, 15800.00, 'ENABLED'),
('P-MOT-001', '驱动电机轴承',     'MOTOR',        12,  5, '个',   280.00,   450.00, 'ENABLED'),
('P-BRK-001', '制动片套装',       'BRAKE',         8,  5, '套',   180.00,   320.00, 'ENABLED'),
('P-TIR-001', '胎压传感器',       'TIRE',         18,  6, '个',    95.00,   180.00, 'ENABLED'),
('P-LVB-001', '12V 低压电池',     'ELECTRIC',      6,  5, '块',   420.00,   680.00, 'ENABLED'),
('P-HV-001',  '高压互锁线束',     'HIGH_VOLTAGE',  3,  4, '套',   320.00,   520.00, 'ENABLED'),
('P-CHG-001', '充电口温度传感器', 'CHARGING',     15,  5, '个',   120.00,   220.00, 'ENABLED'),
('P-FLT-001', '空调滤芯',         'BODY',         25,  8, '个',    45.00,    88.00, 'ENABLED'),
('P-SEN-001', '轮速传感器',       'ELECTRIC',     10,  4, '个',   160.00,   260.00, 'ENABLED'),
('P-CBL-001', '低压线束总成',     'ELECTRIC',      7,  3, '套',   210.00,   350.00, 'ENABLED'),
('P-BMS-001', 'BMS 通讯模块',     'HIGH_VOLTAGE',  2,  3, '个',   880.00,  1280.00, 'ENABLED'),
('P-OIL-001', '减速器润滑油',     'MOTOR',        20,  6, '桶',    68.00,   120.00, 'ENABLED')
ON DUPLICATE KEY UPDATE
    part_name=VALUES(part_name), stock_quantity=VALUES(stock_quantity),
    warning_threshold=VALUES(warning_threshold), unit=VALUES(unit),
    selling_price=VALUES(selling_price), updated_at=CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 9. 备件领用（覆盖 PROPOSED / APPLIED / APPROVED / USED / REJECTED）
-- -----------------------------------------------------------------------------
-- 注：created_at/approved_at 均相对所属工单的 started_at 偏移，避免脱离工单自身时间线（早于创建/晚于完工）
INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id, NULL, 'PROPOSED',
       DATE_ADD(w.started_at, INTERVAL 20 MINUTE), NULL
FROM work_order w JOIN part p ON p.part_no='P-OIL-001'
WHERE w.work_order_no='WO20260710001'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id AND u.status='PROPOSED');

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id, NULL, 'PROPOSED',
       DATE_ADD(w.started_at, INTERVAL 35 MINUTE), NULL
FROM work_order w JOIN part p ON p.part_no='P-BRK-001'
WHERE w.work_order_no='WO20260710001'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id AND u.status='PROPOSED');

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id, NULL, 'APPLIED',
       DATE_ADD(w.started_at, INTERVAL 20 MINUTE), NULL
FROM work_order w JOIN part p ON p.part_no='P-HV-001'
WHERE w.work_order_no='WO20260710002'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id,
       (SELECT user_id FROM sys_user WHERE username='part001'), 'REJECTED',
       DATE_ADD(w.started_at, INTERVAL 40 MINUTE), DATE_ADD(w.started_at, INTERVAL 100 MINUTE)
FROM work_order w JOIN part p ON p.part_no='P-BMS-001'
WHERE w.work_order_no='WO20260710002'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id,
       (SELECT user_id FROM sys_user WHERE username='part001'), 'USED',
       DATE_ADD(w.started_at, INTERVAL 2 HOUR), DATE_ADD(w.started_at, INTERVAL 3 HOUR)
FROM work_order w JOIN part p ON p.part_no='P-CHG-001'
WHERE w.work_order_no='WO20260710003'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id,
       (SELECT user_id FROM sys_user WHERE username='part001'), 'USED',
       DATE_ADD(w.started_at, INTERVAL 2 HOUR), DATE_ADD(w.started_at, INTERVAL 3 HOUR)
FROM work_order w JOIN part p ON p.part_no='P-CBL-001'
WHERE w.work_order_no='WO20260710004'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id,
       (SELECT user_id FROM sys_user WHERE username='part001'), 'USED',
       DATE_ADD(w.started_at, INTERVAL 2 HOUR), DATE_ADD(w.started_at, INTERVAL 3 HOUR)
FROM work_order w JOIN part p ON p.part_no='P-TIR-001'
WHERE w.work_order_no='WO20260710005'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id,
       (SELECT user_id FROM sys_user WHERE username='part001'), 'USED',
       DATE_ADD(w.started_at, INTERVAL 2 HOUR), DATE_ADD(w.started_at, INTERVAL 3 HOUR)
FROM work_order w JOIN part p ON p.part_no='P-FLT-001'
WHERE w.work_order_no='WO20260710006'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, created_at, approved_at)
SELECT w.work_order_id, p.part_id, 2, p.selling_price, w.technician_id,
       (SELECT user_id FROM sys_user WHERE username='part001'), 'USED',
       DATE_ADD(w.started_at, INTERVAL 150 MINUTE), DATE_ADD(w.started_at, INTERVAL 210 MINUTE)
FROM work_order w JOIN part p ON p.part_no='P-OIL-001'
WHERE w.work_order_no='WO20260710006'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id=w.work_order_id AND u.part_id=p.part_id AND u.status='USED');

-- -----------------------------------------------------------------------------
-- 10. 结算单（仅已完工工单，覆盖 4 种审核/支付状态）
-- -----------------------------------------------------------------------------
INSERT INTO settlement (settlement_no, work_order_id, labor_amount, part_amount, warranty_amount, total_amount,
                        payment_status, manager_status, approved_by, approved_at, paid_at)
SELECT 'SET20260710001', w.work_order_id, 280.00, 220.00, 50.00, 450.00,
       'PAID', 'APPROVED', (SELECT user_id FROM sys_user WHERE username='manager001'), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM work_order w WHERE w.work_order_no='WO20260710003'
  AND NOT EXISTS (SELECT 1 FROM settlement WHERE settlement_no='SET20260710001');

INSERT INTO settlement (settlement_no, work_order_id, labor_amount, part_amount, warranty_amount, total_amount,
                        payment_status, manager_status, approved_by, approved_at, paid_at)
SELECT 'SET20260710002', w.work_order_id, 150.00, 350.00, 0.00, 500.00,
       'UNPAID', 'APPROVED', (SELECT user_id FROM sys_user WHERE username='manager001'), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL
FROM work_order w WHERE w.work_order_no='WO20260710004'
  AND NOT EXISTS (SELECT 1 FROM settlement WHERE settlement_no='SET20260710002');

INSERT INTO settlement (settlement_no, work_order_id, labor_amount, part_amount, warranty_amount, total_amount,
                        payment_status, manager_status, approved_by, approved_at, paid_at)
SELECT 'SET20260710003', w.work_order_id, 200.00, 180.00, 0.00, 380.00,
       'UNPAID', 'PENDING_APPROVAL', NULL, NULL, NULL
FROM work_order w WHERE w.work_order_no='WO20260710005'
  AND NOT EXISTS (SELECT 1 FROM settlement WHERE settlement_no='SET20260710003');

INSERT INTO settlement (settlement_no, work_order_id, labor_amount, part_amount, warranty_amount, total_amount,
                        payment_status, manager_status, approved_by, approved_at, paid_at)
SELECT 'SET20260710004', w.work_order_id, 180.00, 328.00, 100.00, 408.00,
       'UNPAID', 'REJECTED', (SELECT user_id FROM sys_user WHERE username='manager001'), DATE_SUB(NOW(), INTERVAL 10 DAY), NULL
FROM work_order w WHERE w.work_order_no='WO20260710006'
  AND NOT EXISTS (SELECT 1 FROM settlement WHERE settlement_no='SET20260710004');

-- -----------------------------------------------------------------------------
-- 11. 电池健康（多时间点，支撑趋势图）
-- -----------------------------------------------------------------------------
INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT v.vin, v.soh, v.cycles, v.max_t, v.min_t, v.vdiff, v.wlevel, v.dtime FROM (
    SELECT 'LC0CE4DB7N0000001' vin, 78.50 soh, 380 cycles, 40.2 max_t, 18.5 min_t, 0.072 vdiff, 'WARNING' wlevel, DATE_SUB(NOW(), INTERVAL 90 DAY) dtime UNION ALL
    SELECT 'LC0CE4DB7N0000001', 75.20, 400, 41.8, 18.0, 0.079, 'WARNING', DATE_SUB(NOW(), INTERVAL 45 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000001', 72.50, 420, 42.5, 18.2, 0.085, 'DANGER',  DATE_SUB(NOW(), INTERVAL 3 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000002', 86.00, 470, 41.0, 18.8, 0.065, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 60 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000002', 83.50, 490, 42.5, 18.5, 0.078, 'WARNING', DATE_SUB(NOW(), INTERVAL 30 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000002', 81.20, 510, 43.2, 18.4, 0.092, 'WARNING', DATE_SUB(NOW(), INTERVAL 2 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000006', 72.00, 820, 52.0, 17.8, 0.125, 'DANGER',  DATE_SUB(NOW(), INTERVAL 60 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000006', 69.00, 850, 55.5, 17.5, 0.135, 'DANGER',  DATE_SUB(NOW(), INTERVAL 30 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000006', 66.50, 880, 57.2, 17.6, 0.142, 'DANGER',  DATE_SUB(NOW(), INTERVAL 1 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000007', 91.00, 220, 36.0, 20.5, 0.030, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 40 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000007', 88.50, 245, 38.5, 20.0, 0.042, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 10 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000003', 93.50, 120, 35.5, 21.0, 0.028, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 50 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000003', 91.80, 145, 36.2, 20.8, 0.033, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 5 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000004', 74.00, 610, 46.5, 19.0, 0.098, 'WARNING', DATE_SUB(NOW(), INTERVAL 40 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000004', 68.50, 640, 49.8, 18.7, 0.112, 'DANGER',  DATE_SUB(NOW(), INTERVAL 4 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000005', 97.80, 60,  33.0, 22.0, 0.015, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 20 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000005', 96.50, 78,  34.0, 21.5, 0.018, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 2 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000008', 95.20, 90,  34.5, 21.2, 0.020, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 15 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000008', 94.00, 105, 35.0, 21.0, 0.024, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 1 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000009', 87.60, 350, 41.5, 19.5, 0.068, 'NORMAL',  DATE_SUB(NOW(), INTERVAL 35 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000009', 85.30, 375, 42.8, 19.2, 0.075, 'WARNING', DATE_SUB(NOW(), INTERVAL 3 DAY)  UNION ALL
    SELECT 'LC0CE4DB7N0000010', 82.40, 290, 44.0, 19.8, 0.088, 'WARNING', DATE_SUB(NOW(), INTERVAL 25 DAY) UNION ALL
    SELECT 'LC0CE4DB7N0000010', 80.10, 310, 44.8, 19.6, 0.094, 'WARNING', DATE_SUB(NOW(), INTERVAL 6 DAY)
) v
WHERE NOT EXISTS (
    SELECT 1 FROM battery_health_record b WHERE b.vin=v.vin AND b.detect_time=v.dtime
);

-- -----------------------------------------------------------------------------
-- 12. 车辆健康快照（每车 1 条最新快照）
-- -----------------------------------------------------------------------------
INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT v.vin, v.score, v.lvl, v.summary, v.suggestion, DATE_SUB(NOW(), INTERVAL 1 DAY) FROM (
    SELECT 'LC0CE4DB7N0000001' vin, 72 score, 'DANGER'  lvl, '电池健康风险较高' summary, '优先预约电池检测' suggestion UNION ALL
    SELECT 'LC0CE4DB7N0000002', 78, 'WARNING', '保养逾期且里程较高', '尽快预约保养和制动检查' UNION ALL
    SELECT 'LC0CE4DB7N0000003', 94, 'NORMAL',  '车辆整体健康良好', '按期保养即可' UNION ALL
    SELECT 'LC0CE4DB7N0000004', 69, 'DANGER',  '制动和电池需复检', '优先处理制动异响' UNION ALL
    SELECT 'LC0CE4DB7N0000005', 97, 'NORMAL',  '新车健康状态优秀', '保持常规使用习惯' UNION ALL
    SELECT 'LC0CE4DB7N0000006', 58, 'DANGER',  '高压电池均需紧急处理', '立即拖车入站检测' UNION ALL
    SELECT 'LC0CE4DB7N0000007', 85, 'NORMAL',  '快充变慢已修复，当前状态良好', '继续观察充电表现' UNION ALL
    SELECT 'LC0CE4DB7N0000008', 92, 'NORMAL',  '新车状态良好，各项指标正常', '保持常规使用习惯' UNION ALL
    SELECT 'LC0CE4DB7N0000009', 88, 'NORMAL',  '半年保养后状态稳定', '下次保养前无需额外处理' UNION ALL
    SELECT 'LC0CE4DB7N0000010', 75, 'WARNING', '电池压差偏高，建议复检', '预约充电系统检测'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM vehicle_health_snapshot s WHERE s.vin=v.vin AND s.detect_time=DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 健康检查项（仅为 4 辆代表性车辆插入，避免脚本过长）
INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'LC0CE4DB7N0000001' vin, 'BATTERY' item_type, '动力电池' item_name, 'DANGER' level, '72.5% SOH' metric_value, 'SOH 偏低' description, '预约电池深度检测' action_suggestion UNION ALL
    SELECT 'LC0CE4DB7N0000001', 'MAINTENANCE', '定期保养', 'WARNING', '10 天后到期', '保养临近', '预约定期保养' UNION ALL
    SELECT 'LC0CE4DB7N0000001', 'INSPECTION_INSURANCE', '年检/保险', 'WARNING', '25 天后到期', '年检临近', '预约年检代办' UNION ALL
    SELECT 'LC0CE4DB7N0000006', 'BATTERY', '动力电池', 'DANGER', '66.5% SOH', 'SOH 低且高压报警', '立即拖车入站' UNION ALL
    SELECT 'LC0CE4DB7N0000006', 'CHARGING_SYSTEM', '充电系统', 'DANGER', '0.142V 压差', '压差超阈值', '停止高负荷使用' UNION ALL
    SELECT 'LC0CE4DB7N0000006', 'MAINTENANCE', '定期保养', 'DANGER', '保养逾期', '需立即保养', '立即预约保养'
) x ON x.vin = s.vin
WHERE s.detect_time = DATE_SUB(NOW(), INTERVAL 1 DAY)
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id=s.snapshot_id AND i.item_type=x.item_type);

-- -----------------------------------------------------------------------------
-- 13. 车主提醒（种子数据，补充运行时自动生成）
-- -----------------------------------------------------------------------------
INSERT INTO vehicle_reminder (reminder_no, vin, owner_id, reminder_type, level, title, content, due_time, status)
SELECT v.reminder_no, v.vin, veh.owner_id, v.rtype, v.lvl, v.title, v.content, v.due, 'UNREAD'
FROM (
    SELECT 'RM20260710001' reminder_no, 'LC0CE4DB7N0000001' vin, 'MAINTENANCE' rtype, 'WARNING' lvl,
           '保养即将到期' title, '您的车辆即将到达保养周期，建议尽快预约。' content, DATE_ADD(NOW(), INTERVAL 10 DAY) due UNION ALL
    SELECT 'RM20260710002', 'LC0CE4DB7N0000002', 'MAINTENANCE', 'DANGER', '保养已逾期',
           '车辆保养已逾期，请尽快进店保养。', DATE_SUB(NOW(), INTERVAL 5 DAY) UNION ALL
    SELECT 'RM20260710003', 'LC0CE4DB7N0000006', 'BATTERY', 'DANGER', '电池健康异常',
           '电池 SOH 偏低且存在高压风险，请尽快预约检测。', DATE_ADD(NOW(), INTERVAL 1 DAY) UNION ALL
    SELECT 'RM20260710004', 'LC0CE4DB7N0000004', 'INSPECTION', 'WARNING', '年检临近',
           '车辆年检即将到期，可预约代办服务。', DATE_ADD(NOW(), INTERVAL 15 DAY) UNION ALL
    SELECT 'RM20260710005', 'LC0CE4DB7N0000003', 'INSURANCE', 'INFO', '保险即将到期',
           '车辆保险即将到期，请及时续保。', DATE_ADD(NOW(), INTERVAL 60 DAY) UNION ALL
    SELECT 'RM20260710006', 'LC0CE4DB7N0000007', 'MAINTENANCE', 'INFO', '维修已完成',
           '上次快充故障已修复，欢迎评价本次服务。', DATE_SUB(NOW(), INTERVAL 2 DAY)
) v
JOIN vehicle veh ON veh.vin = v.vin
WHERE NOT EXISTS (SELECT 1 FROM vehicle_reminder r WHERE r.reminder_no = v.reminder_no);

-- -----------------------------------------------------------------------------
-- 14. 操作日志（督办 / 完工 / 车辆维修记录 / 电池提醒）
-- -----------------------------------------------------------------------------
INSERT INTO operation_log (business_type, business_id, action, operator_id, detail, created_at)
SELECT * FROM (
    SELECT 'WORK_ORDER' bt, w.work_order_id bid, 'SUPERVISE' act, (SELECT user_id FROM sys_user WHERE username='manager001') op,
           CONCAT('督办工单[', w.work_order_no, ']，当前状态=', w.status) detail, DATE_SUB(NOW(), INTERVAL 1 HOUR) cat
    FROM work_order w WHERE w.work_order_no='WO20260710001'
    UNION ALL
    SELECT 'WORK_ORDER', w.work_order_id, 'COMPLETE', (SELECT user_id FROM sys_user WHERE username='tech001'),
           CONCAT('工单[', w.work_order_no, ']完工，结算单已生成'), DATE_SUB(NOW(), INTERVAL 2 DAY)
    FROM work_order w WHERE w.work_order_no='WO20260710003'
    UNION ALL
    SELECT 'VEHICLE', w.work_order_id, 'REPAIR_RECORD_UPDATED', (SELECT user_id FROM sys_user WHERE username='tech001'),
           '车辆[LC0CE4DB7N0000007]维修记录已更新，状态=NORMAL', DATE_SUB(NOW(), INTERVAL 2 DAY)
    FROM work_order w WHERE w.work_order_no='WO20260710003'
    UNION ALL
    SELECT 'WORK_ORDER', w.work_order_id, 'COMPLETE', (SELECT user_id FROM sys_user WHERE username='tech002'),
           CONCAT('工单[', w.work_order_no, ']完工'), DATE_SUB(NOW(), INTERVAL 1 DAY)
    FROM work_order w WHERE w.work_order_no='WO20260710004'
    UNION ALL
    SELECT 'BATTERY', 0, 'REMIND_OWNER', (SELECT user_id FROM sys_user WHERE username='manager001'),
           '已提醒车主关注车辆 LC0CE4DB7N0000006 电池健康', DATE_SUB(NOW(), INTERVAL 6 HOUR)
    UNION ALL
    SELECT 'SETTLEMENT', s.settlement_id, 'APPROVE', (SELECT user_id FROM sys_user WHERE username='manager001'),
           CONCAT('结算单[', s.settlement_no, ']审核通过'), DATE_SUB(NOW(), INTERVAL 2 DAY)
    FROM settlement s WHERE s.settlement_no='SET20260710001'
    UNION ALL
    SELECT 'SETTLEMENT', s.settlement_id, 'REJECT', (SELECT user_id FROM sys_user WHERE username='manager001'),
           CONCAT('结算单[', s.settlement_no, ']审核驳回，质保抵扣金额需复核'), DATE_SUB(NOW(), INTERVAL 10 DAY)
    FROM settlement s WHERE s.settlement_no='SET20260710004'
) logs
WHERE NOT EXISTS (
    SELECT 1 FROM operation_log ol
    WHERE ol.business_type=logs.bt AND ol.business_id=logs.bid AND ol.action=logs.act AND ol.detail=logs.detail
);
