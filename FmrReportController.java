/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import com.ams.common.ResponseCodes;
import com.ams.dao.FmrDao;
import com.ams.dao.FmrFundBasicInfoMapper;
import com.ams.dao.FundDefinitionRowMapper;
import com.ams.model.FmrFundBasicInfo;
import com.ams.model.FundDefinition;
import com.ams.model.fmr_asset_allocation_model;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 *
 * @author mohammad.bassam
 */
@Controller
public class FmrReportController {

    private static final String OUTPUT_FILE = "C:\\Users\\mohammad.bassam\\Desktop/message.pdf";
    private static final String UTF_8 = "UTF-8";

    @RequestMapping(value = "/fmr/fmrExportPdf", method = RequestMethod.GET)
    public String fmrExportPdf(ModelMap model) {

        if ((new File(OUTPUT_FILE)).exists()) {
            new File(OUTPUT_FILE).delete();
        }

        try {
            ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
            emailTemplateResolver.setPrefix("/templates/");
            emailTemplateResolver.setTemplateMode("HTML5");
            emailTemplateResolver.setSuffix(".html");
            emailTemplateResolver.setTemplateMode("XHTML");
            emailTemplateResolver.setCharacterEncoding("UTF-8");

            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(emailTemplateResolver);

            Context context = fillModelMap(model);
            //context.setVariable("fif_def", fif_def);
            //context.setVariable("fif_basicInfo", fif_basicInfo);

            String html = templateEngine.process("fmrPdfTemplate", context);// IOUtils.toString(new FileInputStream("D:\\Users\\mohammad.bassam\\Documents\\PdfProgram\\src\\pdfprogram\\test.html"));

//            OutputStream outputStream = new FileOutputStream(OUTPUT_FILE);
//            ITextRenderer renderer = new ITextRenderer();
//            renderer.getSharedContext().setReplacedElementFactory(new MediaReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory()));
//            renderer.setDocumentFromString(html);
//            renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\impact.TTF", true);
//            renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\calibri.TTF", true);
//            renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\arial.TTF", true);
//            renderer.layout();
//            renderer.createPDF(outputStream);
//            outputStream.close();

//            if ((new File(OUTPUT_FILE)).exists()) {
//                Process p = Runtime
//                        .getRuntime()
//                        .exec("rundll32 url.dll,FileProtocolHandler " + OUTPUT_FILE);
//                p.waitFor();
//            }
            return "";
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public Context fillModelMap(ModelMap model) {

        Context context = new Context();
        FmrDao dao = new FmrDao();
        FundDefinition input = new FundDefinition();
        input.setFundCode("00001");
        input.setTransDate("31/10/2018");
        FundDefinition fif_def = new FundDefinition();
        FmrFundBasicInfo fif_basicInfo = new FmrFundBasicInfo();

        try {

            String sql = "select id, to_char(fmr.trans_date, 'dd/MM/yyyy') trans_date, fmr.fund_code, fmr.members, f.fund_name, commentary, objective from fmr_def fmr, fund f where fmr.fund_code = f.fund_code and fmr.fund_code = ? and fmr.trans_date = to_date(?, 'dd/MM/yyyy')";
            fif_def = (FundDefinition) jdbcTemplate.queryForObject(
                    sql, new Object[]{input.fundCode, input.transDate}, new FundDefinitionRowMapper());

            String fif_basicInfo_sql = "select TRANSDATE, ID,fund_code, FUND_TYPE,CATEGORY,LAUNCHDATE,NETASSETS,NETASSETEXFOF,NAV,BENCHMARK,DEALING_DAYS,CUTT_OFF_TIME,PRICING_MECHANISM,\n"
                    + "MANAGEMENT_FEE,FRONT_END_LOAD,TRUSTEE,AUDITOR,ASSET_MANAGER_RATING,RISK_PROF_FUND,perform_ranking,FUND_MANGER,LISTING, COMMENTS from fmr_fund_basic_info t\n"
                    + "where fund_code = 00001 and transdate = '31-oct-2018'";
            fif_basicInfo = (FmrFundBasicInfo) jdbcTemplate.queryForObject(
                    fif_basicInfo_sql, new Object[]{}, new FmrFundBasicInfoMapper());
            String fperformance_sql = "select descp, B_MONTH1, YTD, ST_DEV, SHARP_RATIO, ALPHA\n"
                    + "  from fund_performence\n"
                    + " where FNDID = 00001\n"
                    + "   and trans_date = '31-OCT-2018'";
            List<Map<String, Object>> fif_perform_data = jdbcTemplate.queryForList(fperformance_sql);

            List<Map<String, Object>> fif_asset_header = getAssetAllocHeader("00001");
            List<fmr_asset_allocation_model> fif_prev_asset_alloc = getAssetAllocBody("00001");

            String fperformance_sql1 = "select descp, B_MONTH3, B_MONTH6, B_YEAR1, B_YEAR3, B_YEAR5, B_SINCE\n"
                    + "  from fund_performence\n"
                    + " where FNDID = 00001\n"
                    + "   and trans_date = '31-OCT-2018'";
            List<Map<String, Object>> fif_perform_data1 = jdbcTemplate.queryForList(fperformance_sql1);

            String fmr_tech_info_sql = "select transdate, description, value\n"
                    + "  from fmr_tech_info\n"
                    + " where FUND_CODE = 00001\n"
                    + "   and TRANSDATE = '31-OCT-2018'";
            List<Map<String, Object>> fmr_tech_info_sql_data = jdbcTemplate.queryForList(fmr_tech_info_sql);

            String fmr_top_tfc_sukuk_holding_sql = "select * from fmr_top_tfc_sukuk_holding where FUND_CODE=$P{sakuk_fund_code} and TRANS_DATE='31-OCT-2018'";
            String fmr_non_complaints_invest_sql = "select * from fmr_non_complaints_invest where FUND_CODE = $P{noncompliant_fund_code} and TRANSDATE='31-OCT-2018'";

            //bar chart query
            String fmr_barchart_sql = "select g.transdate,\n"
                    + "       g.bm_return,\n"
                    + "       g.fund_return,\n"
                    + "       (select f.fund_short_name from fund f where f.fund_code = 00001) as title\n"
                    + "  from FMR_FUND_PERFORMENCE_GRAPH g\n"
                    + " where g.fund_code = 00001\n"
                    + "   and g.transdate between add_months(to_date('31-OCT-2018'), '-12') and\n"
                    + "       '31-OCT-2018'\n"
                    + " order by g.transdate";

            //pie chart query, rating , vv
            String fmr_piechart_sql = "select rating, sum(perofnav) vv\n"
                    + "  from FMR_CreditQuality\n"
                    + " where fund_code = 00001\n"
                    + "   and transdate = to_date('31-OCT-2018')\n"
                    + "   and perofnav > 0\n"
                    + " group by rating";

//            List<Map<String, Object>> list = jdbcTemplate.queryForList(fmr_piechart_sql);
//            List<Slice> pieList = new ArrayList();
//            for (Map<String, Object> dt : list) {
//                pieList.add(Slice.newSlice(Math.round(Float.valueOf(dt.get("VV").toString())), dt.get("RATING").toString() + ", " + dt.get("VV").toString()));
//            }
//            Slice[] pieArray = pieList.toArray(new Slice[pieList.size()]);
//            PieChart fif_pieChart = GCharts.newPieChart(pieArray);
//            fif_pieChart.setTitle("Credit Quality of Portfolio (% of Total Assets)", Color.BLACK, 15);
//            fif_pieChart.setSize(720, 360);
//            fif_pieChart.setThreeD(true);

            barJfree();
            //String barUrl = barchart();

            //Cash fund, bar chart, pie
            context.setVariable("fif_def", fif_def);
            context.setVariable("fif_basicInfo", fif_basicInfo);
            context.setVariable("fif_perform_data", fif_perform_data);
            context.setVariable("fif_perform_data1", fif_perform_data1);
            context.setVariable("fif_asset_header", fif_asset_header);
            context.setVariable("fassetallocation_data", fif_prev_asset_alloc);
            context.setVariable("fmr_tech_info_sql_data", fmr_tech_info_sql_data);
            //context.setVariable("fif_pieChart", fif_pieChart.toURLString());
            //context.setVariable("fif_barChart", barUrl);

        } catch (Exception ex) {
            fif_def.setResponse_code(ResponseCodes.FAILURE);
        }

        return context;
    }

    private List<fmr_asset_allocation_model> FillAssetAllocation(List<Map<String, Object>> rows) {

        List<fmr_asset_allocation_model> list = new ArrayList<fmr_asset_allocation_model>();
        for (Map row : rows) {
            fmr_asset_allocation_model customer = new fmr_asset_allocation_model();
            customer.setCode(row.get("CODE").toString());
            list.add(customer);
        }
        return list;
    }

    private List<Map<String, Object>> getAssetAllocHeader(String fundcode) {

        String fassetallocation_sql = "select to_char(p.aa_trans_date , 'Monthdd,yyyy') prev, to_char(c.aa_trans_date , 'Monthdd,yyyy') curr \n"
                + "  from fmr_prev_asset_alloocation p, fmr_current_asset_alloocation c\n"
                + " where p.fund_code = 00001\n"
                + "   and p.aa_trans_date =\n"
                + "       last_day(to_date(to_char(add_months(get_system_date, -2), 'YYYYMM'),\n"
                + "                        'YYYYMM'))\n"
                + "   and c.aa_trans_date =\n"
                + "       last_day(to_date(to_char(add_months(get_system_date, -1), 'YYYYMM'),\n"
                + "                        'YYYYMM'))\n"
                + "   and rownum = '" + fundcode + "'";
        List<Map<String, Object>> fif_perform_data1 = jdbcTemplate.queryForList(fassetallocation_sql);
        return fif_perform_data1;
    }

    private List<fmr_asset_allocation_model> getAssetAllocBody(String fundcode) {

        List<fmr_asset_allocation_model> currlist = new ArrayList<fmr_asset_allocation_model>();
        String currsql = "select code, aa_asset_description, aa_prevmth, aa_currmth\n"
                + "  from fmr_current_asset_alloocation\n"
                + " where fund_code = '" + fundcode + "'\n"
                + "   and aa_trans_date =\n"
                + "       last_day(to_date(to_char(add_months(get_system_date, -1), 'YYYYMM'),\n"
                + "                        'YYYYMM'))";
        List<Map<String, Object>> currdata = jdbcTemplate.queryForList(currsql);
        for (Map row : currdata) {
            fmr_asset_allocation_model customer = new fmr_asset_allocation_model();
            customer.setCode(row.get("CODE").toString());
            customer.setSecName(row.get("AA_ASSET_DESCRIPTION").toString());
            if (row.get("AA_PREVMTH") != null) {
                customer.setPrev_month(row.get("AA_PREVMTH").toString());
            }
            if (row.get("AA_CURRMTH") != null) {
                customer.setCurr_month(row.get("AA_CURRMTH").toString());
            }
            currlist.add(customer);
        }

        String prevsql = "select code, aa_asset_description, aa_prevmth, aa_currmth\n"
                + "  from fmr_prev_asset_alloocation\n"
                + " where fund_code = 00001\n"
                + "   and aa_trans_date =\n"
                + "       last_day(to_date(to_char(add_months(get_system_date, -2), 'YYYYMM'),\n"
                + "                        'YYYYMM'))";
        List<Map<String, Object>> prevdata = jdbcTemplate.queryForList(prevsql);
        for (fmr_asset_allocation_model crow : currlist) {
            for (Map prow : prevdata) {
                String pr = prow.get("CODE").toString();
                String cr = crow.getCode();
                if (cr.equals(pr)) {
                    crow.setPrev_month(prow.get("AA_PREVMTH").toString());
                }
            }
        }
        return currlist;
    }

    public void barJfree() throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.setValue(0, "Fund", "January");
        dataset.setValue(0, "Benchmark", "January");

        dataset.setValue(4, "Fund", "February");
        dataset.setValue(5, "Benchmark", "February");

        dataset.setValue(3, "Fund", "March");
        dataset.setValue(6, "Benchmark", "March");

        dataset.setValue(5, "Fund", "April");
        dataset.setValue(6, "Benchmark", "April");

        dataset.setValue(4, "Fund", "May");
        dataset.setValue(6, "Benchmark", "May");

        dataset.setValue(3, "Fund", "June");
        dataset.setValue(6, "Benchmark", "June");

        dataset.setValue(3, "Fund", "July");
        dataset.setValue(5, "Benchmark", "July");

        JFreeChart chart = ChartFactory.createBarChart(
                "ABL - IF Vs Benchmark (MOM Returns)", "", "",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.getTitle().setPaint(java.awt.Color.BLACK);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        BarRenderer br = (BarRenderer) plot.getRenderer();
        br.setItemMargin(0);
        br.setSeriesPaint(0, java.awt.Color.blue.darker());
        br.setSeriesPaint(1, java.awt.Color.yellow.darker());

        Font font3 = new Font("Dialog", Font.PLAIN, 15);
        plot.getDomainAxis().setLabelFont(font3);
        plot.getRangeAxis().setLabelFont(font3);

        CategoryAxis domain = plot.getDomainAxis();
        domain.setLowerMargin(0.25);
        domain.setUpperMargin(0.25);
        int width = 640;
        int height = 480;
        File barChart3D = new File("D:\\Users\\mohammad.bassam\\Documents\\OnlinePortal\\src\\main\\resources\\public/barChart3D.jpeg");
        ChartUtilities.saveChartAsJPEG(barChart3D, chart, width, height);

    }

    @GetMapping("/getReport/{fundcode}")
    public void getReport(@PathVariable String fundcode, HttpServletRequest request, HttpServletResponse response) {

        try {
            //jdbcTemplate.setDataSource(dss);
//            FmrReport.run(jdbcTemplate, request, response);
        } catch (Exception ex) {
            Logger.getLogger(FmrReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
