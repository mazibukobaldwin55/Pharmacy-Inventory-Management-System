package com.healthfirst.pims.model;


public class User{
    private final int userId;
    private final String username;
    private final String role;       // "Admin" or "Cashier"
    private final String fullName;

    public User(int userId, String username, String role, String fullName){
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }
    public int getUserId(){ return userId; }
    public String getUsername(){ return username; }
    public String getRole(){ return role; }
    public String getFullName(){ return fullName; }
    public boolean isAdmin(){ return "Admin".equalsIgnoreCase(role); }
    public boolean isCashier(){ return "Cashier".equalsIgnoreCase(role); }
    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}

