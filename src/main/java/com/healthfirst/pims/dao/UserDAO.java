package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, role, full_name " + "FROM users WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
    public List<User> getCashiers() throws SQLException{
        String sql = "SELECT user_id, username, role, full_name FROM users " + "WHERE role = 'Cashier' ORDER BY full_name";
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
    //CREATE allows an administrator to make a new cashier account.
    public void addCashier(String username, String fullName, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, full_name) " + "VALUES (?, ?, 'Cashier', ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);
            ps.executeUpdate();
        }
    }
    //UPDATE can change a cashier's details such as their name and their password.
    public void updateCashier(int userId, String fullName, String newPassword) throws SQLException {
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        String sql = changePassword
                ? "UPDATE users SET full_name = ?, password = ? WHERE user_id = ? AND role = 'Cashier'"
                : "UPDATE users SET full_name = ? WHERE user_id = ? AND role = 'Cashier'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName);
            if (changePassword) {
                ps.setString(2, PasswordUtil.hash(newPassword));
                ps.setInt(3, userId);
            } else {
                ps.setInt(2, userId);
            }
            ps.executeUpdate();
        }
    };
     //DELETE function removes a cashier account this is done by an Administrator.
     //administrator account can never be deleted through this method.
    public void deleteCashier(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'Cashier'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
             ps.setInt(1, userId); ps.executeUpdate();
        }
    }
    private User mapRow(ResultSet rs) throws SQLException{
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("full_name"));
    }
}
