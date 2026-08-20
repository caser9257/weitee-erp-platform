package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementSummaryRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerConfigMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermissionContext;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AR_STATEMENT_NOT_EXISTS;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 应收台账 Service 实现类
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpArStatementServiceImpl implements ErpArStatementService {

    private static final String DEFAULT_CURRENCY_CODE = "CNY";

    /**
     * 应收台账编号前缀
     */
    private static final String AR_STATEMENT_NO_PREFIX = "YSTZ";

    // ========== 应收台账状态 ==========
    private static final int STATUS_UNRECEIVED = 0;       // 待收
    private static final int STATUS_PARTIAL_RECEIVED = 1; // 部分收
    private static final int STATUS_SETTLED = 2;          // 已结清
    private static final int STATUS_CLOSED = 3;           // 已关闭

    // ========== 应收台账明细类型 ==========
    private static final int ITEM_TYPE_CREATED = 1;            // 应收（生成应收）
    private static final int ITEM_TYPE_RECEIPT_ALLOCATED = 2;  // 收款分配
    private static final int ITEM_TYPE_RECEIPT_RETURNED = 3;   // 收款退回
    private static final int ITEM_TYPE_CLOSED = 4;             // 台账关闭

    @Resource
    private ErpArStatementMapper erpArStatementMapper;
    @Resource
    private ErpArStatementItemMapper erpArStatementItemMapper;
    @Resource
    private ErpNoRedisDAO erpNoRedisDAO;
    @Resource
    private ErpCustomerService erpCustomerService;
    @Resource
    private ErpFinanceDualLedgerConfigMapper dualLedgerConfigMapper;

    // ==================== 创建台账 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStatementForSaleOut(ErpSaleOutDO saleOut) {
        if (saleOut == null || saleOut.getId() == null) {
            return;
        }
        // 幂等：同一业务单据只保留一条主台账。反审核会关闭台账，再次审批时需要恢复其可收状态。
        ErpArStatementDO existedStatement = erpArStatementMapper.selectByBizTypeAndBizId(
                ErpBizTypeEnum.SALE_OUT.getType(), saleOut.getId());
        if (existedStatement != null) {
            BigDecimal receivedAmount = defaultAmount(existedStatement.getReceivedAmount());
            BigDecimal remainAmount = defaultAmount(existedStatement.getAmount()).subtract(receivedAmount);
            Integer restoredStatus = calculateStatus(receivedAmount, remainAmount, STATUS_UNRECEIVED);
            erpArStatementMapper.updateById(new ErpArStatementDO()
                    .setId(existedStatement.getId())
                    .setLedgerId(resolveExternalLedgerId(ErpBizTypeEnum.SALE_OUT.getType()))
                    .setRemainAmount(remainAmount)
                    .setStatus(restoredStatus)
                    .setRemark(saleOut.getRemark()));
            return;
        }
        BigDecimal amount = defaultAmount(saleOut.getTotalPrice());
        LocalDateTime bizDate = resolveBizDate(saleOut.getOutTime(), saleOut.getCreateTime(), saleOut.getUpdateTime());
        ErpArStatementDO statement = new ErpArStatementDO()
                .setStatementNo(erpNoRedisDAO.generate(AR_STATEMENT_NO_PREFIX))
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(saleOut.getId())
                .setBizNo(saleOut.getNo())
                .setSourceOrderId(saleOut.getOrderId())
                .setCustomerId(saleOut.getCustomerId())
                .setLedgerId(resolveExternalLedgerId(ErpBizTypeEnum.SALE_OUT.getType()))
                .setAccountId(saleOut.getAccountId())
                .setAmount(amount)
                .setReceivedAmount(BigDecimal.ZERO)
                .setRemainAmount(amount)
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setBizDate(bizDate.toLocalDate())
                .setDueDate(bizDate.toLocalDate())
                .setInvoiceStatus(0)
                .setStatus(STATUS_UNRECEIVED)
                .setRemark(saleOut.getRemark());
        erpArStatementMapper.insert(statement);

        // 创建明细：生成应收
        erpArStatementItemMapper.insert(new ErpArStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ITEM_TYPE_CREATED)
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(statement.getAmount())
                .setAfterReceivedAmount(statement.getReceivedAmount())
                .setAfterRemainAmount(statement.getRemainAmount())
                .setRemark("销售出库生成应收"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStatementForSaleReturn(ErpSaleReturnDO saleReturn) {
        if (saleReturn == null || saleReturn.getId() == null) {
            return;
        }
        // 防重复：同一业务单据只创建一次
        if (erpArStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.SALE_RETURN.getType(), saleReturn.getId()) != null) {
            return;
        }
        // 销售退货为负向金额
        BigDecimal amount = defaultAmount(saleReturn.getTotalPrice()).negate();
        LocalDateTime bizDate = resolveBizDate(saleReturn.getReturnTime(), saleReturn.getCreateTime(), saleReturn.getUpdateTime());
        ErpArStatementDO statement = new ErpArStatementDO()
                .setStatementNo(erpNoRedisDAO.generate(AR_STATEMENT_NO_PREFIX))
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(saleReturn.getId())
                .setBizNo(saleReturn.getNo())
                .setSourceOrderId(saleReturn.getOrderId())
                .setCustomerId(saleReturn.getCustomerId())
                .setLedgerId(resolveExternalLedgerId(ErpBizTypeEnum.SALE_RETURN.getType()))
                .setAccountId(saleReturn.getAccountId())
                .setAmount(amount)
                .setReceivedAmount(BigDecimal.ZERO)
                .setRemainAmount(amount)
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setBizDate(bizDate.toLocalDate())
                .setDueDate(bizDate.toLocalDate())
                .setInvoiceStatus(0)
                .setStatus(STATUS_UNRECEIVED)
                .setRemark(saleReturn.getRemark());
        erpArStatementMapper.insert(statement);

        // 创建明细：生成应收（负向）
        erpArStatementItemMapper.insert(new ErpArStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ITEM_TYPE_CREATED)
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(statement.getAmount())
                .setAfterReceivedAmount(statement.getReceivedAmount())
                .setAfterRemainAmount(statement.getRemainAmount())
                .setRemark("销售退货生成应收（负向）"));
    }

    // ==================== 关闭台账 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeStatementByBiz(Integer bizType, Long bizId, String remark) {
        ErpArStatementDO statement = erpArStatementMapper.selectByBizTypeAndBizId(bizType, bizId);
        if (statement == null || STATUS_CLOSED == statement.getStatus()) {
            return;
        }
        // 更新台账状态为已关闭
        erpArStatementMapper.updateById(new ErpArStatementDO()
                .setId(statement.getId())
                .setStatus(STATUS_CLOSED)
                .setRemark(remark));
        // 插入关闭明细
        erpArStatementItemMapper.insert(new ErpArStatementItemDO()
                .setStatementId(statement.getId())
                .setItemType(ITEM_TYPE_CLOSED)
                .setRefType(statement.getBizType())
                .setRefId(statement.getBizId())
                .setRefNo(statement.getBizNo())
                .setAmount(BigDecimal.ZERO)
                .setAfterReceivedAmount(defaultAmount(statement.getReceivedAmount()))
                .setAfterRemainAmount(defaultAmount(statement.getRemainAmount()))
                .setRemark(remark));
    }

    // ==================== 刷新金额 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshStatementAmountByIds(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return;
        }
        List<ErpArStatementDO> statements = erpArStatementMapper.selectBatchIds(statementIds);
        if (CollUtil.isEmpty(statements)) {
            return;
        }
        for (ErpArStatementDO statement : statements) {
            if (STATUS_CLOSED == statement.getStatus()) {
                continue;
            }
            // 计算新的剩余金额
            BigDecimal receivedAmount = defaultAmount(statement.getReceivedAmount());
            BigDecimal remainAmount = defaultAmount(statement.getAmount()).subtract(receivedAmount);
            erpArStatementMapper.updateById(new ErpArStatementDO()
                    .setId(statement.getId())
                    .setRemainAmount(remainAmount)
                    .setStatus(calculateStatus(receivedAmount, remainAmount, statement.getStatus())));
        }
    }

    // ==================== 分页查询 ====================

    @Override
    public PageResult<ErpArStatementRespVO> getStatementPage(ErpArStatementPageReqVO reqVO) {
        Set<Long> visibleLedgerIds = getVisibleLedgerIds();
        if (visibleLedgerIds != null && visibleLedgerIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        PageResult<ErpArStatementDO> pageResult = visibleLedgerIds == null
                ? erpArStatementMapper.selectPage(reqVO)
                : erpArStatementMapper.selectPageByVisibleLedgerIds(reqVO, visibleLedgerIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        // 批量获取客户名称
        Set<Long> customerIds = pageResult.getList().stream()
                .map(ErpArStatementDO::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ErpCustomerDO> customerMap = erpCustomerService.getCustomerMap(customerIds);

        // 转换为 RespVO
        List<ErpArStatementRespVO> voList = pageResult.getList().stream()
                .map(statement -> convertStatement(statement, customerMap))
                .collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal());
    }

    // ==================== 获取详情 ====================

    @Override
    public ErpArStatementRespVO getStatement(Long id) {
        ErpArStatementDO statement = erpArStatementMapper.selectById(id);
        if (statement == null) {
            return null;
        }
        ensureLedgerVisible(statement.getLedgerId());
        // 获取客户信息
        Map<Long, ErpCustomerDO> customerMap = Collections.emptyMap();
        if (statement.getCustomerId() != null) {
            customerMap = erpCustomerService.getCustomerMap(Collections.singleton(statement.getCustomerId()));
        }
        ErpArStatementRespVO respVO = convertStatement(statement, customerMap);

        // 加载明细
        List<ErpArStatementItemDO> items = erpArStatementItemMapper.selectListByStatementId(id);
        if (CollUtil.isNotEmpty(items)) {
            respVO.setItems(items.stream().map(this::convertItem).collect(Collectors.toList()));
        }
        return respVO;
    }

    // ==================== 按客户汇总 ====================

    @Override
    public List<ErpArStatementSummaryRespVO> getStatementSummary(Long customerId) {
        // 构建查询条件：排除已关闭的台账
        Set<Long> visibleLedgerIds = getVisibleLedgerIds();
        List<ErpArStatementDO> statements = visibleLedgerIds == null
                ? erpArStatementMapper.selectList(new LambdaQueryWrapperX<ErpArStatementDO>()
                .eqIfPresent(ErpArStatementDO::getCustomerId, customerId)
                .ne(ErpArStatementDO::getStatus, STATUS_CLOSED))
                : erpArStatementMapper.selectListByVisibleLedgerIds(customerId, visibleLedgerIds);
        if (CollUtil.isEmpty(statements)) {
            return Collections.emptyList();
        }

        // 按客户分组汇总
        Map<Long, List<ErpArStatementDO>> groupedMap = statements.stream()
                .collect(Collectors.groupingBy(ErpArStatementDO::getCustomerId));

        // 获取客户名称
        Map<Long, ErpCustomerDO> customerMap = erpCustomerService.getCustomerMap(groupedMap.keySet());

        List<ErpArStatementSummaryRespVO> result = new ArrayList<>();
        groupedMap.forEach((custId, stmtList) -> {
            ErpArStatementSummaryRespVO summary = new ErpArStatementSummaryRespVO();
            summary.setCustomerId(custId);
            ErpCustomerDO customer = customerMap.get(custId);
            summary.setCustomerName(customer != null ? customer.getName() : null);
            summary.setTotalAmount(stmtList.stream()
                    .map(s -> defaultAmount(s.getAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            summary.setTotalReceivedAmount(stmtList.stream()
                    .map(s -> defaultAmount(s.getReceivedAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            summary.setTotalRemainAmount(stmtList.stream()
                    .map(s -> defaultAmount(s.getRemainAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            summary.setStatementCount((long) stmtList.size());
            result.add(summary);
        });
        // 按剩余金额降序排列
        result.sort((a, b) -> defaultAmount(b.getTotalRemainAmount())
                .compareTo(defaultAmount(a.getTotalRemainAmount())));
        return result;
    }

    // ==================== 简单查询 ====================

    @Override
    public ErpArStatementDO getArStatement(Long id) {
        return erpArStatementMapper.selectById(id);
    }

    @Override
    public ErpArStatementDO getArStatementByBiz(Integer bizType, Long bizId) {
        return erpArStatementMapper.selectByBizTypeAndBizId(bizType, bizId);
    }

    @Override
    public List<ErpArStatementDO> getArStatementListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpArStatementMapper.selectBatchIds(ids);
    }

    // ==================== 私有方法 ====================

    /**
     * 计算台账状态
     */
    private Integer calculateStatus(BigDecimal receivedAmount, BigDecimal remainAmount, Integer currentStatus) {
        if (STATUS_CLOSED == currentStatus) {
            return currentStatus;
        }
        if (remainAmount.compareTo(BigDecimal.ZERO) == 0) {
            return STATUS_SETTLED;
        }
        if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return STATUS_UNRECEIVED;
        }
        return STATUS_PARTIAL_RECEIVED;
    }

    private Set<Long> getVisibleLedgerIds() {
        List<Long> visibleLedgerIds = FinanceDataPermissionContext.getVisibleLedgerIds();
        return visibleLedgerIds == null ? null : Set.copyOf(visibleLedgerIds);
    }

    private void ensureLedgerVisible(Long ledgerId) {
        Set<Long> visibleLedgerIds = getVisibleLedgerIds();
        if (visibleLedgerIds != null && (ledgerId == null || !visibleLedgerIds.contains(ledgerId))) {
            throw exception(FORBIDDEN);
        }
    }

    private Long resolveExternalLedgerId(Integer bizType) {
        if (dualLedgerConfigMapper == null) {
            return null;
        }
        var config = dualLedgerConfigMapper.selectByBizType(bizType);
        return config == null ? null : config.getExternalLedgerId();
    }

    /**
     * 默认金额：null 转为 0
     */
    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    /**
     * 解析业务日期
     */
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

    /**
     * 将 DO 转换为 RespVO
     */
    private ErpArStatementRespVO convertStatement(ErpArStatementDO statement, Map<Long, ErpCustomerDO> customerMap) {
        ErpArStatementRespVO respVO = new ErpArStatementRespVO();
        respVO.setId(statement.getId());
        respVO.setStatementNo(statement.getStatementNo());
        respVO.setBizType(statement.getBizType());
        respVO.setBizId(statement.getBizId());
        respVO.setBizNo(statement.getBizNo());
        respVO.setSourceOrderId(statement.getSourceOrderId());
        respVO.setSourceOrderNo(statement.getSourceOrderNo());
        respVO.setCustomerId(statement.getCustomerId());
        respVO.setAccountId(statement.getAccountId());
        respVO.setAmount(statement.getAmount());
        respVO.setReceivedAmount(statement.getReceivedAmount());
        respVO.setRemainAmount(statement.getRemainAmount());
        respVO.setCurrencyCode(statement.getCurrencyCode());
        respVO.setBizDate(statement.getBizDate());
        respVO.setDueDate(statement.getDueDate());
        respVO.setInvoiceStatus(statement.getInvoiceStatus());
        respVO.setInvoiceNo(statement.getInvoiceNo());
        respVO.setInvoiceAmount(statement.getInvoiceAmount());
        respVO.setStatus(statement.getStatus());
        respVO.setRemark(statement.getRemark());
        respVO.setCreateTime(statement.getCreateTime());
        // 客户名称
        if (statement.getCustomerId() != null) {
            ErpCustomerDO customer = customerMap.get(statement.getCustomerId());
            respVO.setCustomerName(customer != null ? customer.getName() : null);
        }
        return respVO;
    }

    /**
     * 将明细 DO 转换为 RespVO.Item
     */
    private ErpArStatementRespVO.Item convertItem(ErpArStatementItemDO item) {
        ErpArStatementRespVO.Item itemVO = new ErpArStatementRespVO.Item();
        itemVO.setId(item.getId());
        itemVO.setItemType(item.getItemType());
        itemVO.setRefType(item.getRefType());
        itemVO.setRefId(item.getRefId());
        itemVO.setRefNo(item.getRefNo());
        itemVO.setAmount(item.getAmount());
        itemVO.setAfterReceivedAmount(item.getAfterReceivedAmount());
        itemVO.setAfterRemainAmount(item.getAfterRemainAmount());
        itemVO.setRemark(item.getRemark());
        itemVO.setCreateTime(item.getCreateTime());
        return itemVO;
    }

}
