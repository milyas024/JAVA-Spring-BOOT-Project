/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.dao;

import com.ams.common.ResponseCodes;
import com.ams.model.FundDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 *
 * @author mohammad.bassam
 */
@Component
public class FmrDao {

    public FundDefinition getFrmDefinitionImpl(FundDefinition input) {
        FundDefinition fmr = new FundDefinition();
        try {
            String sql = "select id, to_char(fmr.trans_date, 'dd/MM/yyyy') trans_date, fmr.fund_code, fmr.members, f.fund_name, commentary, objective from fmr_def fmr, fund f where fmr.fund_code = f.fund_code and fmr.fund_code = ? and fmr.trans_date = to_date(?, 'dd/MM/yyyy')";
            fmr = (FundDefinition) jdbcTemplate.queryForObject(
                    sql, new Object[]{input.fundCode, input.transDate}, new FundDefinitionRowMapper());
            fmr.setResponse_code(ResponseCodes.SUCCESS);
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }
        return fmr;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
