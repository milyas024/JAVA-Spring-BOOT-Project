/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.dao;

import com.ams.model.FundDefinition;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author mohammad.bassam
 */
public class FundDefinitionRowMapper implements RowMapper<Object>{
    
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		FundDefinition customer = new FundDefinition();
                customer.setId(rs.getString("ID"));
                customer.setFundCode(rs.getString("FUND_CODE"));
                //customer.setMembers(rs.getString("MEMBERS"));
                customer.setCommentary(rs.getString("COMMENTARY"));
                customer.setObjective(rs.getString("OBJECTIVE"));
                customer.setTransDate(rs.getString("TRANS_DATE"));
                customer.setMemberList(rs.getString("MEMBERS"));
		return customer;
	}
    
}
