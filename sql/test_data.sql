INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES ('advisor001', '1234', 'zhangguwen', '13800000001', 'ADVISOR', 'ENABLED');

INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES ('tech001', '1234', 'lijiShi', '13800000002', 'TECHNICIAN', 'ENABLED');

INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES ('manager001', '1234', 'wangjingli', '13800000003', 'SERVICE_MANAGER', 'ENABLED');

INSERT INTO vehicle (vin, owner_id, license_plate, model, battery_model, purchase_date, current_mileage, vehicle_status)
VALUES ('LVSHFFAN6KF000001', 1, 'YUEB12345', 'HanEV', 'BladeBattery2.0', '2022-06-15', 45000.5, 'NORMAL');

INSERT INTO vehicle (vin, owner_id, license_plate, model, battery_model, purchase_date, current_mileage, vehicle_status)
VALUES ('LVSHFFAN6KF000002', 1, 'YUEB67890', 'TangDMi', 'BladeBattery1.0', '2021-03-20', 68000.0, 'NORMAL');

INSERT INTO fault_record (fault_no, vin, owner_id, advisor_id, fault_description, fault_level, status)
VALUES ('F20260707001', 'LVSHFFAN6KF000001', 1, 1, 'Fast charging speed dropped from 60kW to 30kW, dashboard shows battery temperature high', 'MEDIUM', 'REGISTERED');

INSERT INTO fault_record (fault_no, vin, owner_id, advisor_id, fault_description, fault_level, status)
VALUES ('F20260707002', 'LVSHFFAN6KF000002', 1, 1, 'Range dropped significantly, full charge only 200km, used to be 400km', 'HIGH', 'REGISTERED');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
VALUES ('LVSHFFAN6KF000001', 92.50, 350, 38.50, 15.20, 0.020, 'NORMAL', '2026-06-01 10:00:00');

INSERT INTO battery_health_record (vin, soh, charge_cycles, max_temperature, min_temperature, voltage_diff, warning_level, detect_time)
VALUES ('LVSHFFAN6KF000002', 55.80, 2100, 58.30, -5.00, 0.120, 'DANGER', '2026-07-05 14:30:00');
