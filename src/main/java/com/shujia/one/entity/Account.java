package com.shujia.one.entity;
//账户类实体信息，储存用户账户信息
public class Account {
 private int id;
 private String username;
 private String password;
 private String email;

 public String getEmail() {
  return email;
 }

 public void setEmail(String email) {
  this.email = email;
 }

 public String getPassword() {
  return password;
 }

 public void setPassword(String password) {
  this.password = password;
 }



 public int getId() {
  return id;
 }

 public void setId(int id) {
  this.id = id;
 }

 public String getUsername() {
  return username;
 }

 public void setUsername(String username) {
  this.username = username;
 }
}
