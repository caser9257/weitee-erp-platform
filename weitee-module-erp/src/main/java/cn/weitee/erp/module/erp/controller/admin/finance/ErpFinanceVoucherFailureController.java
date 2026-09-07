package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure.ErpFinanceVoucherFailurePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure.ErpFinanceVoucherFailureRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherFailureDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherFailureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

/**
 * ERP 凭证生成失败记录 Controller
 *
 * @author WeTai
 */
@Tag(name = "管理后台 - ERP 凭证生成失败记录")
@RestController
@RequestMapping("/erp/finance-voucher-failure")
@Validated
public class ErpFinanceVoucherFailureController {

    @Resource
    private ErpFinanceVoucherFailureService erpFinanceVoucherFailureService;

    @GetMapping("/page")
    @Operation(summary = "获得凭证生成失败记录分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-failure:query')")
    public CommonResult<PageResult<ErpFinanceVoucherFailureRespVO>> getVoucherFailurePage(
            @Valid ErpFinanceVoucherFailurePageReqVO pageReqVO) {
        PageResult<ErpFinanceVoucherFailureDO> pageResult = erpFinanceVoucherFailureService.getVoucherFailurePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpFinanceVoucherFailureRespVO.class));
    }

    @PostMapping("/retry")
    @Operation(summary = "重试生成凭证")
    @Parameter(name = "id", description = "失败记录编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-failure:retry')")
    public CommonResult<Long> retryVoucherFailure(@RequestParam("id") Long id) {
        return success(erpFinanceVoucherFailureService.retryVoucherFailure(id));
    }

    @PostMapping("/confirm")
    @Operation(summary = "人工确认关闭失败记录")
    @Parameter(name = "id", description = "失败记录编号", required = true, example = "1")
    @Parameter(name = "reason", description = "关闭原因", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-failure:confirm')")
    public CommonResult<Boolean> confirmVoucherFailure(@RequestParam("id") Long id,
                                                       @RequestParam("reason") String reason) {
        erpFinanceVoucherFailureService.confirmVoucherFailure(id, reason);
        return success(true);
    }

}
