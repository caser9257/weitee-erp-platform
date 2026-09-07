package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ErrorCode;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionInboundMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionInboundStatusEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class ErpProductionInboundServiceImpl implements ErpProductionInboundService {

    private static final ErrorCode PRODUCTION_INBOUND_NOT_EXISTS =
            new ErrorCode(1_030_700_040, "自制入库单不存在");
    private static final ErrorCode PRODUCTION_INBOUND_STATUS_INVALID =
            new ErrorCode(1_030_700_041, "当前自制入库单状态不允许执行该操作");
    private static final ErrorCode PRODUCTION_INBOUND_WAREHOUSE_REQUIRED =
            new ErrorCode(1_030_700_042, "生产工单缺少完工仓库，无法生成自制入库单");
    private static final ErrorCode PRODUCTION_INBOUND_BATCH_NOT_EXISTS =
            new ErrorCode(1_030_700_043, "自制入库对应的成品批次不存在，无法反执行");
    private static final String PRODUCTION_INBOUND_SOURCE_BIZ_TYPE = "PRODUCTION_INBOUND";

    @Resource
    private ErpProductionInboundMapper erpProductionInboundMapper;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductionCostService productionCostService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpFinanceBizHookService financeBizHookService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionInboundFromQuality(ErpProductionFinishQualityDO quality) {
        ErpProductionInboundDO existed = erpProductionInboundMapper.selectByFinishQualityId(quality.getId());
        if (existed != null) {
            return existed.getId();
        }
        ErpProductionOrderDO order = productionOrderService.getProductionOrder(quality.getProductionOrderId());
        if (order == null) {
            throw exception(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS);
        }
        if (order.getWarehouseId() == null) {
            throw exception(PRODUCTION_INBOUND_WAREHOUSE_REQUIRED);
        }
        BigDecimal inboundQty = defaultDecimal(quality.getQualifiedQty());
        ErpProductionCostDetailRespVO costDetail = productionCostService.getCostDetail(order.getId());
        BigDecimal unitCost = normalizeDecimal(costDetail == null ? BigDecimal.ZERO : costDetail.getUnitCost());
        BigDecimal totalCost = normalizeDecimal(unitCost.multiply(inboundQty));
        ErpProductionInboundDO inbound = new ErpProductionInboundDO()
                .setNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_INBOUND_NO_PREFIX))
                .setFinishQualityId(quality.getId())
                .setFinishQualityNo(quality.getNo())
                .setProductionOrderId(order.getId())
                .setProductionOrderNo(order.getOrderNo())
                .setProjectId(order.getProjectId())
                .setProductId(order.getProductId())
                .setWarehouseId(order.getWarehouseId())
                .setInboundQty(inboundQty)
                .setUnitCost(unitCost)
                .setTotalCost(totalCost)
                .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus())
                .setRemark(quality.getRemark());
        erpProductionInboundMapper.insert(inbound);
        return inbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeProductionInbound(Long operatorUserId, Long id) {
        ErpProductionInboundDO inbound = validateProductionInboundExists(id);
        if (!ErpProductionInboundStatusEnum.PENDING.getStatus().equals(inbound.getStatus())) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        // CAS 先行：仅当状态仍为 PENDING 才允许进入执行，并发重复执行在写库存前即被拒绝，
        // 避免双写批次量、库存流水和入库成本（历史缺陷：读-判-写竞态导致自制入库重复记账）
        if (erpProductionInboundMapper.updateByIdAndStatus(id, ErpProductionInboundStatusEnum.PENDING.getStatus(),
                new ErpProductionInboundDO()
                        .setStatus(ErpProductionInboundStatusEnum.EXECUTED.getStatus())
                        .setInboundTime(now)
                        .setExecutedBy(operatorUserId)
                        .setExecutedTime(now)) == 0) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
        stockBatchService.createOrIncreaseBatch(new ErpStockBatchInboundReqBO(
                inbound.getProductId(), inbound.getWarehouseId(), inbound.getNo(), now, null, null,
                inbound.getInboundQty(), Boolean.FALSE, ErpStockRecordBizTypeEnum.PRODUCTION_IN.getType(),
                inbound.getId(), inbound.getId(), inbound.getNo(), PRODUCTION_INBOUND_SOURCE_BIZ_TYPE,
                null, null, inbound.getRemark()));
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                inbound.getProductId(), inbound.getWarehouseId(), inbound.getInboundQty(),
                ErpStockRecordBizTypeEnum.PRODUCTION_IN.getType(), inbound.getId(), inbound.getId(), inbound.getNo(),
                inbound.getUnitCost(), inbound.getTotalCost()));
        financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), id, now.toLocalDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProductionInbound(Long id) {
        validateProductionInboundExists(id);
        // CAS：作废与执行互斥，防止并发下 CANCELED 被 execute 覆盖回 EXECUTED
        if (erpProductionInboundMapper.updateByIdAndStatus(id, ErpProductionInboundStatusEnum.PENDING.getStatus(),
                new ErpProductionInboundDO()
                        .setStatus(ErpProductionInboundStatusEnum.CANCELED.getStatus())) == 0) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertProductionInbound(Long operatorUserId, Long id) {
        ErpProductionInboundDO inbound = validateProductionInboundExists(id);
        if (!ErpProductionInboundStatusEnum.EXECUTED.getStatus().equals(inbound.getStatus())) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
        ErpStockBatchDO stockBatch = stockBatchService.getStockBatchByProductWarehouseAndBatchNo(
                inbound.getProductId(), inbound.getWarehouseId(), inbound.getNo());
        if (stockBatch == null) {
            throw exception(PRODUCTION_INBOUND_BATCH_NOT_EXISTS);
        }
        // CAS 先行：仅当状态仍为 EXECUTED 才允许反执行，并发重复反执行在扣库存前即被拒绝
        if (erpProductionInboundMapper.updateByIdAndStatus(id, ErpProductionInboundStatusEnum.EXECUTED.getStatus(),
                new ErpProductionInboundDO()
                        .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus())) == 0) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
        erpProductionInboundMapper.resetExecutionInfoById(id);
        stockBatchService.decreaseBatch(new ErpStockBatchChangeReqBO(
                stockBatch.getId(), inbound.getInboundQty(), ErpStockRecordBizTypeEnum.PRODUCTION_IN_CANCEL.getType(),
                inbound.getId(), inbound.getId(), inbound.getNo(), "自制入库反执行"));
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                inbound.getProductId(), inbound.getWarehouseId(), inbound.getInboundQty().negate(),
                ErpStockRecordBizTypeEnum.PRODUCTION_IN_CANCEL.getType(), inbound.getId(), inbound.getId(), inbound.getNo(),
                inbound.getUnitCost(), inbound.getTotalCost()));
        financeBizHookService.handleRollbackBiz(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), id, operatorUserId, "自制入库反执行回滚凭证");
    }

    @Override
    public ErpProductionInboundDO getProductionInbound(Long id) {
        return validateProductionInboundExists(id);
    }

    @Override
    public ErpProductionInboundDO getProductionInboundByFinishQualityId(Long finishQualityId) {
        return erpProductionInboundMapper.selectByFinishQualityId(finishQualityId);
    }

    @Override
    public PageResult<ErpProductionInboundDO> getProductionInboundPage(ErpProductionInboundPageReqVO pageReqVO) {
        return erpProductionInboundMapper.selectPage(pageReqVO);
    }

    private ErpProductionInboundDO validateProductionInboundExists(Long id) {
        ErpProductionInboundDO inbound = erpProductionInboundMapper.selectById(id);
        if (inbound == null) {
            throw exception(PRODUCTION_INBOUND_NOT_EXISTS);
        }
        return inbound;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        return defaultDecimal(value).setScale(6, RoundingMode.HALF_UP);
    }

}
