package com.healthfirst.pims.model;

import java.math.BigDecimal;

public class CartItem {
    private final int medicineId;
    private final String name;
    private final BigDecimal unitPrice;
    private int quantity;
    private final int stockLimit;
    public CartItem(int medicineId, String name, BigDecimal unitPrice, int quantity, int stockLimit) {
        this.medicineId = medicineId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.stockLimit = stockLimit;
    }
    public CartItem(int medicineId, String name, BigDecimal unitPrice, int quantity) {
        this(medicineId, name, unitPrice, quantity, quantity);
    }
    public int getMedicineId(){return medicineId;}
    public String getName(){return name;}
    public BigDecimal getUnitPrice(){return unitPrice;}
    public int getQuantity(){return quantity;}
    public void setQuantity(int q){this.quantity = q;}
    public int getStockLimit(){return stockLimit;}
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

