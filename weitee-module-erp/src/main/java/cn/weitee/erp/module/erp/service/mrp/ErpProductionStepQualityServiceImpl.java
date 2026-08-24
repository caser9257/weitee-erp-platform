package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionStepQualityPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionStepQualityDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionStepQualityMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_STEP_QUALITY_COUNT_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_STEP_QUALITY_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_STEP_QUALITY_STATUS_INVALID;

@Service
@Validated
public class ErpProductionStepQualityServiceImpl implements ErpProductionStepQualityService {

    @Resource
    private ErpProductionStepQualityMapper productionStepQualityMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPendingFromReport(Long reportId, List<ErpProductionReportItemDO> items,
                                        List<ErpProductionOrderStepDO> steps) {
        if (CollUtil.isEmpty(items) || CollUtil.isEmpty(steps)) {
            return;
        }
        Map<Long, ErpProductionOrderStepDO> stepMap = steps.stream()
                .collect(Collectors.toMap(ErpProductionOrderStepDO::getId, step -> step, (a, b) -> a));
        for (ErpProductionReportItemDO item : items) {
            ErpProductionOrderStepDO step = stepMap.get(item.getProductionOrderStepId());
            if (step == null || !Boolean.TRUE.equals(step.getQcFlag())) {
                continue;
            }
            ErpProductionStepQualityDO quality = new ErpProductionStepQualityDO()
                    .setNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_STEP_QUALITY_NO_PREFIX))
                    .setProductionOrderId(step.getProductionOrderId())
                    .setProductionOrderStepId(step.getId())
                    .setReportId(reportId)
                    .setStepNo(step.getStepNo())
                    .setStepCode(step.getStepCode())
                    .setStepName(step.getStepName())
                    .setReportQty(item.getReportedQty())
                    .setQualifiedQty(BigDecimal.ZERO)
                    .setUnqualifiedQty(BigDecimal.ZERO)
                    .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus());
            productionStepQualityMapper.insert(quality);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitQuality(Long qualityId, Long checkerUserId, BigDecimal qualifiedQty,
                              BigDecimal unqualifiedQty, String remark) {
        ErpProductionStepQualityDO quality = validateQualityExists(qualityId);
        if (!ObjectUtil.equal(quality.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus())) {
            throw exception(PRODUCTION_STEP_QUALITY_STATUS_INVALID);
        }
        BigDecimal safeQualified = ObjectUtil.defaultIfNull(qualifiedQty, BigDecimal.ZERO);
        BigDecimal safeUnqualified = ObjectUtil.defaultIfNull(unqualifiedQty, BigDecimal.ZERO);
        if (safeQualified.add(safeUnqualified).compareTo(quality.getReportQty()) != 0) {
            throw exception(PRODUCTION_STEP_QUALITY_COUNT_INVALID);
        }
        Integer qaStatus = resolveQaStatus(quality.getReportQty(), safeQualified);
        productionStepQualityMapper.updateById(new ErpProductionStepQualityDO()
                .setId(qualityId)
                .setQualifiedQty(safeQualified)
                .setUnqualifiedQty(safeUnqualified)
                .setStatus(qaStatus)
                .setCheckerUserId(checkerUserId)
                .setCheckTime(LocalDateTime.now())
                .setRemark(remark));
    }

    @Override
    public ErpProductionStepQualityDO getQuality(Long id) {
        return validateQualityExists(id);
    }

    @Override
    public PageResult<ErpProductionStepQualityDO> getQualityPage(ErpProductionStepQualityPageReqVO pageReqVO) {
        return productionStepQualityMapper.selectPage(pageReqVO);
    }

    private ErpProductionStepQualityDO validateQualityExists(Long id) {
        ErpProductionStepQualityDO quality = productionStepQualityMapper.selectById(id);
        if (quality == null) {
            throw exception(PRODUCTION_STEP_QUALITY_NOT_EXISTS);
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
