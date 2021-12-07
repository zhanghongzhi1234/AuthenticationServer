package com.bigdata.springboot.bean;

public class UserBean {

    private String username;
    private String password;
    private String email;
    private String groupid;
    private String token;

    public UserBean() {
    }

    public UserBean(String username, String password, String email, String groupid) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.groupid = groupid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
