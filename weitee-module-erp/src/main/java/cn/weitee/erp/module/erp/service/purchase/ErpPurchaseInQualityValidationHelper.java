package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.exception.ErrorCode;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityStatusEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_COUNT;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_NEGATIVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_QA_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS;

@Component
class ErpPurchaseInQualityValidationHelper {

    private static final ErrorCode SAMPLE_COUNT_INVALID =
            new ErrorCode(1_030_102_028, "采购入库 IQC 明细({})抽检数量不正确");
    private static final ErrorCode DEFECT_REQUIRED =
            new ErrorCode(1_030_102_029, "采购入库 IQC 明细({})存在不合格数量，必须填写不良原因");
    private static final ErrorCode DEFECT_COUNT_INVALID =
            new ErrorCode(1_030_102_030, "采购入库 IQC 明细({})不良原因数量汇总必须等于不合格数量");

    void validatePurchaseInCanCheck(ErpPurchaseInDO purchaseIn) {
        if (!ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS);
        }
        if (purchaseIn.getQaStatus() != null
                && !ObjectUtil.equal(purchaseIn.getQaStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_QA_STATUS);
        }
    }

    void validateCount(Long purchaseInItemId, BigDecimal totalCount,
                       BigDecimal qaPassCount, BigDecimal qaRejectCount) {
        BigDecimal safeTotalCount = ObjectUtil.defaultIfNull(totalCount, BigDecimal.ZERO);
        BigDecimal safePassCount = ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO);
        BigDecimal safeRejectCount = ObjectUtil.defaultIfNull(qaRejectCount, BigDecimal.ZERO);
        if (safePassCount.compareTo(BigDecimal.ZERO) < 0 || safeRejectCount.compareTo(BigDecimal.ZERO) < 0) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_NEGATIVE, purchaseInItemId);
        }
        if (safePassCount.add(safeRejectCount).compareTo(safeTotalCount) != 0) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_COUNT, purchaseInItemId, safeTotalCount);
        }
    }

    void validateSampleCount(Long qualityItemId, BigDecimal plannedSampleCount, BigDecimal actualSampleCount) {
        BigDecimal safePlannedSampleCount = ObjectUtil.defaultIfNull(plannedSampleCount, BigDecimal.ZERO);
        BigDecimal safeActualSampleCount = ObjectUtil.defaultIfNull(actualSampleCount, BigDecimal.ZERO);
        if (safeActualSampleCount.compareTo(BigDecimal.ZERO) <= 0
                || safeActualSampleCount.compareTo(safePlannedSampleCount) > 0) {
            throw exception(SAMPLE_COUNT_INVALID, qualityItemId);
        }
    }

    void validateRoundCount(Long qualityItemId, BigDecimal sampleCount,
                            BigDecimal roundPassCount, BigDecimal roundRejectCount) {
        validateCount(qualityItemId, sampleCount, roundPassCount, roundRejectCount);
    }

    void validateFirstCheckDefects(Long qualityItemId, BigDecimal rejectCount,
                                   List<ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect> defects) {
        List<BigDecimal> defectCounts = defects == null ? null
                : convertList(defects, ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect::getDefectCount);
        validateDefectCount(qualityItemId, rejectCount, defectCounts);
    }

    void validateRecheckDefects(Long qualityItemId, BigDecimal rejectCount,
                                List<ErpPurchaseInQualitySubmitRecheckReqVO.Defect> defects) {
        List<BigDecimal> defectCounts = defects == null ? null
                : convertList(defects, ErpPurchaseInQualitySubmitRecheckReqVO.Defect::getDefectCount);
        validateDefectCount(qualityItemId, rejectCount, defectCounts);
    }

    void validateDefectCount(Long qualityItemId, BigDecimal rejectCount, List<BigDecimal> defectCounts) {
        BigDecimal safeRejectCount = ObjectUtil.defaultIfNull(rejectCount, BigDecimal.ZERO);
        if (safeRejectCount.compareTo(BigDecimal.ZERO) == 0) {
            if (CollUtil.isEmpty(defectCounts)) {
                return;
            }
            BigDecimal totalDefectCount = defectCounts.stream()
                    .map(defectCount -> ObjectUtil.defaultIfNull(defectCount, BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalDefectCount.compareTo(BigDecimal.ZERO) != 0) {
                throw exception(DEFECT_COUNT_INVALID, qualityItemId);
            }
            return;
        }

        if (CollUtil.isEmpty(defectCounts)) {
            throw exception(DEFECT_REQUIRED, qualityItemId);
        }

        BigDecimal totalDefectCount = BigDecimal.ZERO;
        for (BigDecimal defectCount : defectCounts) {
            BigDecimal safeDefectCount = ObjectUtil.defaultIfNull(defectCount, BigDecimal.ZERO);
            if (safeDefectCount.compareTo(BigDecimal.ZERO) < 0) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_NEGATIVE, qualityItemId);
            }
            totalDefectCount = totalDefectCount.add(safeDefectCount);
        }
        if (totalDefectCount.compareTo(safeRejectCount) != 0) {
            throw exception(DEFECT_COUNT_INVALID, qualityItemId);
        }
    }

    Integer resolveItemResult(BigDecimal qaPassCount, BigDecimal qaRejectCount) {
        if (ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.REJECTED.getStatus();
        }
        if (ObjectUtil.defaultIfNull(qaRejectCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.PASSED.getStatus();
        }
        return ErpPurchaseInQualityResultEnum.PARTIAL.getStatus();
    }

    ErpPurchaseInQualityResultEnum resolveQualityResult(BigDecimal qaPassCount, BigDecimal qaRejectCount) {
        if (ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.REJECTED;
        }
        if (ObjectUtil.defaultIfNull(qaRejectCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.PASSED;
        }
        return ErpPurchaseInQualityResultEnum.PARTIAL;
    }

    ErpQaStatusEnum resolveQaStatus(ErpPurchaseInQualityResultEnum result) {
        return switch (result) {
            case PARTIAL -> ErpQaStatusEnum.PARTIAL;
            case PASSED -> ErpQaStatusEnum.PASSED;
            case REJECTED -> ErpQaStatusEnum.REJECTED;
            default -> throw new IllegalStateException("Unsupported quality result: " + result);
        };
    }

    boolean canSubmit(Integer status) {
        return ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.DRAFT.getStatus())
                || ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus());
    }

    boolean canAssignChecker(Integer status) {
        return ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())
                || ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.WAIT_RECHECK.getStatus())
                || ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus());
    }
}