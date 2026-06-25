package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMovePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMoveSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveItemDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMoveItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMoveMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 库存调拨单 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class ErpStockMoveServiceImpl implements ErpStockMoveService {

    @Resource
    private ErpStockMoveMapper erpStockMoveMapper;
    @Resource
    private ErpStockMoveItemMapper erpStockMoveItemMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockMove(ErpStockMoveSaveReqVO createReqVO) {
        // 1.1 校验出库项的有效性
        List<ErpStockMoveItemDO> stockMoveItems = validateStockMoveItems(createReqVO.getItems());
        // 1.2 生成调拨单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.STOCK_MOVE_NO_PREFIX);
        if (erpStockMoveMapper.selectByNo(no) != null) {
            throw exception(STOCK_MOVE_NO_EXISTS);
        }

        // 2.1 插入出库单
        ErpStockMoveDO stockMove = BeanUtils.toBean(createReqVO, ErpStockMoveDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setTotalCount(getSumValue(stockMoveItems, ErpStockMoveItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockMoveItems, ErpStockMoveItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO)));
        erpStockMoveMapper.insert(stockMove);
        // 2.2 插入出库单项
        stockMoveItems.forEach(o -> o.setMoveId(stockMove.getId()));
        erpStockMoveItemMapper.insertBatch(stockMoveItems);
        return stockMove.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockMove(ErpStockMoveSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpStockMoveDO stockMove = validateStockMoveExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(stockMove.getStatus())) {
            throw exception(STOCK_MOVE_UPDATE_FAIL_APPROVE, stockMove.getNo());
        }
        // 1.2 校验出库项的有效性
        List<ErpStockMoveItemDO> stockMoveItems = validateStockMoveItems(updateReqVO.getItems());

        // 2.1 更新出库单
        ErpStockMoveDO updateObj = BeanUtils.toBean(updateReqVO, ErpStockMoveDO.class, in -> in
                .setTotalCount(getSumValue(stockMoveItems, ErpStockMoveItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockMoveItems, ErpStockMoveItemDO::getTotalPrice, BigDecimal::add)));
        erpStockMoveMapper.updateById(updateObj);
        // 2.2 更新出库单项
        updateStockMoveItemList(updateReqVO.getId(), stockMoveItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockMoveStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpStockMoveDO stockMove = validateStockMoveExists(id);
        // 1.2 校验状态
        if (stockMove.getStatus().equals(status)) {
            throw exception(approve ? STOCK_MOVE_APPROVE_FAIL : STOCK_MOVE_PROCESS_FAIL);
        }

        // 2. 更新状态
        int updateCount = erpStockMoveMapper.updateByIdAndStatus(id, stockMove.getStatus(),
                new ErpStockMoveDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? STOCK_MOVE_APPROVE_FAIL : STOCK_MOVE_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpStockMoveItemDO> stockMoveItems = erpStockMoveItemMapper.selectListByMoveId(id);
        Integer fromBizType = approve ? ErpStockRecordBizTypeEnum.MOVE_OUT.getType()
                : ErpStockRecordBizTypeEnum.MOVE_OUT_CANCEL.getType();
        Integer toBizType = approve ? ErpStockRecordBizTypeEnum.MOVE_IN.getType()
                : ErpStockRecordBizTypeEnum.MOVE_IN_CANCEL.getType();
        stockMoveItems.forEach(stockMoveItem -> {
            BigDecimal fromCount = approve ? stockMoveItem.getCount().negate() : stockMoveItem.getCount();
            BigDecimal toCount = approve ? stockMoveItem.getCount() : stockMoveItem.getCount().negate();
            // 获取加权平均成本作为移库价格
            ErpStockDO stock = stockService.getStock(stockMoveItem.getProductId(), stockMoveItem.getFromWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(stockMoveItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockMoveItem.getProductId(), stockMoveItem.getFromWarehouseId(), fromCount,
                    fromBizType, stockMoveItem.getMoveId(), stockMoveItem.getId(), stockMove.getNo(),
                    price, amount));
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockMoveItem.getProductId(), stockMoveItem.getToWarehouseId(), toCount,
                    toBizType, stockMoveItem.getMoveId(), stockMoveItem.getId(), stockMove.getNo(),
                    price, amount));
        });
    }

    private List<ErpStockMoveItemDO> validateStockMoveItems(List<ErpStockMoveSaveReqVO.Item> list) {
        // 1.1 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpStockMoveSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 1.2 校验仓库存在
        warehouseService.validWarehouseList(convertSetByFlatMap(list,
                item -> Stream.of(item.getFromWarehouseId(),  item.getToWarehouseId())));
        // 2. 转化为 ErpStockMoveItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpStockMoveItemDO.class, item -> item
                .setProductUnitId(productMap.get(item.getProductId()).getUnitId())
                .setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()))));
    }

    private void updateStockMoveItemList(Long id, List<ErpStockMoveItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpStockMoveItemDO> oldList = erpStockMoveItemMapper.selectListByMoveId(id);
        List<List<ErpStockMoveItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setMoveId(id));
            erpStockMoveItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpStockMoveItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpStockMoveItemMapper.deleteByIds(convertList(diffList.get(2), ErpStockMoveItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockMove(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpStockMoveDO> stockMoves = erpStockMoveMapper.selectByIds(ids);
        if (CollUtil.isEmpty(stockMoves)) {
            return;
        }
        stockMoves.forEach(stockMove -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(stockMove.getStatus())) {
                throw exception(STOCK_MOVE_DELETE_FAIL_APPROVE, stockMove.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        stockMoves.forEach(stockMove -> {
            // 2.1 删除出库单
            erpStockMoveMapper.deleteById(stockMove.getId());
            // 2.2 删除出库单项
            erpStockMoveItemMapper.deleteByMoveId(stockMove.getId());
        });
    }

    private ErpStockMoveDO validateStockMoveExists(Long id) {
        ErpStockMoveDO stockMove = erpStockMoveMapper.selectById(id);
        if (stockMove == null) {
            throw exception(STOCK_MOVE_NOT_EXISTS);
        }
        return stockMove;
    }

    @Override
    public ErpStockMoveDO getStockMove(Long id) {
        return erpStockMoveMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockMoveDO> getStockMovePage(ErpStockMovePageReqVO pageReqVO) {
        return erpStockMoveMapper.selectPage(pageReqVO);
    }

    // ==================== 出库项 ====================

    @Override
    public List<ErpStockMoveItemDO> getStockMoveItemListByMoveId(Long moveId) {
        return erpStockMoveItemMapper.selectListByMoveId(moveId);
    }

    @Override
    public List<ErpStockMoveItemDO> getStockMoveItemListByMoveIds(Collection<Long> moveIds) {
        if (CollUtil.isEmpty(moveIds)) {
            return Collections.emptyList();
        }
        return erpStockMoveItemMapper.selectListByMoveIds(moveIds);
    }

}
