package com.collegecanteen.model;

public class Admin extends User {
    private String designation;

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String username, String password, String fullName, String email, String phone, String designation) {
        super(id, username, password, fullName, email, phone, "ADMIN");
        this.designation = designation;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
