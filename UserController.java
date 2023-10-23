/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import com.ams.common.ResponseCodes;
import com.ams.model.UserModel;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 *
 * @author mohammad.bassam
 */
@RestController
@RequestMapping("/user")
public class UserController {    
    
    @GetMapping("/validate")
    public UserModel validate() {

        UserModel userModel = new UserModel();
        Object[] res1 = {""};
        try{
            userModel = validateUser("ADMIN", "ADMIN123*+", "localhost", "123123123123");
        }catch(Exception ex){
            System.out.println(ex.toString());
        }
        return userModel;
    }
    
    @GetMapping("/validate/{id}/{pwd}")
    public UserModel validate(@PathVariable String id, @PathVariable String pwd) {
        
        UserModel userModel = new UserModel();
        Object[] res1 = {""};
        try{
            userModel = validateUser(id, pwd, "localhost", "123123123123");
        }catch(Exception ex){
            System.out.println(ex.toString());
        }
        return userModel;
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody Object input) {
        return null;
    }
    
    @PostMapping
    public Object post(@RequestBody Object input) {
        
   
        return input;
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return null;
    }
    
    public UserModel validateUser(String user, String password, String ip, String sessionId) {
        
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("validateUser");
        SqlParameterSource in = new MapSqlParameterSource().addValue("USER", user).addValue("PASS", password).addValue("REMOTEADD", ip).addValue("SESSIONID", sessionId);
        Map<String, Object> out = jdbcCall.execute(in);
        UserModel userModel = new UserModel();
        if(out.get("USERTYPE")!=null && out.get("ROLECODE")!=null){
            userModel.Name = user;
            userModel.UserId = user;
            userModel.ROLE = out.get("ROLECODE").toString();
            userModel.setResponse_code(ResponseCodes.VALIDUSER);
        }else{
            userModel.setResponse_code(ResponseCodes.INVALIDUSER);
        }
        
        
        return userModel;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;
}
