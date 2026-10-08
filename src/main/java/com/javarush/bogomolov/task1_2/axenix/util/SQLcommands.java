package com.javarush.bogomolov.task1_2.axenix.util;

public interface SQLcommands {
    String selectEmployeeFullNameDepartmentAndPosition = """
                SELECT
                    e.last_name || ' ' || e.first_name ||
                        COALESCE(' ' || e.surname, '')             AS full_name,
                    p.name                                          AS position_name,
                    COALESCE(
                        STRING_AGG(d.name, ', ' ORDER BY d.name),
                        '—'
                    )                                               AS departments,
                    e.email
                FROM employee e
                JOIN position p ON p.id = e.position_id
                LEFT JOIN employee_department ed ON ed.employee_id = e.id
                LEFT JOIN department d          ON d.id = ed.department_id
                WHERE e.deleted_at IS NULL
                GROUP BY e.id, e.last_name, e.first_name, e.surname, p.name, e.email
                ORDER BY e.last_name, e.first_name
                """;
}
