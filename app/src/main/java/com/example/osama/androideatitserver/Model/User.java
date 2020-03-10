package com.example.osama.androideatitserver.Model;

public class User {

    private String Name,Password,Phone,IsStaff;
    private String SecureCode;

    public User(){

    }

    public User(String name, String password,String secureCode) {
        Name = name;
        Password = password;
        SecureCode = secureCode;

    }
    public String getSecureCode() {
        return SecureCode;
    }

    public void setSecureCode(String secureCode) {
        SecureCode = secureCode;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }
}
