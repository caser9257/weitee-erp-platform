package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundCancelReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundExecuteReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionInboundStatusEnum;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionInboundService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 自制入库")
@RestController
@RequestMapping("/erp/production-inbound")
@Validated
public class ErpProductionInboundController {

    @Resource
    private ErpProductionInboundService productionInboundService;
    @Resource
    private ErpWarehouseService warehouseService;

    @GetMapping("/get")
    @Operation(summary = "获得自制入库单")
    @PreAuthorize("@ss.hasPermission('erp:production-inbound:query')")
    public CommonResult<ErpProductionInboundRespVO> getProductionInbound(@RequestParam("id") Long id) {
        return success(buildRespVO(productionInboundService.getProductionInbound(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得自制入库分页")
    @PreAuthorize("@ss.hasPermission('erp:production-inbound:query')")
    public CommonResult<PageResult<ErpProductionInboundRespVO>> getProductionInboundPage(
            @Valid ErpProductionInboundPageReqVO pageReqVO) {
        PageResult<ErpProductionInboundDO> pageResult = productionInboundService.getProductionInboundPage(pageReqVO);
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(pageResult.getList(), ErpProductionInboundDO::getWarehouseId));
        List<ErpProductionInboundRespVO> list = BeanUtils.toBean(pageResult.getList(), ErpProductionInboundRespVO.class, item -> {
            ErpWarehouseDO warehouse = warehouseMap.get(item.getWarehouseId());
            if (warehouse != null) {
                item.setWarehouseName(warehouse.getName());
            }
            item.setStatusName(ErpProductionInboundStatusEnum.getNameByStatus(item.getStatus()));
        });
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @PutMapping("/execute")
    @Operation(summary = "执行自制入库")
    @PreAuthorize("@ss.hasPermission('erp:production-inbound:update')")
    public CommonResult<Boolean> executeProductionInbound(@Valid @RequestBody ErpProductionInboundExecuteReqVO reqVO) {
        productionInboundService.executeProductionInbound(getLoginUserId(), reqVO.getId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "作废自制入库单")
    @PreAuthorize("@ss.hasPermission('erp:production-inbound:update')")
    public CommonResult<Boolean> cancelProductionInbound(@Valid @RequestBody ErpProductionInboundCancelReqVO reqVO) {
        productionInboundService.cancelProductionInbound(reqVO.getId());
        return success(true);
    }

    @PutMapping("/revert")
    @Operation(summary = "反执行自制入库单")
    @PreAuthorize("@ss.hasPermission('erp:production-inbound:update')")
    public CommonResult<Boolean> revertProductionInbound(@Valid @RequestBody ErpProductionInboundCancelReqVO reqVO) {
        productionInboundService.revertProductionInbound(getLoginUserId(), reqVO.getId());
        return success(true);
    }

    private ErpProductionInboundRespVO buildRespVO(ErpProductionInboundDO inbound) {
        if (inbound == null) {
            return null;
        }
        ErpProductionInboundRespVO respVO = BeanUtils.toBean(inbound, ErpProductionInboundRespVO.class);
        ErpWarehouseDO warehouse = inbound.getWarehouseId() == null ? null : warehouseService.getWarehouse(inbound.getWarehouseId());
        if (warehouse != null) {
            respVO.setWarehouseName(warehouse.getName());
        }
        respVO.setStatusName(ErpProductionInboundStatusEnum.getNameByStatus(inbound.getStatus()));
        return respVO;
    }

}
