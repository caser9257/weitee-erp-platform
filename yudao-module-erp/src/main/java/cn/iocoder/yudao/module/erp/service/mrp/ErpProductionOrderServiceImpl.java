package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderFinishReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionOrderStatusEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.BOM_ITEM_EMPTY;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_EFFECTIVE_BOM_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STATUS_INVALID;

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
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductionFinishQualityService productionFinishQualityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionOrder(ErpProductionOrderSaveReqVO createReqVO) {
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
        if (ErpProductionOrderStatusEnum.FINISHED.getStatus().equals(order.getStatus())
                || ErpProductionOrderStatusEnum.CLOSED.getStatus().equals(order.getStatus())) {
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
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
        erpProductionMaterialMapper.insertBatch(bomItems.stream()
                .map(item -> buildProductionMaterial(order, item))
                .collect(Collectors.toList()));
        erpProductionOrderMapper.updateById(new ErpProductionOrderDO().setId(id)
                .setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishProductionOrder(ErpProductionOrderFinishReqVO reqVO) {
        ErpProductionOrderDO order = validateProductionOrderExists(reqVO.getId());
        if (!(ErpProductionOrderStatusEnum.CREATED.getStatus().equals(order.getStatus())
                || ErpProductionOrderStatusEnum.RELEASED.getStatus().equals(order.getStatus()))) {
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
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

    private ErpProductionOrderDO validateProductionOrderExists(Long id) {
        ErpProductionOrderDO order = erpProductionOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        return order;
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
