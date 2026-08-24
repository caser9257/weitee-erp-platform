package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderFinishReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderSummaryRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProcessRouteMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProcessRouteStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStepStatusEnum;
import cn.weitee.erp.module.erp.framework.event.ErpProductionOrderReleasedEvent;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.ErpWarehouseService;
import cn.hutool.core.collection.CollUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_ITEM_EMPTY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_EFFECTIVE_BOM_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_FINISH_QTY_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_RELEASED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_PLAN_TIME_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_ROUTE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_UNFINISHED;

@Service
@Validated
public class ErpProductionOrderServiceImpl implements ErpProductionOrderService {

    @Resource
    private ErpProductionOrderMapper erpProductionOrderMapper;
    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpProductionMaterialMapper erpProductionMaterialMapper;
    @Resource
    private ErpProcessRouteMapper erpProcessRouteMapper;
    @Resource
    private ErpProcessRouteStepMapper erpProcessRouteStepMapper;
    @Resource
    private ErpProductionOrderStepMapper erpProductionOrderStepMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductionFinishQualityService productionFinishQualityService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionOrder(ErpProductionOrderSaveReqVO createReqVO) {
        validatePlanTime(createReqVO.getPlanStartTime(), createReqVO.getPlanEndTime());
        productService.validProductList(List.of(createReqVO.getProductId()));
        ErpProductionOrderDO order = BeanUtils.toBean(createReqVO, ErpProductionOrderDO.class, item -> item
                .setOrderNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_ORDER_NO_PREFIX))
                .setFinishedQty(BigDecimal.ZERO)
                .setStatus(ErpProductionOrderStatusEnum.CREATED.getStatus())
                .setSourceType("MANUAL"));
        erpProductionOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionOrderBySuggest(ErpProductionSuggestDO suggest, String remark) {
        ErpProductionOrderDO order = new ErpProductionOrderDO()
                .setOrderNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_ORDER_NO_PREFIX))
                .setProductId(suggest.getProductId())
                .setProjectId(suggest.getProjectId())
                .setPlanQty(suggest.getSuggestQty())
                .setFinishedQty(BigDecimal.ZERO)
                .setPlanStartTime(suggest.getSuggestStartDate().atStartOfDay())
                .setPlanEndTime(suggest.getSuggestEndDate().atTime(23, 59, 59))
                .setStatus(ErpProductionOrderStatusEnum.CREATED.getStatus())
                .setSourceType("MRP_SUGGEST")
                .setSourceId(suggest.getId())
                .setSourceOrderId(suggest.getSourceOrderId())
                .setSourceItemId(suggest.getSourceItemId())
                .setRemark(remark);
        erpProductionOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductionOrder(ErpProductionOrderSaveReqVO updateReqVO) {
        ErpProductionOrderDO order = validateProductionOrderExists(updateReqVO.getId());
        if (!ErpProductionOrderStatusEnum.CREATED.getStatus().equals(order.getStatus())) {
            // 已下达/已完工/已关闭工单禁止修改，避免与已生成的用料、工序快照不一致
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
        validatePlanTime(updateReqVO.getPlanStartTime(), updateReqVO.getPlanEndTime());
        productService.validProductList(List.of(updateReqVO.getProductId()));
        erpProductionOrderMapper.updateById(BeanUtils.toBean(updateReqVO, ErpProductionOrderDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseProductionOrder(Long id) {
        ErpProductionOrderDO order = validateProductionOrderExists(id);
        if (!ErpProductionOrderStatusEnum.CREATED.getStatus().equals(order.getStatus())) {
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
        ErpBomDO bom = erpBomMapper.selectEffectiveByProductId(order.getProductId());
        if (bom == null) {
            throw exception(PRODUCTION_ORDER_EFFECTIVE_BOM_NOT_EXISTS);
        }
        List<ErpBomItemDO> bomItems = erpBomItemMapper.selectListByBomId(bom.getId());
        if (bomItems == null || bomItems.isEmpty()) {
            throw exception(BOM_ITEM_EMPTY);
        }
        // 先校验路线（有路线工单必须存在且启用），全部校验通过后才写入快照
        ErpProcessRouteDO route = null;
        if (order.getRouteId() != null) {
            route = validateRouteForRelease(order);
        }
        // 用料快照：从有效 BOM 复制
        erpProductionMaterialMapper.insertBatch(bomItems.stream()
                .map(item -> buildProductionMaterial(order, item))
                .collect(Collectors.toList()));
        // 工序快照：工单绑定有效工艺路线时，从路线复制；无路线工单保留兼容路径，只生成用料快照
        String routeVersion = order.getRouteVersion();
        if (route != null) {
            routeVersion = route.getVersion();
            List<ErpProcessRouteStepDO> routeSteps = erpProcessRouteStepMapper.selectListByRouteId(route.getId());
            if (CollUtil.isNotEmpty(routeSteps)) {
                erpProductionOrderStepMapper.insertBatch(routeSteps.stream()
                        .map(step -> buildProductionOrderStep(order, step))
                        .collect(Collectors.toList()));
            }
        }
        erpProductionOrderMapper.updateById(new ErpProductionOrderDO().setId(id)
                .setRouteVersion(routeVersion)
                .setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
        // 发布下达事件：供 MES 模块监听生成工序任务（AFTER_COMMIT 执行，不影响本事务）
        eventPublisher.publishEvent(new ErpProductionOrderReleasedEvent(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishProductionOrder(ErpProductionOrderFinishReqVO reqVO) {
        ErpProductionOrderDO order = validateProductionOrderExists(reqVO.getId());
        if (!ErpProductionOrderStatusEnum.RELEASED.getStatus().equals(order.getStatus())) {
            throw exception(order == null || ErpProductionOrderStatusEnum.CREATED.getStatus().equals(order.getStatus())
                    ? PRODUCTION_ORDER_NOT_RELEASED
                    : PRODUCTION_ORDER_STATUS_INVALID);
        }
        if (order.getPlanQty() != null && reqVO.getFinishedQty().compareTo(order.getPlanQty()) > 0) {
            throw exception(PRODUCTION_ORDER_FINISH_QTY_EXCEED);
        }
        // 工序完整性：存在工序快照的工单，完工前必须全部工序完工（无快照旧工单兼容）
        List<ErpProductionOrderStepDO> steps = erpProductionOrderStepMapper.selectListByOrderId(order.getId());
        if (CollUtil.isNotEmpty(steps) && steps.stream()
                .anyMatch(step -> !ErpProductionOrderStepStatusEnum.FINISHED.getStatus().equals(step.getStepStatus()))) {
            throw exception(PRODUCTION_ORDER_STEP_UNFINISHED);
        }
        warehouseService.validWarehouseList(List.of(reqVO.getWarehouseId()));
        erpProductionOrderMapper.updateById(new ErpProductionOrderDO().setId(order.getId())
                .setFinishedQty(reqVO.getFinishedQty())
                .setWarehouseId(reqVO.getWarehouseId())
                .setStatus(ErpProductionOrderStatusEnum.FINISHED.getStatus()));
        productionFinishQualityService.createPendingQualityAfterFinish(order, reqVO.getFinishedQty());
    }

    @Override
    public ErpProductionOrderDO getProductionOrder(Long id) {
        return erpProductionOrderMapper.selectById(id);
    }

    @Override
    public List<ErpProductionOrderDO> getProductionOrderList(Collection<Long> ids) {
        return erpProductionOrderMapper.selectListByIds(ids);
    }

    @Override
    public PageResult<ErpProductionOrderDO> getProductionOrderPage(ErpProductionOrderPageReqVO pageReqVO) {
        return erpProductionOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public ErpProductionOrderSummaryRespVO getSummary() {
        ErpProductionOrderSummaryRespVO summary = new ErpProductionOrderSummaryRespVO()
                .setCreated(erpProductionOrderMapper.selectCount(ErpProductionOrderStatusEnum.CREATED.getStatus()))
                .setReleased(erpProductionOrderMapper.selectCount(ErpProductionOrderStatusEnum.RELEASED.getStatus()))
                .setFinished(erpProductionOrderMapper.selectCount(ErpProductionOrderStatusEnum.FINISHED.getStatus()))
                .setClosed(erpProductionOrderMapper.selectCount(ErpProductionOrderStatusEnum.CLOSED.getStatus()));
        summary.setTotal(summary.getCreated() + summary.getReleased() + summary.getFinished() + summary.getClosed());
        return summary;
    }

    private ErpProductionOrderDO validateProductionOrderExists(Long id) {
        ErpProductionOrderDO order = erpProductionOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        return order;
    }

    /**
     * 校验计划时间顺序：结束时间不能早于开始时间。
     */
    private void validatePlanTime(LocalDateTime planStartTime, LocalDateTime planEndTime) {
        if (planStartTime != null && planEndTime != null && planEndTime.isBefore(planStartTime)) {
            throw exception(PRODUCTION_ORDER_PLAN_TIME_INVALID);
        }
    }

    /**
     * 校验下达时绑定的工艺路线：必须存在、启用且属于工单产品。
     */
    private ErpProcessRouteDO validateRouteForRelease(ErpProductionOrderDO order) {
        ErpProcessRouteDO route = erpProcessRouteMapper.selectById(order.getRouteId());
        if (route == null || !Integer.valueOf(1).equals(route.getStatus())) {
            throw exception(PRODUCTION_ORDER_ROUTE_NOT_EXISTS);
        }
        if (!route.getProductId().equals(order.getProductId())) {
            throw exception(PRODUCTION_ORDER_ROUTE_NOT_EXISTS);
        }
        return route;
    }

    private ErpProductionOrderStepDO buildProductionOrderStep(ErpProductionOrderDO order, ErpProcessRouteStepDO routeStep) {
        return new ErpProductionOrderStepDO()
                .setProductionOrderId(order.getId())
                .setRouteStepId(routeStep.getId())
                .setStepNo(routeStep.getStepNo())
                .setStepCode(routeStep.getStepCode())
                .setStepName(routeStep.getStepName())
                .setWorkCenterId(routeStep.getWorkCenterId())
                .setQcFlag(routeStep.getQcFlag())
                .setPlanQty(order.getPlanQty())
                .setReportedQty(BigDecimal.ZERO)
                .setQualifiedQty(BigDecimal.ZERO)
                .setScrapQty(BigDecimal.ZERO)
                .setStepStatus(0)
                .setRemark(routeStep.getRemark());
    }

    private ErpProductionMaterialDO buildProductionMaterial(ErpProductionOrderDO order, ErpBomItemDO bomItem) {
        return new ErpProductionMaterialDO()
                .setProductionOrderId(order.getId())
                .setBomItemId(bomItem.getId())
                .setMaterialId(bomItem.getMaterialId())
                .setRequiredQty(calculateRequiredQty(order.getPlanQty(), bomItem.getUsageQty(), bomItem.getLossRate()))
                .setIssuedQty(BigDecimal.ZERO)
                .setReturnedQty(BigDecimal.ZERO)
                .setScrapQty(BigDecimal.ZERO)
                .setIssueMode(1)
                .setBackflushFlag(Boolean.FALSE)
                .setRemark(bomItem.getRemark());
    }

    private BigDecimal calculateRequiredQty(BigDecimal planQty, BigDecimal usageQty, BigDecimal lossRate) {
        BigDecimal safeLossRate = lossRate == null ? BigDecimal.ZERO : lossRate;
        return planQty.multiply(usageQty)
                .multiply(BigDecimal.ONE.add(safeLossRate))
                .setScale(6, RoundingMode.HALF_UP);
    }

}
