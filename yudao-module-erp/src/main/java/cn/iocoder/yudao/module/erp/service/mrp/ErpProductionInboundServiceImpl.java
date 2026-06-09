package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionInboundMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionInboundStatusEnum;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceBizHookService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockRecordService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class ErpProductionInboundServiceImpl implements ErpProductionInboundService {

    private static final ErrorCode PRODUCTION_INBOUND_NOT_EXISTS =
            new ErrorCode(1_030_700_040, "自制入库单不存在");
    private static final ErrorCode PRODUCTION_INBOUND_STATUS_INVALID =
            new ErrorCode(1_030_700_041, "当前自制入库单状态不允许执行该操作");
    private static final ErrorCode PRODUCTION_INBOUND_WAREHOUSE_REQUIRED =
            new ErrorCode(1_030_700_042, "生产工单缺少完工仓库，无法生成自制入库单");

    @Resource
    private ErpProductionInboundMapper erpProductionInboundMapper;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductionCostService productionCostService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
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
            throw exception(cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS);
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
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                inbound.getProductId(), inbound.getWarehouseId(), inbound.getInboundQty(),
                ErpStockRecordBizTypeEnum.PRODUCTION_IN.getType(), inbound.getId(), inbound.getId(), inbound.getNo(),
                inbound.getUnitCost(), inbound.getTotalCost()));
        LocalDateTime now = LocalDateTime.now();
        erpProductionInboundMapper.updateById(new ErpProductionInboundDO()
                .setId(id)
                .setStatus(ErpProductionInboundStatusEnum.EXECUTED.getStatus())
                .setInboundTime(now)
                .setExecutedBy(operatorUserId)
                .setExecutedTime(now));
        financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), id, now.toLocalDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProductionInbound(Long id) {
        ErpProductionInboundDO inbound = validateProductionInboundExists(id);
        if (!ErpProductionInboundStatusEnum.PENDING.getStatus().equals(inbound.getStatus())) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
        erpProductionInboundMapper.updateById(new ErpProductionInboundDO()
                .setId(id)
                .setStatus(ErpProductionInboundStatusEnum.CANCELED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertProductionInbound(Long operatorUserId, Long id) {
        ErpProductionInboundDO inbound = validateProductionInboundExists(id);
        if (!ErpProductionInboundStatusEnum.EXECUTED.getStatus().equals(inbound.getStatus())) {
            throw exception(PRODUCTION_INBOUND_STATUS_INVALID);
        }
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                inbound.getProductId(), inbound.getWarehouseId(), inbound.getInboundQty().negate(),
                ErpStockRecordBizTypeEnum.PRODUCTION_IN_CANCEL.getType(), inbound.getId(), inbound.getId(), inbound.getNo(),
                inbound.getUnitCost(), inbound.getTotalCost()));
        erpProductionInboundMapper.updateById(new ErpProductionInboundDO()
                .setId(id)
                .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus())
                .setExecutedBy(null)
                .setExecutedTime(null)
                .setInboundTime(null));
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
