package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Data Access for sales and sale_items.
public class SaleDAO {
    public Sale checkout(User cashier, List<CartItem> cart) throws SQLException, SaleException {
        if (cart == null || cart.isEmpty()) {
            throw new SaleException("The cart is empty.");
        }
        Set<Integer> seen = new HashSet<>();
        for (CartItem item : cart) {
            if (!seen.add(item.getMedicineId())) {
                throw new IllegalArgumentException("Cart contains the same medicine twice: " + item.getName());
            }
        }
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Sale sale = processSale(con, cashier, cart);
                con.commit();
                return sale;
            } catch (SQLException | SaleException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
    private Sale processSale(Connection con, User cashier, List<CartItem> cart) throws SQLException, SaleException{
        List<CartItem> byId = new ArrayList<>(cart);
        byId.sort(Comparator.comparingInt(CartItem::getMedicineId));
        Map<Integer, CartItem> priced = new HashMap<>();
        String lockSql = "SELECT name, price, quantity_in_stock, expiry_date " + "FROM medicines WHERE medicine_id = ? FOR UPDATE";
        try (PreparedStatement lock = con.prepareStatement(lockSql)) {
            for (CartItem item : byId) {
                lock.setInt(1, item.getMedicineId());
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        throw new SaleException("\"" + item.getName() + "\" no longer exists in the system.");
                    }
                    String name = rs.getString("name");
                    BigDecimal price = rs.getBigDecimal("price");
                    int stock = rs.getInt("quantity_in_stock");
                    Date expiry = rs.getDate("expiry_date");
                    if (expiry != null && expiry.toLocalDate().isBefore(LocalDate.now())) {
                        throw new SaleException(name + " has expired and can't be sold.");
                    }
                    if (stock <= 0) {
                        throw new SaleException(name + " is out of stock.");
                    }
                    if (stock < item.getQuantity()) {
                        throw new SaleException("Only " + stock + " of " + name + " left in stock, " + "but the cart has " + item.getQuantity() + ".");
                    }
                    priced.put(item.getMedicineId(),
                            new CartItem(item.getMedicineId(), name, price, item.getQuantity()));
                }
            }
        }
        List<CartItem> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            CartItem line = priced.get(item.getMedicineId());
            lines.add(line);
            total = total.add(line.getLineTotal());
        }
        int saleId;
        String saleSql = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, cashier.getUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                saleId = keys.getInt(1);
            }
        }
        String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) " +"VALUES (?, ?, ?, ?)";
        String stockSql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? " + "WHERE medicine_id = ?";
        try (PreparedStatement itemPs = con.prepareStatement(itemSql);
             PreparedStatement stockPs = con.prepareStatement(stockSql)) {
            for (CartItem line : lines) {
                itemPs.setInt(1, saleId);
                itemPs.setInt(2, line.getMedicineId());
                itemPs.setInt(3, line.getQuantity());
                itemPs.setBigDecimal(4, line.getUnitPrice());
                itemPs.executeUpdate();
                stockPs.setInt(1, line.getQuantity());
                stockPs.setInt(2, line.getMedicineId());
                stockPs.executeUpdate();
            }
        }
        LocalDateTime saleDate = LocalDateTime.now();
        try (PreparedStatement ps = con.prepareStatement("SELECT sale_date FROM sales WHERE sale_id = ?")){
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getTimestamp(1) != null) {
                    saleDate = rs.getTimestamp(1).toLocalDateTime();
                }
            }
        }
        return new Sale(saleId, saleDate, total, cashier.getFullName(), lines);
    }
}
