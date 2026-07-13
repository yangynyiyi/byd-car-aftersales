#!/usr/bin/env python3
"""Restore structured fault_description text with proper UTF-8."""
import pymysql

UPDATES = [
    (
        "FLT20260710001",
        "故障现象：其他\n【车主描述】制动异响并伴随踏板抖动，需检查制动片和悬挂\n"
        "【顾问判断】接车试车复现异响，初步判断制动片磨损或悬挂松旷，建议举升检查制动盘片与连接件",
    ),
    (
        "FLT20260710002",
        "故障现象：仪表报警\n【车主描述】无法启动，高压系统报警，需拖车进站检测\n"
        "【顾问判断】远程确认高压系统报警，存在绝缘与启动风险，安排拖车进站并优先做高压安全检测",
    ),
    (
        "FLT20260710003",
        "故障现象：电池温度异常\n【车主描述】快充变慢，仪表提示电池温度异常\n"
        "【顾问判断】接车时检查充电口温感与冷却回路，疑似热管理模块异常，安排高压检测与快充复现",
    ),
    (
        "FLT20260710004",
        "故障现象：仪表报警\n【车主描述】仪表偶发报警，需读取故障码\n"
        "【顾问判断】仪表报警为间歇性，建议先读取全车故障码并检查网关与低压供电",
    ),
    (
        "FLT20260710005",
        "故障现象：其他\n【车主描述】胎压传感器异常\n"
        "【顾问判断】四轮胎压读数异常，逐个检测传感器信号并排除电磁干扰",
    ),
    (
        "FLT20260710006",
        "故障现象：其他\n【车主描述】半年保养及全车检查\n"
        "【顾问判断】按保养手册执行全车检查、油液核对与软件版本确认",
    ),
    (
        "FLT20260710007",
        "故障现象：续航下降\n【车主描述】车辆充电缓慢，续航明显下降\n"
        "【顾问判断】结合里程与充电习惯，优先检查电池SOH与压差，建议安排深度检测",
    ),
    (
        "FLT20260710008",
        "故障现象：其他\n【车主描述】新车首保检查\n"
        "【顾问判断】首保项目以全车检查、胎压核对与软件升级为主",
    ),
]


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
            for fault_no, description in UPDATES:
                cur.execute(
                    "UPDATE fault_record SET fault_description = %s WHERE fault_no = %s",
                    (description, fault_no),
                )
            conn.commit()
            cur.execute(
                "SELECT COUNT(*) FROM fault_record WHERE fault_description LIKE %s",
                ("%【顾问判断】%",),
            )
            count = cur.fetchone()[0]
            print(f"structured count: {count}")
            cur.execute(
                "SELECT fault_no, fault_description FROM fault_record WHERE fault_no = %s",
                ("FLT20260710001",),
            )
            row = cur.fetchone()
            print(row[0], row[1][:40] if row else "")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
