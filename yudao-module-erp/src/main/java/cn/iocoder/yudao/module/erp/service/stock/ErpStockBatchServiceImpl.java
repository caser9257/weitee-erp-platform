package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchAdjustmentMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchMapper;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockBatchAdjustTypeEnum;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_ADJUSTMENT_NO_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_INSUFFICIENT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_LOCKED_INSUFFICIENT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_LOCK_INSUFFICIENT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_NOT_EXISTS;

@Service
@Validated
public class ErpStockBatchServiceImpl implements ErpStockBatchService {

    @Resource
    private ErpStockBatchMapper erpStockBatchMapper;
    @Resource
    private ErpStockBatchAdjustmentMapper erpStockBatchAdjustmentMapper;
    @Resource
    private ErpStockBatchRecordService stockBatchRecordService;

    @Override
    public ErpStockBatchDO getStockBatch(Long id) {
        return erpStockBatchMapper.selectById(id);
    }

    @Override
    public ErpStockBatchDO validateStockBatch(Long id) {
        ErpStockBatchDO stockBatch = erpStockBatchMapper.selectById(id);
        if (stockBatch == null) {
            throw exception(STOCK_BATCH_NOT_EXISTS);
        }
        return stockBatch;
    }

    @Override
    public List<ErpStockBatchDO> getAvailableStockBatchList(Long productId, Long warehouseId) {
        return erpStockBatchMapper.selectAvailableList(productId, warehouseId);
    }

    @Override
    public PageResult<ErpStockBatchDO> getStockBatchPage(ErpStockBatchPageReqVO reqVO) {
        return erpStockBatchMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpStockBatchDO> getStockBatchList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return erpStockBatchMapper.selectListByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO createOrIncreaseBatch(ErpStockBatchInboundReqBO reqBO) {
        ErpStockBatchDO stockBatch = erpStockBatchMapper.selectByProductWarehouseAndBatchNo(
                reqBO.getProductId(), reqBO.getWarehouseId(), reqBO.getBatchNo());
        if (stockBatch == null) {
            stockBatch = new ErpStockBatchDO()
                    .setProductId(reqBO.getProductId())
                    .setWarehouseId(reqBO.getWarehouseId())
                    .setBatchNo(reqBO.getBatchNo())
                    .setInboundTime(reqBO.getInboundTime())
                    .setProduceDate(reqBO.getProduceDate())
                    .setExpireDate(reqBO.getExpireDate())
                    .setTotalQty(reqBO.getCount())
                    .setAvailableQty(reqBO.getCount())
                    .setLockedQty(BigDecimal.ZERO)
                    .setVirtualFlag(Boolean.TRUE.equals(reqBO.getVirtualFlag()))
                    .setSourceBizType(reqBO.getSourceBizType())
                    .setSourceBizId(reqBO.getBizId())
                    .setSourceBizItemId(reqBO.getBizItemId())
                    .setPurchaseSourceBatchId(reqBO.getPurchaseSourceBatchId())
                    .setSourceBizNo(reqBO.getBizNo())
                    .setPurchaseSourceBatchNo(reqBO.getPurchaseSourceBatchNo())
                    .setRemark(reqBO.getRemark());
            erpStockBatchMapper.insert(stockBatch);
        } else {
            LocalDateTime inboundTime = stockBatch.getInboundTime();
            if (inboundTime == null || reqBO.getInboundTime().isBefore(inboundTime)) {
                stockBatch.setInboundTime(reqBO.getInboundTime());
            }
            if (stockBatch.getProduceDate() == null) {
                stockBatch.setProduceDate(reqBO.getProduceDate());
            }
            if (stockBatch.getExpireDate() == null) {
                stockBatch.setExpireDate(reqBO.getExpireDate());
            }
            if (StrUtil.isBlank(stockBatch.getRemark())) {
                stockBatch.setRemark(reqBO.getRemark());
            }
            erpStockBatchMapper.updateById(stockBatch);
            erpStockBatchMapper.updateQtyIncrement(stockBatch.getId(), reqBO.getCount());
            stockBatch = erpStockBatchMapper.selectById(stockBatch.getId());
        }
        createRecord(stockBatch, reqBO.getCount(), reqBO.getBizType(), reqBO.getBizId(),
                reqBO.getBizItemId(), reqBO.getBizNo(), reqBO.getRemark());
        return stockBatch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO adjustBatch(ErpStockBatchAdjustReqVO reqVO) {
        Integer bizType = getManualAdjustBizType(reqVO.getAdjustType());
        if (erpStockBatchAdjustmentMapper.selectByAdjustNo(reqVO.getAdjustNo()) != null) {
            throw exception(STOCK_BATCH_ADJUSTMENT_NO_EXISTS, reqVO.getAdjustNo());
        }
        ErpStockBatchDO beforeBatch = validateStockBatch(reqVO.getStockBatchId());
        ErpStockBatchAdjustmentDO adjustment = buildAdjustment(reqVO, beforeBatch);
        erpStockBatchAdjustmentMapper.insert(adjustment);

        ErpStockBatchChangeReqBO changeReqBO = buildManualAdjustChangeReq(reqVO, bizType, adjustment.getId());
        ErpStockBatchDO afterBatch = ObjectUtil.equal(reqVO.getAdjustType(), ErpStockBatchAdjustTypeEnum.INCREASE.getType())
                ? increaseBatch(changeReqBO) : decreaseBatch(changeReqBO);
        adjustment.setAfterTotalQty(afterBatch.getTotalQty())
                .setAfterAvailableQty(afterBatch.getAvailableQty());
        erpStockBatchAdjustmentMapper.updateById(adjustment);
        return afterBatch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO increaseBatch(ErpStockBatchChangeReqBO reqBO) {
        ErpStockBatchDO stockBatch = validateStockBatch(reqBO.getStockBatchId());
        erpStockBatchMapper.updateQtyIncrement(stockBatch.getId(), reqBO.getCount());
        stockBatch = erpStockBatchMapper.selectById(stockBatch.getId());
        createRecord(stockBatch, reqBO.getCount(), reqBO.getBizType(), reqBO.getBizId(),
                reqBO.getBizItemId(), reqBO.getBizNo(), reqBO.getRemark());
        return stockBatch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO decreaseBatch(ErpStockBatchChangeReqBO reqBO) {
        ErpStockBatchDO stockBatch = validateStockBatch(reqBO.getStockBatchId());
        if (erpStockBatchMapper.updateQtyDecrement(stockBatch.getId(), reqBO.getCount()) == 0) {
            throw exception(STOCK_BATCH_INSUFFICIENT, stockBatch.getBatchNo(), stockBatch.getAvailableQty());
        }
        stockBatch = erpStockBatchMapper.selectById(stockBatch.getId());
        createRecord(stockBatch, reqBO.getCount().negate(), reqBO.getBizType(), reqBO.getBizId(),
                reqBO.getBizItemId(), reqBO.getBizNo(), reqBO.getRemark());
        return stockBatch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO lockBatch(ErpStockBatchChangeReqBO reqBO) {
        ErpStockBatchDO stockBatch = validateStockBatch(reqBO.getStockBatchId());
        if (erpStockBatchMapper.updateLockIncrement(stockBatch.getId(), reqBO.getCount()) == 0) {
            throw exception(STOCK_BATCH_LOCK_INSUFFICIENT, stockBatch.getBatchNo(), stockBatch.getAvailableQty());
        }
        return erpStockBatchMapper.selectById(stockBatch.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO releaseLockedBatch(ErpStockBatchChangeReqBO reqBO) {
        ErpStockBatchDO stockBatch = validateStockBatch(reqBO.getStockBatchId());
        if (erpStockBatchMapper.updateLockRelease(stockBatch.getId(), reqBO.getCount()) == 0) {
            throw exception(STOCK_BATCH_LOCKED_INSUFFICIENT, stockBatch.getBatchNo(), stockBatch.getLockedQty());
        }
        return erpStockBatchMapper.selectById(stockBatch.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchDO deductLockedBatch(ErpStockBatchChangeReqBO reqBO) {
        ErpStockBatchDO stockBatch = validateStockBatch(reqBO.getStockBatchId());
        if (erpStockBatchMapper.updateLockedDeduct(stockBatch.getId(), reqBO.getCount()) == 0) {
            throw exception(STOCK_BATCH_LOCKED_INSUFFICIENT, stockBatch.getBatchNo(), stockBatch.getLockedQty());
        }
        stockBatch = erpStockBatchMapper.selectById(stockBatch.getId());
        createRecord(stockBatch, reqBO.getCount().negate(), reqBO.getBizType(), reqBO.getBizId(),
                reqBO.getBizItemId(), reqBO.getBizNo(), reqBO.getRemark());
        return stockBatch;
    }

    private ErpStockBatchAdjustmentDO buildAdjustment(ErpStockBatchAdjustReqVO reqVO, ErpStockBatchDO beforeBatch) {
        return new ErpStockBatchAdjustmentDO()
                .setAdjustNo(reqVO.getAdjustNo())
                .setStockBatchId(beforeBatch.getId())
                .setProductId(beforeBatch.getProductId())
                .setWarehouseId(beforeBatch.getWarehouseId())
                .setBatchNo(beforeBatch.getBatchNo())
                .setAdjustType(reqVO.getAdjustType())
                .setAdjustQty(reqVO.getCount())
                .setBeforeTotalQty(beforeBatch.getTotalQty())
                .setBeforeAvailableQty(beforeBatch.getAvailableQty())
                .setRemark(reqVO.getRemark());
    }

    private Integer getManualAdjustBizType(Integer adjustType) {
        if (ObjectUtil.equal(adjustType, ErpStockBatchAdjustTypeEnum.INCREASE.getType())) {
            return ErpStockRecordBizTypeEnum.BATCH_ADJUST_IN.getType();
        }
        if (ObjectUtil.equal(adjustType, ErpStockBatchAdjustTypeEnum.DECREASE.getType())) {
            return ErpStockRecordBizTypeEnum.BATCH_ADJUST_OUT.getType();
        }
        throw new IllegalArgumentException("Unsupported stock batch adjust type: " + adjustType);
    }

    private ErpStockBatchChangeReqBO buildManualAdjustChangeReq(ErpStockBatchAdjustReqVO reqVO, Integer bizType,
                                                                Long adjustmentId) {
        ErpStockBatchChangeReqBO changeReqBO = new ErpStockBatchChangeReqBO();
        changeReqBO.setStockBatchId(reqVO.getStockBatchId());
        changeReqBO.setCount(reqVO.getCount());
        changeReqBO.setBizType(bizType);
        changeReqBO.setBizId(adjustmentId);
        changeReqBO.setBizNo(reqVO.getAdjustNo());
        changeReqBO.setRemark(reqVO.getRemark());
        return changeReqBO;
    }

    private void createRecord(ErpStockBatchDO stockBatch, BigDecimal count, Integer bizType, Long bizId,
                              Long bizItemId, String bizNo, String remark) {
        stockBatchRecordService.createStockBatchRecord(new ErpStockBatchRecordDO()
                .setProductId(stockBatch.getProductId())
                .setWarehouseId(stockBatch.getWarehouseId())
                .setStockBatchId(stockBatch.getId())
                .setBatchNo(stockBatch.getBatchNo())
                .setCount(count)
                .setAfterAvailableQty(stockBatch.getAvailableQty())
                .setBizType(bizType)
                .setBizId(bizId)
                .setBizItemId(bizItemId)
                .setBizNo(bizNo)
                .setRemark(remark));
    }

}
