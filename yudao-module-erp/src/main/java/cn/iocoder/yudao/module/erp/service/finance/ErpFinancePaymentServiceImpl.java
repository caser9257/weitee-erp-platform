package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_ALLOCATE_AMOUNT_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_CLOSED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_SUPPLIER_NOT_MATCH;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_DELETE_FAIL_APPROVE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_DELETE_FAIL_PROCESSING;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NO_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_PROCESS_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_UPDATE_FAIL_APPROVE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_UPDATE_FAIL_PROCESSING;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_VOID_FAIL;

/**
 * ERP 付款单 Service 实现类
 */
@Service
@Validated
public class ErpFinancePaymentServiceImpl implements ErpFinancePaymentService {

    @Resource
    private ErpFinancePaymentMapper erpFinancePaymentMapper;
    @Resource
    private ErpFinancePaymentItemMapper erpFinancePaymentItemMapper;
    @Resource
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Resource
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Resource
    private ErpApStatementService apStatementService;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinancePayment(ErpFinancePaymentSaveReqVO createReqVO) {
        List<ErpFinancePaymentItemDO> paymentItems = validateFinancePaymentItems(
                createReqVO.getSupplierId(), createReqVO.getItems());
        supplierService.validateSupplier(createReqVO.getSupplierId());
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        if (createReqVO.getFinanceUserId() != null) {
            adminUserApi.validateUser(createReqVO.getFinanceUserId());
        }
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_PAYMENT_NO_PREFIX);
        if (erpFinancePaymentMapper.selectByNo(no) != null) {
            throw exception(FINANCE_PAYMENT_NO_EXISTS);
        }

        ErpFinancePaymentDO payment = BeanUtils.toBean(createReqVO, ErpFinancePaymentDO.class,
                in -> in.setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()));
        calculateTotalPrice(payment, paymentItems);
        erpFinancePaymentMapper.insert(payment);
        paymentItems.forEach(item -> item.setPaymentId(payment.getId()));
        erpFinancePaymentItemMapper.insertBatch(paymentItems);
        return payment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinancePayment(ErpFinancePaymentSaveReqVO updateReqVO) {
        ErpFinancePaymentDO payment = validateFinancePaymentExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(payment.getStatus())) {
            throw exception(FINANCE_PAYMENT_UPDATE_FAIL_APPROVE, payment.getNo());
        }
        if (ErpAuditStatus.PROCESS.getStatus().equals(payment.getStatus())
                && StrUtil.isNotBlank(payment.getProcessInstanceId())) {
            throw exception(FINANCE_PAYMENT_UPDATE_FAIL_PROCESSING, payment.getNo());
        }
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        if (updateReqVO.getFinanceUserId() != null) {
            adminUserApi.validateUser(updateReqVO.getFinanceUserId());
        }
        List<ErpFinancePaymentItemDO> paymentItems = validateFinancePaymentItems(
                updateReqVO.getSupplierId(), updateReqVO.getItems());

        ErpFinancePaymentDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinancePaymentDO.class);
        calculateTotalPrice(updateObj, paymentItems);
        erpFinancePaymentMapper.updateById(updateObj);
        updateFinancePaymentItemList(updateReqVO.getId(), paymentItems);
    }

    private void calculateTotalPrice(ErpFinancePaymentDO payment, List<ErpFinancePaymentItemDO> paymentItems) {
        payment.setTotalPrice(getSumValue(paymentItems, ErpFinancePaymentItemDO::getPaymentPrice, BigDecimal::add, BigDecimal.ZERO));
        payment.setPaymentPrice(payment.getTotalPrice().subtract(payment.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinancePaymentStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        ErpFinancePaymentDO payment = validateFinancePaymentExists(id);
        if (StrUtil.isNotBlank(payment.getProcessInstanceId())) {
            throw exception(FINANCE_PAYMENT_UPDATE_FAIL_PROCESSING, payment.getNo());
        }
        if (payment.getStatus().equals(status)) {
            throw exception(approve ? FINANCE_PAYMENT_APPROVE_FAIL : FINANCE_PAYMENT_PROCESS_FAIL);
        }
        List<ErpFinancePaymentItemDO> paymentItems = erpFinancePaymentItemMapper.selectListByPaymentId(id);
        Map<Long, ErpApStatementDO> statementMap = approve
                ? validateApprovePaymentItems(payment.getSupplierId(), paymentItems)
                : Collections.emptyMap();
        List<ErpFinancePaymentAllocateDO> approvedAllocates = approve
                ? Collections.emptyList()
                : selectApprovedAllocateList(id);

        int updateCount = erpFinancePaymentMapper.updateByIdAndStatus(id, payment.getStatus(),
                new ErpFinancePaymentDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? FINANCE_PAYMENT_APPROVE_FAIL : FINANCE_PAYMENT_PROCESS_FAIL);
        }
        if (approve) {
            createApprovedAllocateFacts(payment, paymentItems, statementMap);
        } else {
            cancelApprovedAllocateFacts(approvedAllocates);
        }
        refreshApStatementAndBizSummary(paymentItems);
        createApStatementItemLogs(payment.getId(), payment.getNo(),
                approve ? buildAllocateAmountMap(paymentItems, statementMap) : buildRollbackAmountMap(approvedAllocates),
                approve ? ErpApStatementItemTypeEnum.PAYMENT_ALLOCATED.getStatus()
                        : ErpApStatementItemTypeEnum.PAYMENT_ALLOCATE_ROLLBACK.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinancePaymentStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        boolean reject = ErpAuditStatus.REJECT.getStatus().equals(status);
        ErpFinancePaymentDO payment = validateFinancePaymentExists(id);
        if (!approve && !reject) {
            throw exception(FINANCE_PAYMENT_PROCESS_FAIL);
        }
        if (!StrUtil.equals(processInstanceId, payment.getProcessInstanceId())) {
            return;
        }

        List<ErpFinancePaymentItemDO> paymentItems = erpFinancePaymentItemMapper.selectListByPaymentId(id);
        Map<Long, ErpApStatementDO> statementMap = approve
                ? validateApprovePaymentItems(payment.getSupplierId(), paymentItems)
                : Collections.emptyMap();

        int updateCount = erpFinancePaymentMapper.updateByIdAndStatus(id, payment.getStatus(),
                new ErpFinancePaymentDO()
                        .setStatus(status)
                        .setProcessInstanceId(processInstanceId));
        if (updateCount == 0) {
            throw exception(approve ? FINANCE_PAYMENT_APPROVE_FAIL : FINANCE_PAYMENT_PROCESS_FAIL);
        }
        if (approve) {
            createApprovedAllocateFacts(payment, paymentItems, statementMap);
            refreshApStatementAndBizSummary(paymentItems);
            createApStatementItemLogs(payment.getId(), payment.getNo(),
                    buildAllocateAmountMap(paymentItems, statementMap),
                    ErpApStatementItemTypeEnum.PAYMENT_ALLOCATED.getStatus());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidFinancePayment(Long id, String reason) {
        ErpFinancePaymentDO payment = validateFinancePaymentExists(id);
        // 幂等处理：已作废则直接返回
        if (ErpAuditStatus.VOID.getStatus().equals(payment.getStatus())) {
            return;
        }
        // 只有已审核的付款单才能作废
        if (!ErpAuditStatus.APPROVE.getStatus().equals(payment.getStatus())) {
            throw exception(FINANCE_PAYMENT_VOID_FAIL, payment.getNo());
        }
        // 更新状态为已作废
        int updateCount = erpFinancePaymentMapper.updateByIdAndStatus(id, payment.getStatus(),
                new ErpFinancePaymentDO()
                        .setStatus(ErpAuditStatus.VOID.getStatus())
                        .setVoidReason(reason)
                        .setVoidTime(LocalDateTime.now())
                        .setVoidBy(SecurityFrameworkUtils.getLoginUserId()));
        if (updateCount == 0) {
            throw exception(FINANCE_PAYMENT_VOID_FAIL, payment.getNo());
        }
        // 释放关联的应付台账额度
        List<ErpFinancePaymentItemDO> paymentItems = erpFinancePaymentItemMapper.selectListByPaymentId(id);
        if (CollUtil.isNotEmpty(paymentItems)) {
            cancelApprovedAllocateFacts(selectApprovedAllocateList(id));
            refreshApStatementAndBizSummary(paymentItems);
            createApStatementItemLogs(payment.getId(), payment.getNo(),
                    buildRollbackAmountMap(selectApprovedAllocateList(id)),
                    ErpApStatementItemTypeEnum.PAYMENT_ALLOCATE_ROLLBACK.getStatus());
        }
    }

    private List<ErpFinancePaymentItemDO> validateFinancePaymentItems(Long supplierId,
                                                                  List<ErpFinancePaymentSaveReqVO.Item> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpApStatementDO> statementMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> requestAmountMap = new LinkedHashMap<>();
        List<ErpFinancePaymentItemDO> paymentItems = new ArrayList<>(list.size());
        list.forEach(reqItem -> {
            ErpApStatementDO statement = validatePaymentStatement(reqItem.getApStatementId(), supplierId);
            validatePaymentPrice(statement, reqItem.getPaymentPrice());
            statementMap.put(statement.getId(), statement);
            requestAmountMap.merge(statement.getId(), defaultAmount(reqItem.getPaymentPrice()), BigDecimal::add);
            paymentItems.add(BeanUtils.toBean(reqItem, ErpFinancePaymentItemDO.class, item -> item
                    .setApStatementId(statement.getId())
                    .setBizType(statement.getBizType())
                    .setBizId(statement.getBizId())
                    .setBizNo(statement.getBizNo())
                    .setTotalPrice(defaultAmount(statement.getAmount()))
                    .setPaidPrice(defaultAmount(statement.getPaidAmount()))));
        });
        validateAllocateAmount(statementMap, requestAmountMap);
        return paymentItems;
    }

    private Map<Long, ErpApStatementDO> validateApprovePaymentItems(Long supplierId,
                                                                    List<ErpFinancePaymentItemDO> paymentItems) {
        if (CollUtil.isEmpty(paymentItems)) {
            return Collections.emptyMap();
        }
        Map<Long, ErpApStatementDO> statementMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> requestAmountMap = new LinkedHashMap<>();
        paymentItems.forEach(paymentItem -> {
            ErpApStatementDO statement = validatePaymentStatement(paymentItem.getApStatementId(), supplierId);
            validatePaymentPrice(statement, paymentItem.getPaymentPrice());
            statementMap.put(statement.getId(), statement);
            requestAmountMap.merge(statement.getId(), defaultAmount(paymentItem.getPaymentPrice()), BigDecimal::add);
        });
        validateAllocateAmount(statementMap, requestAmountMap);
        return statementMap;
    }

    private ErpApStatementDO validatePaymentStatement(Long apStatementId, Long supplierId) {
        ErpApStatementDO statement = apStatementService.validateApStatement(apStatementId);
        if (!ObjectUtil.equal(statement.getSupplierId(), supplierId)) {
            throw exception(AP_STATEMENT_SUPPLIER_NOT_MATCH, statement.getStatementNo());
        }
        if (ErpApStatementStatusEnum.CLOSED.getStatus().equals(statement.getStatus())) {
            throw exception(AP_STATEMENT_CLOSED, statement.getStatementNo());
        }
        return statement;
    }

    private void validatePaymentPrice(ErpApStatementDO statement, BigDecimal paymentPrice) {
        BigDecimal actualPaymentPrice = defaultAmount(paymentPrice);
        if (actualPaymentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(AP_STATEMENT_ALLOCATE_AMOUNT_INVALID, statement.getStatementNo());
        }
    }

    private void validateAllocateAmount(Map<Long, ErpApStatementDO> statementMap,
                                    Map<Long, BigDecimal> requestAmountMap) {
        requestAmountMap.forEach((statementId, requestAmount) -> {
            ErpApStatementDO statement = statementMap.get(statementId);
            BigDecimal availableAmount = defaultAmount(statement.getRemainAmount()).abs();
            if (requestAmount.compareTo(availableAmount) > 0) {
                throw exception(AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED,
                        statement.getStatementNo(), requestAmount, availableAmount);
            }
        });
    }

    private void updateFinancePaymentItemList(Long id, List<ErpFinancePaymentItemDO> newList) {
        List<ErpFinancePaymentItemDO> oldList = erpFinancePaymentItemMapper.selectListByPaymentId(id);
        List<List<ErpFinancePaymentItemDO>> diffList = diffList(oldList, newList,
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(item -> item.setPaymentId(id));
            erpFinancePaymentItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpFinancePaymentItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpFinancePaymentItemMapper.deleteByIds(convertList(diffList.get(2), ErpFinancePaymentItemDO::getId));
        }
    }

    private void createApprovedAllocateFacts(ErpFinancePaymentDO payment,
                                             List<ErpFinancePaymentItemDO> paymentItems,
                                             Map<Long, ErpApStatementDO> statementMap) {
        paymentItems.forEach(paymentItem -> {
            ErpApStatementDO statement = statementMap.get(paymentItem.getApStatementId());
            if (statement == null) {
                return;
            }
            erpFinancePaymentAllocateMapper.insert(new ErpFinancePaymentAllocateDO()
                    .setPaymentId(payment.getId())
                    .setPaymentItemId(paymentItem.getId())
                    .setApStatementId(statement.getId())
                    .setAllocateAmount(resolveAllocateAmount(statement, paymentItem.getPaymentPrice()))
                    .setSupplierId(payment.getSupplierId())
                    .setBizType(statement.getBizType())
                    .setBizId(statement.getBizId())
                    .setBizNo(statement.getBizNo())
                    .setStatus(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus())
                    .setRemark(paymentItem.getRemark()));
        });
    }

    private List<ErpFinancePaymentAllocateDO> selectApprovedAllocateList(Long paymentId) {
        return erpFinancePaymentAllocateMapper.selectListByPaymentId(paymentId).stream()
                .filter(item -> ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus().equals(item.getStatus()))
                .toList();
    }

    private void cancelApprovedAllocateFacts(List<ErpFinancePaymentAllocateDO> approvedAllocates) {
        approvedAllocates.forEach(approvedAllocate -> erpFinancePaymentAllocateMapper.updateById(
                new ErpFinancePaymentAllocateDO()
                        .setId(approvedAllocate.getId())
                        .setStatus(ErpFinancePaymentAllocateStatusEnum.CANCELED.getStatus())));
    }

    private void refreshApStatementAndBizSummary(List<ErpFinancePaymentItemDO> paymentItems) {
        if (CollUtil.isEmpty(paymentItems)) {
            return;
        }
        List<Long> statementIds = paymentItems.stream()
                .map(ErpFinancePaymentItemDO::getApStatementId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(statementIds)) {
            return;
        }
        apStatementService.refreshStatementAmountByIds(statementIds);
        apStatementService.refreshBizSummaryByStatementIds(statementIds);
    }

    private Map<Long, BigDecimal> buildAllocateAmountMap(List<ErpFinancePaymentItemDO> paymentItems,
                                                         Map<Long, ErpApStatementDO> statementMap) {
        Map<Long, BigDecimal> changeAmountMap = new LinkedHashMap<>();
        paymentItems.forEach(paymentItem -> {
            ErpApStatementDO statement = statementMap.get(paymentItem.getApStatementId());
            if (statement == null) {
                return;
            }
            changeAmountMap.merge(statement.getId(),
                    resolveAllocateAmount(statement, paymentItem.getPaymentPrice()), BigDecimal::add);
        });
        return changeAmountMap;
    }

    private Map<Long, BigDecimal> buildRollbackAmountMap(List<ErpFinancePaymentAllocateDO> approvedAllocates) {
        Map<Long, BigDecimal> changeAmountMap = new LinkedHashMap<>();
        approvedAllocates.forEach(allocate -> changeAmountMap.merge(allocate.getApStatementId(),
                defaultAmount(allocate.getAllocateAmount()).negate(), BigDecimal::add));
        return changeAmountMap;
    }

    private void createApStatementItemLogs(Long paymentId, String paymentNo,
                                           Map<Long, BigDecimal> changeAmountMap, Integer itemType) {
        if (changeAmountMap.isEmpty()) {
            return;
        }
        Map<Long, ErpApStatementDO> statementMap = convertMap(
                apStatementService.getApStatementListByIds(changeAmountMap.keySet()),
                ErpApStatementDO::getId);
        changeAmountMap.forEach((statementId, changeAmount) -> {
            ErpApStatementDO statement = statementMap.get(statementId);
            if (statement == null) {
                return;
            }
            erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                    .setStatementId(statementId)
                    .setItemType(itemType)
                    .setRefId(paymentId)
                    .setRefNo(paymentNo)
                    .setAmount(changeAmount)
                    .setAfterPaidAmount(defaultAmount(statement.getPaidAmount()))
                    .setAfterRemainAmount(defaultAmount(statement.getRemainAmount()))
                    .setRemark(buildPaymentItemRemark(itemType, paymentNo)));
        });
    }

    private String buildPaymentItemRemark(Integer itemType, String paymentNo) {
        if (ErpApStatementItemTypeEnum.PAYMENT_ALLOCATE_ROLLBACK.getStatus().equals(itemType)) {
            return "rollback payment " + paymentNo;
        }
        return "allocate payment " + paymentNo;
    }

    private BigDecimal resolveAllocateAmount(ErpApStatementDO statement, BigDecimal paymentPrice) {
        BigDecimal actualPaymentPrice = defaultAmount(paymentPrice);
        if (defaultAmount(statement.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            return actualPaymentPrice.negate();
        }
        return actualPaymentPrice;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinancePayment(List<Long> ids) {
        List<ErpFinancePaymentDO> payments = erpFinancePaymentMapper.selectByIds(ids);
        if (CollUtil.isEmpty(payments)) {
            return;
        }
        payments.forEach(payment -> {
            if (ErpAuditStatus.PROCESS.getStatus().equals(payment.getStatus())
                    && StrUtil.isNotBlank(payment.getProcessInstanceId())) {
                throw exception(FINANCE_PAYMENT_DELETE_FAIL_PROCESSING, payment.getNo());
            }
            if (ErpAuditStatus.APPROVE.getStatus().equals(payment.getStatus())) {
                throw exception(FINANCE_PAYMENT_DELETE_FAIL_APPROVE, payment.getNo());
            }
        });
        payments.forEach(payment -> {
            erpFinancePaymentMapper.deleteById(payment.getId());
            List<ErpFinancePaymentItemDO> paymentItems = erpFinancePaymentItemMapper.selectListByPaymentId(payment.getId());
            erpFinancePaymentItemMapper.deleteByIds(convertSet(paymentItems, ErpFinancePaymentItemDO::getId));
        });
    }

    private ErpFinancePaymentDO validateFinancePaymentExists(Long id) {
        ErpFinancePaymentDO payment = erpFinancePaymentMapper.selectById(id);
        if (payment == null) {
            throw exception(FINANCE_PAYMENT_NOT_EXISTS);
        }
        return payment;
    }

    @Override
    public ErpFinancePaymentDO getFinancePayment(Long id) {
        return erpFinancePaymentMapper.selectById(id);
    }

    @Override
    public List<ErpFinancePaymentDO> getFinancePaymentListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpFinancePaymentMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ErpFinancePaymentDO> getFinancePaymentPage(ErpFinancePaymentPageReqVO pageReqVO) {
        return erpFinancePaymentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpFinancePaymentItemDO> getFinancePaymentItemListByPaymentId(Long paymentId) {
        return erpFinancePaymentItemMapper.selectListByPaymentId(paymentId);
    }

    @Override
    public List<ErpFinancePaymentItemDO> getFinancePaymentItemListByPaymentIds(Collection<Long> paymentIds) {
        if (CollUtil.isEmpty(paymentIds)) {
            return Collections.emptyList();
        }
        return erpFinancePaymentItemMapper.selectListByPaymentIds(paymentIds);
    }

    @Override
    public List<ErpFinancePaymentAllocateDO> getFinancePaymentAllocateListByPaymentId(Long paymentId) {
        return erpFinancePaymentAllocateMapper.selectListByPaymentId(paymentId);
    }

    @Override
    public List<ErpFinancePaymentAllocateDO> getFinancePaymentAllocateListByStatementIds(Collection<Long> statementIds) {
        return erpFinancePaymentAllocateMapper.selectListByStatementIds(statementIds);
    }

}
