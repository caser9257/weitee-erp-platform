package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockOutItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_REBUILD_CONFIRM_REQUIRED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_REBUILD_HAS_FAILED_DETAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_REBUILD_UNSUPPORTED_BIZ_TYPE;

@Service
@Validated
public class ErpStockBatchRebuildServiceImpl implements ErpStockBatchRebuildService {

    private static final String RESULT_READY = "READY";
    private static final String RESULT_SKIPPED = "SKIPPED";
    private static final String RESULT_FAILED = "FAILED";
    private static final String RESULT_REBUILT = "REBUILT";

    @Resource
    private ErpSaleOutMapper erpSaleOutMapper;
    @Resource
    private ErpSaleOutItemMapper erpSaleOutItemMapper;
    @Resource
    private ErpStockOutMapper erpStockOutMapper;
    @Resource
    private ErpStockOutItemMapper erpStockOutItemMapper;
    @Resource
    private ErpStockBatchAllocationMapper erpStockBatchAllocationMapper;
    @Resource
    private ErpStockBatchAllocationService stockBatchAllocationService;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpProductService productService;

    @Override
    public ErpStockBatchRebuildOutboundRespVO previewOutbound(ErpStockBatchRebuildOutboundReqVO reqVO) {
        validateBizType(reqVO.getBizType());
        return buildPreview(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchRebuildOutboundRespVO rebuildOutbound(ErpStockBatchRebuildOutboundReqVO reqVO) {
        validateBizType(reqVO.getBizType());
        if (!Boolean.TRUE.equals(reqVO.getConfirm())) {
            throw exception(STOCK_BATCH_REBUILD_CONFIRM_REQUIRED);
        }
        ErpStockBatchRebuildOutboundRespVO respVO = buildPreview(reqVO);
        if (respVO.getFailedCount() > 0) {
            throw exception(STOCK_BATCH_REBUILD_HAS_FAILED_DETAIL);
        }
        for (ErpStockBatchRebuildOutboundRespVO.Detail detail : respVO.getDetails()) {
            if (!RESULT_READY.equals(detail.getResult())) {
                continue;
            }
            stockBatchAllocationService.allocateOutbound(new ErpStockBatchAllocateOutboundReqBO()
                    .setProductId(detail.getProductId())
                    .setWarehouseId(detail.getWarehouseId())
                    .setCount(detail.getRequiredQty())
                    .setBizType(detail.getBizType())
                    .setBizId(detail.getBizId())
                    .setBizItemId(detail.getBizItemId())
                    .setBizNo(detail.getBizNo())
                    .setRemark("历史出库批次重建"));
            detail.setResult(RESULT_REBUILT);
            detail.setReason("已重建");
            respVO.setRebuiltCount(respVO.getRebuiltCount() + 1);
        }
        respVO.setReadyCount(0);
        return respVO;
    }

    private ErpStockBatchRebuildOutboundRespVO buildPreview(ErpStockBatchRebuildOutboundReqVO reqVO) {
        List<OutboundItem> outboundItems = loadOutboundItems(reqVO);
        Map<String, BigDecimal> plannedQtyMap = new HashMap<>();
        ErpStockBatchRebuildOutboundRespVO respVO = new ErpStockBatchRebuildOutboundRespVO();
        for (OutboundItem outboundItem : outboundItems) {
            respVO.setScannedCount(respVO.getScannedCount() + 1);
            ErpStockBatchRebuildOutboundRespVO.Detail detail = buildDetail(outboundItem);
            fillPreviewResult(detail, plannedQtyMap);
            countResult(respVO, detail);
            respVO.getDetails().add(detail);
        }
        return respVO;
    }

    private List<OutboundItem> loadOutboundItems(ErpStockBatchRebuildOutboundReqVO reqVO) {
        Integer limit = ObjectUtil.defaultIfNull(reqVO.getLimit(), 100);
        List<OutboundItem> result = new ArrayList<>();
        if (ObjectUtil.equal(reqVO.getBizType(), ErpStockRecordBizTypeEnum.SALE_OUT.getType())) {
            List<ErpSaleOutDO> saleOuts = erpSaleOutMapper.selectApprovedListForBatchRebuild(reqVO.getBizId(), limit);
            for (ErpSaleOutDO saleOut : saleOuts) {
                for (ErpSaleOutItemDO item : erpSaleOutItemMapper.selectListByOutId(saleOut.getId())) {
                    result.add(new OutboundItem(reqVO.getBizType(), saleOut.getId(), item.getId(), saleOut.getNo(),
                            item.getProductId(), item.getWarehouseId(), item.getCount()));
                }
            }
            return result;
        }
        List<ErpStockOutDO> stockOuts = erpStockOutMapper.selectApprovedListForBatchRebuild(reqVO.getBizId(), limit);
        for (ErpStockOutDO stockOut : stockOuts) {
            for (ErpStockOutItemDO item : erpStockOutItemMapper.selectListByOutId(stockOut.getId())) {
                result.add(new OutboundItem(reqVO.getBizType(), stockOut.getId(), item.getId(), stockOut.getNo(),
                        item.getProductId(), item.getWarehouseId(), item.getCount()));
            }
        }
        return result;
    }

    private void fillPreviewResult(ErpStockBatchRebuildOutboundRespVO.Detail detail, Map<String, BigDecimal> plannedQtyMap) {
        List<ErpStockBatchAllocationDO> allocations = erpStockBatchAllocationMapper.selectListByBizItem(
                detail.getBizType(), detail.getBizId(), detail.getBizItemId());
        if (CollUtil.isNotEmpty(allocations)) {
            detail.setResult(RESULT_SKIPPED);
            detail.setReason("已有批次分配");
            return;
        }
        ErpProductDO product = productService.getProduct(detail.getProductId());
        if (product == null || !Boolean.TRUE.equals(product.getBatchControlFlag())) {
            detail.setResult(RESULT_SKIPPED);
            detail.setReason("非批次管理产品");
            return;
        }
        BigDecimal availableQty = getAvailableQty(detail);
        String key = detail.getProductId() + ":" + detail.getWarehouseId();
        BigDecimal plannedQty = ObjectUtil.defaultIfNull(plannedQtyMap.get(key), BigDecimal.ZERO);
        BigDecimal remainingAvailableQty = availableQty.subtract(plannedQty);
        detail.setAvailableQty(remainingAvailableQty);
        if (remainingAvailableQty.compareTo(detail.getRequiredQty()) < 0) {
            detail.setResult(RESULT_FAILED);
            detail.setReason("批次可用量不足");
            return;
        }
        plannedQtyMap.put(key, plannedQty.add(detail.getRequiredQty()));
        detail.setResult(RESULT_READY);
        detail.setReason("可重建");
    }

    private BigDecimal getAvailableQty(ErpStockBatchRebuildOutboundRespVO.Detail detail) {
        return stockBatchService.getAvailableStockBatchList(detail.getProductId(), detail.getWarehouseId()).stream()
                .map(batch -> ObjectUtil.defaultIfNull(batch.getAvailableQty(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void countResult(ErpStockBatchRebuildOutboundRespVO respVO, ErpStockBatchRebuildOutboundRespVO.Detail detail) {
        if (RESULT_READY.equals(detail.getResult())) {
            respVO.setCandidateCount(respVO.getCandidateCount() + 1);
            respVO.setReadyCount(respVO.getReadyCount() + 1);
        } else if (RESULT_FAILED.equals(detail.getResult())) {
            respVO.setCandidateCount(respVO.getCandidateCount() + 1);
            respVO.setFailedCount(respVO.getFailedCount() + 1);
        } else {
            respVO.setSkippedCount(respVO.getSkippedCount() + 1);
        }
    }

    private ErpStockBatchRebuildOutboundRespVO.Detail buildDetail(OutboundItem item) {
        ErpStockBatchRebuildOutboundRespVO.Detail detail = new ErpStockBatchRebuildOutboundRespVO.Detail();
        detail.setBizType(item.bizType);
        detail.setBizId(item.bizId);
        detail.setBizItemId(item.bizItemId);
        detail.setBizNo(item.bizNo);
        detail.setProductId(item.productId);
        detail.setWarehouseId(item.warehouseId);
        detail.setRequiredQty(item.count);
        return detail;
    }

    private void validateBizType(Integer bizType) {
        if (ObjectUtil.equal(bizType, ErpStockRecordBizTypeEnum.SALE_OUT.getType())
                || ObjectUtil.equal(bizType, ErpStockRecordBizTypeEnum.OTHER_OUT.getType())) {
            return;
        }
        throw exception(STOCK_BATCH_REBUILD_UNSUPPORTED_BIZ_TYPE);
    }

    private static class OutboundItem {

        private final Integer bizType;
        private final Long bizId;
        private final Long bizItemId;
        private final String bizNo;
        private final Long productId;
        private final Long warehouseId;
        private final BigDecimal count;

        private OutboundItem(Integer bizType, Long bizId, Long bizItemId, String bizNo,
                             Long productId, Long warehouseId, BigDecimal count) {
            this.bizType = bizType;
            this.bizId = bizId;
            this.bizItemId = bizItemId;
            this.bizNo = bizNo;
            this.productId = productId;
            this.warehouseId = warehouseId;
            this.count = count;
        }

    }

}
