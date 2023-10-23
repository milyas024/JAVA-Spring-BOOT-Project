/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import java.util.Map;
import java.util.List;
import com.ams.common.ResponseCodes;
import com.ams.dao.FmrFundBasicInfoMapper;
import com.ams.dao.FmrNoncompliantMapper;
import com.ams.dao.FundDefinitionRowMapper;
import com.ams.model.FmrFundBasicInfo;
import com.ams.model.FundDefinition;
import com.ams.model.MainModel;
import com.ams.model.fmr.FmrFundPerformance;
import com.ams.model.fmr.FmrNoncompliant;
import com.ams.utility.ReportDbUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author mohammad.bassam
 */
@Controller
@RestController
@RequestMapping("/fmr/fmr")
public class FmrController {

    @GetMapping()
    public List<Object> list() {
        return null;
    }

    @GetMapping("/generateReport")
    public MainModel get(@RequestParam(value = "transDate", required = false) String transDate) {
        ReportDbUtil r = new ReportDbUtil();
        MainModel resp = r.ExecuteFmrProcess(transDate, jdbcTemplate);
        return resp;
    }

    /* ------------ FUND DEFINITION SECTION ------------*/
    @PostMapping(path = "/getFrmDefinition", consumes = "application/json", produces = "application/json")
    public FundDefinition getFmrDefinition(@RequestBody FundDefinition input) {
        FundDefinition fundDef = getFrmDefinitionImpl(input);
        return fundDef;
    }

    @PostMapping(path = "/saveFundDefinition", consumes = "application/json", produces = "application/json")
    public Object saveFundDefinition(@RequestBody FundDefinition input) {
        FundDefinition fundDef = saveFrmDefinitionImpl(input);
        return fundDef;
    }

    @PostMapping(path = "/updateFundDefinition", consumes = "application/json", produces = "application/json")
    public Object updateFundDefinition(@RequestBody FundDefinition input) {
        FundDefinition fundDef = updateFundDefinitionImpl(input);
        return fundDef;
    }

    private FundDefinition getFrmDefinitionImpl(FundDefinition input) {
        FundDefinition fmr = new FundDefinition();
        try {
            String sql = "select to_char(fmr.trans_date, 'dd/MM/yyyy') trans_date, fmr.fund_code, fmr.members, f.fund_name, commentary, objective from fmr_def fmr, fund f where fmr.fund_code = f.fund_code and fmr.fund_code = ? and fmr.trans_date = to_date(?, 'dd/MM/yyyy')";
            fmr = (FundDefinition) jdbcTemplate.queryForObject(
                    sql, new Object[]{input.fundCode, input.transDate}, new FundDefinitionRowMapper());
            fmr.setResponse_code(ResponseCodes.SUCCESS);
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }
        return fmr;
    }

    private FundDefinition updateFundDefinitionImpl(FundDefinition fundDef) {

        FundDefinition fmr = new FundDefinition();
        try {
            String SQL = "update fmr_def set members = ? , commentary = ?, objective = ? where fund_code = ? and trans_date = to_date(?, 'dd/MM/yyyy')";
            int resp = jdbcTemplate.update(SQL, fundDef.getMembers(), fundDef.getCommentary(), fundDef.getObjective(), fundDef.getFundCode(), fundDef.getTransDate());

            if (resp == 1) {
                System.out.println("Updated Record with ID = " + fundDef.getFundCode());
                fmr.setResponse_code(ResponseCodes.SUCCESS);
            } else {
                fmr.setResponse_code(ResponseCodes.FAILURE);
            }

        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }

        return fmr;
    }

    private FundDefinition saveFrmDefinitionImpl(FundDefinition fundDef) {

        FundDefinition fmr = new FundDefinition();
        try {
            String sql = "INSERT \n"
                    + "WHEN not exists(SELECT 1 FROM fmr_def WHERE fund_code = ? and trans_date = last_day(to_date(to_char(add_months(get_system_date,-1),'YYYYMM'),'YYYYMM'))) \n"
                    + "THEN\n"
                    + "INTO fmr_def (fund_code, members, commentary, objective, trans_date) \n"
                    + "SELECT ?, ?, ?, ?, last_day(to_date(to_char(add_months(get_system_date,-1),'YYYYMM'),'YYYYMM')) FROM DUAL";
            int resp = jdbcTemplate.update(sql, fundDef.getFundCode(), fundDef.getFundCode(), fundDef.getMembers(), fundDef.getCommentary(), fundDef.getObjective());
            if (resp == 1) {
                fmr.setResponse_code(ResponseCodes.SUCCESS);
                System.out.println("Record inserted with ID = " + fundDef.getFundCode());
            } else {
                fmr.setResponse_code(ResponseCodes.FAILURE);
            }
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }
        return fmr;
    }

    @PostMapping(path = "/getFundBasicInfoList", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getFundBasicInfoList() {

        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select id, to_char(transdate, 'dd/MM/yyyy')  as trans_date, \n"
                    + "       f.fund_name,\n"
                    + "       fb.fund_type,\n"
                    + "       fb.category,\n"
                    + "       fb.benchmark,\n"
                    + "       fb.dealing_days,\n"
                    + "       fb.management_fee\n"
                    + "  from fmr_fund_basic_info fb, fund f\n"
                    + " where fb.fund_code = f.fund_code\n"
                    + " order by id desc");//and rownum<=5

        } catch (Exception e) {
            return data;
        }
        return data;
    }

    @PostMapping(path = "/getFundBasicInformation", consumes = "application/json", produces = "application/json")
    public FmrFundBasicInfo getFundBasicInformation(@RequestBody FmrFundBasicInfo input) {

        FmrFundBasicInfo fmr = new FmrFundBasicInfo();
        try {
            String sql = "select TRANSDATE, ID,fund_code, FUND_TYPE,CATEGORY,LAUNCHDATE,NETASSETS,NETASSETEXFOF,NAV,BENCHMARK,DEALING_DAYS,CUTT_OFF_TIME,PRICING_MECHANISM,\n"
                    + "MANAGEMENT_FEE,FRONT_END_LOAD,TRUSTEE,AUDITOR,ASSET_MANAGER_RATING,RISK_PROF_FUND,perform_ranking,FUND_MANGER,LISTING, COMMENTS,ter, nav nav1 from fmr_fund_basic_info t \n"
                    + "where id = ? ";
            fmr = (FmrFundBasicInfo) jdbcTemplate.queryForObject(
                    sql, new Object[]{input.id}, new FmrFundBasicInfoMapper());
            fmr.setResponse_code(ResponseCodes.SUCCESS);
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }
        return fmr;
    }

    @PostMapping(path = "/saveFundPerformance", consumes = "application/json", produces = "application/json")
    public FmrFundPerformance saveFundPerformance(@RequestBody FmrFundPerformance input) {
        List<Map<String, Object>> data = null;
        FmrFundPerformance fmr = new FmrFundPerformance();
        try {
            if (input.getFundCode() != null) {
                data = jdbcTemplate.queryForList("select 1 from fund_performence where fndid = '" + input.getFundCode() + "' and trans_date = to_date('" + input.getTransDate()+ "' , 'dd/MM/yyyy') ");//and rownum<=5
            }
            if (data != null && data.size() > 0) {
                String qry = "update fund_performence f set "
                        + " f.st_dev = ?, f.sharp_ratio = ?, f.alpha = ?  \n"
                        + "where fndid = '" + input.getFundCode() + "' and trans_date = to_date('" + input.getTransDate()+ "' , 'dd/MM/yyyy') and PERFORMENCE_TYPE='Fund' ";
                int resp = jdbcTemplate.update(qry, input.getStDev(), input.getSharpRatio(), input.getAlpha());                
                qry = "update fund_performence f set "
                        + " f.st_dev = ?, f.sharp_ratio = ?, f.alpha = ?  \n"
                        + "where fndid = '" + input.getFundCode() + "' and trans_date = to_date('" + input.getTransDate()+ "' , 'dd/MM/yyyy') and PERFORMENCE_TYPE='BenchMark' ";
                resp = jdbcTemplate.update(qry, input.getBstDev(), input.getBsharpRatio(), input.getBalpha());                
                if (resp == 1) {
                    fmr.setResponse_code(ResponseCodes.SUCCESS);
                } else {
                    fmr.setResponse_code(ResponseCodes.FAILURE);
                }
            }
        } catch (Exception e) {
            return fmr;
        }
        return fmr;
    }
    
    @PostMapping(path = "/saveFundBasicInformation", consumes = "application/json", produces = "application/json")
    public FmrFundBasicInfo saveFundBasicInformation(@RequestBody FmrFundBasicInfo input) {
        
        List<Map<String, Object>> data = null;
        FmrFundBasicInfo fmr = new FmrFundBasicInfo();
        try {
            if (input.id != null) {
                data = jdbcTemplate.queryForList("select 1 from fmr_fund_basic_info where id = '" + input.id + "'");//and rownum<=5
            }
            if (data != null && data.size() > 0) {
                String qry = "update fmr_fund_basic_info set "
                        + "fund_type = ?, category = ?, benchmark = ?, dealing_days = ?, cutt_off_time = ?, pricing_mechanism = ?, management_fee = ?, \n"
                        + "front_end_load = ?, trustee = ?, auditor = ?, asset_manager_rating = ?, risk_prof_fund = ?, perform_ranking = ?, fund_manger = ?, listing = ?, comments = ?, ter = ?, nav = ? \n"
                        + "where id = ? ";
                int resp = jdbcTemplate.update(qry, input.getFund_type(), input.getCategory(), input.getBenchmark(), input.getDealing_days(),
                        input.getCutt_off_time(), input.getPricing_mechanism(), input.getManagement_fee(), input.getFront_end_load(), input.getTrustee(), input.getAuditor(), input.getAsset_manager_rating(), input.getRisk_prof_fund(), input.getPerform_ranking(),
                        input.getFund_manger(), input.getListing(), input.getComments(), input.getFundTer(), input.getNav(), input.getId());
                if (resp == 1) {
                    fmr.setResponse_code(ResponseCodes.SUCCESS);
                } else {
                    fmr.setResponse_code(ResponseCodes.FAILURE);
                }
            } else {
                int resp = 0;
                if (isValidString(input.getFund_code()) && isValidString(input.getBenchmark()) && isValidString(input.getFund_type())) {
                    String qry = "INSERT INTO fmr_fund_basic_info (id , transdate, fund_code, fund_type, category, benchmark, dealing_days, cutt_off_time, pricing_mechanism, management_fee, \n"
                            + "front_end_load, trustee, auditor, asset_manager_rating, risk_prof_fund, perform_ranking, fund_manger, listing, comments, ter) \n"
                            + "SELECT (select nvl(max(id)+1,1) from fmr_fund_basic_info) , last_day(to_date(to_char(add_months(get_system_date,-1),'YYYYMM'),'YYYYMM')) ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? FROM DUAL ";
                    resp = jdbcTemplate.update(qry, input.getFund_code(), input.getFund_type(), input.getCategory(), input.getBenchmark(), input.getDealing_days(),
                            input.getCutt_off_time(), input.getPricing_mechanism(), input.getManagement_fee(), input.getFront_end_load(), input.getTrustee(), input.getAuditor(), input.getAsset_manager_rating(), input.getRisk_prof_fund(), input.getPerform_ranking(),
                            input.getFund_manger(), input.getListing(), input.getComments(), input.getFundTer());
                    if (resp == 1) {
                        fmr.setResponse_code(ResponseCodes.SUCCESS);
                    } else {
                        fmr.setResponse_code(ResponseCodes.FAILURE);
                    }
                }
            }
        } catch (Exception e) {
            return fmr;
        }
        return fmr;
    }

    @PostMapping(path = "/getFundDefList", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getFundDefList() {

        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select id,\n"
                    + "       to_char(trans_date, 'dd/MM/yyyy') as trans_date,\n"
                    + "       f.fund_name,\n"
                    + "       SUBSTR(members ,0, 20) as members,\n"
                    + "       SUBSTR(objective ,0, 20) as objective,\n"
                    + "       SUBSTR(commentary ,0, 20) as commentary\n"
                    + "  from fmr_def fb, fund f\n"
                    + " where fb.fund_code = f.fund_code\n"
                    + " order by id desc");//and rownum<=5
        } catch (Exception e) {
            return data;
        }
        return data;
    }

    @PostMapping(path = "/getFundPerformanceList", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getFundPerformanceList(@RequestBody FundDefinition input) {
        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select f.id, to_char(trans_date , 'dd/MM/yyyy') trans_date, fund_name, f.st_dev, f.sharp_ratio, f.alpha, performence_type \n"
                    + "  from fund_performence f, fund ff\n"
                    + " where fndid = '"+input.getFundCode()+"' and trans_date = to_date('"+input.getTransDate()+"' , 'dd/MM/yyyy') \n"
                    + "   and fndid = fund_code\n"
                    + " order by performence_type");
        } catch (Exception e) {
            return data;
        }
        return data;
    }
    
    @PostMapping(path = "/getFundNonComplaintList", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getFundNonComplaintList(@RequestBody FundDefinition input) {
        List<Map<String, Object>> data = null;
        try {
            data = jdbcTemplate.queryForList("select fund_code,\n" +
                                "       transdate,\n" +
                                "       non_complaint_investment,\n" +
                                "       type_of_investment, \n" +
                                "       exposure_limit, \n" +
                                "       percent_of_net_asset,   \n" +
                                "       percent_of_total_asset, \n" +
                                "       excess_exposure_per_of_net, \n" +
                                "       excess_exposure_per_of_total    \n" +
                                "  from fmr_non_complaints_invest   \n" +
                                " where fund_code = '"+input.getFundCode()+"' " +
                                "   and transdate = to_date('"+input.getTransDate()+"', 'dd/MM/yyyy')");
        } catch (Exception e) {
            return data;
        }
        return data;
    }
    
    @PostMapping(path = "/getFundDefInformation", consumes = "application/json", produces = "application/json")
    public FundDefinition getFundDefInformation(@RequestBody FundDefinition input) {
        FundDefinition fmr = new FundDefinition();
        try { String sql = "select id,\n"
                    + "       to_char(trans_date, 'dd/MM/yyyy') as trans_date,\n"
                    + "       f.fund_code, f.fund_name,\n"
                    + "       members,\n"
                    + "       objective,\n"
                    + "       commentary \n"
                    + "  from fmr_def fb, fund f\n"
                    + " where fb.fund_code = f.fund_code and id = ? ";
            fmr = (FundDefinition) jdbcTemplate.queryForObject(
                    sql, new Object[]{input.id}, new FundDefinitionRowMapper());
            fmr.setResponse_code(ResponseCodes.SUCCESS);
        } catch (Exception ex) {
            fmr.setResponse_code(ResponseCodes.FAILURE);
        }

        return fmr;
    }

    @PostMapping(path = "/deleteFundDef", consumes = "application/json", produces = "application/json")
    public Object deleteFundDef(@RequestBody FundDefinition fundDef) {

        FundDefinition fmr = new FundDefinition();
        try {
            String sql = "delete from fmr_def where id = '" + fundDef.getId() + "' ";
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

    @PostMapping(path = "/saveFundDef", consumes = "application/json", produces = "application/json")
    public FundDefinition saveFundDef(@RequestBody FundDefinition input) {

        List<Map<String, Object>> data = null;
        FundDefinition fmr = new FundDefinition();
        try {
            if (input.id != null) {
                data = jdbcTemplate.queryForList("select 1 from fmr_def where id = '" + input.id + "'");//and rownum<=5
            }
            if (data != null && data.size() > 0) {
                String qry = "update fmr_def\n"
                        + "   set fund_code = ?,\n"
                        + "   members = ?,\n"
                        + "   objective = ?,\n"
                        + "   commentary = ? \n"
                        + " where id = ? ";
//            String qry = "update fmr_def\n"
//                        + "   set members = ? ";
                int resp = jdbcTemplate.update(qry, input.getFundCode(), input.getMemberString(), input.getObjective(), input.getCommentary(), input.getId());
                //int resp = jdbcTemplate.update(qry, input.getMemberString());
                if (resp == 1) {
                    fmr.setResponse_code(ResponseCodes.SUCCESS);
                } else {
                    fmr.setResponse_code(ResponseCodes.FAILURE);
                }
            } else {
                int resp = 0;
                if (isValidString(input.getFundCode())) {
                    String qry = "INSERT INTO fmr_def (id , TRANS_DATE, fund_code, members, objective, commentary , left_header, right_header)\n"
                            + "SELECT (select nvl(max(id)+1,1) from fmr_def) , last_day(add_months(get_system_date, -1)) ,?,?,?,?, '" + getLeftHeader(input.getFundCode()) + "', '" + getRightHeader(input.getFundCode()) + "' FROM DUAL ";
                    resp = jdbcTemplate.update(qry, input.getFundCode(), input.getMembers(), input.getObjective(), input.getCommentary());
                    if (resp == 1) {
                        fmr.setResponse_code(ResponseCodes.SUCCESS);
                    } else {
                        fmr.setResponse_code(ResponseCodes.FAILURE);
                    }
                }
            }
        } catch (Exception e) {
            return fmr;
        }

        return fmr;
    }

    private boolean isValidString(String str) {
        return str != null && !str.equals("") ? true : false;
    }

    private String getLeftHeader(String fundCode) {
        String header = "";
        if (fundCode.equals("00001")) {
            header = "AIF_left_header.png";
        } else if (fundCode.equals("00003")) {
            header = "Cash_left_header.png";
        } else if (fundCode.equals("00004")) {
            header = "AIIF_left_header.png";
        } else if (fundCode.equals("00005")) {
            header = "GSF_left_header.png";
        }
        return header;
    }

    private String getRightHeader(String fundCode) {
        String header = "";
        if (fundCode.equals("00001")) {
            header = "AIF_right_header.png";
        } else if (fundCode.equals("00003")) {
            header = "Cash_right_header.png";
        } else if (fundCode.equals("00004")) {
            header = "AIIF_right_header.png";
        } else if (fundCode.equals("00005")) {
            header = "GSF_right_header.png";
        }
        return header;
    }
    
    // ------------------ fmr non compliant investment --------------------------
    @PostMapping(path = "/getNoncompliantDetails", consumes = "application/json", produces = "application/json")
    public FmrNoncompliant getNoncompliantDetails(@RequestBody FmrFundBasicInfo input) {

        FmrNoncompliant fmr = new FmrNoncompliant();
try {
    String sql = "select fund_code, \n" +
"       to_char(transdate , 'dd/MM/yyyy') transdate ,  \n" +
"       non_complaint_investment,   \n" +
"       type_of_investment, \n" +
"       exposure_limit, \n" +
"       percent_of_net_asset,   \n" +
"       percent_of_total_asset, \n" +
"       excess_exposure_per_of_net, \n" +
"       excess_exposure_per_of_total    \n" +
"  from fmr_non_complaints_invest   \n" +
" where non_complaint_investment = 'DHCL'   \n" +
"   and transdate = '28-feb-2019'   ";
    fmr = (FmrNoncompliant) jdbcTemplate.queryForObject(
            sql, new Object[]{}, new FmrNoncompliantMapper());
    fmr.setResponse_code(ResponseCodes.SUCCESS);
} catch (Exception ex) {
    fmr.setResponse_code(ResponseCodes.FAILURE);
}
        return fmr;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
