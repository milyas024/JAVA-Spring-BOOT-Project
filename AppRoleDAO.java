/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mohammad.bassam
 */
@Repository
@Transactional
public class AppRoleDAO {

    public List<String> getRoleNames(Long userId) {

        List<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN");
 
        return roles;
    }

}
