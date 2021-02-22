package com.revature.models;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

@Entity(tableName = "job_table")
public class Job {

    @Id(columnName = "id")
    private int id;

    @Column(columnName = "job")
    private String job;

    @Column(columnName = "salary")
    private int salary;

    public Job(){

    }
    public Job(String job, int salary) {
        this.job = job;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
}
