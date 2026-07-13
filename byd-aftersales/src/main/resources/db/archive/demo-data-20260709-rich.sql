-- 富演示数据：覆盖多车主、多门店、多服务类型、多工单状态和多车辆健康风险
USE byd_aftersales;

INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES
('owner002', '12345678', '陈宇', '13800000007', 'OWNER', 'ENABLED'),
('owner003', '12345678', '刘芳', '13800000008', 'OWNER', 'ENABLED'),
('advisor002', '12345678', '孙顾问', '13800000009', 'ADVISOR', 'ENABLED'),
('tech002', '12345678', '周技师', '13800000010', 'TECHNICIAN', 'ENABLED')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    real_name = VALUES(real_name),
    role = VALUES(role),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_center (center_name, city, address, phone, status)
SELECT '比亚迪广州天河售后服务中心', '广州', '广州市天河区示范大道 88 号', '020-88886666', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM service_center WHERE phone = '020-88886666');

INSERT INTO service_center (center_name, city, address, phone, status)
SELECT '比亚迪上海浦东售后服务中心', '上海', '上海市浦东新区示范路 66 号', '021-88887777', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM service_center WHERE phone = '021-88887777');

UPDATE service_center
SET center_name = '比亚迪广州天河售后服务中心',
    city = '广州',
    address = '广州市天河区示范大道 88 号'
WHERE center_name = 'BYD Guangzhou Tianhe Service Center';

UPDATE service_center
SET center_name = '比亚迪上海浦东售后服务中心',
    city = '上海',
    address = '上海市浦东新区示范路 66 号'
WHERE center_name = 'BYD Shanghai Pudong Service Center';

INSERT INTO vehicle
    (vin, owner_id, license_plate, model, battery_model, purchase_date,
     last_maintenance_date, next_maintenance_date, next_inspection_date, insurance_expire_date,
     current_mileage, vehicle_status)
VALUES
('LC0CE4DB7N0000002', (SELECT user_id FROM sys_user WHERE username = 'owner001'), '粤B·23456', '海狮 07 EV', '刀片电池 B2 型', '2023-11-12',
 DATE_SUB(CURDATE(), INTERVAL 6 MONTH), DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), DATE_ADD(CURDATE(), INTERVAL 32 DAY), 42880.0, 'NORMAL'),
('LC0CE4DB7N0000003', (SELECT user_id FROM sys_user WHERE username = 'owner002'), '粤A·34567', '宋 PLUS DM-i', '刀片电池 DM1 型', '2024-02-20',
 DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 35 DAY), DATE_ADD(CURDATE(), INTERVAL 65 DAY), DATE_ADD(CURDATE(), INTERVAL 90 DAY), 12650.0, 'NORMAL'),
('LC0CE4DB7N0000004', (SELECT user_id FROM sys_user WHERE username = 'owner002'), '沪A·45678', '唐 DM-i', '刀片电池 DM2 型', '2022-09-08',
 DATE_SUB(CURDATE(), INTERVAL 8 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 56820.0, 'REPAIRING'),
('LC0CE4DB7N0000005', (SELECT user_id FROM sys_user WHERE username = 'owner003'), '沪C·56789', '海豚', '刀片电池 C1 型', '2025-01-15',
 DATE_SUB(CURDATE(), INTERVAL 2 MONTH), DATE_ADD(CURDATE(), INTERVAL 70 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 160 DAY), 6800.0, 'NORMAL'),
('LC0CE4DB7N0000006', (SELECT user_id FROM sys_user WHERE username = 'owner003'), '粤B·67890', '汉 EV', '刀片电池 A2 型', '2021-06-18',
 DATE_SUB(CURDATE(), INTERVAL 10 MONTH), DATE_SUB(CURDATE(), INTERVAL 18 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 78200.0, 'REPAIRING')
ON DUPLICATE KEY UPDATE
    owner_id = VALUES(owner_id),
    license_plate = VALUES(license_plate),
    model = VALUES(model),
    battery_model = VALUES(battery_model),
    last_maintenance_date = VALUES(last_maintenance_date),
    next_maintenance_date = VALUES(next_maintenance_date),
    next_inspection_date = VALUES(next_inspection_date),
    insurance_expire_date = VALUES(insurance_expire_date),
    current_mileage = VALUES(current_mileage),
    vehicle_status = VALUES(vehicle_status),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260709001', 'LC0CE4DB7N0000002', v.owner_id, (SELECT center_id FROM service_center WHERE phone = '0755-88888888' ORDER BY center_id LIMIT 1), DATE_ADD(NOW(), INTERVAL 1 DAY), 'SCHEDULED_MAINTENANCE', '下次保养已逾期，希望做整车检查和轮胎换位', 'CONFIRMED'
FROM vehicle v WHERE v.vin = 'LC0CE4DB7N0000002' AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20260709001');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260709002', 'LC0CE4DB7N0000003', v.owner_id, (SELECT center_id FROM service_center WHERE phone = '020-88886666' ORDER BY center_id LIMIT 1), DATE_ADD(NOW(), INTERVAL 3 DAY), 'ANNUAL_INSPECTION', '年检资料咨询，预约门店协助代办', 'PENDING'
FROM vehicle v WHERE v.vin = 'LC0CE4DB7N0000003' AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20260709002');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260709003', 'LC0CE4DB7N0000004', v.owner_id, (SELECT center_id FROM service_center WHERE phone = '021-88887777' ORDER BY center_id LIMIT 1), DATE_ADD(NOW(), INTERVAL 2 DAY), 'FAULT_REPAIR', '车辆低速异响，制动踏板反馈异常', 'ARRIVED'
FROM vehicle v WHERE v.vin = 'LC0CE4DB7N0000004' AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20260709003');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260709004', 'LC0CE4DB7N0000005', v.owner_id, (SELECT center_id FROM service_center WHERE phone = '020-88886666' ORDER BY center_id LIMIT 1), DATE_ADD(NOW(), INTERVAL 6 DAY), 'SCHEDULED_MAINTENANCE', '新车首保和车机系统检查', 'COMPLETED'
FROM vehicle v WHERE v.vin = 'LC0CE4DB7N0000005' AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20260709004');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260709005', 'LC0CE4DB7N0000006', v.owner_id, (SELECT center_id FROM service_center WHERE phone = '0755-88888888' ORDER BY center_id LIMIT 1), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'EMERGENCY_RESCUE', '车辆无法启动，仪表提示高压系统异常，需要拖车救援', 'PENDING'
FROM vehicle v WHERE v.vin = 'LC0CE4DB7N0000006' AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20260709005');

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
SELECT 'APT20260709006', 'LC0CE4DB7N0000001', v.owner_id, (SELECT center_id FROM service_center WHERE phone = '021-88887777' ORDER BY center_id LIMIT 1), DATE_SUB(NOW(), INTERVAL 2 DAY), 'FAULT_REPAIR', '临时取消的车辆异响检查预约', 'CANCELLED'
FROM vehicle v WHERE v.vin = 'LC0CE4DB7N0000001' AND NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20260709006');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260709001', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username = 'advisor001'), '制动异响并伴随踏板抖动，需检查制动片和悬挂', 'MEDIUM', 'DIAGNOSED'
FROM appointment a WHERE a.appointment_no = 'APT20260709003' AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no = 'FLT20260709001');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20260709002', a.appointment_id, a.vin, a.owner_id, (SELECT user_id FROM sys_user WHERE username = 'advisor002'), '无法启动，高压系统报警，需拖车进站检测', 'CRITICAL', 'REGISTERED'
FROM appointment a WHERE a.appointment_no = 'APT20260709005' AND NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no = 'FLT20260709002');

INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '建议优先检查制动片磨损、制动液含水率和底盘悬挂连接件。', 'MEDIUM', '制动系统;底盘悬挂;试车复现', 0.86, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no = 'FLT20260709001'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id = f.fault_id AND d.agent_name = 'Qwen-Demo');

INSERT INTO agent_diagnosis (fault_id, input_text, diagnosis_suggestion, risk_level, recommended_checks, confidence_score, agent_name, raw_response)
SELECT f.fault_id, f.fault_description, '高压系统异常风险较高，应先做绝缘检测、低压电源检测和电池管理系统诊断。', 'HIGH', '高压安全;低压电池;BMS;拖车入站', 0.91, 'Qwen-Demo', '{}'
FROM fault_record f WHERE f.fault_no = 'FLT20260709002'
  AND NOT EXISTS (SELECT 1 FROM agent_diagnosis d WHERE d.fault_id = f.fault_id AND d.agent_name = 'Qwen-Demo');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, started_at, finished_at)
SELECT 'WO20260709001', f.fault_id, d.diagnosis_id, (SELECT user_id FROM sys_user WHERE username = 'tech001'), 'IN_PROGRESS', 300.00, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL
FROM fault_record f JOIN agent_diagnosis d ON d.fault_id = f.fault_id
WHERE f.fault_no = 'FLT20260709001' AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no = 'WO20260709001');

INSERT INTO work_order (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost, repair_result, started_at, finished_at)
SELECT 'WO20260709002', f.fault_id, d.diagnosis_id, (SELECT user_id FROM sys_user WHERE username = 'tech002'), 'PART_WAITING', 500.00, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL
FROM fault_record f JOIN agent_diagnosis d ON d.fault_id = f.fault_id
WHERE f.fault_no = 'FLT20260709002' AND NOT EXISTS (SELECT 1 FROM work_order WHERE work_order_no = 'WO20260709002');

UPDATE fault_record f
JOIN work_order w ON w.fault_id = f.fault_id AND w.deleted = 0
SET f.status = 'WORK_ORDER_CREATED'
WHERE f.fault_no IN ('FLT20260709001', 'FLT20260709002')
  AND f.status <> 'WORK_ORDER_CREATED';

INSERT INTO part (part_no, part_name, category, stock_quantity, warning_threshold, purchase_price, selling_price, status)
VALUES
('P-TIR-001', '胎压传感器', 'TIRE', 18, 6, 95.00, 180.00, 'ENABLED'),
('P-LVB-001', '12V 低压电池', 'ELECTRIC', 4, 5, 420.00, 680.00, 'ENABLED'),
('P-HV-001', '高压互锁线束', 'HIGH_VOLTAGE', 3, 4, 320.00, 520.00, 'ENABLED')
ON DUPLICATE KEY UPDATE
    stock_quantity = VALUES(stock_quantity),
    warning_threshold = VALUES(warning_threshold),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id, (SELECT user_id FROM sys_user WHERE username = 'part001'), 'APPROVED', NOW()
FROM work_order w JOIN part p ON p.part_no = 'P-BRK-001'
WHERE w.work_order_no = 'WO20260709001'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id = w.work_order_id AND u.part_id = p.part_id);

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_price, technician_id, approved_by, status, approved_at)
SELECT w.work_order_id, p.part_id, 1, p.selling_price, w.technician_id, NULL, 'APPLIED', NULL
FROM work_order w JOIN part p ON p.part_no = 'P-HV-001'
WHERE w.work_order_no = 'WO20260709002'
  AND NOT EXISTS (SELECT 1 FROM part_usage u WHERE u.work_order_id = w.work_order_id AND u.part_id = p.part_id);

INSERT INTO settlement (settlement_no, work_order_id, labor_amount, part_amount, warranty_amount, total_amount, payment_status, manager_status, approved_by, approved_at)
SELECT 'SET20260709001', w.work_order_id, 300.00, 320.00, 0.00, 620.00, 'UNPAID', 'APPROVED', (SELECT user_id FROM sys_user WHERE username = 'manager001'), NOW()
FROM work_order w WHERE w.work_order_no = 'WO20260709001'
  AND NOT EXISTS (SELECT 1 FROM settlement WHERE settlement_no = 'SET20260709001');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT 'LC0CE4DB7N0000002', 81.20, 510, 43.2, 18.4, 0.092, 'WARNING', '2026-07-07 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM battery_health_record WHERE vin = 'LC0CE4DB7N0000002' AND detect_time = '2026-07-07 09:00:00');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT 'LC0CE4DB7N0000003', 93.60, 180, 36.4, 20.1, 0.035, 'NORMAL', '2026-07-06 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM battery_health_record WHERE vin = 'LC0CE4DB7N0000003' AND detect_time = '2026-07-06 09:00:00');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT 'LC0CE4DB7N0000004', 78.40, 620, 48.8, 18.0, 0.118, 'WARNING', '2026-07-08 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM battery_health_record WHERE vin = 'LC0CE4DB7N0000004' AND detect_time = '2026-07-08 09:00:00');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT 'LC0CE4DB7N0000005', 96.80, 90, 34.1, 21.0, 0.022, 'NORMAL', '2026-07-04 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM battery_health_record WHERE vin = 'LC0CE4DB7N0000005' AND detect_time = '2026-07-04 09:00:00');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT 'LC0CE4DB7N0000006', 66.50, 880, 57.2, 17.6, 0.142, 'DANGER', '2026-07-09 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM battery_health_record WHERE vin = 'LC0CE4DB7N0000006' AND detect_time = '2026-07-09 08:00:00');

INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT 'LC0CE4DB7N0000001', 72, 'DANGER', '电池健康风险较高，建议尽快处理', '优先预约电池检测，并检查热管理和充电系统。', '2026-07-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_health_snapshot WHERE vin = 'LC0CE4DB7N0000001' AND detect_time = '2026-07-08 10:00:00');

INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT 'LC0CE4DB7N0000002', 78, 'WARNING', '保养逾期且里程较高', '建议尽快预约定期保养、轮胎检查和制动检查。', '2026-07-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_health_snapshot WHERE vin = 'LC0CE4DB7N0000002' AND detect_time = '2026-07-08 10:00:00');

INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT 'LC0CE4DB7N0000003', 94, 'NORMAL', '车辆整体健康良好', '按期保养，年检和保险暂无临近风险。', '2026-07-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_health_snapshot WHERE vin = 'LC0CE4DB7N0000003' AND detect_time = '2026-07-08 10:00:00');

INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT 'LC0CE4DB7N0000004', 69, 'DANGER', '年检已逾期，制动和电池需复检', '优先处理年检逾期和制动异响，再进行电池均衡检测。', '2026-07-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_health_snapshot WHERE vin = 'LC0CE4DB7N0000004' AND detect_time = '2026-07-08 10:00:00');

INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT 'LC0CE4DB7N0000005', 97, 'NORMAL', '新车健康状态优秀', '保持常规使用习惯，到期首保即可。', '2026-07-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_health_snapshot WHERE vin = 'LC0CE4DB7N0000005' AND detect_time = '2026-07-08 10:00:00');

INSERT INTO vehicle_health_snapshot (vin, health_score, overall_level, summary, suggestion, detect_time)
SELECT 'LC0CE4DB7N0000006', 58, 'DANGER', '高压、电池、保险和保养均需紧急处理', '建议立即安排拖车入站，先做高压安全和电池管理系统诊断。', '2026-07-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_health_snapshot WHERE vin = 'LC0CE4DB7N0000006' AND detect_time = '2026-07-08 10:00:00');

INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'BATTERY' item_type, '动力电池' item_name, 'DANGER' level, '72.5% SOH' metric_value, 'SOH 偏低且压差接近风险阈值。' description, '预约电池深度检测' action_suggestion UNION ALL
    SELECT 'TIRE_PRESSURE', '轮胎/胎压', 'NORMAL', '正常', '暂无胎压异常。', '下次保养复查' UNION ALL
    SELECT 'BRAKE', '制动系统', 'NORMAL', '正常', '暂无制动系统预警。', '保持常规检查' UNION ALL
    SELECT 'LOW_VOLTAGE_BATTERY', '低压电池', 'NORMAL', '正常', '低压供电正常。', '无需处理' UNION ALL
    SELECT 'CHARGING_SYSTEM', '充电系统', 'WARNING', '0.085V 压差', '充电压差偏高，建议复检。', '检查充电系统' UNION ALL
    SELECT 'THERMAL_MANAGEMENT', '热管理/高压安全', 'NORMAL', '42.5°C', '温度正常。', '无需处理' UNION ALL
    SELECT 'MAINTENANCE', '定期保养', 'WARNING', '10 天后到期', '定期保养临近。', '预约定期保养' UNION ALL
    SELECT 'INSPECTION_INSURANCE', '年检/保险', 'WARNING', '年检 25 天后到期', '年检临近。', '预约年检代办'
) x
WHERE s.vin = 'LC0CE4DB7N0000001'
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id = s.snapshot_id);

INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'BATTERY' item_type, '动力电池' item_name, 'WARNING' level, '81.2% SOH' metric_value, '电池接近预警区间。' description, '近期复检电池' action_suggestion UNION ALL
    SELECT 'TIRE_PRESSURE', '轮胎/胎压', 'WARNING', '42880 km', '里程较高，建议检查胎压和胎纹。', '预约轮胎检查' UNION ALL
    SELECT 'BRAKE', '制动系统', 'WARNING', '建议检查', '里程达到制动检查区间。', '检查制动片' UNION ALL
    SELECT 'LOW_VOLTAGE_BATTERY', '低压电池', 'WARNING', '510 次循环', '循环次数较高。', '检查 12V 电池' UNION ALL
    SELECT 'CHARGING_SYSTEM', '充电系统', 'WARNING', '0.092V 压差', '压差偏高。', '检查充电系统' UNION ALL
    SELECT 'THERMAL_MANAGEMENT', '热管理/高压安全', 'NORMAL', '43.2°C', '热管理正常。', '无需处理' UNION ALL
    SELECT 'MAINTENANCE', '定期保养', 'DANGER', '已逾期 5 天', '保养已逾期。', '立即预约保养' UNION ALL
    SELECT 'INSPECTION_INSURANCE', '年检/保险', 'WARNING', '年检 18 天后到期', '年检临近。', '预约年检代办'
) x
WHERE s.vin = 'LC0CE4DB7N0000002'
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id = s.snapshot_id);

INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'BATTERY' item_type, '动力电池' item_name, 'NORMAL' level, '93.6% SOH' metric_value, '电池健康良好。' description, '保持良好充电习惯' action_suggestion UNION ALL
    SELECT 'TIRE_PRESSURE', '轮胎/胎压', 'NORMAL', '12650 km', '轮胎风险低。', '下次保养复查' UNION ALL
    SELECT 'BRAKE', '制动系统', 'NORMAL', '正常', '暂无制动风险。', '无需处理' UNION ALL
    SELECT 'LOW_VOLTAGE_BATTERY', '低压电池', 'NORMAL', '正常', '低压供电正常。', '无需处理' UNION ALL
    SELECT 'CHARGING_SYSTEM', '充电系统', 'NORMAL', '0.035V 压差', '充电状态正常。', '无需处理' UNION ALL
    SELECT 'THERMAL_MANAGEMENT', '热管理/高压安全', 'NORMAL', '36.4°C', '温度正常。', '无需处理' UNION ALL
    SELECT 'MAINTENANCE', '定期保养', 'NORMAL', '35 天后到期', '按计划保养。', '按期保养' UNION ALL
    SELECT 'INSPECTION_INSURANCE', '年检/保险', 'NORMAL', '年检 65 天后到期', '暂无临近风险。', '无需处理'
) x
WHERE s.vin = 'LC0CE4DB7N0000003'
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id = s.snapshot_id);

INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'BATTERY' item_type, '动力电池' item_name, 'WARNING' level, '78.4% SOH' metric_value, '电池已进入预警区间。' description, '预约电池检测' action_suggestion UNION ALL
    SELECT 'TIRE_PRESSURE', '轮胎/胎压', 'WARNING', '56820 km', '里程高，轮胎磨损风险增加。', '检查胎压胎纹' UNION ALL
    SELECT 'BRAKE', '制动系统', 'WARNING', '制动异响', '车主反馈低速制动异响。', '优先检查制动系统' UNION ALL
    SELECT 'LOW_VOLTAGE_BATTERY', '低压电池', 'WARNING', '620 次循环', '循环次数高。', '检查低压电池' UNION ALL
    SELECT 'CHARGING_SYSTEM', '充电系统', 'WARNING', '0.118V 压差', '压差接近危险阈值。', '做电池均衡检测' UNION ALL
    SELECT 'THERMAL_MANAGEMENT', '热管理/高压安全', 'WARNING', '48.8°C', '温度偏高。', '复检冷却系统' UNION ALL
    SELECT 'MAINTENANCE', '定期保养', 'WARNING', '8 天后到期', '保养临近。', '预约保养' UNION ALL
    SELECT 'INSPECTION_INSURANCE', '年检/保险', 'DANGER', '年检已逾期 3 天', '年检已逾期。', '立即处理年检'
) x
WHERE s.vin = 'LC0CE4DB7N0000004'
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id = s.snapshot_id);

INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'BATTERY' item_type, '动力电池' item_name, 'NORMAL' level, '96.8% SOH' metric_value, '新车电池健康优秀。' description, '保持良好充电习惯' action_suggestion UNION ALL
    SELECT 'TIRE_PRESSURE', '轮胎/胎压', 'NORMAL', '6800 km', '轮胎状态良好。', '首保复查' UNION ALL
    SELECT 'BRAKE', '制动系统', 'NORMAL', '正常', '暂无风险。', '无需处理' UNION ALL
    SELECT 'LOW_VOLTAGE_BATTERY', '低压电池', 'NORMAL', '正常', '低压供电正常。', '无需处理' UNION ALL
    SELECT 'CHARGING_SYSTEM', '充电系统', 'NORMAL', '0.022V 压差', '充电系统正常。', '无需处理' UNION ALL
    SELECT 'THERMAL_MANAGEMENT', '热管理/高压安全', 'NORMAL', '34.1°C', '热管理正常。', '无需处理' UNION ALL
    SELECT 'MAINTENANCE', '定期保养', 'NORMAL', '70 天后到期', '首保未临近。', '按期首保' UNION ALL
    SELECT 'INSPECTION_INSURANCE', '年检/保险', 'NORMAL', '年检 120 天后到期', '暂无临近风险。', '无需处理'
) x
WHERE s.vin = 'LC0CE4DB7N0000005'
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id = s.snapshot_id);

INSERT INTO vehicle_health_item (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
SELECT s.snapshot_id, x.item_type, x.item_name, x.level, x.metric_value, x.description, x.action_suggestion
FROM vehicle_health_snapshot s
JOIN (
    SELECT 'BATTERY' item_type, '动力电池' item_name, 'DANGER' level, '66.5% SOH' metric_value, 'SOH 低且存在高压系统报警。' description, '立即拖车入站检测' action_suggestion UNION ALL
    SELECT 'TIRE_PRESSURE', '轮胎/胎压', 'WARNING', '78200 km', '高里程车辆，轮胎老化风险高。', '检查并视情况更换轮胎' UNION ALL
    SELECT 'BRAKE', '制动系统', 'WARNING', '建议检查', '高里程建议做制动系统专项检查。', '检查制动系统' UNION ALL
    SELECT 'LOW_VOLTAGE_BATTERY', '低压电池', 'WARNING', '880 次循环', '低压电池老化风险高。', '更换或检测 12V 电池' UNION ALL
    SELECT 'CHARGING_SYSTEM', '充电系统', 'DANGER', '0.142V 压差', '压差超过危险阈值。', '停止高负荷使用并检测' UNION ALL
    SELECT 'THERMAL_MANAGEMENT', '热管理/高压安全', 'DANGER', '57.2°C', '温度异常偏高。', '立即检查热管理系统' UNION ALL
    SELECT 'MAINTENANCE', '定期保养', 'DANGER', '已逾期 18 天', '保养逾期。', '立即预约保养' UNION ALL
    SELECT 'INSPECTION_INSURANCE', '年检/保险', 'DANGER', '保险已逾期 2 天', '保险已逾期，影响上路和年检。', '立即处理保险和年检'
) x
WHERE s.vin = 'LC0CE4DB7N0000006'
  AND NOT EXISTS (SELECT 1 FROM vehicle_health_item i WHERE i.snapshot_id = s.snapshot_id);
