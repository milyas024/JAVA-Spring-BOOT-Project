/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import com.ams.model.AmlReportModel;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;

/**
 *
 * @author mohammad.bassam
 */
@RestController
@RequestMapping("/amlservice")
public class AmlController {

    @GetMapping()
    public List<Object> list() {
        return null;
    }

    @PostMapping(path = "/getDormantAccout", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> getDormantAccout(@RequestBody AmlReportModel model) {

        List<Map<String, Object>> data = null;
        try {
            if (model.pFromDate != null && !model.pFromDate.equals("") && model.pToDate != null && !model.pToDate.equals("")) {
                if (!busy) {
                    busy = true;
                    data = jdbcTemplate.queryForList("select ua.folio_number,\n"
                            + "       ua.title, decode(nvl(primary_client, 0),0,'Corporate', 'Individual') as account_type, (select to_char(max(closing_date),'MM/dd/yyyy') from unit_balance_electronic where folio_number = ua.folio_number) last_activity_date, "
                            + "     round(sysdate - (select max(closing_date) from unit_balance_electronic where folio_number = ua.folio_number)) as days , \n"
                            + "       to_char(get_folio_total_balance(ua.folio_number)) as balance\n"
                            + "  from unit_account ua\n"
                            + " where (select max(closing_date)\n"
                            + "          from unit_balance_electronic ube\n"
                            + "         where ua.folio_number = ube.folio_number) not between\n"
                            + "       to_date('" + model.pFromDate + "', 'dd/MM/yyyy') and\n"
                            + "       to_date('" + model.pToDate + "', 'dd/MM/yyyy') and ua.folio_number not like '%:%' \n"
                            + " group by ua.folio_number, ua.title, primary_client\n"
                            + " order by to_date(last_activity_date , 'MM/dd/yyyy') ");//and rownum<=5
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

    @PostMapping(path = "/kycCheckListReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> kycCheckListReport(@RequestBody AmlReportModel model) {

        List<Map<String, Object>> data = null;
        try {
            //if (model.pFromDate != null && !model.pFromDate.equals("") && model.pToDate != null && !model.pToDate.equals("")) {
            if (!busy) {
                busy = true;
                data = jdbcTemplate.queryForList("select ua.folio_number,\n"
                        + "       ua.title,\n"
                        + "       client_code,\n"
                        + "       decode(nvl(primary_client, 0), 0, 'Corporate', 'Individual') as account_type,\n"
                        + "       CASE (select 0 from dual where cnic_nicop_poc_arc_pr_nara = 0 and passport=0 and visa_details_pr_card=0 and form_b_crc_frc=0 and certificate_of_incorporation=0) WHEN 0 THEN 'INVALID' ELSE 'VALID' END as Identity_Proof,\n"
                        + "       CASE (select 0 from dual where sales_tax_registration = 0 and visiting_or_busin_card=0 and certi_membrshp_trd_bodies=0 and employment_card=0 and student_identity_card=0 and utility_bill_busins_addrs=0 and land_ownership_document=0 and bank_certi_declaration_proprie=0 and any_other_doc_empl_or_busins=0) WHEN 0 THEN 'INVALID' ELSE 'VALID' END as Business_Proof,\n"
                        + "       CASE (select 0 from dual where sala_slip_or_appoi_let = 0 and succen_cert_issued_by_court=0 and benefi_fund_slip=0 and gift_lineal_ascen_desce_husb=0 and lottery_prize_money_slip=0 and rental_agreement=0 and foreign_remittance_proof=0 and any_other_doc_funds=0) WHEN 0 THEN 'INVALID' ELSE 'VALID' END as Source_Funds_Income_Proof,\n"
                        + " CASE (select 0 from dual where resolution_bod_gover_body = 0 and memo_arti_assoc=0 and list_of_directors_comp_ordi=0 and form_29_wherever_applicable=0 and certi_copies_registr_trust=0 and certi_copy_pwr_attorney=0 and annual_financial_statements=0 and share_holding_pattern=0 and sales_tax_regis=0 and declaration_source_funds=0 and risk_profile_of_investor=0 and crs_self_declaration_forms=0 and internal_fatca_forms=0 and kyc_cdd_form=0) WHEN 0 THEN 'INVALID' ELSE 'VALID' END as Corporate_Investors "
                        + "  from client_checklist ck, unit_account ua\n"
                        + " where ua.folio_number = ck.folio_number and ((cnic_nicop_poc_arc_pr_nara = 0 and passport=0 and visa_details_pr_card=0 and form_b_crc_frc=0 and certificate_of_incorporation=0)\n"
                        + " or (visiting_or_busin_card=0 and certi_membrshp_trd_bodies=0 and employment_card=0 and student_identity_card=0 and utility_bill_busins_addrs=0 and land_ownership_document=0 and bank_certi_declaration_proprie=0 and any_other_doc_empl_or_busins=0)\n"
                        + " or (sala_slip_or_appoi_let = 0 and succen_cert_issued_by_court=0 and benefi_fund_slip=0 and gift_lineal_ascen_desce_husb=0 and lottery_prize_money_slip=0 and rental_agreement=0 and foreign_remittance_proof=0 and any_other_doc_funds=0)\n"
                        + " or (resolution_bod_gover_body = 0 and memo_arti_assoc=0 and list_of_directors_comp_ordi=0 and form_29_wherever_applicable=0 and certi_copies_registr_trust=0 and certi_copy_pwr_attorney=0 and annual_financial_statements=0 and share_holding_pattern=0 and sales_tax_regis=0 and declaration_source_funds=0 and risk_profile_of_investor=0 and crs_self_declaration_forms=0 and internal_fatca_forms=0 and kyc_cdd_form=0) )");//and rownum<=5
            } else {
                return null;
            }
            // }

        } catch (Exception e) {
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

    @PostMapping(path = "/suspeciousTransactionReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> suspeciousTransactionReport(@RequestBody AmlReportModel model) {

        // initial investment account
        List<Map<String, Object>> data = null;
        try {
            //if (model.pFromDate != null && !model.pFromDate.equals("") && model.pToDate != null && !model.pToDate.equals("")) {
            if (!busy) {
                busy = true;
                data = jdbcTemplate.queryForList("select ube.folio_number, to_char(ua.account_date , 'dd/MM/yyyy') account_date, sum(pd.amount) amount , (select sum(pd.amount)\n"
                        + "          from unit_sale uss, payment_detail pd\n"
                        + "         where uss.folio_number = ua.folio_number and uss.payment_id = pd.payment_id) currAmount , (select count(1) from unit_sale uss where uss.folio_number = ua.folio_number) investment_count, (select count(1) from unit_redemption uss where uss.folio_number = ua.folio_number) redemp_count, (select nvl(round(sum(urr.uncertified_quantity*urr.nav) , 2) , 0) from unit_redemption urr where urr.folio_number = ua.folio_number) TOTAL_REDEM_AMOUNT \n"
                        + "  from unit_sale ube, unit_account ua, payment_detail pd\n"
                        + " where ube.folio_number = ua.folio_number\n"
                        + " and ube.payment_id = pd.payment_id \n"
                        + "   and ube.sale_id =\n"
                        + "       (select min(uss.sale_id)\n"
                        + "          from unit_sale uss\n"
                        + "         where uss.folio_number = ua.folio_number) \n"
                        + " group by ube.folio_number, ube.fund_code, ua.account_date, ua.folio_number \n"
                        + " order by ua.account_date desc");//and rownum<=5
            } else {
                return null;
            }
            // }

        } catch (Exception e) {
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

    @GetMapping(path = "/dataScreening")
    public List<Map<String, Object>> dataScreening(HttpServletRequest request) {

        String contextPath = request.getRealPath("/");
        String jasperFileName = contextPath + "/Content/data.xlsx";
        List<Map<String, Object>> data = null;

        Connection connection;
        try {
            FileInputStream excelFile = new FileInputStream(new File(jasperFileName));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "insert into TEMP_CLIENT1 (name, father, province, district) values (?, ?, ?,?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                String name = currentRow.getCell(1).toString();
                String father = currentRow.getCell(2).toString();
                String province = currentRow.getCell(3).toString();
                String district = currentRow.getCell(4).toString();

                final int batchSize = 500;
                int count = 0;
                ps.setString(1, name);
                ps.setString(2, father);
                ps.setString(3, province);
                ps.setString(4, district);
                ps.addBatch();
                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch(); // insert remaining records
            ps.close();
            connection.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

//    @GetMapping("/getReport/{fundcode}")
//    public void getReport(@PathVariable String fundcode, HttpServletRequest request, HttpServletResponse response) {
//
//        try {
//            FmrReport.run(jdbcTemplate, request, response);
//        } catch (Exception ex) {
//            Logger.getLogger(AmlController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    public static void run(JdbcTemplate jdbcTemplate, HttpServletRequest request, HttpServletResponse response) throws JRException, IOException, SQLException {
//
//        String contextPath = request.getRealPath("/");
//        String jasperFileName = contextPath + "/Content/report/";
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFileName + "play.jasper",
//                parameters, jdbcTemplate.getDataSource().getConnection());
//        File outDir = new File(jasperFileName + "FMR_REPORT.pdf");
//        outDir.delete();
//        JasperExportManager.exportReportToPdfFile(jasperPrint, jasperFileName + "FMR_REPORT.pdf");
//
//        File repFile = new File(jasperFileName + "FMR_REPORT.pdf");
//        if (repFile.exists()) {
//            FileInputStream fileInputStream = new FileInputStream(repFile);
//            response.setContentType("application/pdf");
//            response.addHeader("Content-Disposition", "attachment; filename=FMR_REPORT.pdf");
//            org.apache.commons.io.IOUtils.copy(fileInputStream, response.getOutputStream());
//            response.flushBuffer();
//        }
//    }

    @GetMapping("/{id}")
    public Object get(@PathVariable String id) {
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody Object input) {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Object input) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return null;
    }

    private static boolean busy = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

}
