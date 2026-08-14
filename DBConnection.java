package com.smartbilling;

import java.sql.*;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/smartbilling?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
