package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.*;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionOrderService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 生产工单")
@RestController
@RequestMapping("/erp/production-order")
@Validated
public class ErpProductionOrderController {

    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建生产工单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:create')")
    public CommonResult<Long> createProductionOrder(@Valid @RequestBody ErpProductionOrderSaveReqVO createReqVO) {
        return success(productionOrderService.createProductionOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产工单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> updateProductionOrder(@Valid @RequestBody ErpProductionOrderSaveReqVO updateReqVO) {
        productionOrderService.updateProductionOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/release")
    @Operation(summary = "下达生产工单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> releaseProductionOrder(@RequestParam("id") Long id) {
        productionOrderService.releaseProductionOrder(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完工生产工单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> finishProductionOrder(@Valid @RequestBody ErpProductionOrderFinishReqVO reqVO) {
        productionOrderService.finishProductionOrder(reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产工单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionOrderRespVO> getProductionOrder(@RequestParam("id") Long id) {
        return success(buildRespVO(productionOrderService.getProductionOrder(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产工单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpProductionOrderRespVO>> getProductionOrderPage(@Valid ErpProductionOrderPageReqVO pageReqVO) {
        PageResult<ErpProductionOrderDO> pageResult = productionOrderService.getProductionOrderPage(pageReqVO);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(pageResult.getList(), ErpProductionOrderDO::getProductId));
        List<ErpProductionOrderRespVO> list = BeanUtils.toBean(pageResult.getList(), ErpProductionOrderRespVO.class, item -> {
            ErpProductRespVO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
        });
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    private ErpProductionOrderRespVO buildRespVO(ErpProductionOrderDO order) {
        if (order == null) {
            return null;
        }
        ErpProductionOrderRespVO respVO = BeanUtils.toBean(order, ErpProductionOrderRespVO.class);
        ErpProductRespVO product = productService.getProductVOMap(List.of(order.getProductId())).get(order.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        return respVO;
    }

}
