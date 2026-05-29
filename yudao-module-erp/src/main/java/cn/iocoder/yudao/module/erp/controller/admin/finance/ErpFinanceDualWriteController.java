package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogRespVO;
import cn.iocoder.yudao.module.erp.convert.finance.ErpFinanceDualWriteConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceDualWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 财务双写")
@RestController
@RequestMapping("/erp/finance/dual-write")
public class ErpFinanceDualWriteController {

    @Resource
    private ErpFinanceDualWriteService dualWriteService;

    @Resource
    private ErpFinanceDualWriteConvert dualWriteConvert;

    @GetMapping("/log/page")
    @Operation(summary = "获得双写日志分页")
    @PreAuthorize("@ss.hasPermission('erp:finance:dual-write:log:query')")
    public CommonResult<PageResult<ErpFinanceDualWriteLogRespVO>> getDualWriteLogPage(
            ErpFinanceDualWriteLogPageReqVO pageReqVO) {
        PageResult<ErpFinanceDualWriteLogDO> pageResult = dualWriteService.getDualWriteLogPage(pageReqVO);
        return success(dualWriteConvert.convertPage(pageResult));
    }

    @GetMapping("/log/get")
    @Operation(summary = "获得双写日志详情")
    @PreAuthorize("@ss.hasPermission('erp:finance:dual-write:log:query')")
    public CommonResult<ErpFinanceDualWriteLogRespVO> getDualWriteLog(@RequestParam("id") Long id) {
        ErpFinanceDualWriteLogDO logDO = dualWriteService.getDualWriteLog(id);
        return success(dualWriteConvert.convert(logDO));
    }

    @PostMapping("/log/retry")
    @Operation(summary = "重试失败的双写任务")
    @PreAuthorize("@ss.hasPermission('erp:finance:dual-write:log:retry')")
    public CommonResult<Boolean> retryDualWrite(@RequestParam("id") Long id) {
        dualWriteService.retryDualWrite(id);
        return success(true);
    }
}
