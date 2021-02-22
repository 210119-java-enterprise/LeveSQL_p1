package com.revature.models;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

@Entity(tableName = "user_table")
public class DemoUser {

    @Id(columnName = "id")
    private int id;

    @Column(columnName = "first_name")
    private String firstName;

    @Column(columnName = "last_name")
    private String lastName;

    @Column(columnName = "email_address")
    private String email;

    public DemoUser() {
    }

    public DemoUser(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
