package com.healthfirst.pims.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        String[] tables = {"users", "suppliers", "medicines", "sales", "sale_items"};
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            System.out.println("Connected to MySQL successfully.\n");
            for (String table : tables) {
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    rs.next();
                    System.out.printf("%-12s -> %d rows%n", table, rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection FAILED: " + e.getMessage());
            System.err.println("Check: MySQL is running, database.sql was executed, " + "and USER/PASSWORD in DBConnection.java are correct.");
        }
    }
}
