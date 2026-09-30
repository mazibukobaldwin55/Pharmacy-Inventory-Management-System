package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Medicine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

//Data Access for the medicines table Create, Read, Update, Delete done by admin
public class MedicineDAO {
    private static final String SELECT_ALL =
            "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, "
                    + "       m.quantity_in_stock, m.reorder_level, m.expiry_date, "
                    + "       m.supplier_id, s.name AS supplier_name "
                    + "FROM medicines m "
                    + "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id "
                    + "ORDER BY m.name";
    public List<Medicine> getAll() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
    public void add(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicines "
                + "(name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, m);
            ps.executeUpdate();
        }
    }
    public void update(Medicine m) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, company = ?, medicine_type = ?, price = ?, "
                + "quantity_in_stock = ?, reorder_level = ?, expiry_date = ?, supplier_id = ? " + "WHERE medicine_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, m);
            ps.setInt(9, m.getId());
            ps.executeUpdate();
        }
    }
    public void delete(int medicineId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.executeUpdate();
        }
    }
    private void bind(PreparedStatement ps, Medicine m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setString(2, m.getCompany());
        ps.setString(3, m.getType());
        ps.setBigDecimal(4, m.getPrice());
        ps.setInt(5, m.getQuantity());
        ps.setInt(6, m.getReorderLevel());
        ps.setDate(7, Date.valueOf(m.getExpiryDate()));
        if (m.getSupplierId() == null) {
            ps.setNull(8, Types.INTEGER);
        } else {
            ps.setInt(8, m.getSupplierId());
        }
    }
    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setCompany(rs.getString("company"));
        m.setType(rs.getString("medicine_type"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setQuantity(rs.getInt("quantity_in_stock"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        Date expiry = rs.getDate("expiry_date");
        m.setExpiryDate(expiry == null ? null : expiry.toLocalDate());
        int supplierId = rs.getInt("supplier_id");
        m.setSupplierId(rs.wasNull() ? null : supplierId);
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }
}
