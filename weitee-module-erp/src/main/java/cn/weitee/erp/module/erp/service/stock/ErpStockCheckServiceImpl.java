package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.check.ErpStockCheckPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.check.ErpStockCheckSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckSnapshotDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockCheckStatusEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 库存盘点单 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpStockCheckServiceImpl implements ErpStockCheckService {

    @Resource
    private ErpStockCheckMapper erpStockCheckMapper;
    @Resource
    private ErpStockCheckItemMapper erpStockCheckItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductionCostService productionCostService;
    @Resource
    private ErpStockCheckSnapshotService stockCheckSnapshotService;
    @Resource
    private ErpFinanceVoucherService voucherService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockCheck(ErpStockCheckSaveReqVO createReqVO) {
        // 1.1 校验盘点项的有效性
        List<ErpStockCheckItemDO> stockCheckItems = validateStockCheckItems(createReqVO.getItems());
        // 1.2 生成盘点单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.STOCK_CHECK_NO_PREFIX);
        if (erpStockCheckMapper.selectByNo(no) != null) {
            throw exception(STOCK_CHECK_NO_EXISTS);
        }

        // 2.1 插入盘点单（使用DRAFT状态）
        ErpStockCheckDO stockCheck = BeanUtils.toBean(createReqVO, ErpStockCheckDO.class, in -> in
                .setNo(no).setStatus(ErpStockCheckStatusEnum.DRAFT.getStatus())
                .setTotalCount(getSumValue(stockCheckItems, ErpStockCheckItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockCheckItems, ErpStockCheckItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO)));
        erpStockCheckMapper.insert(stockCheck);

        // 2.2 插入盘点单项
        stockCheckItems.forEach(o -> o.setCheckId(stockCheck.getId()));
        erpStockCheckItemMapper.insertBatch(stockCheckItems);
        return stockCheck.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockCheck(ErpStockCheckSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpStockCheckDO stockCheck = validateStockCheckExists(updateReqVO.getId());
        if (ErpStockCheckStatusEnum.APPROVED.getStatus().equals(stockCheck.getStatus())
                || ErpStockCheckStatusEnum.CLOSED.getStatus().equals(stockCheck.getStatus())) {
            throw exception(STOCK_CHECK_UPDATE_FAIL_APPROVE, stockCheck.getNo());
        }
        // 1.2 校验盘点项的有效性
        List<ErpStockCheckItemDO> stockCheckItems = validateStockCheckItems(updateReqVO.getItems());

        // 2.1 更新盘点单
        ErpStockCheckDO updateObj = BeanUtils.toBean(updateReqVO, ErpStockCheckDO.class, in -> in
                .setTotalCount(getSumValue(stockCheckItems, ErpStockCheckItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockCheckItems, ErpStockCheckItemDO::getTotalPrice, BigDecimal::add)));
        erpStockCheckMapper.updateById(updateObj);
        // 2.2 更新盘点单项
        updateStockCheckItemList(updateReqVO.getId(), stockCheckItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockCheckStatus(Long id, Integer status) {
        // 1.1 校验存在
        ErpStockCheckDO stockCheck = validateStockCheckExists(id);
        Integer currentStatus = stockCheck.getStatus();

        // 1.2 校验状态流转是否合法
        ErpStockCheckStatusEnum currentEnum = ErpStockCheckStatusEnum.fromStatus(currentStatus);
        if (currentEnum == null || !currentEnum.canTransitionTo(status)) {
            throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, currentStatus, status);
        }

        // 2. 根据目标状态执行不同逻辑
        ErpStockCheckStatusEnum targetEnum = ErpStockCheckStatusEnum.fromStatus(status);

        switch (targetEnum) {
            case COUNTING -> {
                // DRAFT → COUNTING：生成快照 + 冻结仓库
                startCounting(stockCheck);
            }
            case REVIEWING -> {
                // COUNTING → REVIEWING：提交审核
                submitForReview(stockCheck);
            }
            case APPROVED -> {
                // REVIEWING → APPROVED：更新库存 + 生成凭证 + 变为 CLOSED
                approveAndClose(stockCheck);
            }
            case DRAFT -> {
                // COUNTING → DRAFT 或 REVIEWING → COUNTING：驳回修改
                rejectToPrevious(stockCheck, currentStatus);
            }
            default -> {
                throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, currentStatus, status);
            }
        }
    }

    /**
     * 启动盘点（DRAFT → COUNTING）
     *
     * 生成快照 + 冻结仓库
     */
    private void startCounting(ErpStockCheckDO stockCheck) {
        Long checkId = stockCheck.getId();

        // 1. 生成快照
        int snapshotCount = stockCheckSnapshotService.createSnapshot(checkId);
        if (snapshotCount == 0) {
            throw exception(STOCK_CHECK_SNAPSHOT_FAIL);
        }

        // 2. 冻结相关仓库
        List<ErpStockCheckItemDO> checkItems = erpStockCheckItemMapper.selectListByCheckId(checkId);
        Set<Long> warehouseIds = checkItems.stream()
                .map(ErpStockCheckItemDO::getWarehouseId)
                .collect(Collectors.toSet());
        warehouseIds.forEach(warehouseId -> {
            warehouseService.updateWarehouse(new ErpWarehouseSaveReqVO()
                    .setId(warehouseId).setFrozen(true));
        });

        // 3. 更新状态为 COUNTING，记录快照时间
        erpStockCheckMapper.updateById(new ErpStockCheckDO()
                .setId(checkId)
                .setStatus(ErpStockCheckStatusEnum.COUNTING.getStatus())
                .setSnapshotTime(LocalDateTime.now()));
    }

    /**
     * 提交审核（COUNTING → REVIEWING）
     *
     * 计算差异金额
     */
    private void submitForReview(ErpStockCheckDO stockCheck) {
        Long checkId = stockCheck.getId();

        // 1. 计算差异金额
        calculateDiffAmount(checkId);

        // 2. 更新状态为 REVIEWING
        erpStockCheckMapper.updateById(new ErpStockCheckDO()
                .setId(checkId)
                .setStatus(ErpStockCheckStatusEnum.REVIEWING.getStatus()));
    }

    /**
     * 审核通过并关闭（REVIEWING → APPROVED → CLOSED）
     *
     * 先写 APPROVED 保留审计轨迹，再执行业务，最后写 CLOSED。
     * 包含：更新库存、盘亏结转制造费用、生成凭证、解冻仓库。
     */
    private void approveAndClose(ErpStockCheckDO stockCheck) {
        Long checkId = stockCheck.getId();

        // 0. 先写 APPROVED 状态，保留审计轨迹
        erpStockCheckMapper.updateById(new ErpStockCheckDO()
                .setId(checkId)
                .setStatus(ErpStockCheckStatusEnum.APPROVED.getStatus()));

        // 1. 更新库存
        List<ErpStockCheckItemDO> stockCheckItems = erpStockCheckItemMapper.selectListByCheckId(checkId);
        // 批量查询库存（消除 N+1）
        Set<Long> checkProductIds = convertSet(stockCheckItems, ErpStockCheckItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(checkProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        stockCheckItems.forEach(stockCheckItem -> {
            // 没有盈亏，不用出入库
            if (stockCheckItem.getCount().compareTo(BigDecimal.ZERO) == 0) {
                return;
            }

            BigDecimal count = stockCheckItem.getCount();
            Integer bizType = count.compareTo(BigDecimal.ZERO) > 0
                    ? ErpStockRecordBizTypeEnum.CHECK_MORE_IN.getType()
                    : ErpStockRecordBizTypeEnum.CHECK_LESS_OUT.getType();

            // 获取加权平均成本作为盘点价格
            ErpStockDO stock = stockMap.get(stockCheckItem.getProductId() + ":" + stockCheckItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(stockCheckItem.getCount().abs()) : null;

            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockCheckItem.getProductId(), stockCheckItem.getWarehouseId(), count,
                    bizType, stockCheckItem.getCheckId(), stockCheckItem.getId(), stockCheck.getNo(),
                    price, amount));
        });

        // 2. 盘亏结转至制造费用
        stockCheckItems.forEach(stockCheckItem -> {
            // 只处理盘亏（count < 0 表示账面 > 实际，即盘亏）
            if (stockCheckItem.getCount().compareTo(BigDecimal.ZERO) >= 0) {
                return;
            }
            // 计算盘亏金额
            BigDecimal lossAmount = stockCheckItem.getCount().abs().multiply(
                    defaultAmount(stockCheckItem.getProductPrice()));
            if (lossAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            // 创建生产成本条目（制造费用-OTHER）
            ErpProductionCostEntryDO costEntry = ErpProductionCostEntryDO.builder()
                    .costType(ErpProductionCostTypeEnum.OTHER.getType())
                    .sourceType(ErpProductionCostSourceTypeEnum.SYSTEM.getType())
                    .amount(lossAmount)
                    .sourceId(stockCheckItem.getCheckId())
                    .sourceNo(stockCheck.getNo())
                    .remark("盘点盘亏自动结转")
                    .build();
            productionCostService.createProductionCostEntryFromCheck(costEntry);
        });

        // 3. 生成凭证
        Long voucherId = null;
        try {
            voucherId = voucherService.autoGenerateVoucher(ErpBizTypeEnum.STOCK_CHECK.getType(), checkId);
        } catch (Exception e) {
            log.error("[approveAndClose] 盘点凭证生成失败，checkId={}", checkId, e);
            // 凭证生成失败不影响主流程，记录日志即可
        }

        // 4. 更新状态为 CLOSED
        ErpStockCheckDO updateObj = new ErpStockCheckDO()
                .setId(checkId)
                .setStatus(ErpStockCheckStatusEnum.CLOSED.getStatus());
        if (voucherId != null) {
            updateObj.setVoucherId(voucherId);
        }
        erpStockCheckMapper.updateById(updateObj);

        // 5. 解冻仓库
        Set<Long> warehouseIds = stockCheckItems.stream()
                .map(ErpStockCheckItemDO::getWarehouseId)
                .collect(Collectors.toSet());
        warehouseIds.forEach(warehouseId -> {
            ErpWarehouseDO warehouse = warehouseService.getWarehouse(warehouseId);
            if (warehouse != null && Boolean.TRUE.equals(warehouse.getFrozen())) {
                warehouseService.updateWarehouse(new ErpWarehouseSaveReqVO()
                        .setId(warehouseId).setFrozen(false));
            }
        });
    }

    /**
     * 驳回到上一状态
     *
     * REVIEWING → COUNTING 或 COUNTING → DRAFT
     */
    private void rejectToPrevious(ErpStockCheckDO stockCheck, Integer currentStatus) {
        Integer targetStatus;
        if (ErpStockCheckStatusEnum.REVIEWING.getStatus().equals(currentStatus)) {
            targetStatus = ErpStockCheckStatusEnum.COUNTING.getStatus();
        } else if (ErpStockCheckStatusEnum.COUNTING.getStatus().equals(currentStatus)) {
            targetStatus = ErpStockCheckStatusEnum.DRAFT.getStatus();
            // 删除快照
            stockCheckSnapshotService.deleteSnapshot(stockCheck.getId());
            // 解冻仓库
            List<ErpStockCheckItemDO> checkItems = erpStockCheckItemMapper.selectListByCheckId(stockCheck.getId());
            Set<Long> warehouseIds = checkItems.stream()
                    .map(ErpStockCheckItemDO::getWarehouseId)
                    .collect(Collectors.toSet());
            warehouseIds.forEach(warehouseId -> {
                ErpWarehouseDO warehouse = warehouseService.getWarehouse(warehouseId);
                if (warehouse != null && Boolean.TRUE.equals(warehouse.getFrozen())) {
                    warehouseService.updateWarehouse(new ErpWarehouseSaveReqVO()
                            .setId(warehouseId).setFrozen(false));
                }
            });
        } else {
            throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, currentStatus, null);
        }

        erpStockCheckMapper.updateById(new ErpStockCheckDO()
                .setId(stockCheck.getId())
                .setStatus(targetStatus));
    }

    /**
     * 计算差异金额
     */
    private void calculateDiffAmount(Long checkId) {
        List<ErpStockCheckItemDO> checkItems = erpStockCheckItemMapper.selectListByCheckId(checkId);
        List<ErpStockCheckSnapshotDO> snapshots = stockCheckSnapshotService.getSnapshotList(checkId);

        // 构建快照索引：product_warehouse -> snapshot
        Map<String, ErpStockCheckSnapshotDO> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(
                        s -> s.getProductId() + "_" + s.getWarehouseId(),
                        s -> s,
                        (a, b) -> a
                ));

        for (ErpStockCheckItemDO item : checkItems) {
            String key = item.getProductId() + "_" + item.getWarehouseId();
            ErpStockCheckSnapshotDO snapshot = snapshotMap.get(key);

            if (snapshot != null && snapshot.getAverageCost() != null) {
                // 差异数量 = 实际数量 - 账面数量
                BigDecimal diffQty = item.getActualCount().subtract(item.getStockCount());
                // 差异金额 = 差异数量 * 平均成本
                BigDecimal diffAmount = diffQty.multiply(snapshot.getAverageCost());

                erpStockCheckItemMapper.updateById(new ErpStockCheckItemDO()
                        .setId(item.getId())
                        .setDiffAmount(diffAmount));
            }
        }
    }

    private List<ErpStockCheckItemDO> validateStockCheckItems(List<ErpStockCheckSaveReqVO.Item> list) {
        // 1.1 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpStockCheckSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 1.2 校验仓库存在
        warehouseService.validWarehouseList(convertSet(list, ErpStockCheckSaveReqVO.Item::getWarehouseId));
        // 2. 转化为 ErpStockCheckItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpStockCheckItemDO.class, item -> item
                .setProductUnitId(productMap.get(item.getProductId()).getUnitId())
                .setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()))));
    }

    private void updateStockCheckItemList(Long id, List<ErpStockCheckItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpStockCheckItemDO> oldList = erpStockCheckItemMapper.selectListByCheckId(id);
        List<List<ErpStockCheckItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setCheckId(id));
            erpStockCheckItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpStockCheckItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpStockCheckItemMapper.deleteByIds(convertList(diffList.get(2), ErpStockCheckItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockCheck(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpStockCheckDO> stockChecks = erpStockCheckMapper.selectByIds(ids);
        if (CollUtil.isEmpty(stockChecks)) {
            return;
        }
        stockChecks.forEach(stockCheck -> {
            if (ErpStockCheckStatusEnum.APPROVED.getStatus().equals(stockCheck.getStatus())
                    || ErpStockCheckStatusEnum.CLOSED.getStatus().equals(stockCheck.getStatus())) {
                throw exception(STOCK_CHECK_DELETE_FAIL_APPROVE, stockCheck.getNo());
            }
        });

        // 2. 批量删除盘点单和盘点项
        erpStockCheckMapper.deleteByIds(ids);
        stockChecks.forEach(stockCheck -> erpStockCheckItemMapper.deleteByCheckId(stockCheck.getId()));
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private ErpStockCheckDO validateStockCheckExists(Long id) {
        ErpStockCheckDO stockCheck = erpStockCheckMapper.selectById(id);
        if (stockCheck == null) {
            throw exception(STOCK_CHECK_NOT_EXISTS);
        }
        return stockCheck;
    }

    @Override
    public ErpStockCheckDO getStockCheck(Long id) {
        return erpStockCheckMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockCheckDO> getStockCheckPage(ErpStockCheckPageReqVO pageReqVO) {
        return erpStockCheckMapper.selectPage(pageReqVO);
    }

    // ==================== 盘点项 ====================

    @Override
    public List<ErpStockCheckItemDO> getStockCheckItemListByCheckId(Long checkId) {
        return erpStockCheckItemMapper.selectListByCheckId(checkId);
    }

    @Override
    public List<ErpStockCheckItemDO> getStockCheckItemListByCheckIds(Collection<Long> checkIds) {
        if (CollUtil.isEmpty(checkIds)) {
            return Collections.emptyList();
        }
        return erpStockCheckItemMapper.selectListByCheckIds(checkIds);
    }

    // ==================== 场景A状态流转方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startCounting(Long id) {
        ErpStockCheckDO stockCheck = validateStockCheckExists(id);
        ErpStockCheckStatusEnum currentEnum = ErpStockCheckStatusEnum.fromStatus(stockCheck.getStatus());
        if (currentEnum == null || !currentEnum.canTransitionTo(ErpStockCheckStatusEnum.COUNTING.getStatus())) {
            throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, stockCheck.getStatus(), ErpStockCheckStatusEnum.COUNTING.getStatus());
        }
        startCounting(stockCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long id) {
        ErpStockCheckDO stockCheck = validateStockCheckExists(id);
        ErpStockCheckStatusEnum currentEnum = ErpStockCheckStatusEnum.fromStatus(stockCheck.getStatus());
        if (currentEnum == null || !currentEnum.canTransitionTo(ErpStockCheckStatusEnum.REVIEWING.getStatus())) {
            throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, stockCheck.getStatus(), ErpStockCheckStatusEnum.REVIEWING.getStatus());
        }
        submitForReview(stockCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAndClose(Long id) {
        ErpStockCheckDO stockCheck = validateStockCheckExists(id);
        ErpStockCheckStatusEnum currentEnum = ErpStockCheckStatusEnum.fromStatus(stockCheck.getStatus());
        if (currentEnum == null || !currentEnum.canTransitionTo(ErpStockCheckStatusEnum.APPROVED.getStatus())) {
            throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, stockCheck.getStatus(), ErpStockCheckStatusEnum.APPROVED.getStatus());
        }
        approveAndClose(stockCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id) {
        ErpStockCheckDO stockCheck = validateStockCheckExists(id);
        Integer currentStatus = stockCheck.getStatus();

        // 根据当前状态决定驳回到哪个状态
        Integer targetStatus;
        if (ErpStockCheckStatusEnum.REVIEWING.getStatus().equals(currentStatus)) {
            targetStatus = ErpStockCheckStatusEnum.COUNTING.getStatus();
        } else if (ErpStockCheckStatusEnum.COUNTING.getStatus().equals(currentStatus)) {
            targetStatus = ErpStockCheckStatusEnum.DRAFT.getStatus();
        } else {
            throw exception(STOCK_CHECK_STATUS_TRANSITION_FAIL, currentStatus, null);
        }

        rejectToPrevious(stockCheck, currentStatus);
    }

    @Override
    public BigDecimal[] getDiffReport(Long checkId) {
        List<ErpStockCheckItemDO> checkItems = erpStockCheckItemMapper.selectListByCheckId(checkId);

        BigDecimal totalProfit = BigDecimal.ZERO;  // 盘盈总额
        BigDecimal totalLoss = BigDecimal.ZERO;    // 盘亏总额

        for (ErpStockCheckItemDO item : checkItems) {
            if (item.getDiffAmount() == null) {
                continue;
            }

            if (item.getDiffAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalProfit = totalProfit.add(item.getDiffAmount());
            } else {
                totalLoss = totalLoss.add(item.getDiffAmount().abs());
            }
        }

        return new BigDecimal[]{totalProfit, totalLoss};
    }

}
