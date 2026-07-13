package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssemblePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssembleSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockAssembleItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockAssembleMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockAssembleActionTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.mrp.ErpBomService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpStockAssembleServiceImpl implements ErpStockAssembleService {

    private static final int QTY_SCALE = 6;

    @Resource
    private ErpStockAssembleMapper assembleMapper;
    @Resource
    private ErpStockAssembleItemMapper assembleItemMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpBomService bomService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockBatchAllocationService stockBatchAllocationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockAssemble(ErpStockAssembleSaveReqVO reqVO) {
        PreparedAssemble prepared = prepare(reqVO);
        String no = noRedisDAO.generate(ErpNoRedisDAO.STOCK_ASSEMBLE_NO_PREFIX);
        if (assembleMapper.selectByNo(no) != null) {
            throw exception(STOCK_ASSEMBLE_NO_EXISTS);
        }
        ErpStockAssembleDO assemble = new ErpStockAssembleDO()
                .setNo(no)
                .setActionType(reqVO.getActionType())
                .setWarehouseId(reqVO.getWarehouseId())
                .setProductId(reqVO.getProductId())
                .setBomId(prepared.bom().getId())
                .setCount(reqVO.getCount())
                .setTotalCost(prepared.totalCost())
                .setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setRemark(reqVO.getRemark());
        assembleMapper.insert(assemble);
        saveItems(assemble.getId(), prepared.items());
        return assemble.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockAssemble(ErpStockAssembleSaveReqVO reqVO) {
        ErpStockAssembleDO existed = validateExists(reqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(existed.getStatus())) {
            throw exception(STOCK_ASSEMBLE_UPDATE_FAIL_APPROVE, existed.getNo());
        }
        PreparedAssemble prepared = prepare(reqVO);
        assembleMapper.updateById(new ErpStockAssembleDO()
                .setId(existed.getId())
                .setActionType(reqVO.getActionType())
                .setWarehouseId(reqVO.getWarehouseId())
                .setProductId(reqVO.getProductId())
                .setBomId(prepared.bom().getId())
                .setCount(reqVO.getCount())
                .setTotalCost(prepared.totalCost())
                .setRemark(reqVO.getRemark()));
        assembleItemMapper.deleteByAssembleId(existed.getId());
        saveItems(existed.getId(), prepared.items());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockAssembleStatus(Long id, Integer status) {
        if (!ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            throw exception(STOCK_ASSEMBLE_STATUS_UPDATE_ILLEGAL);
        }
        ErpStockAssembleDO assemble = validateExists(id);
        if (!ErpAuditStatus.DRAFT.getStatus().equals(assemble.getStatus())
                && !ErpAuditStatus.REJECT.getStatus().equals(assemble.getStatus())) {
            throw exception(STOCK_ASSEMBLE_APPROVE_FAIL);
        }
        int updated = assembleMapper.updateStatusIfMatch(id, assemble.getStatus(), status);
        if (updated == 0) {
            throw exception(STOCK_ASSEMBLE_APPROVE_FAIL);
        }
        List<ErpStockAssembleItemDO> items = assembleItemMapper.selectListByAssembleId(id);
        if (CollUtil.isEmpty(items)) {
            throw exception(STOCK_ASSEMBLE_BOM_ITEM_INVALID);
        }
        for (ErpStockAssembleItemDO item : items) {
            BigDecimal delta = item.getCount().multiply(BigDecimal.valueOf(item.getStockDirection()));
            Integer bizType = resolveRecordBizType(assemble.getActionType(), item.getStockDirection());
            if (delta.signum() < 0) {
                stockBatchAllocationService.allocateOutbound(new ErpStockBatchAllocateOutboundReqBO()
                        .setProductId(item.getProductId())
                        .setWarehouseId(assemble.getWarehouseId())
                        .setCount(delta.abs())
                        .setBizType(bizType)
                        .setBizId(assemble.getId())
                        .setBizItemId(item.getId())
                        .setBizNo(assemble.getNo())
                        .setRemark(assemble.getRemark()));
            }
            BigDecimal amount = defaultAmount(item.getUnitCost()).multiply(item.getCount()).setScale(2, RoundingMode.HALF_UP);
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    item.getProductId(), assemble.getWarehouseId(), delta, bizType,
                    assemble.getId(), item.getId(), assemble.getNo(),
                    delta.signum() > 0 ? item.getUnitCost() : null, amount));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockAssemble(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<ErpStockAssembleDO> list = assembleMapper.selectByIds(ids);
        for (ErpStockAssembleDO assemble : list) {
            if (ErpAuditStatus.APPROVE.getStatus().equals(assemble.getStatus())) {
                throw exception(STOCK_ASSEMBLE_DELETE_FAIL_APPROVE, assemble.getNo());
            }
        }
        list.forEach(assemble -> {
            assembleItemMapper.deleteByAssembleId(assemble.getId());
            assembleMapper.deleteById(assemble.getId());
        });
    }

    @Override
    public ErpStockAssembleDO getStockAssemble(Long id) {
        return assembleMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockAssembleDO> getStockAssemblePage(ErpStockAssemblePageReqVO reqVO) {
        return assembleMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpStockAssembleItemDO> getStockAssembleItemListByAssembleId(Long assembleId) {
        return assembleItemMapper.selectListByAssembleId(assembleId);
    }

    @Override
    public List<ErpStockAssembleItemDO> getStockAssembleItemListByAssembleIds(Collection<Long> assembleIds) {
        if (CollUtil.isEmpty(assembleIds)) {
            return Collections.emptyList();
        }
        return assembleItemMapper.selectListByAssembleIds(assembleIds);
    }

    private PreparedAssemble prepare(ErpStockAssembleSaveReqVO reqVO) {
        if (!ErpStockAssembleActionTypeEnum.isValid(reqVO.getActionType())) {
            throw exception(STOCK_ASSEMBLE_ACTION_TYPE_INVALID);
        }
        productService.validProductList(List.of(reqVO.getProductId()));
        warehouseService.validWarehouseList(List.of(reqVO.getWarehouseId()));
        ErpBomDO bom = bomService.getEffectiveBom(reqVO.getProductId());
        if (bom == null) {
            throw exception(STOCK_ASSEMBLE_BOM_NOT_EXISTS);
        }
        List<ErpBomItemDO> bomItems = bomService.getBomItemList(bom.getId());
        if (CollUtil.isEmpty(bomItems)) {
            throw exception(STOCK_ASSEMBLE_BOM_ITEM_INVALID);
        }
        List<Long> materialIds = new ArrayList<>(convertSet(bomItems, ErpBomItemDO::getMaterialId));
        productService.validProductList(materialIds);
        Map<Long, ErpProductDO> productMap = productService.validProductList(materialIds).stream()
                .collect(Collectors.toMap(ErpProductDO::getId, Function.identity(), (left, right) -> left));
        List<ErpStockAssembleItemDO> items = new ArrayList<>();
        if (ErpStockAssembleActionTypeEnum.ASSEMBLE.getType().equals(reqVO.getActionType())) {
            BigDecimal totalCost = BigDecimal.ZERO;
            for (ErpBomItemDO bomItem : bomItems) {
                validateBomItem(reqVO.getProductId(), bomItem);
                BigDecimal count = reqVO.getCount().multiply(defaultAmount(bomItem.getUsageQty()))
                        .multiply(BigDecimal.ONE.add(defaultAmount(bomItem.getLossRate())))
                        .setScale(QTY_SCALE, RoundingMode.UP);
                BigDecimal unitCost = resolveUnitCost(bomItem.getMaterialId(), reqVO.getWarehouseId(), productMap);
                totalCost = totalCost.add(count.multiply(unitCost));
                items.add(new ErpStockAssembleItemDO().setProductId(bomItem.getMaterialId())
                        .setCount(count).setUnitCost(unitCost).setStockDirection(-1)
                        .setRemark("BOM " + bom.getBomCode()));
            }
            BigDecimal outputUnitCost = totalCost.divide(reqVO.getCount(), QTY_SCALE, RoundingMode.HALF_UP);
            items.add(new ErpStockAssembleItemDO().setProductId(reqVO.getProductId())
                    .setCount(reqVO.getCount()).setUnitCost(outputUnitCost).setStockDirection(1)
                    .setRemark("组装成品"));
            return new PreparedAssemble(bom, items, totalCost.setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal parentUnitCost = resolveUnitCost(reqVO.getProductId(), reqVO.getWarehouseId(),
                Map.of(reqVO.getProductId(), productService.getProduct(reqVO.getProductId())));
        BigDecimal totalCost = reqVO.getCount().multiply(parentUnitCost);
        items.add(new ErpStockAssembleItemDO().setProductId(reqVO.getProductId())
                .setCount(reqVO.getCount()).setUnitCost(parentUnitCost).setStockDirection(-1)
                .setRemark("拆卸成品"));
        BigDecimal totalComponentQty = bomItems.stream().map(item -> defaultAmount(item.getUsageQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalComponentQty.signum() <= 0) {
            throw exception(STOCK_ASSEMBLE_BOM_ITEM_INVALID);
        }
        for (ErpBomItemDO bomItem : bomItems) {
            validateBomItem(reqVO.getProductId(), bomItem);
            BigDecimal count = reqVO.getCount().multiply(defaultAmount(bomItem.getUsageQty()))
                    .setScale(QTY_SCALE, RoundingMode.UP);
            BigDecimal unitCost = totalCost.divide(totalComponentQty, QTY_SCALE, RoundingMode.HALF_UP);
            items.add(new ErpStockAssembleItemDO().setProductId(bomItem.getMaterialId())
                    .setCount(count).setUnitCost(unitCost).setStockDirection(1)
                    .setRemark("拆卸组件"));
        }
        return new PreparedAssemble(bom, items, totalCost.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal resolveUnitCost(Long productId, Long warehouseId, Map<Long, ErpProductDO> productMap) {
        ErpStockDO stock = stockService.getStock(productId, warehouseId);
        if (stock != null && stock.getAverageCost() != null) {
            return stock.getAverageCost().setScale(QTY_SCALE, RoundingMode.HALF_UP);
        }
        ErpProductDO product = productMap.get(productId);
        return product == null || product.getPurchasePrice() == null
                ? BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.HALF_UP)
                : product.getPurchasePrice().setScale(QTY_SCALE, RoundingMode.HALF_UP);
    }

    private void validateBomItem(Long productId, ErpBomItemDO item) {
        if (ObjectUtil.equal(productId, item.getMaterialId())
                || item.getUsageQty() == null || item.getUsageQty().signum() <= 0) {
            throw exception(STOCK_ASSEMBLE_BOM_ITEM_INVALID);
        }
    }

    private Integer resolveRecordBizType(String actionType, Integer direction) {
        boolean assemble = ErpStockAssembleActionTypeEnum.ASSEMBLE.getType().equals(actionType);
        if (direction < 0) {
            return assemble ? ErpStockRecordBizTypeEnum.ASSEMBLE_OUT.getType()
                    : ErpStockRecordBizTypeEnum.DISASSEMBLE_OUT.getType();
        }
        return assemble ? ErpStockRecordBizTypeEnum.ASSEMBLE_IN.getType()
                : ErpStockRecordBizTypeEnum.DISASSEMBLE_IN.getType();
    }

    private void saveItems(Long assembleId, List<ErpStockAssembleItemDO> items) {
        items.forEach(item -> item.setId(null).setAssembleId(assembleId));
        assembleItemMapper.insertBatch(items);
    }

    private ErpStockAssembleDO validateExists(Long id) {
        ErpStockAssembleDO assemble = assembleMapper.selectById(id);
        if (assemble == null) {
            throw exception(STOCK_ASSEMBLE_NOT_EXISTS);
        }
        return assemble;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record PreparedAssemble(ErpBomDO bom, List<ErpStockAssembleItemDO> items, BigDecimal totalCost) {
    }
}
