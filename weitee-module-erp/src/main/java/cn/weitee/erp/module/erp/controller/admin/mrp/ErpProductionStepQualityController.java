package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionStepQualityPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionStepQualityRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionStepQualityDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionStepQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 工序质检")
@RestController
@RequestMapping("/erp/production-step-quality")
@Validated
public class ErpProductionStepQualityController {

    @Resource
    private ErpProductionStepQualityService productionStepQualityService;
    @Resource
    private ErpProductionOrderMapper productionOrderMapper;

    @GetMapping("/page")
    @Operation(summary = "获得工序质检分页")
    @PreAuthorize("@ss.hasPermission('erp:step-quality:query')")
    public CommonResult<PageResult<ErpProductionStepQualityRespVO>> getQualityPage(@Validated ErpProductionStepQualityPageReqVO pageReqVO) {
        PageResult<ErpProductionStepQualityDO> pageResult = productionStepQualityService.getQualityPage(pageReqVO);
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderMapper.selectBatchIds(
                        convertSet(pageResult.getList(), ErpProductionStepQualityDO::getProductionOrderId))
                .stream().collect(Collectors.toMap(ErpProductionOrderDO::getId, order -> order, (a, b) -> a));
        List<ErpProductionStepQualityRespVO> list = pageResult.getList().stream().map(quality -> {
            ErpProductionStepQualityRespVO resp = BeanUtils.toBean(quality, ErpProductionStepQualityRespVO.class);
            ErpProductionOrderDO order = orderMap.get(quality.getProductionOrderId());
            if (order != null) {
                resp.setProductionOrderNo(order.getOrderNo());
            }
            return resp;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工序质检")
    @PreAuthorize("@ss.hasPermission('erp:step-quality:query')")
    public CommonResult<ErpProductionStepQualityRespVO> getQuality(@RequestParam("id") Long id) {
        ErpProductionStepQualityDO quality = productionStepQualityService.getQuality(id);
        if (quality == null) {
            return success(null);
        }
        ErpProductionStepQualityRespVO resp = BeanUtils.toBean(quality, ErpProductionStepQualityRespVO.class);
        ErpProductionOrderDO order = productionOrderMapper.selectById(quality.getProductionOrderId());
        if (order != null) {
            resp.setProductionOrderNo(order.getOrderNo());
        }
        return success(resp);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交工序质检")
    @PreAuthorize("@ss.hasPermission('erp:step-quality:submit')")
    public CommonResult<Boolean> submitQuality(@RequestParam("id") Long id,
                                               @NotNull(message = "合格数量不能为空") @RequestParam("qualifiedQty") BigDecimal qualifiedQty,
                                               @NotNull(message = "不合格数量不能为空") @RequestParam("unqualifiedQty") BigDecimal unqualifiedQty,
                                               @RequestParam(value = "remark", required = false) String remark) {
        productionStepQualityService.submitQuality(id, SecurityFrameworkUtils.getLoginUserId(),
                qualifiedQty, unqualifiedQty, remark);
        return success(true);
    }

}
