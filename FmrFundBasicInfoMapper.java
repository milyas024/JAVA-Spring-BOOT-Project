/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.dao;

import com.ams.model.FmrFundBasicInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author mohammad.bassam
 */
public class FmrFundBasicInfoMapper implements RowMapper<Object> {

    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

        FmrFundBasicInfo bi = new FmrFundBasicInfo();
        bi.id = rs.getString("ID");
        bi.transDate = rs.getString("TRANSDATE");
        bi.fundCode = rs.getString("FUND_CODE");
        bi.fund_type = rs.getString("FUND_TYPE");
        bi.category = rs.getString("CATEGORY");
        bi.launchdate = rs.getString("LAUNCHDATE");
        bi.setNetassets(rs.getString("NETASSETS"));
        if (rs.getString("NETASSETEXFOF") != null) {
            bi.setNetassetexfof(rs.getString("NETASSETEXFOF"));
        }
        try {
            if (rs.getString("NAV") != null && !rs.getString("NAV").equals("") && !rs.getString("NAV").equals("null")) {
                bi.setNav(rs.getString("NAV"));
            } else {
                bi.setNav(rs.getString("NAV1"));
            }
            bi.fundTer = rs.getString("TER");
        } catch (Exception e) {
        }
        bi.benchmark = rs.getString("BENCHMARK");
        bi.dealing_days = rs.getString("DEALING_DAYS");
        bi.cutt_off_time = rs.getString("CUTT_OFF_TIME");
        bi.pricing_mechanism = rs.getString("PRICING_MECHANISM");
        bi.management_fee = rs.getString("MANAGEMENT_FEE");
        bi.front_end_load = rs.getString("FRONT_END_LOAD");
        bi.trustee = rs.getString("TRUSTEE");
        bi.auditor = rs.getString("AUDITOR");
        bi.asset_manager_rating = rs.getString("ASSET_MANAGER_RATING");
        bi.risk_prof_fund = rs.getString("RISK_PROF_FUND");
        bi.perform_ranking = rs.getString("PERFORM_RANKING");
        bi.fund_manger = rs.getString("FUND_MANGER");
        bi.listing = rs.getString("LISTING");
        bi.comments = rs.getString("COMMENTS");
        return bi;
    }
}
