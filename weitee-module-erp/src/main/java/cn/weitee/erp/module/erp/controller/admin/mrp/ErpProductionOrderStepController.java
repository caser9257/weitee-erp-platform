package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionOrderStepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 生产工单工序")
@RestController
@RequestMapping("/erp/production-order-step")
@Validated
public class ErpProductionOrderStepController {

    @Resource
    private ErpProductionOrderStepService productionOrderStepService;

    @GetMapping("/list")
    @Operation(summary = "获得生产工单工序列表")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<List<ErpProductionOrderStepDO>> getStepList(@RequestParam("productionOrderId") Long productionOrderId) {
        return success(productionOrderStepService.getStepList(productionOrderId));
    }

    @PutMapping("/start")
    @Operation(summary = "工序开工")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> startStep(@RequestParam("id") Long id) {
        productionOrderStepService.startStep(id);
        return success(true);
    }

    @PutMapping("/pause")
    @Operation(summary = "工序暂停")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> pauseStep(@RequestParam("id") Long id) {
        productionOrderStepService.pauseStep(id);
        return success(true);
    }

    @PutMapping("/resume")
    @Operation(summary = "工序恢复")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> resumeStep(@RequestParam("id") Long id) {
        productionOrderStepService.resumeStep(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "工序完工")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> finishStep(@RequestParam("id") Long id) {
        productionOrderStepService.finishStep(id);
        return success(true);
    }

}
