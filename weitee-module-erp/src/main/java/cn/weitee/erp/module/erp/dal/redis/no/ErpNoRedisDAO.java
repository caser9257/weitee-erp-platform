package cn.weitee.erp.module.erp.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.weitee.erp.module.erp.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * Erp 订单序号的 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
public class ErpNoRedisDAO {

    /**
     * 其它入库 {@link cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO}
     */
    public static final String STOCK_IN_NO_PREFIX = "QTRK";
    /**
     * 其它出库 {@link cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO}
     */
    public static final String STOCK_OUT_NO_PREFIX = "QCKD";

    /**
     * 库存调拨 {@link cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO}
     */
    public static final String STOCK_MOVE_NO_PREFIX = "QCDB";

    /**
     * 库存盘点 {@link cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckDO}
     */
    public static final String STOCK_CHECK_NO_PREFIX = "QCPD";
    public static final String STOCK_ASSEMBLE_NO_PREFIX = "ZZCX";

    /**
     * 销售订单 {@link cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO}
     */
    public static final String SALE_ORDER_NO_PREFIX = "XSDD";
    /**
     * 销售出库 {@link cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO}
     */
    public static final String SALE_OUT_NO_PREFIX = "XSCK";
    /**
     * 销售退货 {@link cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO}
     */
    public static final String SALE_RETURN_NO_PREFIX = "XSTH";

    /**
     * 采购订单 {@link cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO}
     */
    public static final String PURCHASE_ORDER_NO_PREFIX = "CGDD";
    /**
     * 采购入库 {@link cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO}
     */
    public static final String PURCHASE_IN_NO_PREFIX = "CGRK";
    /**
     * 采购入库质检单 {@link cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO}
     */
    public static final String PURCHASE_IN_QUALITY_NO_PREFIX = "CGZJ";
    /**
     * 采购退货 {@link cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO}
     */
    public static final String PURCHASE_RETURN_NO_PREFIX = "CGTH";
    /**
     * 采购来源批次 {@link cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO}
     */
    public static final String PURCHASE_SOURCE_BATCH_NO_PREFIX = "CGLY";

    /**
     * MRP 计划 {@link cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO}
     */
    public static final String MRP_PLAN_NO_PREFIX = "MRP";

    /**
     * 项目 {@link cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO}
     */
    public static final String PROJECT_NO_PREFIX = "XM";

    /**
     * 生产工单 {@link cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO}
     */
    public static final String PRODUCTION_ORDER_NO_PREFIX = "SCGD";
    public static final String PRODUCTION_FINISH_QUALITY_NO_PREFIX = "CPZJ";
    public static final String PRODUCTION_INBOUND_NO_PREFIX = "ZZRK";
    public static final String PRODUCTION_ISSUE_VOUCHER_NO_PREFIX = "CKPZ";
    public static final String PRODUCTION_COST_ALLOCATION_NO_PREFIX = "CBFT";
    public static final String PRODUCTION_REPORT_NO_PREFIX = "BGDG";
    public static final String PRODUCTION_STEP_QUALITY_NO_PREFIX = "GXZJ";
    public static final String OUTSOURCE_ORDER_NO_PREFIX = "WWDD";
    public static final String OUTSOURCE_ISSUE_NO_PREFIX = "WWFL";
    public static final String OUTSOURCE_RETURN_NO_PREFIX = "WWTL";
    public static final String OUTSOURCE_INBOUND_NO_PREFIX = "WWRK";
    public static final String OUTSOURCE_FEE_NO_PREFIX = "WWJG";

    /**
     * 付款单 {@link cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO}
     */
    public static final String FINANCE_PAYMENT_NO_PREFIX = "FKD";
    /**
     * 预付款单 {@link cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO}
     */
    public static final String FINANCE_PREPAYMENT_NO_PREFIX = "YFKD";
    /**
     * 收款单 {@link cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO}
     */
    public static final String FINANCE_RECEIPT_NO_PREFIX = "SKD";
    public static final String FINANCE_EXPENSE_NO_PREFIX = "LSBX";
    public static final String FINANCE_VOUCHER_NO_PREFIX = "CWPZ";
    /**
     * 销项发票 {@link cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceDO}
     */
    public static final String INVOICE_NO_PREFIX = "XPF";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + yyyyMMdd + 6 位自增
     * 例如说：QTRK 202109 000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix) {
        // 递增序号
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = RedisKeyConstants.NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + String.format("%06d", no);
    }

}
