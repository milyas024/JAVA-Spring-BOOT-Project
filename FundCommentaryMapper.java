/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.dao;

import com.ams.model.FundCommentary;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author mohammad.bassam
 */
public class FundCommentaryMapper implements RowMapper<Object>{
    
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		FundCommentary customer = new FundCommentary();
                customer.setFundCode(rs.getString("FUND_CODE"));
                customer.setCommentary(rs.getString("DESCRIPTION"));
		return customer;
	}

    
    
}
