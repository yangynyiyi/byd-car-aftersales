#!/usr/bin/env python3
"""Add part.unit column and backfill demo units."""
import pymysql

UNITS = {
    "P-BAT-001": "套",
    "P-MOT-001": "个",
    "P-BRK-001": "套",
    "P-TIR-001": "个",
    "P-LVB-001": "块",
    "P-HV-001": "套",
    "P-CHG-001": "个",
    "P-FLT-001": "个",
    "P-SEN-001": "个",
    "P-CBL-001": "套",
    "P-BMS-001": "个",
    "P-OIL-001": "桶",
}


def main() -> None:
    conn = pymysql.connect(
        host="127.0.0.1",
        user="root",
        password="123456",
        database="byd_aftersales",
        charset="utf8mb4",
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'part' AND COLUMN_NAME = 'unit'
                """
            )
            if cur.fetchone()[0] == 0:
                cur.execute(
                    "ALTER TABLE part ADD COLUMN unit VARCHAR(10) NOT NULL DEFAULT '个' AFTER warning_threshold"
                )
                print("added column part.unit")
            for part_no, unit in UNITS.items():
                cur.execute("UPDATE part SET unit = %s WHERE part_no = %s", (unit, part_no))
            conn.commit()
            cur.execute("SELECT part_no, unit, selling_price FROM part ORDER BY part_no LIMIT 3")
            for row in cur.fetchall():
                print(row)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
