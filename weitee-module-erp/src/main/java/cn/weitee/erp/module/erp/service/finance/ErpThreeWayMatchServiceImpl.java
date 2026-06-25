package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.match.ErpThreeWayMatchPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpLeaseContractMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpServiceReceiptMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpThreeWayMatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

/**
 * 三单匹配 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class ErpThreeWayMatchServiceImpl implements ErpThreeWayMatchService {

    @Resource
    private ErpThreeWayMatchMapper threeWayMatchMapper;

    @Resource
    private ErpLeaseContractMapper leaseContractMapper;

    @Resource
    private ErpServiceReceiptMapper serviceReceiptMapper;

    @Resource
    private ErpApInvoiceService erpApInvoiceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long match(Long leaseContractId, Long serviceReceiptId, String invoiceNo) {
        // 1. 获取合同和接收单
        ErpLeaseContractDO contract = leaseContractMapper.selectById(leaseContractId);
        ErpServiceReceiptDO receipt = serviceReceiptMapper.selectById(serviceReceiptId);

        if (contract == null) {
            throw exception(THREE_WAY_MATCH_CONTRACT_NOT_EXISTS);
        }
        if (receipt == null) {
            throw exception(THREE_WAY_MATCH_RECEIPT_NOT_EXISTS);
        }

        // 2. 幂等检查：同一合同+接收单不允许重复匹配
        long existingCount = threeWayMatchMapper.selectCount(
                new LambdaQueryWrapperX<ErpThreeWayMatchDO>()
                        .eq(ErpThreeWayMatchDO::getLeaseContractId, leaseContractId)
                        .eq(ErpThreeWayMatchDO::getServiceReceiptId, serviceReceiptId)
        );
        if (existingCount > 0) {
            log.warn("[match] 重复匹配请求，leaseContractId={}, serviceReceiptId={}", leaseContractId, serviceReceiptId);
            // 返回已存在的匹配记录ID
            ErpThreeWayMatchDO existingMatch = threeWayMatchMapper.selectOne(
                    new LambdaQueryWrapperX<ErpThreeWayMatchDO>()
                            .eq(ErpThreeWayMatchDO::getLeaseContractId, leaseContractId)
                            .eq(ErpThreeWayMatchDO::getServiceReceiptId, serviceReceiptId)
                            .last("LIMIT 1")
            );
            return existingMatch.getId();
        }

        // 3. 从发票系统查询发票金额（安全：不信任前端传入的金额）
        BigDecimal invoiceAmount = erpApInvoiceService.getAmountByInvoiceNo(invoiceNo);
        if (invoiceAmount == null) {
            throw exception(THREE_WAY_MATCH_INVOICE_NOT_EXISTS);
        }

        // 4. 执行匹配
        BigDecimal contractAmount = contract.getMonthlyRent();
        BigDecimal receiptAmount = receipt.getAmount();

        int matchResult = 0; // 默认不匹配
        String matchRemark = "";

        if (contractAmount.compareTo(receiptAmount) == 0
                && receiptAmount.compareTo(invoiceAmount) == 0) {
            matchResult = 1; // 完全匹配
            matchRemark = "三单金额完全一致";
        } else if (contractAmount.compareTo(receiptAmount) == 0
                || receiptAmount.compareTo(invoiceAmount) == 0
                || contractAmount.compareTo(invoiceAmount) == 0) {
            matchResult = 2; // 部分匹配
            matchRemark = "部分金额一致，需人工确认";
        } else {
            matchResult = 0; // 不匹配
            matchRemark = "三单金额不一致";
        }

        // 5. 保存匹配记录
        ErpThreeWayMatchDO match = ErpThreeWayMatchDO.builder()
                .leaseContractId(leaseContractId)
                .leaseContractNo(contract.getNo())
                .serviceReceiptId(serviceReceiptId)
                .serviceReceiptNo(receipt.getNo())
                .invoiceNo(invoiceNo)
                .invoiceAmount(invoiceAmount)
                .contractAmount(contractAmount)
                .receiptAmount(receiptAmount)
                .matchResult(matchResult)
                .matchRemark(matchRemark)
                .status(0) // 待匹配
                .build();
        threeWayMatchMapper.insert(match);

        log.info("[match] 三单匹配完成，matchId={}, result={}", match.getId(), matchResult);
        return match.getId();
    }

    @Override
    public PageResult<ErpThreeWayMatchDO> getMatchPage(ErpThreeWayMatchPageReqVO reqVO) {
        return threeWayMatchMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpThreeWayMatchDO> getMatchList() {
        return threeWayMatchMapper.selectList();
    }

    @Override
    public ErpThreeWayMatchDO getMatch(Long id) {
        return threeWayMatchMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmMatch(Long id) {
        ErpThreeWayMatchDO match = threeWayMatchMapper.selectById(id);
        if (match == null) {
            throw exception(THREE_WAY_MATCH_NOT_EXISTS);
        }

        // 校验状态：只有待匹配(0)状态才能确认
        if (match.getStatus() != 0) {
            throw exception(THREE_WAY_MATCH_STATUS_INVALID);
        }

        // TODO: 生成应付台账 - 需要实现 AP Statement 生成逻辑
        // 当前实现：确认后状态变为"已确认"(10)，而非"已生成应付"(20)
        // 待 AP Statement 功能实现后，应在生成成功后才将状态更新为 20
        log.warn("[confirmMatch] 应付台账生成功能尚未实现，matchId={}", id);

        // 更新状态为已确认(10)
        // 状态说明：0-待匹配 -> 10-已确认 -> 20-已生成应付（待实现）
        match.setStatus(10);
        threeWayMatchMapper.updateById(match);

        log.info("[confirmMatch] 三单匹配已确认，matchId={}", id);
    }

}
