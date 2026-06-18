package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpLeaseContractMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpServiceReceiptMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpThreeWayMatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

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

    @Override
    public Long match(Long leaseContractId, Long serviceReceiptId, String invoiceNo, BigDecimal invoiceAmount) {
        // 1. 获取合同和接收单
        ErpLeaseContractDO contract = leaseContractMapper.selectById(leaseContractId);
        ErpServiceReceiptDO receipt = serviceReceiptMapper.selectById(serviceReceiptId);

        if (contract == null) {
            throw new RuntimeException("[match] 租赁合同不存在：" + leaseContractId);
        }
        if (receipt == null) {
            throw new RuntimeException("[match] 服务接收单不存在：" + serviceReceiptId);
        }

        // TODO: 安全风险 - invoiceAmount 不应由前端传入，应从发票系统获取
        // 生产环境应改为：通过 invoiceNo 从 ErpApInvoiceService 查询真实发票金额
        // BigDecimal invoiceAmount = erpApInvoiceService.getAmountByInvoiceNo(invoiceNo);

        // 2. 执行匹配
        BigDecimal contractAmount = contract.getMonthlyRent();
        BigDecimal receiptAmount = receipt.getAmount();

        int matchResult = 0; // 默认不匹配
        String matchRemark = "";

        if (contractAmount.compareTo(receiptAmount) == 0
                && receiptAmount.compareTo(invoiceAmount) == 0) {
            matchResult = 1; // 完全匹配
            matchRemark = "三单金额完全一致";
        } else if (contractAmount.compareTo(receiptAmount) == 0
                || receiptAmount.compareTo(invoiceAmount) == 0) {
            matchResult = 2; // 部分匹配
            matchRemark = "部分金额一致，需人工确认";
        } else {
            matchResult = 0; // 不匹配
            matchRemark = "三单金额不一致";
        }

        // 3. 保存匹配记录
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
            throw new RuntimeException("[confirmMatch] 匹配记录不存在：" + id);
        }

        // 校验状态：只有待匹配(0)状态才能确认
        if (match.getStatus() != 0) {
            throw new RuntimeException("[confirmMatch] 匹配记录状态不正确，当前状态：" + match.getStatus());
        }

        // TODO: 生成应付台账 - 需要实现 AP Statement 生成逻辑
        // ErpApStatementDO apStatement = new ErpApStatementDO();
        // apStatement.set...
        // apStatementMapper.insert(apStatement);
        log.warn("[confirmMatch] 应付台账生成功能尚未实现，matchId={}", id);

        // 更新状态为已确认(10)，而非已生成应付(20)
        // 只有在应付台账实际生成成功后，才能标记为20
        match.setStatus(10); // 已确认
        threeWayMatchMapper.updateById(match);

        log.info("[confirmMatch] 三单匹配已确认，matchId={}", id);
    }

}
