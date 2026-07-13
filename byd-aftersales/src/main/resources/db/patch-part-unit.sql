-- 备件表增加计量单位，并回填演示数据
ALTER TABLE part ADD COLUMN unit VARCHAR(10) NOT NULL DEFAULT '个' AFTER warning_threshold;

UPDATE part SET unit = '套' WHERE part_no IN ('P-BAT-001', 'P-BRK-001', 'P-HV-001', 'P-CBL-001');
UPDATE part SET unit = '块' WHERE part_no = 'P-LVB-001';
UPDATE part SET unit = '桶' WHERE part_no = 'P-OIL-001';
UPDATE part SET unit = '个' WHERE unit IS NULL OR unit = '';
