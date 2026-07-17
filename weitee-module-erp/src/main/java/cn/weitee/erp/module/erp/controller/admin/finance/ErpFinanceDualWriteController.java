package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogRespVO;
import cn.weitee.erp.module.erp.convert.finance.ErpFinanceDualWriteConvert;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualWriteService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "管理后台 - ERP 财务双写")
@RestController
@RequestMapping("/erp/finance/dual-write")
public class ErpFinanceDualWriteController {

    @Resource
    private ErpFinanceDualWriteService dualWriteService;

    @Resource
    private ErpFinanceDualWriteConvert dualWriteConvert;

    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @GetMapping("/log/page")
    @Operation(summary = "获得双写日志分页")
    @PreAuthorize("@ss.hasPermission('erp:finance:dual-write:log:query')")
    public CommonResult<PageResult<ErpFinanceDualWriteLogRespVO>> getDualWriteLogPage(
            ErpFinanceDualWriteLogPageReqVO pageReqVO) {
        PageResult<ErpFinanceDualWriteLogDO> pageResult = dualWriteService.getDualWriteLogPage(pageReqVO);
        List<ErpFinanceDualWriteLogDO> visibleLogs = pageResult.getList().stream()
                .filter(this::isLogVisible).toList();
        if (visibleLogs.isEmpty()) {
            return success(PageResult.empty(0L));
        }
        PageResult<ErpFinanceDualWriteLogDO> visiblePage = new PageResult<>(visibleLogs, (long) visibleLogs.size());
        return success(dualWriteConvert.convertPage(visiblePage));
    }

    @GetMapping("/log/get")
    @Operation(summary = "获得双写日志详情")
    @PreAuthorize("@ss.hasPermission('erp:finance:dual-write:log:query')")
    public CommonResult<ErpFinanceDualWriteLogRespVO> getDualWriteLog(@RequestParam("id") Long id) {
        ErpFinanceDualWriteLogDO logDO = dualWriteService.getDualWriteLog(id);
        if (logDO != null && !isLogVisible(logDO)) {
            return success(null);
        }
        return success(dualWriteConvert.convert(logDO));
    }

    @PostMapping("/log/retry")
    @Operation(summary = "重试失败的双写任务")
    @PreAuthorize("@ss.hasPermission('erp:finance:dual-write:log:retry')")
    public CommonResult<Boolean> retryDualWrite(@RequestParam("id") Long id) {
        ErpFinanceDualWriteLogDO logDO = dualWriteService.getDualWriteLog(id);
        if (logDO != null && !isLogVisible(logDO)) {
            throw exception(NOT_FOUND);
        }
        dualWriteService.retryDualWrite(id);
        return success(true);
    }

    private boolean isLogVisible(ErpFinanceDualWriteLogDO logDO) {
        return logDO != null
                && financeDataPermissionService.canAccessLedger(logDO.getSourceLedgerId())
                && financeDataPermissionService.canAccessLedger(logDO.getTargetLedgerId());
    }
}
