package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Data Access for admins for the suppliers allwoing CRUD - Create, Read, Update, Delete.
public class SupplierDAO {
    public List<Supplier> getAll() throws SQLException {
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address " + "FROM suppliers ORDER BY name";
        List<Supplier> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")));
            }
        }
        return list;
    }
    //CREATE allows the admin to insert a new supplier.
    public void add(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) " + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, s);
            ps.executeUpdate();
        }
    };
    //UPDATE lets the admin change the details of an existing supplier
    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, " + "email = ?, address = ? WHERE supplier_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, s);
            ps.setInt(6, s.getId());
            ps.executeUpdate();
        }
    };
    //DELETE: lets an admin remove a supplier.
    public void delete(int supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.executeUpdate();
        }
    }
    public int countMedicines(int supplierId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicines WHERE supplier_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
    private void bind(PreparedStatement ps, Supplier s) throws SQLException {
        ps.setString(1, s.getName());
        ps.setString(2, s.getContactPerson());
        ps.setString(3, s.getPhone());
        ps.setString(4, s.getEmail());
        ps.setString(5, s.getAddress());
    }
}