USE byd_aftersales;

INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES
('owner001', '12345678', 'Zhang San', '13800000001', 'OWNER', 'ENABLED'),
('advisor001', '12345678', 'Li Advisor', '13800000002', 'ADVISOR', 'ENABLED'),
('tech001', '12345678', 'Wang Technician', '13800000003', 'TECHNICIAN', 'ENABLED'),
('part001', '12345678', 'Chen Parts', '13800000004', 'PART_ADMIN', 'ENABLED')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_center (center_name, city, address, phone, status)
VALUES ('BYD Shenzhen After-Sales Center', 'Shenzhen', 'No. 1 Demo Road', '0755-88888888', 'OPEN');

INSERT INTO vehicle (vin, owner_id, license_plate, model, battery_model, purchase_date, current_mileage, vehicle_status)
VALUES ('LC0CE4DB7N0000001', 1, 'YueB12345', 'Han EV', 'Blade Battery A1', '2024-05-01', 18320.5, 'NORMAL')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
