package com.bigdata.springboot.model;

import org.springframework.stereotype.Component;

@Component
public class TokenModel {

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token;

}
