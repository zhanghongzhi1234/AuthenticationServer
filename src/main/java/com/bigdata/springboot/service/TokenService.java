package com.bigdata.springboot.service;

import com.bigdata.springboot.bean.JWTUtil;
import io.jsonwebtoken.Claims;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenService {

    private static final Logger logger = LogManager.getLogger();

    @Value("${jwt.issuer}")
    private String jwt_issuer;

    @Value("${jwt.subject}")
    private String jwt_subject;

    @Value("${jwt.timetolive}")
    private int jwt_timetolive;

    public String createToken(String jwtId){
        String jwt = JWTUtil.createJWT(
                jwtId, // claim = jti
                jwt_issuer, // claim = iss
                jwt_subject, // claim = sub
                jwt_timetolive // used to calculate expiration (claim = exp)
        );

        logger.info("jwt = \"" + jwt.toString() + "\"");

        return jwt;
    }

    public boolean validateToken(String jwt, String jwtId){
        Claims claims = JWTUtil.decodeJWT(jwt);
        logger.info("claims = " + claims.toString());

        Date expiration = claims.getExpiration();
        Date date = new Date();
        return jwtId.equals(claims.getId()) && jwt_issuer.equals(claims.getIssuer()) && jwt_subject.equals(claims.getSubject()) && date.before(expiration);
    }

    public String getIdFromToken(String jwt){
        Claims claims = null;
        try {
            claims = JWTUtil.decodeJWT(jwt);
        } catch(Exception ex){
            System.out.println(ex.toString());
            return "";      //not a valid token or may expired
        }

        logger.info("claims = " + claims.toString());
        Date expiration = claims.getExpiration();
        Date date = new Date();
        if(jwt_issuer.equals(claims.getIssuer()) && jwt_subject.equals(claims.getSubject()) && date.before(expiration))
            return claims.getId();
        else
            return "";
    }
}
