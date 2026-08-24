package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockTaskFailureLogDO;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionIqcStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 库存任务失败记录")
@RestController
@RequestMapping("/erp/stock-task-failure")
@Validated
public class ErpStockTaskFailureController {

    @Resource
    private ErpProductionIqcStockService iqcStockService;

    @GetMapping("/list")
    @Operation(summary = "获得库存任务失败记录列表")
    @PreAuthorize("@ss.hasPermission('erp:stock-task-failure:query')")
    public CommonResult<List<ErpStockTaskFailureLogDO>> getFailureLogList(
            @RequestParam(value = "status", required = false) Integer status) {
        return success(iqcStockService.getStockTaskFailureLogList(status));
    }

    @PostMapping("/retry")
    @Operation(summary = "重试失败的库存任务（生产领料扣减 / IQC 移可用）")
    @PreAuthorize("@ss.hasPermission('erp:stock-task-failure:retry')")
    public CommonResult<Boolean> retryFailure(@RequestParam("id") Long id) {
        iqcStockService.retryStockTaskFailure(id);
        return success(true);
    }

}
