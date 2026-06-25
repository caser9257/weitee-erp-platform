package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalancePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalanceRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceGeneralLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 财务总账")
@RestController
@RequestMapping("/erp/finance-general-ledger")
@Validated
public class ErpFinanceGeneralLedgerController {

    @Resource
    private ErpFinanceGeneralLedgerService financeGeneralLedgerService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinancePeriodService financePeriodService;

    @GetMapping("/subject-balance-page")
    @Operation(summary = "获得科目余额分页")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<PageResult<ErpFinanceSubjectBalanceRespVO>> getSubjectBalancePage(
            @Valid ErpFinanceSubjectBalancePageReqVO reqVO) {
        PageResult<ErpFinanceSubjectBalanceDO> pageResult = financeGeneralLedgerService.getSubjectBalancePage(reqVO);
        ErpFinanceLedgerDO ledger = financeLedgerService.getFinanceLedger(reqVO.getLedgerId());
        ErpFinancePeriodDO period = financePeriodService.getFinancePeriod(reqVO.getPeriodId());
        List<ErpFinanceSubjectBalanceRespVO> respList = convertList(pageResult.getList(), item -> {
            ErpFinanceSubjectBalanceRespVO respVO = BeanUtils.toBean(item, ErpFinanceSubjectBalanceRespVO.class);
            respVO.setLedgerName(ledger == null ? null : ledger.getName());
            respVO.setPeriodCode(period == null ? null : period.getPeriodCode());
            return respVO;
        });
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/detail")
    @Operation(summary = "获得总账明细")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceGeneralLedgerDetailRespVO> getGeneralLedgerDetail(
            @Valid ErpFinanceGeneralLedgerDetailReqVO reqVO) {
        ErpFinanceGeneralLedgerDetailRespVO respVO = financeGeneralLedgerService.getGeneralLedgerDetail(reqVO);
        if (respVO != null && ObjectUtil.isEmpty(respVO.getItems())) {
            respVO.setItems(java.util.Collections.emptyList());
        }
        return success(respVO);
    }

    @PostMapping("/rebuild-subject-balance")
    @Operation(summary = "重建账簿科目余额")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<ErpFinanceGeneralLedgerRebuildRespVO> rebuildSubjectBalance(
            @Valid @RequestBody ErpFinanceGeneralLedgerRebuildReqVO reqVO) {
        return success(financeGeneralLedgerService.rebuildSubjectBalance(reqVO));
    }

}
