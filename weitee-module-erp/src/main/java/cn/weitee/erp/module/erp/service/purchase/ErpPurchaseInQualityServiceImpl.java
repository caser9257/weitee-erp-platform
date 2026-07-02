package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.exception.ErrorCode;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityAssignCheckerReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityStartRecheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityRoundMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityRoundTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import cn.weitee.erp.module.system.api.permission.PermissionApi;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.enums.permission.RoleCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_NO_REJECT_ITEMS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ORDER_SUBMIT_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ASSIGN_CHECKER_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_REQUIRED;

@Service
@Validated
@Slf4j
public class ErpPurchaseInQualityServiceImpl implements ErpPurchaseInQualityService {

    // region 本类保留的 ErrorCode

    private static final ErrorCode PURCHASE_IN_QUALITY_FIRST_CHECK_FAIL_STATUS_LOCAL =
            new ErrorCode(1_030_102_025, "当前采购入库 IQC 单状态不允许提交初检");
    private static final ErrorCode PURCHASE_IN_QUALITY_RECHECK_START_FAIL_STATUS_LOCAL =
            new ErrorCode(1_030_102_026, "当前采购入库 IQC 单状态不允许发起复检");
    private static final ErrorCode PURCHASE_IN_QUALITY_RECHECK_SUBMIT_FAIL_STATUS_LOCAL =
            new ErrorCode(1_030_102_027, "当前采购入库 IQC 单状态不允许提交复检");
    private static final ErrorCode PURCHASE_IN_QUALITY_RECHECK_ALREADY_STARTED_LOCAL =
            new ErrorCode(1_030_102_031, "当前采购入库 IQC 单已发起复检，请勿重复操作");

    // endregion

    // region 依赖注入（直接依赖）

    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpPurchaseInQualityMapper erpPurchaseInQualityMapper;
    @Resource
    private ErpPurchaseInQualityItemMapper erpPurchaseInQualityItemMapper;
    @Resource
    private ErpPurchaseInQualityRoundMapper erpPurchaseInQualityRoundMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private ErpPurchaseReturnService erpPurchaseReturnService;
    @Resource
    private ErpPurchaseReturnMapper erpPurchaseReturnMapper;
    @Resource
    private RedissonClient redissonClient;

    // endregion

    // region 依赖注入（委托给 Helper）

    @Resource
    private ErpPurchaseInQualityValidationHelper validationHelper;
    @Resource
    private ErpPurchaseInQualityDefectHelper defectHelper;
    @Resource
    private ErpPurchaseInQualityQueryHelper queryHelper;
    @Resource
    private ErpPurchaseInQualityNotificationHelper notificationHelper;

    // endregion

    // region 质检单生命周期：创建 / 作废

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQualityOrderIfAbsent(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = queryHelper.getRequiredPurchaseIn(purchaseInId);
        if (!ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS);
        }
        ErpPurchaseInQualityDO existed = erpPurchaseInQualityMapper.selectByPurchaseInId(purchaseInId);
        if (existed != null) {
            return existed.getId();
        }

        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseInId);
        ErpPurchaseInQualityDO quality = new ErpPurchaseInQualityDO()
                .setNo(noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_IN_QUALITY_NO_PREFIX))
                .setPurchaseInId(purchaseInId)
                .setPurchaseInNo(purchaseIn.getNo())
                .setStatus(ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())
                .setResult(ErpPurchaseInQualityResultEnum.TO_DECIDE.getStatus())
                .setCurrentRoundNo(1)
                .setRecheckRequired(Boolean.FALSE)
                .setPassCount(BigDecimal.ZERO)
                .setRejectCount(BigDecimal.ZERO);
        erpPurchaseInQualityMapper.insert(quality);

        List<ErpPurchaseInQualityItemDO> qualityItems = convertList(purchaseInItems, item ->
                new ErpPurchaseInQualityItemDO()
                        .setQualityId(quality.getId())
                        .setPurchaseInItemId(item.getId())
                        .setProductId(item.getProductId())
                        .setWarehouseId(item.getWarehouseId())
                        .setCount(ObjectUtil.defaultIfNull(item.getCount(), BigDecimal.ZERO))
                        .setSampleCount(ObjectUtil.defaultIfNull(item.getCount(), BigDecimal.ZERO))
                        .setQaPassCount(BigDecimal.ZERO)
                        .setQaRejectCount(BigDecimal.ZERO)
                        .setQaResult(ErpPurchaseInQualityResultEnum.TO_DECIDE.getStatus())
                        .setQaRemark(null));
        if (CollUtil.isNotEmpty(qualityItems)) {
            erpPurchaseInQualityItemMapper.insertBatch(qualityItems);
        }
        return quality.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidQualityOrderByPurchaseIn(Long purchaseInId, String reason) {
        ErpPurchaseInQualityDO quality = erpPurchaseInQualityMapper.selectByPurchaseInId(purchaseInId);
        if (quality == null || ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.VOID.getStatus())) {
            return;
        }
        erpPurchaseInQualityMapper.updateById(new ErpPurchaseInQualityDO()
                .setId(quality.getId())
                .setStatus(ErpPurchaseInQualityStatusEnum.VOID.getStatus())
                .setRemark(reason));
    }

    // endregion

    // region 兼容旧页面的直提交流程

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseInQuality(Long userId, ErpPurchaseInQualitySubmitReqVO reqVO) {
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(reqVO.getId());
        submitPurchaseInQuality(userId, quality, reqVO.getRemark(), convertMap(reqVO.getItems(),
                ErpPurchaseInQualitySubmitReqVO.Item::getId, item -> item));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseInQualityByPurchaseIn(Long userId, ErpPurchaseInQualityCheckReqVO reqVO) {
        Long qualityId = createQualityOrderIfAbsent(reqVO.getId());
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(qualityId);
        submitPurchaseInQuality(userId, quality, reqVO.getRemark(), convertMap(reqVO.getItems(),
                ErpPurchaseInQualityCheckReqVO.Item::getId, item -> item));
    }

    private void submitPurchaseInQuality(Long userId, ErpPurchaseInQualityDO quality, String remark,
                                         Map<Long, ?> requestItemMap) {
        if (!validationHelper.canSubmit(quality.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_ORDER_SUBMIT_FAIL_STATUS);
        }
        validateSubmitChecker(userId, quality);

        ErpPurchaseInDO purchaseIn = queryHelper.getRequiredPurchaseIn(quality.getPurchaseInId());
        validationHelper.validatePurchaseInCanCheck(purchaseIn);

        List<ErpPurchaseInQualityItemDO> qualityItems = erpPurchaseInQualityItemMapper.selectListByQualityId(quality.getId());
        if (CollUtil.isEmpty(qualityItems) || qualityItems.size() != requestItemMap.size()) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }

        List<ErpPurchaseInQualityItemDO> updateQualityItems = convertList(qualityItems, qualityItem ->
                buildUpdatedQualityItem(qualityItem, requestItemMap.get(qualityItem.getId())));
        erpPurchaseInQualityItemMapper.updateBatch(updateQualityItems);
        updatePurchaseInItemsByQualityItems(qualityItems, updateQualityItems);

        BigDecimal qaPassCount = getSumValue(updateQualityItems,
                ErpPurchaseInQualityItemDO::getQaPassCount, BigDecimal::add, BigDecimal.ZERO);
        BigDecimal qaRejectCount = getSumValue(updateQualityItems,
                ErpPurchaseInQualityItemDO::getQaRejectCount, BigDecimal::add, BigDecimal.ZERO);
        finishQualityOrder(quality, purchaseIn, userId, remark, qaPassCount, qaRejectCount);
    }

    private ErpPurchaseInQualityItemDO buildUpdatedQualityItem(ErpPurchaseInQualityItemDO qualityItem, Object requestItem) {
        if (requestItem == null) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }

        BigDecimal qaPassCount;
        BigDecimal qaRejectCount;
        String qaRemark;
        if (requestItem instanceof ErpPurchaseInQualitySubmitReqVO.Item submitItem) {
            qaPassCount = submitItem.getQaPassCount();
            qaRejectCount = submitItem.getQaRejectCount();
            qaRemark = submitItem.getQaRemark();
        } else if (requestItem instanceof ErpPurchaseInQualityCheckReqVO.Item submitItem) {
            qaPassCount = submitItem.getQaPassCount();
            qaRejectCount = submitItem.getQaRejectCount();
            qaRemark = submitItem.getQaRemark();
        } else {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }

        validationHelper.validateCount(qualityItem.getPurchaseInItemId(), qualityItem.getCount(), qaPassCount, qaRejectCount);
        return BeanUtils.toBean(qualityItem, ErpPurchaseInQualityItemDO.class)
                .setQaPassCount(qaPassCount)
                .setQaRejectCount(qaRejectCount)
                .setQaResult(validationHelper.resolveItemResult(qaPassCount, qaRejectCount))
                .setQaRemark(qaRemark);
    }

    // endregion

    // region 指派质检人

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignChecker(Long userId, ErpPurchaseInQualityAssignCheckerReqVO reqVO) {
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(reqVO.getId());
        if (!validationHelper.canAssignChecker(quality.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_ASSIGN_CHECKER_FAIL_STATUS);
        }
        adminUserApi.validateUser(reqVO.getAssignedCheckerUserId());
        if (ObjectUtil.equal(quality.getAssignedCheckerUserId(), reqVO.getAssignedCheckerUserId())) {
            return;
        }
        erpPurchaseInQualityMapper.updateById(new ErpPurchaseInQualityDO()
                .setId(quality.getId())
                .setAssignedCheckerUserId(reqVO.getAssignedCheckerUserId())
                .setAssignedCheckerTime(LocalDateTime.now()));
        ErpPurchaseInQualityDO finalQuality = quality;
        ErpTransactionUtils.afterCommit(() -> {
            ErpPurchaseInDO purchaseIn = queryHelper.getRequiredPurchaseIn(finalQuality.getPurchaseInId());
            notificationHelper.sendAssignCheckerNotify(finalQuality, purchaseIn, reqVO.getAssignedCheckerUserId());
        });
    }

    // endregion

    // region 首检提交

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFirstCheck(Long userId, ErpPurchaseInQualitySubmitFirstCheckReqVO reqVO) {
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(reqVO.getId());
        if (!ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_FIRST_CHECK_FAIL_STATUS_LOCAL);
        }
        validateSubmitChecker(userId, quality);

        ErpPurchaseInDO purchaseIn = queryHelper.getRequiredPurchaseIn(quality.getPurchaseInId());
        validationHelper.validatePurchaseInCanCheck(purchaseIn);
        if (erpPurchaseInQualityRoundMapper.existsByQualityIdAndRoundNo(quality.getId(), 1)) {
            throw exception(PURCHASE_IN_QUALITY_FIRST_CHECK_FAIL_STATUS_LOCAL);
        }

        List<ErpPurchaseInQualityItemDO> qualityItems = erpPurchaseInQualityItemMapper.selectListByQualityId(quality.getId());
        if (CollUtil.isEmpty(qualityItems) || qualityItems.size() != reqVO.getItems().size()) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }
        Map<Long, ErpPurchaseInQualityItemDO> qualityItemMap = convertMap(qualityItems, ErpPurchaseInQualityItemDO::getId);

        LocalDateTime checkTime = LocalDateTime.now();
        boolean hasReject = false;
        for (ErpPurchaseInQualitySubmitFirstCheckReqVO.Item reqItem : reqVO.getItems()) {
            ErpPurchaseInQualityItemDO qualityItem = qualityItemMap.get(reqItem.getQualityItemId());
            if (qualityItem == null) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }
            validationHelper.validateSampleCount(qualityItem.getId(), qualityItem.getSampleCount(), reqItem.getSampleCount());
            validationHelper.validateRoundCount(qualityItem.getId(), reqItem.getSampleCount(),
                    reqItem.getRoundPassCount(), reqItem.getRoundRejectCount());
            validationHelper.validateFirstCheckDefects(qualityItem.getId(), reqItem.getRoundRejectCount(), reqItem.getDefects());

            ErpPurchaseInQualityRoundDO round = new ErpPurchaseInQualityRoundDO()
                    .setQualityId(quality.getId())
                    .setQualityItemId(qualityItem.getId())
                    .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                    .setRoundNo(1)
                    .setRoundType(ErpPurchaseInQualityRoundTypeEnum.FIRST.getType())
                    .setSampleCount(reqItem.getSampleCount())
                    .setPassCount(reqItem.getRoundPassCount())
                    .setRejectCount(reqItem.getRoundRejectCount())
                    .setResult(validationHelper.resolveItemResult(reqItem.getRoundPassCount(), reqItem.getRoundRejectCount()))
                    .setCheckerUserId(userId)
                    .setCheckTime(checkTime)
                    .setRemark(reqItem.getRoundRemark());
            erpPurchaseInQualityRoundMapper.insert(round);
            defectHelper.insertFirstCheckDefects(round.getId(), quality.getId(), qualityItem, reqItem.getDefects());

            if (ObjectUtil.defaultIfNull(reqItem.getRoundRejectCount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
                hasReject = true;
            }
        }

        if (!hasReject) {
            List<ErpPurchaseInQualityItemDO> updateQualityItems = convertList(qualityItems, item ->
                    new ErpPurchaseInQualityItemDO()
                            .setId(item.getId())
                            .setQaPassCount(item.getCount())
                            .setQaRejectCount(BigDecimal.ZERO)
                            .setQaResult(ErpPurchaseInQualityResultEnum.PASSED.getStatus())
                            .setQaRemark(reqVO.getRemark()));
            erpPurchaseInQualityItemMapper.updateBatch(updateQualityItems);
            updatePurchaseInItemsByQualityItems(qualityItems, updateQualityItems);

            BigDecimal totalPassCount = getSumValue(updateQualityItems,
                    ErpPurchaseInQualityItemDO::getQaPassCount, BigDecimal::add, BigDecimal.ZERO);
            finishQualityOrder(quality, purchaseIn, userId, reqVO.getRemark(), totalPassCount, BigDecimal.ZERO);
            return;
        }

        erpPurchaseInQualityMapper.updateById(new ErpPurchaseInQualityDO()
                .setId(quality.getId())
                .setStatus(ErpPurchaseInQualityStatusEnum.WAIT_RECHECK.getStatus())
                .setCurrentRoundNo(1)
                .setRecheckRequired(Boolean.TRUE)
                .setCheckerUserId(userId)
                .setCheckTime(checkTime)
                .setRemark(reqVO.getRemark()));
    }

    // endregion

    // region 复检流程

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRecheck(Long userId, ErpPurchaseInQualityStartRecheckReqVO reqVO) {
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(reqVO.getId());
        if (!ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.WAIT_RECHECK.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_RECHECK_START_FAIL_STATUS_LOCAL);
        }
        validateAssignedCheckerExists(quality);
        if (erpPurchaseInQualityRoundMapper.existsByQualityIdAndRoundNo(quality.getId(), 2)) {
            throw exception(PURCHASE_IN_QUALITY_RECHECK_ALREADY_STARTED_LOCAL);
        }

        erpPurchaseInQualityMapper.updateById(new ErpPurchaseInQualityDO()
                .setId(quality.getId())
                .setStatus(ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus())
                .setCurrentRoundNo(2)
                .setRecheckRequired(Boolean.TRUE)
                .setRecheckReason(reqVO.getRecheckReason())
                .setRecheckApplyUserId(userId)
                .setRecheckApplyTime(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRecheck(Long userId, ErpPurchaseInQualitySubmitRecheckReqVO reqVO) {
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(reqVO.getId());
        if (!ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_RECHECK_SUBMIT_FAIL_STATUS_LOCAL);
        }
        validateSubmitChecker(userId, quality);

        ErpPurchaseInDO purchaseIn = queryHelper.getRequiredPurchaseIn(quality.getPurchaseInId());
        validationHelper.validatePurchaseInCanCheck(purchaseIn);
        if (erpPurchaseInQualityRoundMapper.existsByQualityIdAndRoundNo(quality.getId(), 2)) {
            throw exception(PURCHASE_IN_QUALITY_RECHECK_SUBMIT_FAIL_STATUS_LOCAL);
        }

        List<ErpPurchaseInQualityItemDO> qualityItems = erpPurchaseInQualityItemMapper.selectListByQualityId(quality.getId());
        if (CollUtil.isEmpty(qualityItems) || qualityItems.size() != reqVO.getItems().size()) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }
        Map<Long, ErpPurchaseInQualityItemDO> qualityItemMap = convertMap(qualityItems, ErpPurchaseInQualityItemDO::getId);
        Map<Long, ErpPurchaseInQualityRoundDO> firstRoundMap = convertMap(
                erpPurchaseInQualityRoundMapper.selectListByQualityIdAndRoundNo(quality.getId(), 1),
                ErpPurchaseInQualityRoundDO::getQualityItemId);

        LocalDateTime checkTime = LocalDateTime.now();
        List<ErpPurchaseInQualityItemDO> updateQualityItems = convertList(reqVO.getItems(), reqItem -> {
            ErpPurchaseInQualityItemDO qualityItem = qualityItemMap.get(reqItem.getQualityItemId());
            if (qualityItem == null) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }
            ErpPurchaseInQualityRoundDO firstRound = firstRoundMap.get(qualityItem.getId());
            if (firstRound == null) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }

            BigDecimal maxRecheckSampleCount = ObjectUtil.defaultIfNull(firstRound.getRejectCount(), BigDecimal.ZERO);
            validationHelper.validateSampleCount(qualityItem.getId(), maxRecheckSampleCount, reqItem.getSampleCount());
            validationHelper.validateRoundCount(qualityItem.getId(), reqItem.getSampleCount(),
                    reqItem.getRoundPassCount(), reqItem.getRoundRejectCount());
            validationHelper.validateRecheckDefects(qualityItem.getId(), reqItem.getRoundRejectCount(), reqItem.getDefects());
            validationHelper.validateCount(qualityItem.getPurchaseInItemId(), qualityItem.getCount(),
                    reqItem.getFinalPassCount(), reqItem.getFinalRejectCount());

            ErpPurchaseInQualityRoundDO round = new ErpPurchaseInQualityRoundDO()
                    .setQualityId(quality.getId())
                    .setQualityItemId(qualityItem.getId())
                    .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                    .setRoundNo(2)
                    .setRoundType(ErpPurchaseInQualityRoundTypeEnum.RECHECK.getType())
                    .setSampleCount(reqItem.getSampleCount())
                    .setPassCount(reqItem.getRoundPassCount())
                    .setRejectCount(reqItem.getRoundRejectCount())
                    .setResult(validationHelper.resolveItemResult(reqItem.getRoundPassCount(), reqItem.getRoundRejectCount()))
                    .setCheckerUserId(userId)
                    .setCheckTime(checkTime)
                    .setRemark(reqItem.getRoundRemark());
            erpPurchaseInQualityRoundMapper.insert(round);
            defectHelper.insertRecheckDefects(round.getId(), quality.getId(), qualityItem, reqItem.getDefects());

            return new ErpPurchaseInQualityItemDO()
                    .setId(qualityItem.getId())
                    .setQaPassCount(reqItem.getFinalPassCount())
                    .setQaRejectCount(reqItem.getFinalRejectCount())
                    .setQaResult(validationHelper.resolveItemResult(reqItem.getFinalPassCount(), reqItem.getFinalRejectCount()))
                    .setQaRemark(reqItem.getRoundRemark());
        });

        erpPurchaseInQualityItemMapper.updateBatch(updateQualityItems);
        updatePurchaseInItemsByQualityItems(qualityItems, updateQualityItems);

        BigDecimal totalPassCount = getSumValue(updateQualityItems,
                ErpPurchaseInQualityItemDO::getQaPassCount, BigDecimal::add, BigDecimal.ZERO);
        BigDecimal totalRejectCount = getSumValue(updateQualityItems,
                ErpPurchaseInQualityItemDO::getQaRejectCount, BigDecimal::add, BigDecimal.ZERO);
        finishQualityOrder(quality, purchaseIn, userId, reqVO.getRemark(), totalPassCount, totalRejectCount);
    }

    // endregion

    // region 查询方法（委托给 QueryHelper）

    @Override
    public ErpPurchaseInQualityDO getPurchaseInQuality(Long id) {
        return queryHelper.getPurchaseInQuality(id);
    }

    @Override
    public ErpPurchaseInQualityDO getPurchaseInQualityByPurchaseInId(Long purchaseInId) {
        return queryHelper.getPurchaseInQualityByPurchaseInId(purchaseInId);
    }

    @Override
    public List<ErpPurchaseInQualityItemDO> getPurchaseInQualityItemListByQualityId(Long qualityId) {
        return queryHelper.getQualityItemListByQualityId(qualityId);
    }

    @Override
    public List<ErpPurchaseInQualityRoundDO> getRoundDOListByQualityId(Long qualityId) {
        return queryHelper.getRoundDOListByQualityId(qualityId);
    }

    @Override
    public List<ErpPurchaseInQualityDefectDO> getDefectDOListByQualityId(Long qualityId) {
        return queryHelper.getDefectDOListByQualityId(qualityId);
    }

    @Override
    public PageResult<ErpPurchaseInQualityDO> getPurchaseInQualityPage(ErpPurchaseInQualityPageReqVO pageReqVO) {
        return queryHelper.getPurchaseInQualityPage(pageReqVO);
    }

    // endregion

    // region 从质检创建退货单

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturnFromQuality(Long qualityId, Long userId) {
        RLock lock = redissonClient.getLock("erp:purchase-in-quality:create-return:" + qualityId);
        if (!lock.tryLock()) {
            throw exception(PURCHASE_IN_QUALITY_NO_REJECT_ITEMS);
        }
        try {
            return doCreateReturnFromQuality(qualityId);
        } finally {
            unlockAfterTransaction(lock);
        }
    }

    private void unlockAfterTransaction(RLock lock) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            unlockIfHeld(lock);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                unlockIfHeld(lock);
            }
        });
    }

    private void unlockIfHeld(RLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    private Long doCreateReturnFromQuality(Long qualityId) {
        ErpPurchaseInQualityDO quality = queryHelper.getRequiredPurchaseInQuality(qualityId);

        if (quality.getRejectCount() == null || quality.getRejectCount().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PURCHASE_IN_QUALITY_NO_REJECT_ITEMS);
        }

        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(quality.getPurchaseInId());
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }

        List<ErpPurchaseReturnDO> existingReturns = erpPurchaseReturnMapper.selectListByOrderId(purchaseIn.getOrderId());
        if (CollUtil.isNotEmpty(existingReturns)) {
            for (ErpPurchaseReturnDO existingReturn : existingReturns) {
                if (existingReturn.getRemark() != null && existingReturn.getRemark().contains(quality.getNo())) {
                    log.info("[createReturnFromQuality] 质检单已创建过退货单，qualityId={}, returnId={}", qualityId, existingReturn.getId());
                    return existingReturn.getId();
                }
            }
        }

        List<ErpPurchaseInQualityItemDO> qualityItems = erpPurchaseInQualityItemMapper.selectListByQualityId(qualityId);
        if (CollUtil.isEmpty(qualityItems)) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }

        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);

        cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO reqVO =
                new cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO();
        reqVO.setOrderId(purchaseIn.getOrderId());
        reqVO.setAccountId(purchaseIn.getAccountId());
        reqVO.setReturnTime(java.time.LocalDateTime.now());
        reqVO.setRemark("质检不合格退货，质检单号：" + quality.getNo());

        List<cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO.Item> returnItems =
                new ArrayList<>();
        for (ErpPurchaseInQualityItemDO qualityItem : qualityItems) {
            if (qualityItem.getQaRejectCount() == null || qualityItem.getQaRejectCount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(qualityItem.getPurchaseInItemId());
            if (purchaseInItem == null) {
                continue;
            }

            cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO.Item returnItem =
                    new cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO.Item();
            returnItem.setWarehouseId(qualityItem.getWarehouseId());
            returnItem.setProductId(qualityItem.getProductId());
            returnItem.setProductUnitId(purchaseInItem.getProductUnitId());
            returnItem.setCount(qualityItem.getQaRejectCount());
            returnItem.setOrderItemId(purchaseInItem.getOrderItemId());
            returnItem.setProductPrice(purchaseInItem.getProductPrice());
            returnItem.setTaxPercent(purchaseInItem.getTaxPercent());

            returnItems.add(returnItem);
        }

        if (returnItems.isEmpty()) {
            throw exception(PURCHASE_IN_QUALITY_NO_REJECT_ITEMS);
        }
        reqVO.setItems(returnItems);

        Long returnId = erpPurchaseReturnService.createPurchaseReturn(reqVO);

        log.info("[createReturnFromQuality] 从质检创建退货单成功，qualityId={}, returnId={}", qualityId, returnId);
        return returnId;
    }

    // endregion

    // region 内部方法（仅限本类使用）

    /**
     * 将质检明细结果回写到采购入库明细。
     */
    private void updatePurchaseInItemsByQualityItems(List<ErpPurchaseInQualityItemDO> sourceQualityItems,
                                                     List<ErpPurchaseInQualityItemDO> targetQualityItems) {
        Map<Long, ErpPurchaseInQualityItemDO> sourceQualityItemMap =
                convertMap(sourceQualityItems, ErpPurchaseInQualityItemDO::getId);
        List<ErpPurchaseInItemDO> purchaseInItems = convertList(targetQualityItems, targetQualityItem -> {
            ErpPurchaseInQualityItemDO sourceQualityItem = sourceQualityItemMap.get(targetQualityItem.getId());
            if (sourceQualityItem == null) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }
            return new ErpPurchaseInItemDO()
                    .setId(sourceQualityItem.getPurchaseInItemId())
                    .setQaPassCount(targetQualityItem.getQaPassCount())
                    .setQaRejectCount(targetQualityItem.getQaRejectCount())
                    .setQaRemark(targetQualityItem.getQaRemark());
        });
        if (CollUtil.isNotEmpty(purchaseInItems)) {
            erpPurchaseInItemMapper.updateBatch(purchaseInItems);
        }
    }

    /**
     * 完结 IQC 单并同步采购入库的质检结果。
     */
    private void finishQualityOrder(ErpPurchaseInQualityDO quality, ErpPurchaseInDO purchaseIn, Long userId,
                                    String remark, BigDecimal totalPassCount, BigDecimal totalRejectCount) {
        ErpPurchaseInQualityResultEnum result = validationHelper.resolveQualityResult(totalPassCount, totalRejectCount);
        ErpQaStatusEnum qaStatus = validationHelper.resolveQaStatus(result);
        LocalDateTime checkTime = LocalDateTime.now();
        BigDecimal stockInCount = ObjectUtil.defaultIfNull(purchaseIn.getStockInCount(), BigDecimal.ZERO);
        Integer stockInStatus = ErpPurchaseInStockInStatusResolver.resolve(totalPassCount, stockInCount);
        erpPurchaseInQualityMapper.updateById(new ErpPurchaseInQualityDO()
                .setId(quality.getId())
                .setStatus(ErpPurchaseInQualityStatusEnum.DONE.getStatus())
                .setResult(result.getStatus())
                .setCheckerUserId(userId)
                .setCheckTime(checkTime)
                .setRemark(remark)
                .setPassCount(totalPassCount)
                .setRejectCount(totalRejectCount));
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                .setId(purchaseIn.getId())
                .setQaStatus(qaStatus.getStatus())
                .setQaTime(checkTime)
                .setQaUserId(userId)
                .setQaRemark(remark)
                .setQaPassCount(totalPassCount)
                .setQaRejectCount(totalRejectCount)
                .setStockInCount(stockInCount)
                .setStockInStatus(stockInStatus)
                .setStockInTime(stockInCount.compareTo(BigDecimal.ZERO) > 0 ? purchaseIn.getStockInTime() : null)
                .setStockInUserId(stockInCount.compareTo(BigDecimal.ZERO) > 0 ? purchaseIn.getStockInUserId() : null));
        if (ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus())) {
            ErpPurchaseInQualityDO finalQuality = quality;
            ErpPurchaseInDO finalPurchaseIn = purchaseIn;
            ErpTransactionUtils.afterCommit(() -> notificationHelper.sendQualityFinishedNotify(
                    finalQuality, finalPurchaseIn, result, totalPassCount, totalRejectCount, stockInStatus));
        }
    }

    /**
     * 校验当前用户是否可以提交首检/复检。
     * 被指派质检人可提交，超级管理员可兜底。
     */
    private void validateSubmitChecker(Long userId, ErpPurchaseInQualityDO quality) {
        validateAssignedCheckerExists(quality);
        if (ObjectUtil.equal(userId, quality.getAssignedCheckerUserId())) {
            return;
        }
        if (permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return;
        }
        throw exception(PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_FORBIDDEN);
    }

    /**
     * 校验是否已经指派质检人。
     */
    private void validateAssignedCheckerExists(ErpPurchaseInQualityDO quality) {
        if (quality.getAssignedCheckerUserId() == null) {
            throw exception(PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_REQUIRED);
        }
    }

    // endregion
}
