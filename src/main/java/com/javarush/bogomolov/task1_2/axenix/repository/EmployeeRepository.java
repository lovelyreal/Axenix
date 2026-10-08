package com.javarush.bogomolov.task1_2.axenix.repository;


import com.javarush.bogomolov.task1_2.axenix.config.DbConfig;
import com.javarush.bogomolov.task1_2.axenix.util.SQLcommands;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EmployeeRepository {

    private final DbConfig config;

    public EmployeeRepository(DbConfig config) {
        this.config = config;
    }

    public List<EmployeeDto> findEmployeesWithPositionAndDepartments() throws SQLException {


        List<EmployeeDto> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                config.url(), config.user(), config.password());
             PreparedStatement ps = connection.prepareStatement(SQLcommands.selectEmployeeFullNameDepartmentAndPosition);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDto row = new EmployeeDto();
                row.fullName    = rs.getString("full_name");
                row.position    = rs.getString("position_name");
                row.departments = rs.getString("departments");
                row.email       = rs.getString("email");
                result.add(row);
            }
        }

        return result;
    }
}