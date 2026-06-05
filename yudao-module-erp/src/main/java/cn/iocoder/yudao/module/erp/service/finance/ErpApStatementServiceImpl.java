package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPaymentEnablePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementUpdateInvoiceReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApEstimateReverseTypeEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseReturnService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_NOT_EXISTS;

@Service
@Validated
public class ErpApStatementServiceImpl implements ErpApStatementService {

    private static final String DEFAULT_CURRENCY_CODE = "CNY";

    @Resource
    private ErpApStatementMapper erpApStatementMapper;
    @Resource
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Resource
    private ErpFinanceExpenseMapper erpFinanceExpenseMapper;
    @Resource
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Resource
    private ErpFinancePrepaymentAllocateMapper erpFinancePrepaymentAllocateMapper;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpPurchaseReturnService purchaseReturnService;
    @Resource
    private ErpApEstimateService apEstimateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStatementForPurchaseIn(ErpPurchaseInDO purchaseIn) {
        if (purchaseIn == null || purchaseIn.getId() == null) {
            return;
        }
        if (erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.PURCHASE_IN.getType(), purchaseIn.getId()) != null) {
            return;
        }
        BigDecimal amount = defaultAmount(purchaseIn.getTotalPrice());
        LocalDateTime bizDate = resolveBizDate(purchaseIn.getInTime(), purchaseIn.getCreateTime(), purchaseIn.getUpdateTime());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setStatementNo(buildStatementNo(ErpBizTypeEnum.PURCHASE_IN.getType(), purchaseIn.getNo()))
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(purchaseIn.getId())
                .setBizNo(purchaseIn.getNo())
                .setSourceOrderId(purchaseIn.getOrderId())
                .setSourceOrderNo(purchaseIn.getOrderNo())
                .setSupplierId(purchaseIn.getSupplierId())
                .setAccountId(purchaseIn.getAccountId())
                .setAmount(amount)
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(amount)
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setBizDate(bizDate)
                .setDueDate(bizDate)
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setRemark(purchaseIn.getRemark());
        erpApStatementMapper.insert(statement);
        erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ErpApStatementItemTypeEnum.CREATED.getStatus())
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(statement.getAmount())
                .setAfterPaidAmount(statement.getPaidAmount())
                .setAfterRemainAmount(statement.getRemainAmount())
                .setRemark("create statement"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStatementForPurchaseReturn(ErpPurchaseReturnDO purchaseReturn) {
        if (purchaseReturn == null || purchaseReturn.getId() == null) {
            return;
        }
        if (erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.PURCHASE_RETURN.getType(), purchaseReturn.getId()) != null) {
            return;
        }
        BigDecimal amount = defaultAmount(purchaseReturn.getTotalPrice()).negate();
        LocalDateTime bizDate = resolveBizDate(purchaseReturn.getReturnTime(), purchaseReturn.getCreateTime(), purchaseReturn.getUpdateTime());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setStatementNo(buildStatementNo(ErpBizTypeEnum.PURCHASE_RETURN.getType(), purchaseReturn.getNo()))
                .setBizType(ErpBizTypeEnum.PURCHASE_RETURN.getType())
                .setBizId(purchaseReturn.getId())
                .setBizNo(purchaseReturn.getNo())
                .setSourceOrderId(purchaseReturn.getOrderId())
                .setSourceOrderNo(purchaseReturn.getOrderNo())
                .setSupplierId(purchaseReturn.getSupplierId())
                .setAccountId(purchaseReturn.getAccountId())
                .setAmount(amount)
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(amount)
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setBizDate(bizDate)
                .setDueDate(bizDate)
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setRemark(purchaseReturn.getRemark());
        erpApStatementMapper.insert(statement);
        erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ErpApStatementItemTypeEnum.CREATED.getStatus())
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(statement.getAmount())
                .setAfterPaidAmount(statement.getPaidAmount())
                .setAfterRemainAmount(statement.getRemainAmount())
                .setRemark("create statement"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStatementForOutsourceFee(ErpOutsourceFeeDO outsourceFee, ErpOutsourceOrderDO outsourceOrder) {
        if (outsourceFee == null || outsourceFee.getId() == null || outsourceOrder == null || outsourceOrder.getId() == null) {
            return;
        }
        if (erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), outsourceFee.getId()) != null) {
            return;
        }
        BigDecimal amount = defaultAmount(outsourceFee.getFeeAmount());
        LocalDateTime bizDate = resolveBizDate(outsourceFee.getFeeTime(), outsourceFee.getCreateTime(), outsourceFee.getUpdateTime());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setStatementNo(buildStatementNo(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), outsourceFee.getFeeNo()))
                .setBizType(ErpBizTypeEnum.OUTSOURCE_FEE.getType())
                .setBizId(outsourceFee.getId())
                .setBizNo(outsourceFee.getFeeNo())
                .setSourceOrderId(outsourceOrder.getId())
                .setSourceOrderNo(outsourceOrder.getNo())
                .setSupplierId(outsourceOrder.getSupplierId())
                .setAccountId(null)
                .setAmount(amount)
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(amount)
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setBizDate(bizDate)
                .setDueDate(bizDate)
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setRemark(outsourceFee.getRemark());
        erpApStatementMapper.insert(statement);
        erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ErpApStatementItemTypeEnum.CREATED.getStatus())
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(statement.getAmount())
                .setAfterPaidAmount(statement.getPaidAmount())
                .setAfterRemainAmount(statement.getRemainAmount())
                .setRemark("create outsource fee statement"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStatementForFinanceExpense(ErpFinanceExpenseDO expense) {
        if (expense == null || expense.getId() == null) {
            return;
        }
        if (erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId()) != null) {
            return;
        }
        BigDecimal amount = defaultAmount(expense.getExpensePrice());
        LocalDateTime bizDate = resolveBizDate(expense.getExpenseTime(), expense.getCreateTime(), expense.getUpdateTime());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setStatementNo(buildStatementNo(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getNo()))
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(expense.getId())
                .setBizNo(expense.getNo())
                .setSourceOrderId(null)
                .setSourceOrderNo(null)
                .setSupplierId(expense.getSupplierId())
                .setAccountId(expense.getAccountId())
                .setAmount(amount)
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(amount)
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setBizDate(bizDate)
                .setDueDate(bizDate)
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setRemark(expense.getRemark());
        erpApStatementMapper.insert(statement);
        erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ErpApStatementItemTypeEnum.CREATED.getStatus())
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(statement.getAmount())
                .setAfterPaidAmount(statement.getPaidAmount())
                .setAfterRemainAmount(statement.getRemainAmount())
                .setRemark("create finance expense statement"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshStatementAmountByIds(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return;
        }
        List<ErpApStatementDO> statements = erpApStatementMapper.selectBatchIds(statementIds);
        if (CollUtil.isEmpty(statements)) {
            return;
        }
        List<Long> actualStatementIds = convertSet(statements, ErpApStatementDO::getId).stream().toList();
        List<ErpFinancePaymentAllocateDO> paymentAllocates = erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(actualStatementIds);
        List<ErpFinancePrepaymentAllocateDO> prepaymentAllocates = erpFinancePrepaymentAllocateMapper.selectApprovedListByStatementIds(actualStatementIds);
        Map<Long, BigDecimal> approvedAmountMap = new java.util.LinkedHashMap<>();
        paymentAllocates.forEach(allocate -> approvedAmountMap.merge(allocate.getApStatementId(),
                defaultAmount(allocate.getAllocateAmount()), BigDecimal::add));
        prepaymentAllocates.forEach(allocate -> approvedAmountMap.merge(allocate.getApStatementId(),
                defaultAmount(allocate.getAllocateAmount()), BigDecimal::add));
        statements.forEach(statement -> {
            if (ErpApStatementStatusEnum.CLOSED.getStatus().equals(statement.getStatus())) {
                return;
            }
            BigDecimal paidAmount = approvedAmountMap.getOrDefault(statement.getId(), BigDecimal.ZERO);
            BigDecimal remainAmount = defaultAmount(statement.getAmount()).subtract(paidAmount);
            erpApStatementMapper.updateById(new ErpApStatementDO()
                    .setId(statement.getId())
                    .setPaidAmount(paidAmount)
                    .setRemainAmount(remainAmount)
                    .setStatus(calculateStatus(paidAmount, remainAmount, statement.getStatus())));
        });
    }

    @Override
    public void refreshBizSummaryByStatementIds(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return;
        }
        List<ErpApStatementDO> statements = erpApStatementMapper.selectBatchIds(statementIds);
        if (CollUtil.isEmpty(statements)) {
            return;
        }
        statements.forEach(statement -> {
            BigDecimal paidAmount = defaultAmount(statement.getPaidAmount());
            if (ErpBizTypeEnum.PURCHASE_IN.getType().equals(statement.getBizType())) {
                purchaseInService.updatePurchaseInPaymentPrice(statement.getBizId(), paidAmount);
            } else if (ErpBizTypeEnum.PURCHASE_RETURN.getType().equals(statement.getBizType())) {
                purchaseReturnService.updatePurchaseReturnRefundPrice(statement.getBizId(), paidAmount.negate());
            } else if (ErpBizTypeEnum.FINANCE_EXPENSE.getType().equals(statement.getBizType())) {
                erpFinanceExpenseMapper.updateById(new ErpFinanceExpenseDO()
                        .setId(statement.getBizId())
                        .setPaidPrice(defaultAmount(statement.getPaidAmount()))
                        .setRemainPrice(defaultAmount(statement.getRemainAmount())));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeStatementByBiz(Integer bizType, Long bizId, String remark) {
        ErpApStatementDO statement = erpApStatementMapper.selectByBizTypeAndBizId(bizType, bizId);
        if (statement == null || ErpApStatementStatusEnum.CLOSED.getStatus().equals(statement.getStatus())) {
            return;
        }
        erpApStatementMapper.updateById(new ErpApStatementDO()
                .setId(statement.getId())
                .setStatus(ErpApStatementStatusEnum.CLOSED.getStatus())
                .setRemark(remark));
        erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ErpApStatementItemTypeEnum.CLOSED.getStatus())
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(BigDecimal.ZERO)
                .setAfterPaidAmount(defaultAmount(statement.getPaidAmount()))
                .setAfterRemainAmount(defaultAmount(statement.getRemainAmount()))
                .setRemark(remark));
        if (ErpBizTypeEnum.PURCHASE_IN.getType().equals(bizType)) {
            apEstimateService.reverseBySourceBiz(bizType, bizId, null,
                    ErpApEstimateReverseTypeEnum.STATEMENT_CLOSED.getStatus(),
                    statement.getId(),
                    statement.getStatementNo(),
                    remark);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoice(ErpApStatementUpdateInvoiceReqVO updateReqVO) {
        ErpApStatementDO statement = validateApStatement(updateReqVO.getId());
        Integer oldInvoiceStatus = statement.getInvoiceStatus();
        String invoiceNo = StrUtil.isBlank(updateReqVO.getInvoiceNo()) ? null : updateReqVO.getInvoiceNo().trim();
        BigDecimal invoiceAmount = updateReqVO.getInvoiceAmount();
        if (ErpApInvoiceStatusEnum.NONE.getStatus().equals(updateReqVO.getInvoiceStatus())) {
            invoiceNo = null;
            invoiceAmount = null;
        }
        erpApStatementMapper.updateInvoiceById(statement.getId(), updateReqVO.getInvoiceStatus(), invoiceNo, invoiceAmount);
        erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ErpApStatementItemTypeEnum.INVOICE_UPDATED.getStatus())
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(defaultAmount(invoiceAmount))
                .setAfterPaidAmount(defaultAmount(statement.getPaidAmount()))
                .setAfterRemainAmount(defaultAmount(statement.getRemainAmount()))
                .setRemark(buildInvoiceItemRemark(updateReqVO.getRemark(), updateReqVO.getInvoiceStatus(), invoiceNo)));
        apEstimateService.syncByStatementInvoiceChange(statement, oldInvoiceStatus, updateReqVO.getInvoiceStatus(),
                getLoginUserId(), null, invoiceNo);
    }

    @Override
    public ErpApStatementDO validateApStatement(Long id) {
        ErpApStatementDO statement = erpApStatementMapper.selectById(id);
        if (statement == null) {
            throw exception(AP_STATEMENT_NOT_EXISTS);
        }
        return statement;
    }

    @Override
    public ErpApStatementDO getApStatement(Long id) {
        return erpApStatementMapper.selectById(id);
    }

    @Override
    public ErpApStatementDO getApStatementByBizTypeAndBizId(Integer bizType, Long bizId) {
        return erpApStatementMapper.selectByBizTypeAndBizId(bizType, bizId);
    }

    @Override
    public List<ErpApStatementDO> getApStatementListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpApStatementMapper.selectBatchIds(ids);
    }

    @Override
    public List<ErpApStatementDO> getApStatementListByBizTypeAndBizIds(Integer bizType, Collection<Long> bizIds) {
        if (CollUtil.isEmpty(bizIds)) {
            return Collections.emptyList();
        }
        return erpApStatementMapper.selectListByBizTypeAndBizIds(bizType, bizIds);
    }

    @Override
    public List<ErpApStatementItemDO> getApStatementItemListByStatementId(Long statementId) {
        return erpApStatementItemMapper.selectListByStatementId(statementId);
    }

    @Override
    public PageResult<ErpApStatementDO> getApStatementPage(ErpApStatementPageReqVO reqVO) {
        return erpApStatementMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpApStatementSummaryRespVO> getSummaryList(Long supplierId) {
        return erpApStatementMapper.selectSummaryList(supplierId);
    }

    @Override
    public PageResult<ErpApStatementDO> getPaymentEnablePage(ErpApStatementPaymentEnablePageReqVO reqVO) {
        return erpApStatementMapper.selectPaymentEnablePage(reqVO);
    }

    @Override
    public List<ErpApStatementAgingRespVO> getAgingList(ErpApStatementAgingReqVO reqVO) {
        ErpApStatementAgingReqVO actualReqVO = ObjectUtil.defaultIfNull(reqVO, new ErpApStatementAgingReqVO());
        if (actualReqVO.getAsOfDate() == null) {
            actualReqVO.setAsOfDate(LocalDate.now());
        }
        return ObjectUtil.defaultIfNull(erpApStatementMapper.selectAgingList(actualReqVO), Collections.emptyList());
    }

    @Override
    public PageResult<ErpApStatementReconciliationRespVO> getReconciliationPage(ErpApStatementReconciliationReqVO reqVO) {
        Page<ErpApStatementReconciliationRespVO> page = MyBatisUtils.buildPage(reqVO);
        Page<ErpApStatementReconciliationRespVO> result = erpApStatementMapper.selectReconciliationPage(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    private Integer calculateStatus(BigDecimal paidAmount, BigDecimal remainAmount, Integer currentStatus) {
        if (ErpApStatementStatusEnum.CLOSED.getStatus().equals(currentStatus)) {
            return currentStatus;
        }
        if (remainAmount.compareTo(BigDecimal.ZERO) == 0) {
            return ErpApStatementStatusEnum.SETTLED.getStatus();
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return ErpApStatementStatusEnum.UNPAID.getStatus();
        }
        return ErpApStatementStatusEnum.PARTIAL_PAID.getStatus();
    }

    private String buildStatementNo(Integer bizType, String bizNo) {
        return "AP-" + bizType + "-" + bizNo;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    private String buildInvoiceItemRemark(String remark, Integer invoiceStatus, String invoiceNo) {
        if (StrUtil.isNotBlank(remark)) {
            return remark.trim();
        }
        if (ErpApInvoiceStatusEnum.NONE.getStatus().equals(invoiceStatus)) {
            return "清空收票登记";
        }
        if (StrUtil.isNotBlank(invoiceNo)) {
            return "登记发票：" + invoiceNo;
        }
        return "更新收票状态";
    }

    private LocalDateTime resolveBizDate(LocalDateTime bizTime, LocalDateTime createTime, LocalDateTime updateTime) {
        if (bizTime != null) {
            return bizTime;
        }
        if (createTime != null) {
            return createTime;
        }
        if (updateTime != null) {
            return updateTime;
        }
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

}
