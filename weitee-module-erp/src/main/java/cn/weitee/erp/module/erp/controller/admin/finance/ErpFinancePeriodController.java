package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodCreateYearReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePeriodService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - ERP 会计期间")
@RestController
@RequestMapping("/erp/finance-period")
@Validated
public class ErpFinancePeriodController {

    @Resource
    private ErpFinancePeriodService financePeriodService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @PostMapping("/create")
    @Operation(summary = "创建会计期间")
    @PreAuthorize("@ss.hasPermission('erp:finance-period:create')")
    public CommonResult<Long> createFinancePeriod(@Valid @RequestBody ErpFinancePeriodSaveReqVO createReqVO) {
        validateLedgerAccess(createReqVO.getLedgerId());
        return success(financePeriodService.createFinancePeriod(createReqVO));
    }

    @PostMapping("/create-year")
    @Operation(summary = "按年度生成会计期间")
    @PreAuthorize("@ss.hasPermission('erp:finance-period:create')")
    public CommonResult<Integer> createFinancePeriodsByYear(@Valid @RequestBody ErpFinancePeriodCreateYearReqVO createReqVO) {
        validateLedgerAccess(createReqVO.getLedgerId());
        return success(financePeriodService.createFinancePeriodsByYear(createReqVO));
    }

    @PutMapping("/close")
    @Operation(summary = "会计期间关账")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-period:update')")
    public CommonResult<Boolean> closeFinancePeriod(@RequestParam("id") Long id) {
        validateExistingPeriodAccess(id);
        financePeriodService.closeFinancePeriod(id);
        return success(true);
    }

    @PutMapping("/reopen")
    @Operation(summary = "会计期间反关账")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-period:update')")
    public CommonResult<Boolean> reopenFinancePeriod(@RequestParam("id") Long id) {
        validateExistingPeriodAccess(id);
        financePeriodService.reopenFinancePeriod(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会计期间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-period:query')")
    public CommonResult<ErpFinancePeriodRespVO> getFinancePeriod(@RequestParam("id") Long id) {
        ErpFinancePeriodDO period = financePeriodService.getFinancePeriod(id);
        validateExistingPeriodAccess(period);
        return success(toPeriodResp(period, loadLedgerMap(period == null ? List.of() : List.of(period))));
    }

    @GetMapping("/current-open")
    @Operation(summary = "获得业务日期所在的打开期间")
    @Parameters({
            @Parameter(name = "ledgerId", description = "账簿编号", required = true),
            @Parameter(name = "bizDate", description = "业务日期，未传默认今天", example = "2026-04-29")
    })
    @PreAuthorize("@ss.hasPermission('erp:finance-period:query')")
    public CommonResult<ErpFinancePeriodRespVO> getCurrentOpenPeriod(@RequestParam("ledgerId") Long ledgerId,
                                                                     @RequestParam(value = "bizDate", required = false) String bizDate) {
        validateLedgerAccess(ledgerId);
        LocalDate actualBizDate = bizDate == null || bizDate.isBlank() ? null : LocalDate.parse(bizDate);
        ErpFinancePeriodDO period = financePeriodService.getCurrentOpenPeriod(ledgerId, actualBizDate);
        return success(toPeriodResp(period, loadLedgerMap(period == null ? Collections.emptyList() : List.of(period))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会计期间分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-period:query')")
    public CommonResult<PageResult<ErpFinancePeriodRespVO>> getFinancePeriodPage(@Valid ErpFinancePeriodPageReqVO pageReqVO) {
        PageResult<ErpFinancePeriodDO> pageResult = financePeriodService.getFinancePeriodPage(pageReqVO);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = loadLedgerMap(pageResult.getList());
        List<ErpFinancePeriodRespVO> respList = convertList(pageResult.getList(), period -> toPeriodResp(period, ledgerMap));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出会计期间 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-period:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinancePeriodExcel(@Valid ErpFinancePeriodPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpFinancePeriodDO> list = financePeriodService.getFinancePeriodPage(pageReqVO).getList();
        Map<Long, ErpFinanceLedgerDO> ledgerMap = loadLedgerMap(list);
        List<ErpFinancePeriodRespVO> respList = convertList(list, period -> toPeriodResp(period, ledgerMap));
        ExcelUtils.write(response, "会计期间.xls", "数据", ErpFinancePeriodRespVO.class, respList);
    }

    private Map<Long, ErpFinanceLedgerDO> loadLedgerMap(List<ErpFinancePeriodDO> periods) {
        if (periods == null || periods.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ledgerIds = convertList(periods, ErpFinancePeriodDO::getLedgerId);
        if (ledgerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return convertMap(financeLedgerService.getFinanceLedgerList(ledgerIds),
                ErpFinanceLedgerDO::getId);
    }

    private void validateExistingPeriodAccess(Long id) {
        if (id == null) {
            return;
        }
        validateExistingPeriodAccess(financePeriodService.getFinancePeriod(id));
    }

    private void validateExistingPeriodAccess(ErpFinancePeriodDO period) {
        if (period != null) {
            validateLedgerAccess(period.getLedgerId());
        }
    }

    private void validateLedgerAccess(Long ledgerId) {
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw exception(FORBIDDEN);
        }
    }

    private ErpFinancePeriodRespVO toPeriodResp(ErpFinancePeriodDO period, Map<Long, ErpFinanceLedgerDO> ledgerMap) {
        if (period == null) {
            return null;
        }
        return BeanUtils.toBean(period, ErpFinancePeriodRespVO.class, vo -> {
            ErpFinanceLedgerDO ledger = ledgerMap.get(period.getLedgerId());
            vo.setLedgerName(ledger == null ? null : ledger.getName());
        });
    }
}
