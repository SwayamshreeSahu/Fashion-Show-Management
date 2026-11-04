
package com.fashionshow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:oracle:thin:@localhost:1521/XEPDB1";

    private static final String USER =
            "fashion_app";

    private static final String PASSWORD =
            "FashionDB_2026";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
