package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
import cn.weitee.erp.module.erp.service.finance.ErpServiceReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 服务接收单管理")
@RestController
@RequestMapping("/erp/service-receipt")
@Validated
@Slf4j
public class ErpServiceReceiptController {

    @Resource
    private ErpServiceReceiptService serviceReceiptService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('erp:service-receipt:create')")
    @Operation(summary = "创建服务接收单")
    public CommonResult<Long> createServiceReceipt(@Valid @RequestBody ErpServiceReceiptSaveReqVO reqVO) {
        return success(serviceReceiptService.createServiceReceipt(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('erp:service-receipt:update')")
    @Operation(summary = "更新服务接收单")
    public CommonResult<Boolean> updateServiceReceipt(@Valid @RequestBody ErpServiceReceiptSaveReqVO reqVO) {
        serviceReceiptService.updateServiceReceipt(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('erp:service-receipt:delete')")
    @Operation(summary = "删除服务接收单")
    public CommonResult<Boolean> deleteServiceReceipt(@RequestParam("id") Long id) {
        serviceReceiptService.deleteServiceReceipt(id);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:service-receipt:query')")
    @Operation(summary = "获取服务接收单")
    public CommonResult<ErpServiceReceiptRespVO> getServiceReceipt(@RequestParam("id") Long id) {
        ErpServiceReceiptDO receipt = serviceReceiptService.getServiceReceipt(id);
        return success(BeanUtils.toBean(receipt, ErpServiceReceiptRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:service-receipt:query')")
    @Operation(summary = "获取服务接收单分页")
    public CommonResult<PageResult<ErpServiceReceiptRespVO>> getServiceReceiptPage(@Valid ErpServiceReceiptPageReqVO reqVO) {
        PageResult<ErpServiceReceiptDO> pageResult = serviceReceiptService.getServiceReceiptPage(reqVO);
        return success(BeanUtils.toBean(pageResult, ErpServiceReceiptRespVO.class));
    }

    @PutMapping("/confirm")
    @PreAuthorize("@ss.hasPermission('erp:service-receipt:update')")
    @Operation(summary = "确认服务接收单")
    public CommonResult<Boolean> confirmServiceReceipt(@RequestParam("id") Long id) {
        serviceReceiptService.confirmServiceReceipt(id);
        return success(true);
    }

}
