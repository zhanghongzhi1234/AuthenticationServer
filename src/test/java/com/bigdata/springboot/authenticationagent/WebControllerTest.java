package com.bigdata.springboot.authenticationagent;

import com.bigdata.springboot.bean.JWTUtil;
import com.bigdata.springboot.model.LoginModel;
import com.bigdata.springboot.model.RegModel;
import com.bigdata.springboot.model.TokenModel;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class WebControllerTest {

    private Gson gson = new Gson();
    private String username = "zhangsan";
    private String password = "12345";
    private String email = "zhangsan@noexist.com";
    private String token = "";

    @Value("${jwt.cookieid}")
    private String jwt_cookieid;

    @BeforeClass
    public void setUP(){
        //指定 URL 和端口号
    }

    @Test(groups = "groupCorrect")
    public void registryTest(){
        RegModel model = new RegModel();
        model.setUsername(username);
        model.setPassword(password);
        model.setEmail(email);
        Response response =
                given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        body(gson.toJson(model)).
                        when().
                        post("/api/auth/registry").
                        thenReturn();
        String status_code = response.jsonPath().get("status_code");
        String status = response.jsonPath().get("status");
        token = response.jsonPath().get("token");
        Assert.assertEquals(status_code, "200");
        Assert.assertEquals(status, "success");
        Claims claims = JWTUtil.decodeJWT(token);
        Assert.assertTrue(claims.getId().equals(username));
    }

    @Test(groups = "groupCorrect")
    public void loginTest(){
        LoginModel model = new LoginModel();
        model.setUsername(username);
        model.setPassword(password);
        Response response =
                given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        body(gson.toJson(model)).
                        when().
                        post("/api/auth/login").
                        thenReturn();
        String status_code = response.jsonPath().get("status_code");
        String status = response.jsonPath().get("status");
        Assert.assertEquals(status_code, "200");
        Assert.assertEquals(status, "success");
        token = response.jsonPath().get("token");
        Claims claims = JWTUtil.decodeJWT(token);
        Assert.assertTrue(claims.getId().equals(username));
    }

    @Test(groups = "groupCorrect")
    public void logoutTest(){
        TokenModel model = new TokenModel();
        model.setToken(token);
        Response response =
                given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        body(gson.toJson(model)).
                        when().
                        post("/api/auth/logout").
                        thenReturn();
        String status_code = response.jsonPath().get("status_code");
        String status = response.jsonPath().get("status");
        Assert.assertEquals(status_code, "200");
        Assert.assertEquals(status, "success");
    }

    @Test(groups = "groupCorrect")
    public void renewtokenTest(){
        TokenModel model = new TokenModel();
        model.setToken(token);
        Response response =
                given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        body(gson.toJson(model)).
                        when().
                        post("/api/auth/renewtoken").
                        thenReturn();
        String status_code = response.jsonPath().get("status_code");
        String status = response.jsonPath().get("status");
        Assert.assertEquals(status_code, "200");
        Assert.assertEquals(status, "success");
        String newtoken = response.jsonPath().get("token");
        Assert.assertTrue(token != newtoken);
        token = newtoken;
        Claims claims = JWTUtil.decodeJWT(token);
        Assert.assertTrue(claims.getId().equals(username));
    }

    @Test(groups = "groupCorrect")
    public void publickeyTest(){
        Response response =
                given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        when().
                        post("/api/auth/publickey").
                        thenReturn();
        String publicKey = response.toString();
        Assert.assertTrue(publicKey == JWTUtil.getSecretKey());
    }

    @Test(groups = "groupCorrect")
    public void cookieidTest(){
        Response response =
                given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        when().
                        post("/api/auth/cookieid").
                        thenReturn();
        String cookieid = response.toString();
        Assert.assertTrue(cookieid == jwt_cookieid);
    }
}
