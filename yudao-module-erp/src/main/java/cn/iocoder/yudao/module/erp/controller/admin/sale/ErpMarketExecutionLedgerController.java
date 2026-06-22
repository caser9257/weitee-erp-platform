package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerStatsVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpMarketExecutionLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 市场执行台账 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 市场执行台账")
@RestController
@RequestMapping("/erp/market-ledger")
@Validated
@Slf4j
public class ErpMarketExecutionLedgerController {

    @Resource
    private ErpMarketExecutionLedgerService erpMarketExecutionLedgerService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:market-ledger:query')")
    @Operation(summary = "分页查询市场执行台账")
    public CommonResult<PageResult<MarketLedgerVO>> getLedgerPage(@Validated MarketLedgerPageReqVO reqVO) {
        PageResult<MarketLedgerVO> result = erpMarketExecutionLedgerService.getLedgerPage(reqVO);
        return success(result);
    }

    @GetMapping("/stats")
    @PreAuthorize("@ss.hasPermission('erp:market-ledger:query')")
    @Operation(summary = "获取市场执行台账统计")
    public CommonResult<MarketLedgerStatsVO> getLedgerStats() {
        MarketLedgerStatsVO stats = erpMarketExecutionLedgerService.getLedgerStats();
        return success(stats);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@ss.hasPermission('erp:market-ledger:query')")
    @Operation(summary = "获取项目级聚合视图")
    public CommonResult<MarketLedgerVO> getProjectSummary(@PathVariable("projectId") Long projectId) {
        MarketLedgerVO result = erpMarketExecutionLedgerService.getProjectSummary(projectId);
        return success(result);
    }

}
