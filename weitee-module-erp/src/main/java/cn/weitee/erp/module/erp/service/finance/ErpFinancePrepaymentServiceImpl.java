package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentRollbackReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePrepaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.diffList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpFinancePrepaymentServiceImpl implements ErpFinancePrepaymentService {

    @Resource
    private ErpFinancePrepaymentMapper erpFinancePrepaymentMapper;
    @Resource
    private ErpFinancePrepaymentAllocateMapper erpFinancePrepaymentAllocateMapper;
    @Resource
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinancePrepayment(ErpFinancePrepaymentSaveReqVO createReqVO) {
        validateSupplierAccountAndUser(createReqVO.getSupplierId(), createReqVO.getAccountId(), createReqVO.getFinanceUserId());
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_PREPAYMENT_NO_PREFIX);
        if (erpFinancePrepaymentMapper.selectByNo(no) != null) {
            throw exception(PREPAYMENT_NO_EXISTS);
        }
        ErpFinancePrepaymentDO prepayment = BeanUtils.toBean(createReqVO, ErpFinancePrepaymentDO.class, in -> in
                .setNo(no)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setAllocatedPrice(BigDecimal.ZERO)
                .setRemainPrice(defaultAmount(createReqVO.getPrepaymentPrice())));
        erpFinancePrepaymentMapper.insert(prepayment);
        return prepayment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinancePrepayment(ErpFinancePrepaymentSaveReqVO updateReqVO) {
        ErpFinancePrepaymentDO prepayment = validatePrepaymentExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(prepayment.getStatus())) {
            throw exception(PREPAYMENT_UPDATE_FAIL_APPROVE, prepayment.getNo());
        }
        validateSupplierAccountAndUser(updateReqVO.getSupplierId(), updateReqVO.getAccountId(), updateReqVO.getFinanceUserId());
        ErpFinancePrepaymentDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinancePrepaymentDO.class, in -> in
                .setAllocatedPrice(BigDecimal.ZERO)
                .setRemainPrice(defaultAmount(updateReqVO.getPrepaymentPrice())));
        erpFinancePrepaymentMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinancePrepaymentStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        ErpFinancePrepaymentDO prepayment = validatePrepaymentExists(id);
        if (prepayment.getStatus().equals(status)) {
            throw exception(approve ? PREPAYMENT_APPROVE_FAIL : PREPAYMENT_PROCESS_FAIL);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(prepayment.getStatus())) {
            throw exception(approve ? PREPAYMENT_APPROVE_FAIL : PREPAYMENT_PROCESS_FAIL);
        }
        int updateCount = erpFinancePrepaymentMapper.updateByIdAndStatus(id, prepayment.getStatus(),
                new ErpFinancePrepaymentDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? PREPAYMENT_APPROVE_FAIL : PREPAYMENT_PROCESS_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinancePrepayment(List<Long> ids) {
        List<ErpFinancePrepaymentDO> prepayments = erpFinancePrepaymentMapper.selectByIds(ids);
        if (CollUtil.isEmpty(prepayments)) {
            return;
        }
        prepayments.forEach(prepayment -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(prepayment.getStatus())) {
                throw exception(PREPAYMENT_DELETE_FAIL_APPROVE, prepayment.getNo());
            }
        });
        prepayments.forEach(prepayment -> {
            erpFinancePrepaymentMapper.deleteById(prepayment.getId());
            List<ErpFinancePrepaymentAllocateDO> allocates = erpFinancePrepaymentAllocateMapper.selectListByPrepaymentId(prepayment.getId());
            erpFinancePrepaymentAllocateMapper.deleteByIds(convertSet(allocates, ErpFinancePrepaymentAllocateDO::getId));
        });
    }

    @Override
    public ErpFinancePrepaymentDO getFinancePrepayment(Long id) {
        return erpFinancePrepaymentMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinancePrepaymentDO> getFinancePrepaymentPage(ErpFinancePrepaymentPageReqVO pageReqVO) {
        return erpFinancePrepaymentMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocateFinancePrepayment(ErpFinancePrepaymentAllocateReqVO reqVO) {
        ErpFinancePrepaymentDO prepayment = validatePrepaymentExists(reqVO.getPrepaymentId());
        if (!ErpAuditStatus.APPROVE.getStatus().equals(prepayment.getStatus())) {
            throw exception(PREPAYMENT_ALLOCATE_FAIL_APPROVE, prepayment.getNo());
        }
        List<ErpFinancePrepaymentAllocateDO> allocates = validateAllocateItems(prepayment.getId(),
                prepayment.getSupplierId(), reqVO.getItems());
        if (CollUtil.isEmpty(allocates)) {
            return;
        }
        erpFinancePrepaymentAllocateMapper.insertBatch(allocates);
        refreshPrepaymentAmountById(prepayment.getId());
        refreshApStatementAndBizSummary(convertList(allocates, ErpFinancePrepaymentAllocateDO::getApStatementId));
        createApStatementItemLogs(prepayment.getId(), prepayment.getNo(),
                buildAllocateAmountMap(allocates),
                ErpApStatementItemTypeEnum.PREPAYMENT_ALLOCATED.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackFinancePrepaymentAllocate(ErpFinancePrepaymentRollbackReqVO reqVO) {
        List<ErpFinancePrepaymentAllocateDO> allocates = erpFinancePrepaymentAllocateMapper.selectByIds(reqVO.getIds());
        if (CollUtil.isEmpty(allocates)) {
            return;
        }
        Map<Long, List<ErpFinancePrepaymentAllocateDO>> allocateMap = CollectionUtils.convertMultiMap(allocates,
                ErpFinancePrepaymentAllocateDO::getPrepaymentId);
        allocateMap.forEach((prepaymentId, allocateList) -> {
            ErpFinancePrepaymentDO prepayment = validatePrepaymentExists(prepaymentId);
            List<ErpFinancePrepaymentAllocateDO> approvedAllocates = allocateList.stream()
                    .filter(item -> ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus().equals(item.getStatus()))
                    .toList();
            if (CollUtil.isEmpty(approvedAllocates)) {
                return;
            }
            approvedAllocates.forEach(item -> erpFinancePrepaymentAllocateMapper.updateById(
                    new ErpFinancePrepaymentAllocateDO()
                            .setId(item.getId())
                            .setStatus(ErpFinancePrepaymentAllocateStatusEnum.CANCELED.getStatus())));
            refreshPrepaymentAmountById(prepaymentId);
            refreshApStatementAndBizSummary(convertList(approvedAllocates, ErpFinancePrepaymentAllocateDO::getApStatementId));
            createApStatementItemLogs(prepaymentId, prepayment.getNo(),
                    buildRollbackAmountMap(approvedAllocates),
                    ErpApStatementItemTypeEnum.PREPAYMENT_ALLOCATE_ROLLBACK.getStatus());
        });
    }

    @Override
    public List<ErpFinancePrepaymentAllocateDO> getFinancePrepaymentAllocateListByPrepaymentId(Long prepaymentId) {
        return erpFinancePrepaymentAllocateMapper.selectListByPrepaymentId(prepaymentId);
    }

    @Override
    public List<ErpFinancePrepaymentAllocateDO> getFinancePrepaymentAllocateListByPrepaymentIds(Collection<Long> prepaymentIds) {
        if (CollUtil.isEmpty(prepaymentIds)) {
            return Collections.emptyList();
        }
        return erpFinancePrepaymentAllocateMapper.selectListByPrepaymentIds(prepaymentIds);
    }

    @Override
    public List<ErpFinancePrepaymentAllocateDO> getApprovedFinancePrepaymentAllocateListByStatementIds(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return Collections.emptyList();
        }
        return erpFinancePrepaymentAllocateMapper.selectApprovedListByStatementIds(statementIds);
    }

    private List<ErpFinancePrepaymentAllocateDO> validateAllocateItems(Long prepaymentId, Long supplierId,
                                                                       List<ErpFinancePrepaymentAllocateReqVO.Item> items) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        Map<Long, ErpApStatementDO> statementMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> requestAmountMap = new LinkedHashMap<>();
        List<ErpFinancePrepaymentAllocateDO> allocates = new ArrayList<>(items.size());
        for (ErpFinancePrepaymentAllocateReqVO.Item item : items) {
            ErpApStatementDO statement = validateAllocateStatement(item.getApStatementId(), supplierId);
            validateAllocateAmount(statement, item.getAllocateAmount());
            statementMap.put(statement.getId(), statement);
            requestAmountMap.merge(statement.getId(), defaultAmount(item.getAllocateAmount()), BigDecimal::add);
            allocates.add(new ErpFinancePrepaymentAllocateDO()
                    .setPrepaymentId(prepaymentId)
                    .setApStatementId(statement.getId())
                    .setAllocateAmount(resolveAllocateAmount(statement, item.getAllocateAmount()))
                    .setSupplierId(supplierId)
                    .setBizType(statement.getBizType())
                    .setBizId(statement.getBizId())
                    .setBizNo(statement.getBizNo())
                    .setStatus(ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus())
                    .setRemark(item.getRemark()));
        }
        validateAllocateTotalAmount(statementMap, requestAmountMap);
        return allocates;
    }

    private ErpApStatementDO validateAllocateStatement(Long apStatementId, Long supplierId) {
        ErpApStatementDO statement = apStatementService.validateApStatement(apStatementId);
        if (!ObjectUtil.equal(statement.getSupplierId(), supplierId)) {
            throw exception(AP_STATEMENT_SUPPLIER_NOT_MATCH, statement.getStatementNo());
        }
        if (ErpApStatementStatusEnum.CLOSED.getStatus().equals(statement.getStatus())) {
            throw exception(AP_STATEMENT_CLOSED, statement.getStatementNo());
        }
        return statement;
    }

    private void validateAllocateAmount(ErpApStatementDO statement, BigDecimal allocateAmount) {
        BigDecimal actualAllocateAmount = defaultAmount(allocateAmount);
        if (actualAllocateAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(AP_STATEMENT_ALLOCATE_AMOUNT_INVALID, statement.getStatementNo());
        }
    }

    private void validateAllocateTotalAmount(Map<Long, ErpApStatementDO> statementMap,
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

    private void refreshPrepaymentAmountById(Long prepaymentId) {
        ErpFinancePrepaymentDO prepayment = validatePrepaymentExists(prepaymentId);
        BigDecimal allocatedPrice = getSumValue(
                erpFinancePrepaymentAllocateMapper.selectApprovedListByPrepaymentId(prepaymentId),
                ErpFinancePrepaymentAllocateDO::getAllocateAmount, BigDecimal::add, BigDecimal.ZERO);
        BigDecimal remainPrice = defaultAmount(prepayment.getPrepaymentPrice()).subtract(allocatedPrice);
        erpFinancePrepaymentMapper.updateById(new ErpFinancePrepaymentDO()
                .setId(prepaymentId)
                .setAllocatedPrice(allocatedPrice)
                .setRemainPrice(remainPrice));
    }

    private void refreshApStatementAndBizSummary(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return;
        }
        List<Long> actualStatementIds = statementIds.stream().filter(Objects::nonNull).distinct().toList();
        if (CollUtil.isEmpty(actualStatementIds)) {
            return;
        }
        apStatementService.refreshStatementAmountByIds(actualStatementIds);
        apStatementService.refreshBizSummaryByStatementIds(actualStatementIds);
    }

    private Map<Long, BigDecimal> buildAllocateAmountMap(List<ErpFinancePrepaymentAllocateDO> allocates) {
        Map<Long, BigDecimal> changeAmountMap = new LinkedHashMap<>();
        allocates.forEach(allocate -> changeAmountMap.merge(allocate.getApStatementId(),
                defaultAmount(allocate.getAllocateAmount()), BigDecimal::add));
        return changeAmountMap;
    }

    private Map<Long, BigDecimal> buildRollbackAmountMap(List<ErpFinancePrepaymentAllocateDO> allocates) {
        Map<Long, BigDecimal> changeAmountMap = new LinkedHashMap<>();
        allocates.forEach(allocate -> changeAmountMap.merge(allocate.getApStatementId(),
                defaultAmount(allocate.getAllocateAmount()).negate(), BigDecimal::add));
        return changeAmountMap;
    }

    private void createApStatementItemLogs(Long prepaymentId, String prepaymentNo,
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
                    .setRefId(prepaymentId)
                    .setRefNo(prepaymentNo)
                    .setAmount(changeAmount)
                    .setAfterPaidAmount(defaultAmount(statement.getPaidAmount()))
                    .setAfterRemainAmount(defaultAmount(statement.getRemainAmount()))
                    .setRemark(buildAllocateRemark(itemType, prepaymentNo)));
        });
    }

    private String buildAllocateRemark(Integer itemType, String prepaymentNo) {
        if (ErpApStatementItemTypeEnum.PREPAYMENT_ALLOCATE_ROLLBACK.getStatus().equals(itemType)) {
            return "rollback prepayment " + prepaymentNo;
        }
        return "allocate prepayment " + prepaymentNo;
    }

    private ErpFinancePrepaymentDO validatePrepaymentExists(Long id) {
        ErpFinancePrepaymentDO prepayment = erpFinancePrepaymentMapper.selectById(id);
        if (prepayment == null) {
            throw exception(PREPAYMENT_NOT_EXISTS);
        }
        return prepayment;
    }

    private void validateSupplierAccountAndUser(Long supplierId, Long accountId, Long financeUserId) {
        supplierService.validateSupplier(supplierId);
        if (accountId != null) {
            accountService.validateAccount(accountId);
        }
        if (financeUserId != null) {
            adminUserApi.validateUser(financeUserId);
        }
    }

    private BigDecimal resolveAllocateAmount(ErpApStatementDO statement, BigDecimal allocateAmount) {
        BigDecimal actualAllocateAmount = defaultAmount(allocateAmount);
        if (defaultAmount(statement.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            return actualAllocateAmount.negate();
        }
        return actualAllocateAmount;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

}
