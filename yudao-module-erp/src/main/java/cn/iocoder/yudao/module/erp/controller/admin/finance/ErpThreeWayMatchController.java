package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpThreeWayMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 三单匹配管理")
@RestController
@RequestMapping("/erp/three-way-match")
@Validated
@Slf4j
public class ErpThreeWayMatchController {

    @Resource
    private ErpThreeWayMatchService threeWayMatchService;

    @PostMapping("/match")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:match')")
    @Operation(summary = "执行三单匹配")
    public CommonResult<Long> match(
            @RequestParam("leaseContractId") Long leaseContractId,
            @RequestParam("serviceReceiptId") Long serviceReceiptId,
            @RequestParam("invoiceNo") String invoiceNo,
            @RequestParam("invoiceAmount") BigDecimal invoiceAmount) {
        // TODO: 安全风险 - invoiceAmount 不应由前端传入，应从发票系统获取
        // 生产环境应改为：通过 invoiceNo 从 ErpApInvoiceService 查询真实发票金额
        return success(threeWayMatchService.match(leaseContractId, serviceReceiptId, invoiceNo, invoiceAmount));
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:query')")
    @Operation(summary = "获取匹配记录列表")
    public CommonResult<List<ErpThreeWayMatchDO>> getMatchList() {
        return success(threeWayMatchService.getMatchList());
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:query')")
    @Operation(summary = "获取匹配记录")
    public CommonResult<ErpThreeWayMatchDO> getMatch(@RequestParam("id") Long id) {
        return success(threeWayMatchService.getMatch(id));
    }

    @PutMapping("/confirm")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:confirm')")
    @Operation(summary = "确认匹配")
    public CommonResult<Boolean> confirmMatch(@RequestParam("id") Long id) {
        threeWayMatchService.confirmMatch(id);
        return success(true);
    }

}
