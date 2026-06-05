package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionFinishQualityMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import cn.iocoder.yudao.module.erp.framework.event.ErpProductionQualityPassedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ErpProductionFinishQualityServiceImpl implements ErpProductionFinishQualityService {

    private static final ErrorCode PRODUCTION_FINISH_QUALITY_NOT_EXISTS =
            new ErrorCode(1_030_700_014, "成品质检单不存在");
    private static final ErrorCode PRODUCTION_FINISH_QUALITY_STATUS_INVALID =
            new ErrorCode(1_030_700_015, "当前成品质检状态不允许执行该操作");
    private static final ErrorCode PRODUCTION_FINISH_QUALITY_COUNT_INVALID =
            new ErrorCode(1_030_700_016, "成品质检合格数与不合格数之和必须等于报工数量");

    @Resource
    private ErpProductionFinishQualityMapper erpProductionFinishQualityMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    @Lazy
    private ErpProductionInboundService productionInboundService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPendingQualityAfterFinish(ErpProductionOrderDO order, BigDecimal reportQty) {
        ErpProductionFinishQualityDO existed = erpProductionFinishQualityMapper.selectByProductionOrderId(order.getId());
        if (existed != null) {
            return existed.getId();
        }
        ErpProductionFinishQualityDO quality = new ErpProductionFinishQualityDO()
                .setNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_FINISH_QUALITY_NO_PREFIX))
                .setProductionOrderId(order.getId())
                .setProductionOrderNo(order.getOrderNo())
                .setSourceOrderId(order.getSourceOrderId())
                .setSourceItemId(order.getSourceItemId())
                .setProductId(order.getProductId())
                .setReportQty(reportQty)
                .setQualifiedQty(BigDecimal.ZERO)
                .setUnqualifiedQty(BigDecimal.ZERO)
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus());
        erpProductionFinishQualityMapper.insert(quality);
        return quality.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitQuality(Long qualityId, Long checkerUserId, BigDecimal qualifiedQty, BigDecimal unqualifiedQty, String remark) {
        ErpProductionFinishQualityDO quality = validateProductionFinishQualityExists(qualityId);
        if (!ObjectUtil.equal(quality.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus())) {
            throw exception(PRODUCTION_FINISH_QUALITY_STATUS_INVALID);
        }
        BigDecimal safeQualifiedQty = ObjectUtil.defaultIfNull(qualifiedQty, BigDecimal.ZERO);
        BigDecimal safeUnqualifiedQty = ObjectUtil.defaultIfNull(unqualifiedQty, BigDecimal.ZERO);
        if (safeQualifiedQty.add(safeUnqualifiedQty).compareTo(quality.getReportQty()) != 0) {
            throw exception(PRODUCTION_FINISH_QUALITY_COUNT_INVALID);
        }
        Integer qaStatus = resolveQaStatus(quality.getReportQty(), safeQualifiedQty);
        erpProductionFinishQualityMapper.updateById(new ErpProductionFinishQualityDO()
                .setId(qualityId)
                .setQualifiedQty(safeQualifiedQty)
                .setUnqualifiedQty(safeUnqualifiedQty)
                .setStatus(qaStatus)
                .setRemark(remark)
                .setCheckerUserId(checkerUserId)
                .setCheckTime(LocalDateTime.now()));
        boolean passedOrPartial = ObjectUtil.equal(qaStatus, ErpQaStatusEnum.PASSED.getStatus())
                || ObjectUtil.equal(qaStatus, ErpQaStatusEnum.PARTIAL.getStatus());
        if (passedOrPartial) {
            productionInboundService.createProductionInboundFromQuality(new ErpProductionFinishQualityDO()
                    .setId(quality.getId())
                    .setNo(quality.getNo())
                    .setProductionOrderId(quality.getProductionOrderId())
                    .setProductionOrderNo(quality.getProductionOrderNo())
                    .setSourceOrderId(quality.getSourceOrderId())
                    .setSourceItemId(quality.getSourceItemId())
                    .setProductId(quality.getProductId())
                    .setReportQty(quality.getReportQty())
                    .setQualifiedQty(safeQualifiedQty)
                    .setUnqualifiedQty(safeUnqualifiedQty)
                    .setStatus(qaStatus)
                    .setRemark(remark));
        }
        if (passedOrPartial && quality.getSourceOrderId() != null) {
            eventPublisher.publishEvent(new ErpProductionQualityPassedEvent(quality.getSourceOrderId()));
        }
    }

    @Override
    public ErpProductionFinishQualityDO getProductionFinishQuality(Long id) {
        return validateProductionFinishQualityExists(id);
    }

    @Override
    public PageResult<ErpProductionFinishQualityDO> getProductionFinishQualityPage(ErpProductionFinishQualityPageReqVO pageReqVO) {
        return erpProductionFinishQualityMapper.selectPage(pageReqVO);
    }

    private ErpProductionFinishQualityDO validateProductionFinishQualityExists(Long id) {
        ErpProductionFinishQualityDO quality = erpProductionFinishQualityMapper.selectById(id);
        if (quality == null) {
            throw exception(PRODUCTION_FINISH_QUALITY_NOT_EXISTS);
        }
        return quality;
    }

    private Integer resolveQaStatus(BigDecimal reportQty, BigDecimal qualifiedQty) {
        if (qualifiedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpQaStatusEnum.REJECTED.getStatus();
        }
        if (qualifiedQty.compareTo(reportQty) >= 0) {
            return ErpQaStatusEnum.PASSED.getStatus();
        }
        return ErpQaStatusEnum.PARTIAL.getStatus();
    }

}
