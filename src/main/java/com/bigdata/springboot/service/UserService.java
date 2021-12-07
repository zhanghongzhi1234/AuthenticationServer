package com.bigdata.springboot.service;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.bigdata.springboot.arangodbcrud.ArangoDbAdapter;
import com.bigdata.springboot.bean.UserBean;
import com.google.gson.Gson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UserService implements InitializingBean {

    @Value("${arangodb.host}")
    private String arangodb_host;

    @Value("${arangodb.port}")
    private int arangodb_port;

    @Value("${arangodb.name}")
    private String arangodb_name;

    private ArangoDbAdapter arangoDbAdapter;
    private Gson gson = new Gson();
    private List<UserBean> userList = new ArrayList<UserBean>();
    private Set<String> blackList = new HashSet<String>();

    @Override
    public void afterPropertiesSet() throws Exception {
        if(InitDbAdapter())
            LoadAllUsers();
    }

    private boolean InitDbAdapter(){
        boolean ret = false;
        try {
            arangoDbAdapter = new ArangoDbAdapter();
            arangoDbAdapter.Init(arangodb_host, arangodb_port);
            ArangoDatabase arangoDatabase = arangoDbAdapter.selectDatabase(arangodb_name);
            if(arangoDatabase == null){
                System.out.println("Database " + arangodb_name + " not exist");
            }
            else{
                ret = true;
            }
        } catch (Exception ex) {
            System.out.println("Error when init dbadapter, exception= " + ex.toString());
        }
        return ret;
    }

    private void LoadAllUsers(){
        if(arangoDbAdapter.isInitialized()){
            AtomicInteger resultCount = new AtomicInteger();
            try {
                String query = "FOR t IN users RETURN t";
                ArangoCursor<BaseDocument> cursor = arangoDbAdapter.executeQuery(query, null);
                cursor.forEachRemaining(aDocument -> {
                    String streamerId = aDocument.getKey();
                    System.out.println("read Key: " + aDocument.getKey());
                    String username = Objects.toString(aDocument.getAttribute("username"),"");
                    String password = Objects.toString(aDocument.getAttribute("password"), "");
                    String email = Objects.toString(aDocument.getAttribute("email"), "");
                    String groupid = Objects.toString(aDocument.getAttribute("groupid"), "");
                    UserBean user = new UserBean(username, password, email, groupid);
                    userList.add(user);
                    resultCount.getAndIncrement();
                });
                System.out.println("Total read " + resultCount + " document");
            } catch (ArangoDBException e) {
                System.err.println("Failed to execute query. " + e.getMessage());
            }
        }
    }

    public boolean existUser(String email){
        boolean ret = false;
        for(UserBean user : userList){
            if(email.equals(user.getEmail())){
                ret = true;
            }
        }
        return ret;
    }

    //insert user, use username as key
    public boolean insertUser(String username, String password, String email){
        boolean ret = false;
        BaseDocument myObject = new BaseDocument(username);
        myObject.addAttribute("username", username);
        myObject.addAttribute("password", password);
        myObject.addAttribute("email", email);
        try {
            arangoDbAdapter.insertDocument("users", myObject);
            ret = true;
            System.out.println("user created in database");
        } catch (ArangoDBException e) {
            System.err.println("Failed to create user in db. " + e.getMessage());
        }

        if(ret == true){
            UserBean user = new UserBean(username, password, email, "");
            userList.add(user);
        }

        return ret;
    }

    public boolean userLogin(String username, String password){
        boolean ret = false;
        for(UserBean user : userList){
            if(username.equals(user.getUsername()) && password.equals(user.getPassword())){
                ret = true;
                break;
            }
        }
        return ret;
    }

    public boolean setToken(String username, String token){
        boolean ret = false;
        for(UserBean user : userList){
            if(username.equals(user.getUsername())){
                user.setToken(token);
                ret = true;
                break;
            }
        }
        return ret;
    }

    public boolean userLogout(String token) {
        blackList.add(token);
        return true;
    }

    public boolean inBlackList(String token){
        if(blackList.contains(token))
            return true;
        return false;
    }

    public boolean setActiveGroupid(String groupid){
        return true;
    }

}
