/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import com.ams.model.FmrPdfReportModal;
import com.ams.model.FmrPlanPdfReportModal;
import com.ams.utility.MediaReplacedElementFactory;
import com.ams.utility.ReportDbUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.templatemode.TemplateMode;

@Controller
public class MainController {

    @RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
    public String welcomePage(Model model) throws IOException {
        model.addAttribute("title", "Welcome");
        model.addAttribute("message", "This is welcome page!");
        return "welcomePage";
    }

    @RequestMapping(value = {"/fmr"}, method = RequestMethod.GET)
    public String fmrPage(Model model) throws IOException {
        model.addAttribute("title", "Welcome");
        model.addAttribute("message", "This is welcome page!");
        return "fmrSection";
    }

    private static final String OUTPUT_FILE = "FMR_REPORT.pdf";

    @RequestMapping(value = "/fmrExportPdf", method = RequestMethod.GET)
    public String fmrExportPdf(ModelMap model, HttpServletRequest request, @RequestParam(value = "transDate", required = false) String transDate) {

        String contextPath = request.getRealPath("/");
        String jasperFileName = contextPath + "/Content/" + OUTPUT_FILE;
        if ((new File(jasperFileName)).exists()) {
            new File(jasperFileName).delete();
        }
        try {
            ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
            emailTemplateResolver.setPrefix("/templates/");
            emailTemplateResolver.setSuffix(".html");
            emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
            emailTemplateResolver.setCacheable(false);
            emailTemplateResolver.setCharacterEncoding("UTF-8");
            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(emailTemplateResolver);
            List<FmrPdfReportModal> fif_report_data = new ArrayList(), eqf_report_data = new ArrayList(), cpf_report_data = new ArrayList();
            List<FmrPlanPdfReportModal> plan_report_data = new ArrayList(), pf_report_data = new ArrayList();
            Context context = new Context();

            ReportDbUtil rpt = new ReportDbUtil();
            rpt.FillModelMap(fif_report_data, null, transDate, contextPath, context, jdbcTemplate);
            rpt.EqfModalMap(eqf_report_data, null, transDate, contextPath, context, jdbcTemplate);
            rpt.FundPlanModalMap(plan_report_data, null, transDate, contextPath, context, jdbcTemplate);
            
//            ReportDbUtilVps rptvps = new ReportDbUtilVps();            
//            rptvps.VpsModalMap(pf_report_data, null, transDate, contextPath, context, jdbcTemplate);

            File ff = new File(jasperFileName);
            ff.createNewFile();
            String html = templateEngine.process("fmrPdfTemplate", context);
            OutputStream outputStream = new FileOutputStream(jasperFileName);
            ITextRenderer renderer = new ITextRenderer();
            renderer.getSharedContext().setReplacedElementFactory(new MediaReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory(), contextPath));
            renderer.setDocumentFromString(html);
            renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\impact.TTF", true);
            renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\calibri.TTF", true);
            renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\arial.TTF", true);
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
            return "fmrPdfTemplate";
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "fmrPdfTemplate";
    }

    private String getFmrDate(String transDate) {
        String dt = "";
        String fperformance_sql = "select to_char(to_date('" + transDate + "' , 'dd/MM/yyyy') , 'MONTHYYYY') fmr_trans_date from dual ";
        List<Map<String, Object>> lst = jdbcTemplate.queryForList(fperformance_sql);
        for (Map<String, Object> f : lst) {
            dt = f.get("fmr_trans_date".toUpperCase()).toString();
        }
        return dt;
    }

//    private List<Map<String, Object>> getPerformance1(FundDefinition fundDef, String transDate) {
//        String fperformance_sql = "select decode(PERFORMENCE_TYPE, 'Fund', descp, PERFORMENCE_TYPE) descp, B_MONTH1, bYTD, ST_DEV, SHARP_RATIO, ALPHA \n"
//                + "  from fund_performence\n"
//                + " where FNDID = '" + fundDef.getFundCode() + "'\n"
//                + "   and trans_date = to_date('" + transDate + "' , 'dd/MM/yyyy') order by descp ";
//        return jdbcTemplate.queryForList(fperformance_sql);
//    }
//
//    private List<Map<String, Object>> getPerformance2(FundDefinition fundDef, String transDate) {
//        String fperformance_sql1 = "select B_MONTH3, B_MONTH6 ,        B_YEAR1,        B_YEAR3,        B_YEAR5,        B_SINCE, decode(PERFORMENCE_TYPE, 'Fund', descp, PERFORMENCE_TYPE) descp \n"
//                + "  from fund_performence\n"
//                + " where FNDID = '" + fundDef.getFundCode() + "'\n"
//                + "   and trans_date = to_date('" + transDate + "' , 'dd/MM/yyyy') order by descp ";
//        return jdbcTemplate.queryForList(fperformance_sql1);
//    }
    
//    private void populateTopTfcSection(FmrPdfReportModal fpModal, String fundcode, String transDate) {
//        FmrTopTfcModel model = new FmrTopTfcModel();
//        String top_tfc = "select SCHEME_NAME, PREVIOUS_ASSET_PERCENTAGE , TOTAL_ASSET_PERCENTAGE, (select sum(TOTAL_ASSET_PERCENTAGE) from fmr_top_tfc_sukuk_holding where FUND_CODE = '" + fundcode + "' and TRANS_DATE = to_date('" + transDate + "', 'dd/MM/yyyy')) TOTAL_ASSET_PERCENTAGESUM "
//                + "from fmr_top_tfc_sukuk_holding where FUND_CODE='" + fundcode + "' and TRANS_DATE=to_date('" + transDate + "' , 'dd/MM/yyyy') order by TOTAL_ASSET_PERCENTAGE desc ";
//        List<Map<String, Object>> top_tfc_data = jdbcTemplate.queryForList(top_tfc);
//        for (Map row : top_tfc_data) {
//            if (row.get("TOTAL_ASSET_PERCENTAGESUM") != null) {
//                model.setTotal(row.get("TOTAL_ASSET_PERCENTAGESUM").toString());
//            }
//        }
//        model.setTfcData(top_tfc_data);
//        fpModal.setTopTfcModel(model);
//    }

//    private void populateTopTenSection(FmrPdfReportModal fpModal, String fundcode, String transDate) {
//        FmrTopTfcModel model = new FmrTopTfcModel();
//        String top_tfc = "select SCHEME_NAME, PREVIOUS_ASSET_PERCENTAGE , TOTAL_ASSET_PERCENTAGE, (select sum(TOTAL_ASSET_PERCENTAGE) from fmr_top_ten_holding where FUND_CODE = '" + fundcode + "' and TRANS_DATE = to_date('" + transDate + "', 'dd/MM/yyyy')) TOTAL_ASSET_PERCENTAGESUM "
//                + "from fmr_top_ten_holding where FUND_CODE='" + fundcode + "' and TRANS_DATE=to_date('" + transDate + "' , 'dd/MM/yyyy') order by TOTAL_ASSET_PERCENTAGE desc ";
//        List<Map<String, Object>> top_tfc_data = jdbcTemplate.queryForList(top_tfc);
//        for (Map row : top_tfc_data) {
//            if (row.get("TOTAL_ASSET_PERCENTAGESUM") != null) {
//                model.setTotal(row.get("TOTAL_ASSET_PERCENTAGESUM").toString());
//            }
//        }
//        model.setTfcData(top_tfc_data);
//        fpModal.setTopTenHoldingModel(model);
//    }

//    private List<Map<String, Object>> getAssetAllocHeader(String fundcode, String transDate) {
//        String fassetallocation_sql = "select to_char(p.aa_trans_date , 'Monthdd,yyyy') prev, to_char(c.aa_trans_date , 'Monthdd,yyyy') curr \n"
//                + "  from fmr_prev_asset_alloocation p, fmr_current_asset_alloocation c\n"
//                + " where p.fund_code = '" + fundcode + "'\n"
//                + "   and p.aa_trans_date = last_day(to_date(to_char(add_months(to_date('" + transDate + "', 'dd/MM/yyyy'), -1), 'YYYYMM'), 'YYYYMM'))\n"
//                + "   and c.aa_trans_date = to_date('" + transDate + "', 'dd/MM/yyyy') ";
//        List<Map<String, Object>> fif_perform_data1 = jdbcTemplate.queryForList(fassetallocation_sql);
//        return fif_perform_data1;
//    }
//
//    private FmrAssetAllocMain getAssetAllocBody(FmrPdfReportModal fpModal, String fundcode, String transDate, boolean filterByName) {
//
//        FmrAssetAllocMain assetAllocMain = new FmrAssetAllocMain();
//        List<fmr_asset_allocation_model> currlist = new ArrayList<fmr_asset_allocation_model>();
//        String prev_sql = "select code, aa_asset_description, aa_prevmth, aa_currmth ,(select sum(aa_prevmth) from fmr_prev_asset_alloocation where fund_code = 1 and aa_trans_date = last_day(to_date(to_char(add_months(to_date('" + transDate + "','dd/MM/yyyy'),-1),'YYYYMM'),'YYYYMM'))) aa_prevmthTotal \n"
//                + "  from fmr_prev_asset_alloocation\n"
//                + " where fund_code = '" + fundcode + "' and code is not null \n"
//                + "   and aa_trans_date = last_day(to_date(to_char(add_months(to_date('" + transDate + "', 'dd/MM/yyyy'), -1), 'YYYYMM'), 'YYYYMM')) ";
//        List<Map<String, Object>> prev_data = jdbcTemplate.queryForList(prev_sql);
//        String curr_sql = "select code, aa_asset_description, aa_prevmth, aa_currmth , (select sum(aa_currmth) from fmr_current_asset_alloocation  where fund_code = '" + fundcode + "' and aa_trans_date = to_date('" + transDate + "', 'dd/MM/yyyy')) aa_currmthTotal\n"
//                + "  from fmr_current_asset_alloocation\n"
//                + " where fund_code = '" + fundcode + "' and code is not null \n"
//                + "   and aa_trans_date = to_date('" + transDate + "', 'dd/MM/yyyy') order by AA_CURRMTH ";
//        List<Map<String, Object>> curr_data = jdbcTemplate.queryForList(curr_sql);
//        if (prev_data.size() > curr_data.size()) {
//            for (Map row : prev_data) {
//                fmr_asset_allocation_model assetAllocModel = new fmr_asset_allocation_model();
//                assetAllocModel.setCode(row.get("CODE").toString());
//                assetAllocModel.setSecName(row.get("AA_ASSET_DESCRIPTION").toString());
//                if (row.get("AA_PREVMTH") != null) {
//                    assetAllocModel.setPrev_month(row.get("AA_PREVMTH").toString());
//                    assetAllocModel.setCurr_month("0");
//                }
//                if (row.get("AA_PREVMTHTOTAL") != null) {
//                    assetAllocMain.setTotalPrevAlloc(row.get("AA_PREVMTHTOTAL").toString());
//                }
//                currlist.add(assetAllocModel);
//            }
//            for (fmr_asset_allocation_model crow : currlist) {
//                for (Map prow : curr_data) {
//                    String pr = prow.get("CODE").toString();
//                    String cr = crow.getCode();
//                    if (cr.equals(pr)) {
//                        crow.setCurr_month(prow.get("AA_CURRMTH").toString());
//                    }
//                    if (prow.get("AA_CURRMTHTOTAL") != null) {
//                        assetAllocMain.setTotalCurrAlloc(prow.get("AA_CURRMTHTOTAL").toString());
//                    }
//                }
//            }
//            String curr_new_sql = "select code, aa_asset_description, aa_prevmth, aa_currmth , (select sum(aa_currmth) from fmr_current_asset_alloocation  where fund_code = '" + fundcode + "' and aa_trans_date = to_date('" + transDate + "', 'dd/MM/yyyy')) aa_currmthTotal\n"
//                    + "  from fmr_current_asset_alloocation\n"
//                    + " where fund_code = '" + fundcode + "' and code is not null \n"
//                    + "   and aa_trans_date = to_date('" + transDate + "', 'dd/MM/yyyy') and code not in (select code from fmr_prev_asset_alloocation where fund_code = '" + fundcode + "' and aa_trans_date = last_day(to_date(to_char(add_months(to_date('" + transDate + "','dd/MM/yyyy'),-1),'YYYYMM'),'YYYYMM'))) order by AA_CURRMTH ";
//            List<Map<String, Object>> curr_new_data = jdbcTemplate.queryForList(curr_new_sql);
//            for (Map row : curr_new_data) {
//                fmr_asset_allocation_model assetAllocModel = new fmr_asset_allocation_model();
//                assetAllocModel.setCode(row.get("CODE").toString());
//                assetAllocModel.setSecName(row.get("AA_ASSET_DESCRIPTION").toString());
//                if (row.get("AA_CURRMTH") != null) {
//                    assetAllocModel.setCurr_month(row.get("AA_CURRMTH").toString());
//                    assetAllocModel.setPrev_month("0");
//                }
//                if (row.get("AA_CURRMTHTOTAL") != null) {
//                    assetAllocMain.setTotalCurrAlloc(row.get("AA_CURRMTHTOTAL").toString());
//                }
//                currlist.add(assetAllocModel);
//            }
//            for (fmr_asset_allocation_model crow : currlist) {
//                for (Map prow : prev_data) {
//                    String pr = prow.get("CODE").toString();
//                    String cr = crow.getCode();
//                    if (cr.equals(pr)) {
//                        crow.setPrev_month(prow.get("AA_PREVMTH").toString());
//                    }
//                    if (prow.get("AA_PREVMTHTOTAL") != null) {
//                        assetAllocMain.setTotalPrevAlloc(prow.get("AA_PREVMTHTOTAL").toString());
//                    }
//                }
//            }
//
//        } else {
//            for (Map row : curr_data) {
//                fmr_asset_allocation_model assetAllocModel = new fmr_asset_allocation_model();
//                assetAllocModel.setCode(row.get("CODE").toString());
//                assetAllocModel.setSecName(row.get("AA_ASSET_DESCRIPTION").toString());
//                if (row.get("AA_CURRMTH") != null) {
//                    assetAllocModel.setCurr_month(row.get("AA_CURRMTH").toString());
//                    assetAllocModel.setPrev_month("0");
//                }
//                if (row.get("AA_CURRMTHTOTAL") != null) {
//                    assetAllocMain.setTotalCurrAlloc(row.get("AA_CURRMTHTOTAL").toString());
//                }
//                currlist.add(assetAllocModel);
//            }
//            for (fmr_asset_allocation_model crow : currlist) {
//                for (Map prow : prev_data) {
//                    String pr = prow.get("CODE").toString();
//                    String cr = crow.getCode();
//                    if (cr.equals(pr)) {
//                        crow.setPrev_month(prow.get("AA_PREVMTH").toString());
//                    }
//                    if (prow.get("AA_PREVMTHTOTAL") != null) {
//                        assetAllocMain.setTotalPrevAlloc(prow.get("AA_PREVMTHTOTAL").toString());
//                    }
//                }
//            }
//        }
//        assetAllocMain.setFif_prev_asset_alloc(fmr_asset_allocation_model.filter(currlist, filterByName));
//        fpModal.setAssetAllocMain(assetAllocMain);
//        return assetAllocMain;
//    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model) {

        return "loginPage";
    }

    @RequestMapping(value = "/logoutSuccessful", method = RequestMethod.GET)
    public String logoutSuccessfulPage(Model model) {
        model.addAttribute("title", "Logout");
        return "welcomePage";
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
