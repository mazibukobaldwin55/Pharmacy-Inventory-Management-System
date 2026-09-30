package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.time.LocalDate;

//Represents one row of the medicines table and their supplier's name for display
public class Medicine {
    private int id;
    private String name;
    private String company;
    private String type;
    private BigDecimal price;
    private int quantity;
    private int reorderLevel;
    private LocalDate expiryDate;
    private Integer supplierId; // null when the medicine has no supplier
    private String supplierName;
    public int getId(){return id;}
    public void setId(int id){this.id = id;}
    public String getName(){return name;}
    public void setName(String name){ this.name = name;}
    public String getCompany(){return company;}
    public void setCompany(String company){this.company = company;}
    public String getType(){return type;}
    public void setType(String type){this.type = type;}
    public BigDecimal getPrice(){return price;}
    public void setPrice(BigDecimal price){this.price = price;}
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity){this.quantity = quantity; }
    public int getReorderLevel() {return reorderLevel;}
    public void setReorderLevel(int reorderLevel){this.reorderLevel = reorderLevel;}
    public LocalDate getExpiryDate() { return expiryDate;}
    public void setExpiryDate(LocalDate date){ this.expiryDate = date; }
    public Integer getSupplierId(){ return supplierId;}
    public void setSupplierId(Integer id){this.supplierId = id;}
    public String getSupplierName(){return supplierName;}
    public void setSupplierName(String name){this.supplierName = name;}
    //Color coding
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
    public boolean isExpiringSoon() {
        if (expiryDate == null || isExpired()){
            return false;
        }
        return !expiryDate.isAfter(LocalDate.now().plusMonths(1));
    }
    public boolean isOutOfStock() {
        return quantity <= 0;
    }
    public boolean isSellable() {
        return !isExpired() && !isOutOfStock();
    }
    public String getStatusText() {
        if (isExpired())    return "Expired";
        if (isOutOfStock()) return "Out of stock";
        if (isLowStock())   return "Low stock";
        return "In stock";
    }
    @Override
    public String toString() {
        return name;
    }
}
