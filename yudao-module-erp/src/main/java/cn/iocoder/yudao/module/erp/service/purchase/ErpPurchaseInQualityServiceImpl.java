package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityAssignCheckerReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityStartRecheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInQualityDefectMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInQualityItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInQualityMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInQualityRoundMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInQualityRoundTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInQualityStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.erp.util.ErpTransactionUtils;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_COUNT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_NEGATIVE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_QA_STATUS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ASSIGN_CHECKER_FAIL_STATUS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_FORBIDDEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_REQUIRED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_NO_REJECT_ITEMS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ORDER_SUBMIT_FAIL_STATUS;

@Service
@Validated
@Slf4j
public class ErpPurchaseInQualityServiceImpl implements ErpPurchaseInQualityService {

    private static final String IQC_ASSIGN_NOTIFY_TEMPLATE_CODE = "erp_iqc_checker_assigned";
    private static final String IQC_STOCK_IN_READY_NOTIFY_TEMPLATE_CODE = "erp_iqc_stock_in_ready";
    private static final String IQC_RECHECK_REJECTED_NOTIFY_TEMPLATE_CODE = "erp_iqc_recheck_rejected";
    private static final String PURCHASE_IN_CONFIRM_STOCK_PERMISSION = "erp:purchase-in:update-status";

    /** 当前采购入库 IQC 单状态不允许提交初检 */
    private static final ErrorCode PURCHASE_IN_QUALITY_FIRST_CHECK_FAIL_STATUS_LOCAL =
            new ErrorCode(1_030_102_025, "当前采购入库 IQC 单状态不允许提交初检");
    /** 当前采购入库 IQC 单状态不允许发起复检 */
    private static final ErrorCode PURCHASE_IN_QUALITY_RECHECK_START_FAIL_STATUS_LOCAL =
            new ErrorCode(1_030_102_026, "当前采购入库 IQC 单状态不允许发起复检");
    /** 当前采购入库 IQC 单状态不允许提交复检 */
    private static final ErrorCode PURCHASE_IN_QUALITY_RECHECK_SUBMIT_FAIL_STATUS_LOCAL =
            new ErrorCode(1_030_102_027, "当前采购入库 IQC 单状态不允许提交复检");
    /** 采购入库 IQC 明细抽检数量不正确 */
    private static final ErrorCode PURCHASE_IN_QUALITY_SAMPLE_COUNT_INVALID_LOCAL =
            new ErrorCode(1_030_102_028, "采购入库 IQC 明细({})抽检数量不正确");
    /** 存在不合格数量时，必须填写不良原因 */
    private static final ErrorCode PURCHASE_IN_QUALITY_DEFECT_REQUIRED_LOCAL =
            new ErrorCode(1_030_102_029, "采购入库 IQC 明细({})存在不合格数量，必须填写不良原因");
    /** 不良原因数量汇总必须等于不合格数量 */
    private static final ErrorCode PURCHASE_IN_QUALITY_DEFECT_COUNT_INVALID_LOCAL =
            new ErrorCode(1_030_102_030, "采购入库 IQC 明细({})不良原因数量汇总必须等于不合格数量");
    /** 当前采购入库 IQC 单已发起复检 */
    private static final ErrorCode PURCHASE_IN_QUALITY_RECHECK_ALREADY_STARTED_LOCAL =
            new ErrorCode(1_030_102_031, "当前采购入库 IQC 单已发起复检，请勿重复操作");

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
    private ErpPurchaseInQualityDefectMapper erpPurchaseInQualityDefectMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private ErpPurchaseReturnService erpPurchaseReturnService;
    @Resource
    private ErpPurchaseReturnMapper erpPurchaseReturnMapper;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQualityOrderIfAbsent(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(purchaseInId);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseInQuality(Long userId, ErpPurchaseInQualitySubmitReqVO reqVO) {
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(reqVO.getId());
        submitPurchaseInQuality(userId, quality, reqVO.getRemark(), convertMap(reqVO.getItems(),
                ErpPurchaseInQualitySubmitReqVO.Item::getId, item -> item));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseInQualityByPurchaseIn(Long userId, ErpPurchaseInQualityCheckReqVO reqVO) {
        Long qualityId = createQualityOrderIfAbsent(reqVO.getId());
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(qualityId);
        submitPurchaseInQuality(userId, quality, reqVO.getRemark(), convertMap(reqVO.getItems(),
                ErpPurchaseInQualityCheckReqVO.Item::getId, item -> item));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignChecker(Long userId, ErpPurchaseInQualityAssignCheckerReqVO reqVO) {
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(reqVO.getId());
        if (!canAssignChecker(quality.getStatus())) {
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
        ErpTransactionUtils.afterCommit(() -> sendAssignCheckerNotify(quality, reqVO.getAssignedCheckerUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFirstCheck(Long userId, ErpPurchaseInQualitySubmitFirstCheckReqVO reqVO) {
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(reqVO.getId());
        if (!ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_FIRST_CHECK_FAIL_STATUS_LOCAL);
        }
        validateSubmitChecker(userId, quality);

        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(quality.getPurchaseInId());
        validatePurchaseInCanCheck(purchaseIn);
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
            validateSampleCount(qualityItem.getId(), qualityItem.getSampleCount(), reqItem.getSampleCount());
            validateRoundCount(qualityItem.getId(), reqItem.getSampleCount(),
                    reqItem.getRoundPassCount(), reqItem.getRoundRejectCount());
            validateFirstCheckDefects(qualityItem.getId(), reqItem.getRoundRejectCount(), reqItem.getDefects());

            ErpPurchaseInQualityRoundDO round = new ErpPurchaseInQualityRoundDO()
                    .setQualityId(quality.getId())
                    .setQualityItemId(qualityItem.getId())
                    .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                    .setRoundNo(1)
                    .setRoundType(ErpPurchaseInQualityRoundTypeEnum.FIRST.getType())
                    .setSampleCount(reqItem.getSampleCount())
                    .setPassCount(reqItem.getRoundPassCount())
                    .setRejectCount(reqItem.getRoundRejectCount())
                    .setResult(resolveItemResult(reqItem.getRoundPassCount(), reqItem.getRoundRejectCount()))
                    .setCheckerUserId(userId)
                    .setCheckTime(checkTime)
                    .setRemark(reqItem.getRoundRemark());
            erpPurchaseInQualityRoundMapper.insert(round);
            insertFirstCheckDefects(round.getId(), quality.getId(), qualityItem, reqItem.getDefects());

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRecheck(Long userId, ErpPurchaseInQualityStartRecheckReqVO reqVO) {
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(reqVO.getId());
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
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(reqVO.getId());
        if (!ObjectUtil.equal(quality.getStatus(), ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_RECHECK_SUBMIT_FAIL_STATUS_LOCAL);
        }
        validateSubmitChecker(userId, quality);

        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(quality.getPurchaseInId());
        validatePurchaseInCanCheck(purchaseIn);
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
            validateSampleCount(qualityItem.getId(), maxRecheckSampleCount, reqItem.getSampleCount());
            validateRoundCount(qualityItem.getId(), reqItem.getSampleCount(),
                    reqItem.getRoundPassCount(), reqItem.getRoundRejectCount());
            validateRecheckDefects(qualityItem.getId(), reqItem.getRoundRejectCount(), reqItem.getDefects());
            validateCount(qualityItem.getPurchaseInItemId(), qualityItem.getCount(),
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
                    .setResult(resolveItemResult(reqItem.getRoundPassCount(), reqItem.getRoundRejectCount()))
                    .setCheckerUserId(userId)
                    .setCheckTime(checkTime)
                    .setRemark(reqItem.getRoundRemark());
            erpPurchaseInQualityRoundMapper.insert(round);
            insertRecheckDefects(round.getId(), quality.getId(), qualityItem, reqItem.getDefects());

            return new ErpPurchaseInQualityItemDO()
                    .setId(qualityItem.getId())
                    .setQaPassCount(reqItem.getFinalPassCount())
                    .setQaRejectCount(reqItem.getFinalRejectCount())
                    .setQaResult(resolveItemResult(reqItem.getFinalPassCount(), reqItem.getFinalRejectCount()))
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

    @Override
    public ErpPurchaseInQualityDO getPurchaseInQuality(Long id) {
        return erpPurchaseInQualityMapper.selectById(id);
    }

    @Override
    public ErpPurchaseInQualityDO getPurchaseInQualityByPurchaseInId(Long purchaseInId) {
        return erpPurchaseInQualityMapper.selectByPurchaseInId(purchaseInId);
    }

    @Override
    public List<ErpPurchaseInQualityItemDO> getPurchaseInQualityItemListByQualityId(Long qualityId) {
        return erpPurchaseInQualityItemMapper.selectListByQualityId(qualityId);
    }

    @Override
    public List<ErpPurchaseInQualityRoundDO> getRoundDOListByQualityId(Long qualityId) {
        return erpPurchaseInQualityRoundMapper.selectListByQualityId(qualityId);
    }

    @Override
    public List<ErpPurchaseInQualityDefectDO> getDefectDOListByQualityId(Long qualityId) {
        return erpPurchaseInQualityDefectMapper.selectListByQualityId(qualityId);
    }

    @Override
    public PageResult<ErpPurchaseInQualityDO> getPurchaseInQualityPage(ErpPurchaseInQualityPageReqVO pageReqVO) {
        return erpPurchaseInQualityMapper.selectPage(pageReqVO);
    }

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
        // 1. 获取质检单
        ErpPurchaseInQualityDO quality = getRequiredPurchaseInQuality(qualityId);

        // 2. 校验质检结果是否有不合格品
        if (quality.getRejectCount() == null || quality.getRejectCount().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PURCHASE_IN_QUALITY_NO_REJECT_ITEMS);
        }

        // 3. 获取入库单信息
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(quality.getPurchaseInId());
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }

        // 4. 防重复检查：检查该入库单是否已创建过退货单
        List<ErpPurchaseReturnDO> existingReturns = erpPurchaseReturnMapper.selectListByOrderId(purchaseIn.getOrderId());
        if (CollUtil.isNotEmpty(existingReturns)) {
            // 检查退货单是否关联了当前入库单（通过备注或创建时间判断）
            for (ErpPurchaseReturnDO existingReturn : existingReturns) {
                if (existingReturn.getRemark() != null && existingReturn.getRemark().contains(quality.getNo())) {
                    log.info("[createReturnFromQuality] 质检单已创建过退货单，qualityId={}, returnId={}", qualityId, existingReturn.getId());
                    return existingReturn.getId();
                }
            }
        }

        // 5. 获取质检项明细
        List<ErpPurchaseInQualityItemDO> qualityItems = erpPurchaseInQualityItemMapper.selectListByQualityId(qualityId);
        if (CollUtil.isEmpty(qualityItems)) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }

        // 5. 获取入库单明细（用于获取价格和税率信息）
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);

        // 6. 构建退货单保存请求
        cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO reqVO =
                new cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO();
        reqVO.setOrderId(purchaseIn.getOrderId());
        reqVO.setAccountId(purchaseIn.getAccountId());
        reqVO.setReturnTime(java.time.LocalDateTime.now());
        reqVO.setRemark("质检不合格退货，质检单号：" + quality.getNo());

        // 7. 构建退货项（只包含不合格品）
        List<cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO.Item> returnItems =
                new java.util.ArrayList<>();
        for (ErpPurchaseInQualityItemDO qualityItem : qualityItems) {
            if (qualityItem.getQaRejectCount() == null || qualityItem.getQaRejectCount().compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 无不合品，跳过
            }

            // 从入库单明细中获取价格和税率信息
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(qualityItem.getPurchaseInItemId());
            if (purchaseInItem == null) {
                continue;
            }

            cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO.Item returnItem =
                    new cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO.Item();
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

        // 8. 创建退货单
        Long returnId = erpPurchaseReturnService.createPurchaseReturn(reqVO);

        log.info("[createReturnFromQuality] 从质检创建退货单成功，qualityId={}, returnId={}", qualityId, returnId);
        return returnId;
    }

    /**
     * 兼容旧页面的直提交流程。
     */
    private void submitPurchaseInQuality(Long userId, ErpPurchaseInQualityDO quality, String remark,
                                         Map<Long, ?> requestItemMap) {
        if (!canSubmit(quality.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_ORDER_SUBMIT_FAIL_STATUS);
        }
        validateSubmitChecker(userId, quality);

        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(quality.getPurchaseInId());
        validatePurchaseInCanCheck(purchaseIn);

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

    /**
     * 构建兼容旧提交流程的明细结果。
     */
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

        validateCount(qualityItem.getPurchaseInItemId(), qualityItem.getCount(), qaPassCount, qaRejectCount);
        return BeanUtils.toBean(qualityItem, ErpPurchaseInQualityItemDO.class)
                .setQaPassCount(qaPassCount)
                .setQaRejectCount(qaRejectCount)
                .setQaResult(resolveItemResult(qaPassCount, qaRejectCount))
                .setQaRemark(qaRemark);
    }

    /**
     * 校验采购入库当前是否允许执行 IQC。
     */
    private void validatePurchaseInCanCheck(ErpPurchaseInDO purchaseIn) {
        if (!ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS);
        }
        if (purchaseIn.getQaStatus() != null
                && !ObjectUtil.equal(purchaseIn.getQaStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus())) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_QA_STATUS);
        }
    }

    /**
     * 校验合格数与不合格数是否等于目标数量。
     */
    private void validateCount(Long purchaseInItemId, BigDecimal totalCount,
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

    /**
     * 校验抽检数量必须大于 0，且不能超过计划数量。
     */
    private void validateSampleCount(Long qualityItemId, BigDecimal plannedSampleCount, BigDecimal actualSampleCount) {
        BigDecimal safePlannedSampleCount = ObjectUtil.defaultIfNull(plannedSampleCount, BigDecimal.ZERO);
        BigDecimal safeActualSampleCount = ObjectUtil.defaultIfNull(actualSampleCount, BigDecimal.ZERO);
        if (safeActualSampleCount.compareTo(BigDecimal.ZERO) <= 0
                || safeActualSampleCount.compareTo(safePlannedSampleCount) > 0) {
            throw exception(PURCHASE_IN_QUALITY_SAMPLE_COUNT_INVALID_LOCAL, qualityItemId);
        }
    }

    /**
     * 校验单轮检验结果。
     */
    private void validateRoundCount(Long qualityItemId, BigDecimal sampleCount,
                                    BigDecimal roundPassCount, BigDecimal roundRejectCount) {
        validateCount(qualityItemId, sampleCount, roundPassCount, roundRejectCount);
    }

    /**
     * 校验初检不良明细。
     */
    private void validateFirstCheckDefects(Long qualityItemId, BigDecimal rejectCount,
                                           List<ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect> defects) {
        List<BigDecimal> defectCounts = defects == null ? null
                : convertList(defects, ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect::getDefectCount);
        validateDefectCount(qualityItemId, rejectCount, defectCounts);
    }

    /**
     * 校验复检不良明细。
     */
    private void validateRecheckDefects(Long qualityItemId, BigDecimal rejectCount,
                                        List<ErpPurchaseInQualitySubmitRecheckReqVO.Defect> defects) {
        List<BigDecimal> defectCounts = defects == null ? null
                : convertList(defects, ErpPurchaseInQualitySubmitRecheckReqVO.Defect::getDefectCount);
        validateDefectCount(qualityItemId, rejectCount, defectCounts);
    }

    /**
     * 校验不良原因数量汇总。
     */
    private void validateDefectCount(Long qualityItemId, BigDecimal rejectCount, List<BigDecimal> defectCounts) {
        BigDecimal safeRejectCount = ObjectUtil.defaultIfNull(rejectCount, BigDecimal.ZERO);
        if (safeRejectCount.compareTo(BigDecimal.ZERO) == 0) {
            if (CollUtil.isEmpty(defectCounts)) {
                return;
            }
            BigDecimal totalDefectCount = defectCounts.stream()
                    .map(defectCount -> ObjectUtil.defaultIfNull(defectCount, BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalDefectCount.compareTo(BigDecimal.ZERO) != 0) {
                throw exception(PURCHASE_IN_QUALITY_DEFECT_COUNT_INVALID_LOCAL, qualityItemId);
            }
            return;
        }

        if (CollUtil.isEmpty(defectCounts)) {
            throw exception(PURCHASE_IN_QUALITY_DEFECT_REQUIRED_LOCAL, qualityItemId);
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
            throw exception(PURCHASE_IN_QUALITY_DEFECT_COUNT_INVALID_LOCAL, qualityItemId);
        }
    }

    /**
     * 写入初检不良明细。
     */
    private void insertFirstCheckDefects(Long roundId, Long qualityId, ErpPurchaseInQualityItemDO qualityItem,
                                         List<ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect> defects) {
        if (CollUtil.isEmpty(defects)) {
            return;
        }
        List<ErpPurchaseInQualityDefectDO> defectDOList = convertList(defects, defect ->
                new ErpPurchaseInQualityDefectDO()
                        .setQualityId(qualityId)
                        .setRoundId(roundId)
                        .setQualityItemId(qualityItem.getId())
                        .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                        .setDefectReasonId(defect.getDefectReasonId())
                        .setDefectReasonName(defect.getDefectReasonName())
                        .setDefectCount(defect.getDefectCount())
                        .setRemark(defect.getDefectRemark()));
        erpPurchaseInQualityDefectMapper.insertBatch(defectDOList);
    }

    /**
     * 写入复检不良明细。
     */
    private void insertRecheckDefects(Long roundId, Long qualityId, ErpPurchaseInQualityItemDO qualityItem,
                                      List<ErpPurchaseInQualitySubmitRecheckReqVO.Defect> defects) {
        if (CollUtil.isEmpty(defects)) {
            return;
        }
        List<ErpPurchaseInQualityDefectDO> defectDOList = convertList(defects, defect ->
                new ErpPurchaseInQualityDefectDO()
                        .setQualityId(qualityId)
                        .setRoundId(roundId)
                        .setQualityItemId(qualityItem.getId())
                        .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                        .setDefectReasonId(defect.getDefectReasonId())
                        .setDefectReasonName(defect.getDefectReasonName())
                        .setDefectCount(defect.getDefectCount())
                        .setRemark(defect.getDefectRemark()));
        erpPurchaseInQualityDefectMapper.insertBatch(defectDOList);
    }

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
        ErpPurchaseInQualityResultEnum result = resolveQualityResult(totalPassCount, totalRejectCount);
        ErpQaStatusEnum qaStatus = resolveQaStatus(result);
        LocalDateTime checkTime = LocalDateTime.now();
        BigDecimal stockInCount = ObjectUtil.defaultIfNull(purchaseIn.getStockInCount(), BigDecimal.ZERO);
        Integer stockInStatus = resolveStockInStatus(totalPassCount, stockInCount);
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
            ErpTransactionUtils.afterCommit(() -> sendQualityFinishedNotify(quality, purchaseIn, result,
                    totalPassCount, totalRejectCount, stockInStatus));
        }
    }

    private void sendAssignCheckerNotify(ErpPurchaseInQualityDO quality, Long assignedCheckerUserId) {
        String purchaseInNo = quality.getPurchaseInNo();
        if (purchaseInNo == null) {
            purchaseInNo = getRequiredPurchaseIn(quality.getPurchaseInId()).getNo();
        }
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("qualityNo", quality.getNo());
        templateParams.put("purchaseInNo", purchaseInNo);

        NotifySendSingleToUserReqDTO notifyReqDTO = new NotifySendSingleToUserReqDTO();
        notifyReqDTO.setUserId(assignedCheckerUserId);
        notifyReqDTO.setTemplateCode(IQC_ASSIGN_NOTIFY_TEMPLATE_CODE);
        notifyReqDTO.setTemplateParams(templateParams);
        try {
            notifyMessageSendApi.sendSingleMessageToAdmin(notifyReqDTO);
        } catch (Exception ex) {
            log.error("[sendAssignCheckerNotify][qualityId({}) assignedCheckerUserId({}) notify failed]",
                    quality.getId(), assignedCheckerUserId, ex);
        }
    }

    private void sendQualityFinishedNotify(ErpPurchaseInQualityDO quality, ErpPurchaseInDO purchaseIn,
                                           ErpPurchaseInQualityResultEnum result,
                                           BigDecimal totalPassCount, BigDecimal totalRejectCount,
                                           Integer stockInStatus) {
        Set<Long> receiveUserIds = new LinkedHashSet<>();
        addUserId(receiveUserIds, parseUserId(purchaseIn.getCreator()));
        if (result != ErpPurchaseInQualityResultEnum.REJECTED) {
            receiveUserIds.addAll(getUserIdsByPermission(PURCHASE_IN_CONFIRM_STOCK_PERMISSION));
        }
        if (CollUtil.isEmpty(receiveUserIds)) {
            return;
        }

        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("qualityNo", quality.getNo());
        templateParams.put("purchaseInNo", purchaseIn.getNo());
        templateParams.put("qaResult", result.getName());
        templateParams.put("passCount", totalPassCount);
        templateParams.put("rejectCount", totalRejectCount);
        templateParams.put("stockInStatus", stockInStatus);

        String templateCode = result == ErpPurchaseInQualityResultEnum.REJECTED
                ? IQC_RECHECK_REJECTED_NOTIFY_TEMPLATE_CODE
                : IQC_STOCK_IN_READY_NOTIFY_TEMPLATE_CODE;
        for (Long receiveUserId : receiveUserIds) {
            NotifySendSingleToUserReqDTO notifyReqDTO = new NotifySendSingleToUserReqDTO();
            notifyReqDTO.setUserId(receiveUserId);
            notifyReqDTO.setTemplateCode(templateCode);
            notifyReqDTO.setTemplateParams(templateParams);
            try {
                notifyMessageSendApi.sendSingleMessageToAdmin(notifyReqDTO);
            } catch (Exception ex) {
                log.error("[sendQualityFinishedNotify][qualityId({}) purchaseInId({}) receiveUserId({}) notify failed]",
                        quality.getId(), purchaseIn.getId(), receiveUserId, ex);
            }
        }
    }

    private Set<Long> getUserIdsByPermission(String permission) {
        List<Long> menuIds = menuService.getMenuIdListByPermissionFromCache(permission);
        if (CollUtil.isEmpty(menuIds)) {
            return Set.of();
        }
        Set<Long> roleIds = new LinkedHashSet<>();
        menuIds.forEach(menuId -> roleIds.addAll(permissionService.getMenuRoleIdListByMenuIdFromCache(menuId)));
        if (CollUtil.isEmpty(roleIds)) {
            return Set.of();
        }
        return new LinkedHashSet<>(permissionService.getUserRoleIdListByRoleId(roleIds));
    }

    private void addUserId(Set<Long> userIds, Long userId) {
        if (userId != null) {
            userIds.add(userId);
        }
    }

    private Long parseUserId(String value) {
        return StrUtil.isNumeric(value) ? Long.valueOf(value) : null;
    }

    /**
     * 明细结果：0 不合格，0 之外区分全合格和部分合格。
     */
    private Integer resolveItemResult(BigDecimal qaPassCount, BigDecimal qaRejectCount) {
        if (ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.REJECTED.getStatus();
        }
        if (ObjectUtil.defaultIfNull(qaRejectCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.PASSED.getStatus();
        }
        return ErpPurchaseInQualityResultEnum.PARTIAL.getStatus();
    }

    /**
     * 汇总单据结果。
     */
    private ErpPurchaseInQualityResultEnum resolveQualityResult(BigDecimal qaPassCount, BigDecimal qaRejectCount) {
        if (ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.REJECTED;
        }
        if (ObjectUtil.defaultIfNull(qaRejectCount, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0) {
            return ErpPurchaseInQualityResultEnum.PASSED;
        }
        return ErpPurchaseInQualityResultEnum.PARTIAL;
    }

    /**
     * 质检结果映射到采购入库 QA 状态。
     */
    private ErpQaStatusEnum resolveQaStatus(ErpPurchaseInQualityResultEnum result) {
        return switch (result) {
            case PARTIAL -> ErpQaStatusEnum.PARTIAL;
            case PASSED -> ErpQaStatusEnum.PASSED;
            case REJECTED -> ErpQaStatusEnum.REJECTED;
            default -> throw new IllegalStateException("Unsupported quality result: " + result);
        };
    }

    /**
     * 只要存在可入库合格数，就保留待入库状态。
     */
    private Integer resolveStockInStatus(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal safeQaPassCount = ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO);
        BigDecimal safeStockInCount = ObjectUtil.defaultIfNull(stockInCount, BigDecimal.ZERO);
        if (safeQaPassCount.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpPurchaseInStockInStatusEnum.NO_NEED_STOCK_IN.getStatus();
        }
        if (safeStockInCount.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus();
        }
        if (safeStockInCount.compareTo(safeQaPassCount) < 0) {
            return ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus();
        }
        return ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus();
    }

    /**
     * 兼容旧页面时，允许草稿态和首检中态直接提交。
     */
    private boolean canSubmit(Integer status) {
        return ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.DRAFT.getStatus())
                || ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus());
    }

    /**
     * 仅允许在首检中、待复检、复检中指派或改派质检人。
     */
    private boolean canAssignChecker(Integer status) {
        return ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())
                || ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.WAIT_RECHECK.getStatus())
                || ObjectUtil.equal(status, ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus());
    }

    /**
     * 校验是否已经指派质检人。
     */
    private void validateAssignedCheckerExists(ErpPurchaseInQualityDO quality) {
        if (quality.getAssignedCheckerUserId() == null) {
            throw exception(PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_REQUIRED);
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

    private ErpPurchaseInDO getRequiredPurchaseIn(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(purchaseInId);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

    private ErpPurchaseInQualityDO getRequiredPurchaseInQuality(Long id) {
        ErpPurchaseInQualityDO quality = erpPurchaseInQualityMapper.selectById(id);
        if (quality == null) {
            throw exception(PURCHASE_IN_QUALITY_ORDER_NOT_EXISTS);
        }
        return quality;
    }

}
