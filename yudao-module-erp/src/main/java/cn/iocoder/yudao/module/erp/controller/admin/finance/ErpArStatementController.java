package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.finance.ErpArStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 应收台账 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 应收台账")
@RestController
@RequestMapping("/erp/ar-statement")
@Validated
public class ErpArStatementController {

    @Resource
    private ErpArStatementService erpArStatementService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "分页查询应收台账")
    public CommonResult<PageResult<ErpArStatementRespVO>> getStatementPage(@Validated ErpArStatementPageReqVO reqVO) {
        return success(erpArStatementService.getStatementPage(reqVO));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "获取应收台账详情")
    @Parameter(name = "id", description = "台账ID", required = true, example = "1024")
    public CommonResult<ErpArStatementRespVO> getStatement(@RequestParam("id") Long id) {
        return success(erpArStatementService.getStatement(id));
    }

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "获取应收台账汇总（按客户）")
    @Parameter(name = "customerId", description = "客户ID（可选）", example = "1024")
    public CommonResult<List<ErpArStatementSummaryRespVO>> getStatementSummary(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        return success(erpArStatementService.getStatementSummary(customerId));
    }

    @GetMapping("/summary-by-order")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "按订单汇总应收台账")
    @Parameter(name = "orderId", description = "销售订单ID", required = true, example = "1024")
    public CommonResult<ErpArStatementSummaryRespVO> getStatementSummaryByOrderId(
            @RequestParam("orderId") Long orderId) {
        // 查询该订单下所有出库单的应收台账
        List<ErpArStatementRespVO> statements = erpArStatementService.getStatementPage(
                new ErpArStatementPageReqVO().setSourceOrderId(orderId)).getList();
        
        ErpArStatementSummaryRespVO summary = new ErpArStatementSummaryRespVO();
        summary.setCustomerId(null); // 按订单维度，不需要客户ID
        summary.setStatementCount((long) statements.size());
        summary.setTotalAmount(statements.stream()
                .map(s -> s.getAmount() != null ? s.getAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        summary.setTotalReceivedAmount(statements.stream()
                .map(s -> s.getReceivedAmount() != null ? s.getReceivedAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        summary.setTotalRemainAmount(statements.stream()
                .map(s -> s.getRemainAmount() != null ? s.getRemainAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return success(summary);
    }

}
