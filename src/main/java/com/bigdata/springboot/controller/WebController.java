package com.bigdata.springboot.controller;

import com.bigdata.springboot.bean.CommonUtil;
import com.bigdata.springboot.bean.JWTUtil;
import com.bigdata.springboot.bean.ResponseBean;
import com.bigdata.springboot.model.GroupModel;
import com.bigdata.springboot.model.LoginModel;
import com.bigdata.springboot.model.TokenModel;
import com.bigdata.springboot.model.RegModel;
import com.bigdata.springboot.service.TokenService;
import com.bigdata.springboot.service.UserService;
import javafx.scene.input.TouchEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WebController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Value("${jwt.cookieid}")
    private String jwt_cookieid;

    @Value("${jwt.cookiemaxage}")
    private int jwt_cookiemaxage;


    @PostMapping("/api/auth/registry")
    public Object registry(@RequestBody RegModel model, HttpServletResponse response) {
        //check request data format first
        if(model == null)
            return (new ResponseBean(400, "invalid json body object")).getData();
        String username = model.getUsername();
        String password = model.getPassword();
        String email = model.getEmail();
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(email) || !CommonUtil.checkEmail(email))
            return (new ResponseBean(400, "invalid json body object")).getData();

        if(userService.existUser(email)){
            return (new ResponseBean(401, "duplicate with existing email address")).getData();
        }

        boolean ret = userService.insertUser(username, password, email);
        if(!ret)
        {
            return (new ResponseBean(401, "cannot create new user")).getData();
        }
        else{
            ResponseBean responseBean = CreateTokenAndSaveCookie(username, response);
            return responseBean.getData();          //add token to success response
        }
    }

    @PostMapping("/api/auth/login")
    public Object login(@RequestBody LoginModel model, HttpServletResponse response) {
        //check request data format first
        if(model == null)
            return (new ResponseBean(400, "invalid json body object")).getData();
        String username = model.getUsername();
        String password = model.getPassword();

        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
            return (new ResponseBean(400, "input(s) cannot be found in request, is empty or json is malformed")).getData();

        if(userService.userLogin(username, password)){
            ResponseBean responseBean = CreateTokenAndSaveCookie(username, response);
            return responseBean.getData();          //add token to success response
        }
        else{
            return (new ResponseBean(401, "invalid login")).getData();
        }
    }

    private ResponseBean CreateTokenAndSaveCookie(String jwtId, HttpServletResponse response){
        String token = tokenService.createToken(jwtId);
        userService.setToken(jwtId, token);
        //set cookie from server side
        final Cookie cookie = new Cookie(jwt_cookieid, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwt_cookiemaxage);
        response.addCookie(cookie);

        Map<String, String> data = new HashMap<String, String>() {{  put("token", token); }};
        return new ResponseBean(200, "success", data);          //add token to success response
    }

    @PostMapping("/api/auth/logout")
    public Object logout(@RequestBody TokenModel model) {
        //check request data format first
        if(model == null)
            return (new ResponseBean(400, "invalid json body object")).getData();
        String token = model.getToken();

        if(StringUtils.isEmpty(token))
            return (new ResponseBean(400, "input(s) cannot be found in request, is empty or json is malformed")).getData();

        if(tokenService.getIdFromToken(token).isEmpty()){
            return (new ResponseBean(401, "invalid token")).getData();
        }

        //add this token to blacklist
        if(userService.userLogout(token)){
            return (new ResponseBean(200, "logged out successfully")).getData();
        }
        else{
            return (new ResponseBean(401, "logout failed")).getData();
        }
    }

    @PostMapping("/api/auth/renewtoken")
    public Object renewtoken(@RequestBody TokenModel model, HttpServletResponse response) {
        //check request data format first
        if(model == null)
            return (new ResponseBean(400, "invalid json body object")).getData();
        String token = model.getToken();

        if(StringUtils.isEmpty(token))
            return (new ResponseBean(400, "input(s) cannot be found in request, is empty or json is malformed")).getData();

        //check if the token valid
        if(userService.inBlackList(token)){
            return (new ResponseBean(401, "token in blacklist")).getData();
        }
        String username = tokenService.getIdFromToken(token);
        if( username!= ""){
            ResponseBean responseBean = CreateTokenAndSaveCookie(username, response);
            return responseBean.getData();          //add token to success response
        }
        else{
            return (new ResponseBean(401, "invalid token")).getData();
        }
    }

    @PostMapping("/api/auth/publickey")
    public Object publickey() {
        String ret = JWTUtil.getSecretKey();
        return ret;
    }

    @PostMapping("/api/auth/cookieid")
    public Object cookieid() {
        String ret = jwt_cookieid;
        return ret;
    }

    @PostMapping("/api/auth/setactivegroupid")
    public Object setactivegroupid(@RequestBody GroupModel model) {
        //check request data format first
        if(model == null)
            return (new ResponseBean(400, "invalid json body object")).getData();
        String groupid = model.getGroupid();

        if(StringUtils.isEmpty(groupid))
            return (new ResponseBean(400, "input(s) cannot be found in request, is empty or json is malformed")).getData();

        //add this token to blacklist
        if(userService.setActiveGroupid(groupid)){
            return (new ResponseBean(200, "active group id written into cookie")).getData();
        }
        else{
            return (new ResponseBean(401, "set active group id failed")).getData();
        }
    }
}
