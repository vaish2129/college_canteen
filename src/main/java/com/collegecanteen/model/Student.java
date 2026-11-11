package com.collegecanteen.model;

public class Student extends User {
    private String department;
    private String year;
    private String studentId;

    public Student() {
        super();
        setRole("STUDENT");
    }

    public Student(int id, String username, String password, String fullName, String email,
                   String phone, String department, String year, String studentId) {
        super(id, username, password, fullName, email, phone, "STUDENT");
        this.department = department;
        this.year = year;
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
