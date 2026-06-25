package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnableBatchesRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReturnBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReturnItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReturnMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_MATERIAL_ORDER_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_MATERIAL_QTY_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_RETURN_BATCH_COUNT_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_RETURN_BATCH_INVALID;

@Service
@Validated
public class ErpProductionReturnServiceImpl implements ErpProductionReturnService {

    private static final Integer DONE_STATUS = 20;

    @Resource
    private ErpProductionMaterialService productionMaterialService;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductionMaterialMapper erpProductionMaterialMapper;
    @Resource
    private ErpProductionIssueItemMapper erpProductionIssueItemMapper;
    @Resource
    private ErpProductionIssueBatchMapper erpProductionIssueBatchMapper;
    @Resource
    private ErpProductionReturnMapper erpProductionReturnMapper;
    @Resource
    private ErpProductionReturnItemMapper erpProductionReturnItemMapper;
    @Resource
    private ErpProductionReturnBatchMapper erpProductionReturnBatchMapper;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    public ErpProductionReturnableBatchesRespVO getReturnableBatches(Long productionMaterialId) {
        ErpProductionMaterialDO material = productionMaterialService.validateProductionMaterial(productionMaterialId);
        List<ErpProductionIssueItemDO> issueItems = erpProductionIssueItemMapper
                .selectListByProductionMaterialIds(List.of(productionMaterialId));
        Map<Long, ErpProductionIssueItemDO> issueItemMap = convertMap(issueItems, ErpProductionIssueItemDO::getId);
        List<ErpProductionIssueBatchDO> issueBatches = erpProductionIssueBatchMapper.selectListByIssueItemIds(issueItemMap.keySet());
        Map<Long, BigDecimal> returnedQtyMap = CollectionUtils.convertMap(
                erpProductionReturnBatchMapper.selectListByIssueBatchIds(
                        issueBatches.stream().map(ErpProductionIssueBatchDO::getId).toList()),
                ErpProductionReturnBatchDO::getIssueBatchId,
                item -> ObjectUtil.defaultIfNull(item.getReturnQty(), BigDecimal.ZERO),
                BigDecimal::add);
        ErpProductionReturnableBatchesRespVO respVO = new ErpProductionReturnableBatchesRespVO();
        respVO.setProductionMaterialId(material.getId());
        respVO.setMaterialId(material.getMaterialId());
        respVO.setWarehouseId(CollUtil.isEmpty(issueItems) ? null : issueItems.get(0).getWarehouseId());
        respVO.setReturnableBatches(issueBatches.stream().map(issueBatch -> {
            BigDecimal returnedQty = ObjectUtil.defaultIfNull(returnedQtyMap.get(issueBatch.getId()), BigDecimal.ZERO);
            BigDecimal returnableQty = issueBatch.getIssueQty().subtract(returnedQty);
            if (returnableQty.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            ErpProductionIssueItemDO issueItem = issueItemMap.get(issueBatch.getIssueItemId());
            ErpProductionReturnableBatchesRespVO.ReturnableBatch batch = new ErpProductionReturnableBatchesRespVO.ReturnableBatch();
            batch.setIssueBatchId(issueBatch.getId());
            batch.setStockBatchId(issueBatch.getStockBatchId());
            batch.setBatchNo(issueBatch.getBatchNo());
            batch.setWarehouseId(issueItem != null ? issueItem.getWarehouseId() : null);
            batch.setIssuedQty(issueBatch.getIssueQty());
            batch.setReturnedQty(returnedQty);
            batch.setReturnableQty(returnableQty);
            return batch;
        }).filter(java.util.Objects::nonNull).toList());
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionReturn(ErpProductionReturnCreateReqVO reqVO) {
        ErpProductionOrderDO order = productionOrderService.getProductionOrder(reqVO.getProductionOrderId());
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        Map<Long, ErpProductionMaterialDO> materialMap = convertMap(
                erpProductionMaterialMapper.selectListByIds(reqVO.getItems().stream()
                        .map(ErpProductionReturnCreateReqVO.Item::getProductionMaterialId).toList()),
                ErpProductionMaterialDO::getId);
        List<Long> issueBatchIds = reqVO.getItems().stream().flatMap(item -> item.getBatches().stream())
                .map(ErpProductionReturnCreateReqVO.Batch::getIssueBatchId).distinct().toList();
        Map<Long, ErpProductionIssueBatchDO> issueBatchMap = convertMap(
                erpProductionIssueBatchMapper.selectListByIssueItemIds(
                        erpProductionIssueItemMapper.selectListByProductionMaterialIds(materialMap.keySet()).stream()
                                .map(ErpProductionIssueItemDO::getId).toList()),
                ErpProductionIssueBatchDO::getId);
        Map<Long, ErpProductionIssueItemDO> issueItemMap = convertMap(
                erpProductionIssueItemMapper.selectListByProductionMaterialIds(materialMap.keySet()),
                ErpProductionIssueItemDO::getId);
        Map<Long, BigDecimal> existingReturnedQtyMap = CollectionUtils.convertMap(
                erpProductionReturnBatchMapper.selectListByIssueBatchIds(issueBatchIds),
                ErpProductionReturnBatchDO::getIssueBatchId,
                item -> ObjectUtil.defaultIfNull(item.getReturnQty(), BigDecimal.ZERO),
                BigDecimal::add);
        Map<Long, BigDecimal> requestReturnedQtyMap = new HashMap<>();

        ErpProductionReturnDO productionReturn = new ErpProductionReturnDO()
                .setReturnNo(noRedisDAO.generate("SCTL"))
                .setProductionOrderId(reqVO.getProductionOrderId())
                .setReturnTime(LocalDateTime.now())
                .setStatus(DONE_STATUS)
                .setRemark(reqVO.getRemark());
        erpProductionReturnMapper.insert(productionReturn);

        for (ErpProductionReturnCreateReqVO.Item item : reqVO.getItems()) {
            ErpProductionMaterialDO material = materialMap.get(item.getProductionMaterialId());
            validateReturnItem(order, material, item);
            ErpProductionReturnItemDO returnItem = new ErpProductionReturnItemDO()
                    .setReturnId(productionReturn.getId())
                    .setProductionMaterialId(item.getProductionMaterialId())
                    .setMaterialId(item.getMaterialId())
                    .setWarehouseId(item.getWarehouseId())
                    .setReturnQty(item.getReturnQty())
                    .setRemark(item.getRemark());
            erpProductionReturnItemMapper.insert(returnItem);

            for (ErpProductionReturnCreateReqVO.Batch batch : item.getBatches()) {
                ErpProductionIssueBatchDO issueBatch = issueBatchMap.get(batch.getIssueBatchId());
                ErpProductionIssueItemDO issueItem = issueBatch == null ? null : issueItemMap.get(issueBatch.getIssueItemId());
                if (issueBatch == null || issueItem == null
                        || ObjectUtil.notEqual(issueItem.getProductionMaterialId(), item.getProductionMaterialId())
                        || ObjectUtil.notEqual(issueItem.getMaterialId(), item.getMaterialId())
                        || ObjectUtil.notEqual(issueItem.getWarehouseId(), item.getWarehouseId())
                        || ObjectUtil.notEqual(issueBatch.getStockBatchId(), batch.getStockBatchId())
                        || ObjectUtil.notEqual(issueBatch.getBatchNo(), batch.getBatchNo())) {
                    throw exception(PRODUCTION_RETURN_BATCH_INVALID);
                }
                BigDecimal returnedQty = ObjectUtil.defaultIfNull(existingReturnedQtyMap.get(batch.getIssueBatchId()), BigDecimal.ZERO)
                        .add(ObjectUtil.defaultIfNull(requestReturnedQtyMap.get(batch.getIssueBatchId()), BigDecimal.ZERO));
                BigDecimal returnableQty = issueBatch.getIssueQty().subtract(returnedQty);
                if (batch.getReturnQty().compareTo(returnableQty) > 0) {
                    throw exception(PRODUCTION_RETURN_BATCH_INVALID);
                }
                requestReturnedQtyMap.merge(batch.getIssueBatchId(), batch.getReturnQty(), BigDecimal::add);
                stockBatchService.increaseBatch(new ErpStockBatchChangeReqBO(
                        batch.getStockBatchId(), batch.getReturnQty(), ErpStockRecordBizTypeEnum.PRODUCTION_RETURN.getType(),
                        productionReturn.getId(), returnItem.getId(), productionReturn.getReturnNo(), item.getRemark()));
                // 获取加权平均成本作为退料价格
                ErpStockDO stock = stockService.getStock(item.getMaterialId(), item.getWarehouseId());
                BigDecimal price = stock != null ? stock.getAverageCost() : null;
                BigDecimal amount = price != null ? price.multiply(batch.getReturnQty()) : null;
                stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                        item.getMaterialId(), item.getWarehouseId(), batch.getReturnQty(),
                        ErpStockRecordBizTypeEnum.PRODUCTION_RETURN.getType(), productionReturn.getId(), returnItem.getId(), productionReturn.getReturnNo(),
                        price, amount));
                erpProductionReturnBatchMapper.insert(new ErpProductionReturnBatchDO()
                        .setReturnItemId(returnItem.getId())
                        .setIssueBatchId(batch.getIssueBatchId())
                        .setStockBatchId(batch.getStockBatchId())
                        .setBatchNo(batch.getBatchNo())
                        .setReturnQty(batch.getReturnQty()));
            }
            erpProductionMaterialMapper.updateReturnedQtyIncrement(material.getId(), item.getReturnQty());
        }
        return productionReturn.getId();
    }

    private void validateReturnItem(ErpProductionOrderDO order, ErpProductionMaterialDO material,
                                    ErpProductionReturnCreateReqVO.Item item) {
        if (material == null || ObjectUtil.notEqual(material.getProductionOrderId(), order.getId())
                || ObjectUtil.notEqual(material.getMaterialId(), item.getMaterialId())) {
            throw exception(PRODUCTION_MATERIAL_ORDER_MISMATCH);
        }
        if (item.getReturnQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_MATERIAL_QTY_INVALID);
        }
        BigDecimal batchTotalQty = item.getBatches().stream()
                .map(ErpProductionReturnCreateReqVO.Batch::getReturnQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (batchTotalQty.compareTo(item.getReturnQty()) != 0) {
            throw exception(PRODUCTION_RETURN_BATCH_COUNT_MISMATCH);
        }
    }

}
