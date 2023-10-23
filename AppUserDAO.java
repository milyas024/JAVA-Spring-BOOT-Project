/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.dao;

import com.ams.model.AppUser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mohammad.bassam
 */
@Repository
@Transactional
public class AppUserDAO {
 
//    @Autowired
//    public AppUserDAO(DataSource dataSource) {
//        this.setDataSource(dataSource);
//    }
 
    public AppUser findUserAccount(String userName) {
        
        // pass userid to encrypter
        AppUser userInfo = new AppUser();
        userInfo.setUserId(Long.valueOf("1"));
        userInfo.setUserName(userName);
        userInfo.setEncrytedPassword(userName);
        return userInfo;
        
        // Select .. from App_User u Where u.User_Name = ?
//        String sql = AppUserMapper.BASE_SQL + " where u.User_Name = ? ";
// 
//        Object[] params = new Object[] { userName };
//        AppUserMapper mapper = new AppUserMapper();
//        try {
//            AppUser userInfo = this.getJdbcTemplate().queryForObject(sql, params, mapper);
//            return userInfo;
//        } catch (EmptyResultDataAccessException e) {
//            return null;
//        }
    }
 
}