package com.javarush.bogomolov.axenix;

import com.javarush.bogomolov.axenix.config.DbConfig;
import com.javarush.bogomolov.axenix.repository.EmployeeDto;
import com.javarush.bogomolov.axenix.repository.EmployeeRepository;

import java.sql.SQLException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("Сотрудники: должности и департаменты");
        System.out.println();

        DbConfig config = DbConfig.load();
        EmployeeRepository repo = new EmployeeRepository(config);

        try {
            List<EmployeeDto> rows =
                    repo.findEmployeesWithPositionAndDepartments();

            if (rows.isEmpty()) {
                System.out.println("(нет данных)");
                return;
            }

            printTable(rows);

        } catch (SQLException e) {
            System.err.println("Ошибка при работе с БД: " + e.getMessage());
            System.err.println("Проверьте, что PostgreSQL запущен,");
            System.err.println("а параметры подключения в application.properties корректны.");

        }
    }

    /** Простой табличный вывод с выравниванием по ширине колонок. */
    static void printTable(List<EmployeeDto> rows) {
        String[] headers = {"ФИО", "Должность", "Департаменты", "Email"};
        int[] w = new int[headers.length];
        for (int i = 0; i < headers.length; i++) w[i] = headers[i].length();

        for (EmployeeDto r : rows) {
            w[0] = Math.max(w[0], r.fullName.length());
            w[1] = Math.max(w[1], r.position.length());
            w[2] = Math.max(w[2], r.departments.length());
            w[3] = Math.max(w[3], r.email.length());
        }

        String line = "+" + "-".repeat(w[0] + 2)
                + "+" + "-".repeat(w[1] + 2)
                + "+" + "-".repeat(w[2] + 2)
                + "+" + "-".repeat(w[3] + 2) + "+";

        String fmt = "| %-" + w[0] + "s | %-" + w[1] + "s | %-"
                + w[2] + "s | %-" + w[3] + "s |%n";

        System.out.println(line);
        System.out.printf(fmt, (Object[]) headers);
        System.out.println(line);

        for (EmployeeDto r : rows) {
            System.out.printf(fmt, r.fullName, r.position, r.departments, r.email);
        }

        System.out.println(line);
        System.out.println();
        System.out.println("Всего строк: " + rows.size());
    }
}
