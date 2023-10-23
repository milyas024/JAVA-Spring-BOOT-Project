/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ams.controller;

import com.ams.model.FundManageModel;
import com.ams.model.MainModel;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 *
 * @author mohammad.bassam
 */
@RestController
@RequestMapping("/general/general")
public class GeneralController {

    @GetMapping()
    public List<Object> list() {
        return null;
    }

    @PostMapping(path = "/generateFlashReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> generateFlashReport(@RequestBody FundManageModel model) {

        List<Map<String, Object>> data = null;
        try {
            if (!busy && model != null) {
                busy = true;
                data = jdbcTemplate.queryForList("SELECT * from table(GET_FLASH(TO_DATE('" + model.getAsOnDate() + "','dd/MM/yyyy'))) ");//and rownum<=5
                System.out.println(data);
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println(data);
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

    @PostMapping(path = "/generateNAVReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> generateNAVReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            if (!busy && model != null) {
                busy = true;
                data = jdbcTemplate.queryForList(" select f.fund_code,\n"
                        + "       decode(f.fund_code,\n"
                        + "              '00001',\n"
                        + "              'ABL Income Fund',\n"
                        + "              '00002',\n"
                        + "              'ABL Stock Fund',\n"
                        + "              '00003',\n"
                        + "              'ABL Cash Fund',\n"
                        + "              '00004',\n"
                        + "              'ABL Islamic Income Fund',\n"
                        + "              '00005',\n"
                        + "              'ABL Government Securities Fund',\n"
                        + "              '00008',\n"
                        + "              'ABL Islamic Stock Fund',\n"
                        + "              '00011',\n"
                        + "              'ABL IFPF-Conservative Allocation Plan',\n"
                        + "              '00012',\n"
                        + "              'ABL IFPF-Aggressive Allocation Plan',\n"
                        + "              '00013',\n"
                        + "              'ABL IFPF-Active Allocation Plan',\n"
                        + "              '00014',\n"
                        + "              'ABL FPF-Conservative Allocation Plan',\n"
                        + "              '00016',\n"
                        + "              'ABL FPF-Active Allocation Plan',\n"
                        + "              '00017',\n"
                        + "              'ABL-IFPF-Strategic Allocation Plan- I',\n"
                        + "              '00018',\n"
                        + "              'ABL-IFPF-Strategic Allocation Plan- II',\n"
                        + "              '00019',\n"
                        + "              'ABL FPF-Strategic Allocation Plan',\n"
                        + "              '00020',\n"
                        + "              'ABL Islamic Dedicated Stock Fund',\n"
                        + "              '00021',\n"
                        + "              'ABL-IFPF-Strategic Allocation Plan- III',\n"
                        + "              '00022',\n"
                        + "              'ABL-IFPF-Strategic Allocation Plan- IV',\n"
                        + "              '00023',\n"
                        + "              'Allied Capital Protected Fund',\n"
                        + "              '00024',\n"
                        + "              'ABL Islamic Asset Allocation Fund',\n"
                        + "              '00025',\n"
                        + "              'Allied Finergy Fund',\n"
                        + "              '00026',\n"
                        + "              'ABL IFPF Capital Preservation Plan- I',\n"
                        + "              '00027',\n"
                        + "              'ABL Special Savings Plan-I',\n"
                        + "              '00028',\n"
                        + "              'ABL Special Savings Plan-II',\n"
                        + "              '00029',\n"
                        + "              'ABL Special Savings Plan-III	',\n"
                        + "              F.fund_name) Fund,\n"
                        + "       to_char(decode(f.fund_code,\n"
                        + "                      '00017',\n"
                        + "                      null,\n"
                        + "                      '00018',\n"
                        + "                      null,\n"
                        + "                      '00019',\n"
                        + "                      null,\n"
                        + "                      '00021',\n"
                        + "                      null,\n"
                        + "                      '00022',\n"
                        + "                      null,\n"
                        + "                      '00023',\n"
                        + "                      null,\n"
                        + "                      '00026',\n"
                        + "                      null,\n"
                        + "                      u.sale_price),\n"
                        + "               '9999.9999') Offer,\n"
                        + "       to_char(u.redemption_price, '9999.9999') Redemption,\n"
                        + "       to_char(u.nav, '9999.9999') Nav,\n"
                        + "       to_char(decode(f.fund_code, '00003', u.price_date + 1, u.price_date),\n"
                        + "               'dd-MON-yy') price_date\n"
                        + "  from unit_nav u, fund f\n"
                        + " where u.price_date = to_date('" + model.getTransDate() + "','dd/MM/yyyy') \n"
                        + "   and f.fund_code = u.fund_code\n"
                        + " order by u.fund_code ");//and rownum<=5
            } else {
                return null;
            }
        } catch (Exception e) {
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

    @PostMapping(path = "/netAssetsReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> netAssetsReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            String qry = " SELECT to_char(price_date, 'dd/MM/yyyy') price_date, net_assets netAsset \n"
                    + "  FROM TABLE(net_assets_for_date_range(to_date('" + model.getFromDate() + "', 'dd/MM/yyyy'),\n"
                    + "                                       to_date('" + model.getToDate() + "', 'dd/MM/yyyy'),\n"
                    + "                                       '" + model.getFundCode() + "'))"
                    + " ORDER BY price_date ";
            data = jdbcTemplate.queryForList(qry);//and rownum<=5

        } catch (Exception e) {
            System.out.println(e.toString());
            return data;
        }
        return data;
    }

    @PostMapping(path = "/Unrealizedgain", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> Unrealizedgain(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            String qry = " select to_char(ep.price_date, 'dd/MM/yyyy')  pdate,\n"
                    + "       ep.symbol,\n"
                    + "       sum(((afs_volume + hft_volume) * em.close_rate) -\n"
                    + "           (ep.afs_mark_to_mkt_value + ep.hft_mark_to_mkt_value)) gain_loss\n"
                    + "  from equity_portfolio ep, equity_market em, fund_system fs\n"
                    + " WHERE ep.symbol = em.symbol\n"
                    + "   and ep.price_date = em.price_date\n"
                    + "   and ep.fund_code = fs.fund_code\n"
                    + "   and ep.fund_code = '" + model.getFundCode() + "' \n"
                    + "   and ep.price_date between to_date('" + model.getFromDate() + "', 'dd/MM/yyyy') and\n"
                    + "       to_date('" + model.getToDate() + "', 'dd/MM/yyyy')\n"
                    + " group by ep.price_date, ep.fund_code, ep.symbol\n"
                    + " order by ep.price_date ";
            data = jdbcTemplate.queryForList(qry);//and rownum<=5
        } catch (Exception e) {
            System.out.println(e.toString());
            return data;
        }
        return data;
    }

    @PostMapping(path = "/netAssetsMVReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> netAssetsMVReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            String qry = " select to_char(t.Portfolio_Date , 'dd/MM/yyyy') Portfolio_Date,\n"
                    + "       t.Net_Assets,\n"
                    + "       t.market_value,\n"
                    + "       round(t.market_value / t.Net_Assets, 4) * 100 as percentage\n"
                    + "  from avg_protfolio t\n"
                    + " where t.Portfolio_Date between to_date('" + model.getFromDate() + "', 'dd/MM/yyyy') and to_date('" + model.getToDate() + "', 'dd/MM/yyyy')\n"
                    + "   and t.fund_code = '" + model.getFundCode() + "'\n"
                    + "   order by t.Portfolio_Date,t.fund_code ";
            data = jdbcTemplate.queryForList(qry);//and rownum<=5

        } catch (Exception e) {
            return data;
        }
        return data;
    }

    @PostMapping(path = "/autoRedemptionReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> autoRedemptionReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            String qry = " select t. redemption_id AMCTransactionID,\n"
                    + "       decode(f.fund_code,              00001,              'ABLIF',              00002,              'ABLSF',              00003,              'ABLCF',              00004,              'ABLISIF',              00005,              'ABLGSF') fund_short_name,\n"
                    + "       t.house_account_no Fund_Bank_Account,\n"
                    + "       decode(u.plan_name,              'FLEXIBLE INCOME UNITS',              'INCOME UNIT',              'GROWTH UNITS',              'GROWTH UNIT',              'FIXED INCOME UNITS',              'INCOME UNIT') PLAN_NAMe,\n"
                    + "       'AMC ELECTRONIC' Holding_type,\n"
                    + "       to_char(t.cheque_date, 'dd/mm/yyyy') Payment_date,\n"
                    + "       to_char(t.price_date, 'dd/mm/yyyy') NAV_Date,\n"
                    + "       'N' Payment_To_3rd_Party,\n"
                    + "       t.folio_number,\n"
                    + "       ua.title,\n"
                    + "       t.bank_name,\n"
                    + "       t.bank_address,\n"
                    + "       t.bank_city,\n"
                    + "       ua.bank_branch_code,\n"
                    + "       t.account_number,\n"
                    + "       t.comments Instrument_Type,\n"
                    + "       t.uncertified_quantity No_of_units,\n"
                    + "       get_fund_folio_balance(t.redemption_date,\n"
                    + "                              f.fund_code,\n"
                    + "                              t.folio_number) Closing_Balance,\n"
                    + "       round(t.uncertified_quantity * t.nav, 2) Gross_amount,\n"
                    + "       NVL(t.zakat, 0) zakat_amount,\n"
                    + "       NVL(t.contingent_load, 0) backend_load,\n"
                    + "       '0' processing_charges,\n"
                    + "       '0' discount_amount,\n"
                    + "       round(t.uncertified_quantity * t.nav, 2) - round(nvl(t.cgt, 0)) net_amount,\n"
                    + "       ' ' remarks,\n"
                    + "       'N' CommonACTransactionFlag,\n"
                    + "       ' ' RefernceID,\n"
                    + "       '0' Total_amount,\n"
                    + "       ' ' PrintingLocation,\n"
                    + "       ' ' ClearingZoneCode,\n"
                    + "       'Applicable' TaxStatus,\n"
                    + "       nvl(decode(sign((select sum(utt.capital_gain)\n"
                    + "                         from unit_trans_deduction_history utt\n"
                    + "                        where utt.redemption_price_date = to_date('" + model.getNavDate() + "', 'dd/MM/yyyy') \n"
                    + "                          and utt.folio_number = t.folio_number\n"
                    + "                          and utt.fund_code = t.fund_code\n"
                    + "                          and utt.no_of_days <= 1460\n"
                    + "                          and utt.transaction_id = t.redemption_id)),\n"
                    + "                  -1,\n"
                    + "                  0,\n"
                    + "                  (select sum(utt.capital_gain)\n"
                    + "                     from unit_trans_deduction_history utt\n"
                    + "                    where utt.redemption_price_date = to_date('" + model.getNavDate() + "', 'dd/MM/yyyy') \n"
                    + "                      and utt.folio_number = t.folio_number\n"
                    + "                      and utt.fund_code = t.fund_code\n"
                    + "                      and utt.no_of_days <= 1460\n"
                    + "                      and utt.transaction_id = t.redemption_id)),\n"
                    + "           0) TAXABLE_INCOME,\n"
                    + "       '0' LossAdjustment,\n"
                    + "       round(NVL(t.cgt, 0)) TAX_AMOUNT\n"
                    + "  from unit_redemption t, fund f, unit_plan u, unit_account ua\n"
                    + " where t.redemption_date = to_date('" + model.getFromDate() + "', 'dd/MM/yyyy') \n"
                    + "   and t.fund_code = f.fund_code\n"
                    + "   and u.plan_id = ua.plan_id\n"
                    + "   and ua.folio_number = t.folio_number ";
            if (model.getFundCode() != null && !model.getFundCode().equals("") && !model.getFundCode().equals("ALL")) {
                qry += " and f.fund_code = '" + model.getFundCode() + "' ";
            }
            data = jdbcTemplate.queryForList(qry);//and rownum<=5

        } catch (Exception e) {
            return data;
        }
        return data;
    }

    @PostMapping(path = "/reallocationReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> reallocationReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            String qry = " select distinct ua.folio_number AccountNumber,\n"
                    + "                ua.title,\n"
                    + "                get_fund_folio_balance(get_system_date(F.FUND_CODE),\n"
                    + "                                       f.fund_code,\n"
                    + "                                       ua.folio_number) before_balance,\n"
                    + "                nvl((Select ur.uncertified_quantity\n"
                    + "                      from unit_redemption ur\n"
                    + "                     where ur.aplan_reallocation_id = apr.reallocation_id\n"
                    + "                       and ur.fund_code = f.fund_code),\n"
                    + "                    0) reallocation,\n"
                    + "                f.fund_name,\n"
                    + "                nvl((Select ur.uncertified_quantity *\n"
                    + "                           (select un.nav\n"
                    + "                              from unit_nav un\n"
                    + "                             where un.fund_code = f.fund_code\n"
                    + "                               and un.price_date =\n"
                    + "                                   get_system_date(UN.FUND_CODE))\n"
                    + "                      from unit_redemption ur\n"
                    + "                     where ur.aplan_reallocation_id = apr.reallocation_id\n"
                    + "                       and ur.fund_code = f.fund_code),\n"
                    + "                    0) amount,\n"
                    + "                get_fund_folio_balance(get_system_date(F.FUND_CODE),\n"
                    + "                                       f.fund_code,\n"
                    + "                                       ua.folio_number) after_balance,\n"
                    + "                nvl((Select ur.cgt\n"
                    + "                      from unit_redemption ur\n"
                    + "                     where ur.aplan_reallocation_id = apr.reallocation_id\n"
                    + "                       and ur.fund_code = f.fund_code),\n"
                    + "                    0) cgt,\n"
                    + "                to_char(apr.reallocation_date , 'dd/MM/yyyy') reallocation_date\n"
                    + "  from aplan_detail            apd,\n"
                    + "       aplan                   ap,\n"
                    + "       aplan_reallocations     apr,\n"
                    + "       unit_account            ua,\n"
                    + "       APLAN_FUND_DISTRIBUTION apfd,\n"
                    + "       fund                    f\n"
                    + " where ap.aplan_id = apd.aplan_id\n"
                    + "   and apr.aplan_detail_id = apd.aplan_detail_id\n"
                    + "   and ua.folio_number = apr.folio_number\n"
                    + "   and ua.aplan_id = ap.aplan_id\n"
                    + "   AND apd.aplan_detail_id = apfd.aplan_detail_id\n"
                    + "   AND f.fund_code = apfd.fund_code\n"
                    + "   and apr.reallocation_date = to_date('" + model.getFromDate() + "', 'dd/MM/yyyy') ";
            if (model.getFundCode() != null && !model.getFundCode().equals("") && !model.getFundCode().equals("ALL")) {
                qry += " and f.fund_code = '" + model.getFundCode() + "' ";
            }
            data = jdbcTemplate.queryForList(qry);//and rownum<=5

        } catch (Exception e) {
            return data;
        }
        return data;
    }

    @PostMapping(path = "/netAssetsBFReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> netAssetsBFReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            String qry = " select f.fund_short_name, t.FUND_CODE, to_char(t.PRICE_DATE , 'dd/MM/yyyy') PRICE_DATE, t.NET_ASSETS \n"
                    + "  from net_assets_before_fee t, fund f\n"
                    + " where t.price_date between to_date('" + model.getFromDate() + "', 'dd/MM/yyyy') and to_date('" + model.getToDate() + "', 'dd/MM/yyyy')\n"
                    + "   and t.fund_code = f.fund_code\n"
                    + " order by 3 ";
            data = jdbcTemplate.queryForList(qry);//and rownum<=5

        } catch (Exception e) {
            return data;
        }
        return data;
    }

    @PostMapping(path = "/generate1LinkReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> generate1LinkReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            if (!busy && model != null) {
                busy = true;
                String qry = " select us.sale_id,\n"
                        + "       to_char(sale_date , 'dd/MM/yyyy') sale_date,\n"
                        + "       ua.title,\n"
                        + "       f.fund_name,\n"
                        + "       ua.folio_number,\n"
                        + "       b.amount amount,\n"
                        + "       (case when b.amount>0 and b.amount<=10000 then 10 when b.amount>10000 and b.amount<=250000 then 40 when b.amount>250000 and b.amount<=1000000 then 75 when b.amount>1000000 then 150 end) as LinkCharges,\n"
                        + "       b.amount-(case when b.amount>0 and b.amount<=10000 then 10 when b.amount>10000 and b.amount<=250000 then 40 when b.amount>250000 and b.amount<=1000000 then 75 when b.amount>1000000 then 150 end) as NetLinkCharges,\n"
                        + "       f.sales_load - (f.sales_load*(pd.discount_percentage/100)) fel,\n"
                        + "       pd.offer_price offerNav,\n"
                        + "       un.redemption_price redempNav,\n"
                        + "       round((b.amount/pd.offer_price) , 2) units,\n"
                        + "       (case when round((pd.offer_price-un.redemption_price)*(b.amount/pd.offer_price) , 2)<0 then 0 else round((pd.offer_price-un.redemption_price)*(b.amount/pd.offer_price) , 2) end) frontLoad,\n"
                        + "       '1Link' source,\n"
                        + "       to_char(us.posting_date , 'HH:MM am') TransactionTime,\n"
                        + "       (case when us.quantity is not null then 'Yes' else 'No' end) UnitsAllocated , b.pin_code pin_code , decode(pd.payment_mode , 'ON' , 'Online', 'Other') payment_mode \n"
                        + "  from unit_sale us, unit_account ua, fund f, payment_detail pd, unit_nav un, investment_by_bank b\n"
                        + " where ua.folio_number = us.folio_number\n"
                        + " and us.payment_id = pd.payment_id\n"
                        + " and b.sale_id = us.sale_id\n"
                        + "   and us.fund_code = f.fund_code\n"
                        + "   and un.fund_code = us.fund_code\n and us.sale_date>=to_date('" + model.getFromDate() + "' , 'dd/MM/yyyy') and us.sale_date<=to_date('" + model.getToDate() + "' , 'dd/MM/yyyy') ";
                if (model.getFundCode() != null && !model.getFundCode().equals("") && !model.getFundCode().equals("ALL")) {
                    qry += " and us.fund_code = '" + model.getFundCode() + "' ";
                }
                if (model.getSearchFolioNumber() != null && !model.getSearchFolioNumber().equals("")) {
                    String folio[] = model.getSearchFolioNumber().split(",");
                    qry += " and us.folio_number in ( ";
                    for (String ff : folio) {
                        qry += "'" + ff.trim() + "',";
                    }
                    qry += "'')";

                }
                qry += "   and un.price_date = (\n"
                        + "       select max(unn.price_date) from unit_nav unn where unn.fund_code = us.fund_code and unn.price_date<=us.sale_date \n"
                        + "   ) order by sale_id desc ";
                data = jdbcTemplate.queryForList(qry);//and rownum<=5
            } else {
                return null;
            }
        } catch (Exception e) {
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

    @PostMapping(path = "/cnicExpiryReport", consumes = "application/json", produces = "application/json")
    public List<Map<String, Object>> cnicExpiryReport(@RequestBody MainModel model) {
        List<Map<String, Object>> data = null;
        try {
            if (!busy && model != null) {
                busy = true;
                String qry = " select c.client_code customer_id,\n"
                        + "       ua.folio_number account_number,\n"
                        + "       decode(ua.joint_account, '1', 'Joint', 'Single') account_type,\n"
                        + "       c.client_name,\n"
                        + "       c.nic_passport cnic,\n"
                        + "       to_char(c.cnic_expiry_date, 'dd/MM/yyyy') cnic_expiry_date,\n"
                        + "       c.address1 || ' ' || c.address2 address,\n"
                        + "       ct.city,\n"
                        + "       c.phone_one,\n"
                        + "       c.phone_two,\n"
                        + "       c.phone_mobile,\n"
                        + "       c.e_mail,\n"
                        + "       decode(ua.hold_mail, '1', 'Yes', 'No') account_type\n"
                        + "  from client c, unit_account ua, city ct\n"
                        + " where c.client_code = ua.primary_client\n"
                        + "   and c.city_code = ct.city_code\n";
                if (model.getAsOnDate() != null && !model.getAsOnDate().equals("") && model.getAsOnDate().equals("true")) {
                    qry += " and c.cnic_expiry_date <= sysdate ";
                } else {
                    qry += " and c.cnic_expiry_date >= to_date('" + model.getFromDate() + "' , 'dd/MM/yyyy') and c.cnic_expiry_date <= to_date('" + model.getToDate() + "' , 'dd/MM/yyyy') ";
                }
                data = jdbcTemplate.queryForList(qry);//and rownum<=5
            } else {
                return null;
            }
        } catch (Exception e) {
            busy = false;
            return data;
        }
        busy = false;
        return data;
    }

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
