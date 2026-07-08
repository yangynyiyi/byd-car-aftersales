USE byd_aftersales;

INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES
('owner001', '12345678', 'Zhang San', '13800000001', 'OWNER', 'ENABLED'),
('advisor001', '12345678', 'Li Advisor', '13800000002', 'ADVISOR', 'ENABLED'),
('tech001', '12345678', 'Wang Technician', '13800000003', 'TECHNICIAN', 'ENABLED'),
('part001', '12345678', 'Chen Parts', '13800000004', 'PART_ADMIN', 'ENABLED'),
('manager001', '12345678', 'Zhao Manager', '13800000005', 'MANAGER', 'ENABLED'),
('admin001', '12345678', 'System Admin', '13800000006', 'ADMIN', 'ENABLED')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    real_name = VALUES(real_name),
    role = VALUES(role),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_center (center_name, city, address, phone, status)
VALUES ('BYD Shenzhen After-Sales Center', 'Shenzhen', 'No. 1 Demo Road', '0755-88888888', 'OPEN');

INSERT INTO vehicle (vin, owner_id, license_plate, model, battery_model, purchase_date, current_mileage, vehicle_status)
VALUES ('LC0CE4DB7N0000001', 1, 'YueB12345', 'Han EV', 'Blade Battery A1', '2024-05-01', 18320.5, 'NORMAL')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO appointment (appointment_no, vin, owner_id, center_id, appointment_time, problem_description, status)
SELECT 'APT20250707001', 'LC0CE4DB7N0000001', 1, 1, '2025-07-08 10:00:00', '定期保养与电池检测', 'PENDING'
WHERE NOT EXISTS (SELECT 1 FROM appointment WHERE appointment_no = 'APT20250707001');

INSERT INTO fault_record (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
SELECT 'FLT20250707001', 1, 'LC0CE4DB7N0000001', 1, 2, '车辆充电缓慢，续航明显下降', 'HIGH', 'REGISTERED'
WHERE NOT EXISTS (SELECT 1 FROM fault_record WHERE fault_no = 'FLT20250707001');

INSERT INTO part (part_no, part_name, category, stock_quantity, warning_threshold, purchase_price, selling_price, status)
VALUES
('P-BAT-001', '动力电池模组', 'BATTERY', 5, 3, 12000.00, 15800.00, 'ENABLED'),
('P-MOT-001', '驱动电机轴承', 'MOTOR', 12, 5, 280.00, 450.00, 'ENABLED'),
('P-BRK-001', '制动片套装', 'BRAKE', 2, 5, 180.00, 320.00, 'ENABLED')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
SELECT 'LC0CE4DB7N0000001', 72.50, 420, 42.5, 18.2, 0.085, 'DANGER', '2025-07-06 14:30:00'
WHERE NOT EXISTS (
    SELECT 1 FROM battery_health_record
    WHERE vin = 'LC0CE4DB7N0000001' AND detect_time = '2025-07-06 14:30:00'
);
