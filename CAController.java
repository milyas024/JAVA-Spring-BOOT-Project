/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import java.util.List;
import com.ams.common.ResponseCodes;
import com.ams.model.ChartOfAccountModel;
import com.ams.model.ChartOfAccountResponse;
import com.ams.model.UserModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author mohammad.bassam
 */
@RestController
@RequestMapping("/fmr/chartofaccount")
public class CAController {

    @GetMapping("/getChartAccountList")
    public ChartOfAccountResponse getChartAccountList() {

        List<ChartOfAccountModel> respList = null;
        ChartOfAccountResponse fmr = new ChartOfAccountResponse();
        try {
            String sql = "select gl_glmf_code code, gl_glmf_description description  from gl_glmf order by gl_glmf_code";//and rownum<=500
            respList = jdbcTemplate.query(sql,
                    new Object[]{},
                    new RowMapper<ChartOfAccountModel>() {
                public ChartOfAccountModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ChartOfAccountModel c = new ChartOfAccountModel();
                    c.setGlCode(rs.getString(1));
                    c.setDescription(rs.getString(2));
                    return c;
                }
            });
            fmr.setChartOfAccountList(respList);
            fmr.setResponse_code(ResponseCodes.SUCCESS);
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }

        return fmr;
    }

    @PostMapping(path = "/saveChartOfAccount", consumes = "application/json", produces = "application/json")
    public Object saveChartOfAccount(@RequestBody ChartOfAccountModel fundDef) {

        ChartOfAccountModel fmr = new ChartOfAccountModel();
        try {
            String sql = "INSERT \n"
                    + "WHEN not exists(SELECT 1 FROM fmr_chart_of_account WHERE fundcode = ? and gl_code = ? and trans_date = to_date(?,'dd/MM/yyyy')) \n"
                    + "THEN INTO fmr_chart_of_account (id, fundcode, gl_code, description, trans_date) \n"
                    + "SELECT ( select nvl(max(id)+1,1) from fmr_chart_of_account), ?, ?, ?, to_date(?,'dd/MM/yyyy') FROM DUAL";
            int resp = jdbcTemplate.update(sql, fundDef.getFundCode(), fundDef.getGlCode(), fundDef.getTransDate(), fundDef.getFundCode(), fundDef.getGlCode(), fundDef.getDescription(), fundDef.getTransDate());
            if (resp == 1) {
                fmr.setResponse_code(ResponseCodes.SUCCESS);
                System.out.println("Record inserted with ID = " + fundDef.getFundCode());
            } else {
                fmr.setResponse_code(ResponseCodes.FAILURE);
            }
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }

        return fundDef;
    }

    @PostMapping(path = "/deleteCode", consumes = "application/json", produces = "application/json")
    public Object deleteCode(@RequestBody ChartOfAccountModel fundDef) {

        ChartOfAccountModel fmr = new ChartOfAccountModel();
        try {
            String sql = "delete from fmr_chart_of_account where id = '" + fundDef.getId() + "' ";
            int resp = jdbcTemplate.update(sql);
            if (resp == 1) {
                fmr.setResponse_code(ResponseCodes.SUCCESS);
                System.out.println("Record inserted with ID = " + fundDef.getFundCode());
            } else {
                fmr.setResponse_code(ResponseCodes.FAILURE);
            }
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }

        return fundDef;
    }

    @PostMapping(path = "/getList", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getList() {
        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select ID, f.fund_name,\n"
                    + "       gl_code,\n"
                    + "       description,\n"
                    + "       to_char(trans_date, 'dd/MM/yyyy') trans_date\n"
                    + "  from fmr_chart_of_account g, fund f\n"
                    + " where g.FUNDCODE = f.fund_code\n"
                    + " order by id desc");//and rownum<=5
        } catch (Exception e) {
            return data;
        }
        return data;
    }

    public String getNetAssetsFunc(String vfundcode, String transdate, int ntable) {
        String resp = "0";
        try {
            SimpleDateFormat df = new SimpleDateFormat("mm/dd/yyyy");
            java.util.Date fromDate = df.parse(transdate);
            java.sql.Date sqlDate = new java.sql.Date(fromDate.getTime());
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("FMR").withProcedureName("Proc_Get_TotalAssetAmt");
            SqlParameterSource in = new MapSqlParameterSource().addValue("pFundCode", vfundcode).addValue("TransDate", sqlDate).addValue("ntable", 0);
            Map<String, Object> out = jdbcCall.execute(in);
            if (out.get("RESULT") != null) {
                System.out.println("fund code " + out.get("RESULT"));
                resp = out.get("RESULT").toString();
            }
        } catch (ParseException ex) {
            Logger.getLogger(CAController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }

    public List<Map<String, Object>> getPortfolioData(String vfundcode, String fData, String tDate) {
        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select f.fund_name as name,\n"
                    + "       t.symbol,\n"
                    + "       to_char(t.price_date, 'dd/mm/yyyy') price_date , \n"
                    + "       t.hft_volume\n"
                    + "  from equity_portfolio t , fund f \n"
                    + " where t.fund_code = '" + vfundcode + "' and t.fund_code = f.fund_code \n"
                    + "   and t.price_date between to_date('" + fData + "','dd/mm/yyyy') and to_date('" + tDate + "','dd/mm/yyyy') \n"
                    + " order by 3");
        } catch (Exception ex) {
            Logger.getLogger(CAController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public List<Map<String, Object>> getEquityExpData(String vfundcode, String fData, String tDate) {
        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select sum(f.hft_mark_to_mkt_value) hft_mark_to_mkt_value, to_char(f.price_date, 'dd/mm/yyyy') price_date, f.fund_code\n"
                    + "  from equity_portfolio f\n"
                    + " where f.fund_code = '" + vfundcode + "' \n"
                    + "   and f.price_date between to_date('" + fData + "','dd/mm/yyyy') and to_date('" + tDate + "','dd/mm/yyyy') \n"
                    + " group by f.price_date, f.fund_code\n"
                    + " order by f.price_date");
        } catch (Exception ex) {
            Logger.getLogger(CAController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    @PostMapping(path = "/getNetAssets", consumes = "application/json", produces = "application/json")
    public String getNetAssets(@RequestBody ChartOfAccountModel fundDef) {
        String resp = getNetAssetsFunc(fundDef.getFundCode(), fundDef.getTransDate(), 1);
        return resp;
    }

    @PostMapping(path = "/getPortfolioData", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getPortfolioData(@RequestBody ChartOfAccountModel f) {
        List<Map<String, Object>> data = getPortfolioData(f.getFundCode(), f.getfDate(), f.gettDate());
        return data;
    }

    @PostMapping(path = "/getEquityExpData", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getEquityExpData(@RequestBody ChartOfAccountModel f) {
        List<Map<String, Object>> data = getEquityExpData(f.getFundCode(), f.getfDate(), f.gettDate());
        return data;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
