package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private final int saleId;
    private final LocalDateTime saleDate;
    private final BigDecimal totalAmount;
    private final String cashierName;
    private final List<CartItem> items;
    public Sale(int saleId, LocalDateTime saleDate, BigDecimal totalAmount, String cashierName, List<CartItem> items) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.cashierName = cashierName;
        this.items = items;
    }
    public int getSaleId(){return saleId; }
    public LocalDateTime getSaleDate(){return saleDate; }
    public BigDecimal getTotalAmount(){return totalAmount; }
    public String getCashierName(){return cashierName; }
    public List<CartItem> getItems(){return items;}
    //Total number of units sold
    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }
}