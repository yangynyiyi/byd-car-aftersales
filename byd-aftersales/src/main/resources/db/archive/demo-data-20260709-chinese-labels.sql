-- 演示数据中文化：把门店、车牌、车主/员工姓名、车型、电池型号改为中文展示
USE byd_aftersales;

UPDATE sys_user
SET real_name = CASE username
    WHEN 'owner001' THEN '张三'
    WHEN 'advisor001' THEN '李顾问'
    WHEN 'tech001' THEN '王技师'
    WHEN 'part001' THEN '陈备件'
    WHEN 'manager001' THEN '赵经理'
    WHEN 'admin001' THEN '系统管理员'
    WHEN 'owner002' THEN '陈宇'
    WHEN 'owner003' THEN '刘芳'
    WHEN 'advisor002' THEN '孙顾问'
    WHEN 'tech002' THEN '周技师'
    ELSE real_name
END
WHERE username IN (
    'owner001', 'advisor001', 'tech001', 'part001', 'manager001', 'admin001',
    'owner002', 'owner003', 'advisor002', 'tech002'
);

UPDATE service_center
SET center_name = '比亚迪深圳南山售后服务中心',
    city = '深圳',
    address = '深圳市南山区科技园示范路 1 号'
WHERE phone = '0755-88888888' OR center_name = 'BYD Shenzhen After-Sales Center';

UPDATE service_center
SET center_name = '比亚迪广州天河售后服务中心',
    city = '广州',
    address = '广州市天河区示范大道 88 号'
WHERE phone = '020-88886666' OR center_name = 'BYD Guangzhou Tianhe Service Center';

UPDATE service_center
SET center_name = '比亚迪上海浦东售后服务中心',
    city = '上海',
    address = '上海市浦东新区示范路 66 号'
WHERE phone = '021-88887777' OR center_name = 'BYD Shanghai Pudong Service Center';

-- 修正旧脚本重复插入深圳门店的问题：保留同电话最小 center_id，引用迁移后删除重复行。
UPDATE appointment a
JOIN (
    SELECT phone, MIN(center_id) AS keep_id
    FROM service_center
    WHERE phone IN ('0755-88888888', '020-88886666', '021-88887777')
    GROUP BY phone
) s
JOIN service_center current_center ON current_center.center_id = a.center_id AND current_center.phone = s.phone
SET a.center_id = s.keep_id;

DELETE sc FROM service_center sc
JOIN (
    SELECT * FROM (
    SELECT phone, MIN(center_id) AS keep_id
    FROM service_center
    WHERE phone IN ('0755-88888888', '020-88886666', '021-88887777')
    GROUP BY phone
    HAVING COUNT(*) > 1
    ) d
) duplicated ON sc.phone = duplicated.phone AND sc.center_id <> duplicated.keep_id;

-- 富演示预约使用真实门店，不依赖旧库里的 center_id 顺序。
UPDATE appointment
SET center_id = (SELECT center_id FROM service_center WHERE phone = '0755-88888888' ORDER BY center_id LIMIT 1)
WHERE appointment_no IN ('APT20260709001', 'APT20260709005');

UPDATE appointment
SET center_id = (SELECT center_id FROM service_center WHERE phone = '020-88886666' ORDER BY center_id LIMIT 1)
WHERE appointment_no IN ('APT20260709002', 'APT20260709004');

UPDATE appointment
SET center_id = (SELECT center_id FROM service_center WHERE phone = '021-88887777' ORDER BY center_id LIMIT 1)
WHERE appointment_no IN ('APT20260709003', 'APT20260709006');

UPDATE vehicle
SET license_plate = CASE vin
        WHEN 'LC0CE4DB7N0000001' THEN '粤B·12345'
        WHEN 'LC0CE4DB7N0000002' THEN '粤B·23456'
        WHEN 'LC0CE4DB7N0000003' THEN '粤A·34567'
        WHEN 'LC0CE4DB7N0000004' THEN '沪A·45678'
        WHEN 'LC0CE4DB7N0000005' THEN '沪C·56789'
        WHEN 'LC0CE4DB7N0000006' THEN '粤B·67890'
        ELSE license_plate
    END,
    model = CASE vin
        WHEN 'LC0CE4DB7N0000001' THEN '汉 EV'
        WHEN 'LC0CE4DB7N0000002' THEN '海狮 07 EV'
        WHEN 'LC0CE4DB7N0000003' THEN '宋 PLUS DM-i'
        WHEN 'LC0CE4DB7N0000004' THEN '唐 DM-i'
        WHEN 'LC0CE4DB7N0000005' THEN '海豚'
        WHEN 'LC0CE4DB7N0000006' THEN '汉 EV'
        ELSE model
    END,
    battery_model = CASE vin
        WHEN 'LC0CE4DB7N0000001' THEN '刀片电池 A1 型'
        WHEN 'LC0CE4DB7N0000002' THEN '刀片电池 B2 型'
        WHEN 'LC0CE4DB7N0000003' THEN '刀片电池 DM1 型'
        WHEN 'LC0CE4DB7N0000004' THEN '刀片电池 DM2 型'
        WHEN 'LC0CE4DB7N0000005' THEN '刀片电池 C1 型'
        WHEN 'LC0CE4DB7N0000006' THEN '刀片电池 A2 型'
        ELSE battery_model
    END
WHERE vin IN (
    'LC0CE4DB7N0000001', 'LC0CE4DB7N0000002', 'LC0CE4DB7N0000003',
    'LC0CE4DB7N0000004', 'LC0CE4DB7N0000005', 'LC0CE4DB7N0000006'
);
