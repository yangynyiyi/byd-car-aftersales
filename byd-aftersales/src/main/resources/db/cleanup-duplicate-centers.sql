USE byd_aftersales;

DELETE sc FROM service_center sc
JOIN (
    SELECT phone, MIN(center_id) AS keep_id
    FROM service_center
    WHERE deleted = 0
    GROUP BY phone
    HAVING COUNT(*) > 1
) dup ON sc.phone = dup.phone AND sc.center_id <> dup.keep_id;
