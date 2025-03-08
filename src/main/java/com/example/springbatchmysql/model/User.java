package com.example.springbatchmysql.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    private Integer age;
    private Date birthday;
    private String country;
    private String email;
    private String firstName;
    private String gender;
    private String lastName;
    private String personId;
} 