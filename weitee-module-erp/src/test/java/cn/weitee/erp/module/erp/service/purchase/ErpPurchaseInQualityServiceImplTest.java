package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityAssignCheckerReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityStartRecheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityDefectMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityRoundMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.weitee.erp.module.system.api.permission.PermissionApi;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.service.permission.MenuService;
import cn.weitee.erp.module.system.service.permission.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErpPurchaseInQualityServiceImplTest {

    private final AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInItemDO>> purchaseInItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<ErpPurchaseInQualityDO> qualityRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInQualityItemDO>> qualityItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInQualityRoundDO>> firstRoundListRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInQualityRoundDO>> insertedRoundsRef = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<List<ErpPurchaseInQualityDefectDO>> insertedDefectsRef = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<ErpPurchaseInQualityDO> insertedQualityRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInQualityItemDO>> insertedQualityItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<ErpPurchaseInQualityDO> updatedQualityRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInQualityItemDO>> updatedQualityItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInItemDO>> updatedPurchaseInItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<ErpPurchaseInDO> updatedPurchaseInRef = new AtomicReference<>();
    private final AtomicReference<Boolean> recheckRoundExistsRef = new AtomicReference<>(false);
    private final AtomicReference<List<Long>> validatedUserIdsRef = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<Boolean> superAdminRef = new AtomicReference<>(false);
    private final AtomicReference<List<NotifySendSingleToUserReqDTO>> notifyReqListRef = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<List<Long>> stockInMenuIdsRef = new AtomicReference<>(List.of(2679L));
    private final AtomicReference<Set<Long>> stockInRoleIdsRef = new AtomicReference<>(Set.of(901L));
    private final AtomicReference<Set<Long>> stockInUserIdsRef = new AtomicReference<>(Set.of(188L, 199L));

    private ErpPurchaseInQualityServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpPurchaseInQualityServiceImpl();
        purchaseInRef.set(null);
        purchaseInItemsRef.set(List.of());
        qualityRef.set(null);
        qualityItemsRef.set(List.of());
        firstRoundListRef.set(List.of());
        insertedRoundsRef.set(new ArrayList<>());
        insertedDefectsRef.set(new ArrayList<>());
        insertedQualityRef.set(null);
        insertedQualityItemsRef.set(List.of());
        updatedQualityRef.set(null);
        updatedQualityItemsRef.set(List.of());
        updatedPurchaseInItemsRef.set(List.of());
        updatedPurchaseInRef.set(null);
        recheckRoundExistsRef.set(false);
        validatedUserIdsRef.set(new ArrayList<>());
        superAdminRef.set(false);
        notifyReqListRef.set(new ArrayList<>());
        stockInMenuIdsRef.set(List.of(2679L));
        stockInRoleIdsRef.set(Set.of(901L));
        stockInUserIdsRef.set(Set.of(188L, 199L));

        ErpPurchaseInMapper purchaseInMapper = createPurchaseInMapperProxy();
        ErpPurchaseInItemMapper purchaseInItemMapper = createPurchaseInItemMapperProxy();
        ErpPurchaseInQualityMapper purchaseInQualityMapper = createPurchaseInQualityMapperProxy();
        ErpPurchaseInQualityItemMapper purchaseInQualityItemMapper = createPurchaseInQualityItemMapperProxy();
        ErpPurchaseInQualityRoundMapper purchaseInQualityRoundMapper = createPurchaseInQualityRoundMapperProxy();
        ErpPurchaseInQualityDefectMapper purchaseInQualityDefectMapper = createPurchaseInQualityDefectMapperProxy();
        AdminUserApi adminUserApi = createAdminUserApiProxy();
        PermissionApi permissionApi = createPermissionApiProxy();
        MenuService menuService = createMenuServiceProxy();
        PermissionService permissionService = createPermissionServiceProxy();
        NotifyMessageSendApi notifyMessageSendApi = createNotifyMessageSendApiProxy();

        setField(service, "erpPurchaseInMapper", purchaseInMapper);
        setField(service, "erpPurchaseInItemMapper", purchaseInItemMapper);
        setField(service, "erpPurchaseInQualityMapper", purchaseInQualityMapper);
        setField(service, "erpPurchaseInQualityItemMapper", purchaseInQualityItemMapper);
        setField(service, "erpPurchaseInQualityRoundMapper", purchaseInQualityRoundMapper);
        setField(service, "adminUserApi", adminUserApi);
        setField(service, "permissionApi", permissionApi);
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "CGZJ20260410000001";
            }
        });

        ErpPurchaseInQualityValidationHelper validationHelper = new ErpPurchaseInQualityValidationHelper();

        ErpPurchaseInQualityDefectHelper defectHelper = new ErpPurchaseInQualityDefectHelper();
        setField(defectHelper, "erpPurchaseInQualityDefectMapper", purchaseInQualityDefectMapper);

        ErpPurchaseInQualityQueryHelper queryHelper = new ErpPurchaseInQualityQueryHelper();
        setField(queryHelper, "erpPurchaseInMapper", purchaseInMapper);
        setField(queryHelper, "erpPurchaseInQualityMapper", purchaseInQualityMapper);
        setField(queryHelper, "erpPurchaseInQualityItemMapper", purchaseInQualityItemMapper);
        setField(queryHelper, "erpPurchaseInQualityRoundMapper", purchaseInQualityRoundMapper);
        setField(queryHelper, "erpPurchaseInQualityDefectMapper", purchaseInQualityDefectMapper);

        ErpPurchaseInQualityNotificationHelper notificationHelper = new ErpPurchaseInQualityNotificationHelper();
        setField(notificationHelper, "notifyMessageSendApi", notifyMessageSendApi);
        setField(notificationHelper, "menuService", menuService);
        setField(notificationHelper, "permissionService", permissionService);

        setField(service, "validationHelper", validationHelper);
        setField(service, "defectHelper", defectHelper);
        setField(service, "queryHelper", queryHelper);
        setField(service, "notificationHelper", notificationHelper);
    }

    @Test
    void createQualityOrderIfAbsent_shouldCreateQualityOrderAndCopyItems() {
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        purchaseInItemsRef.set(List.of(purchaseInItem(11L, "6"), purchaseInItem(12L, "4")));

        Long qualityId = service.createQualityOrderIfAbsent(1L);

        assertThat(qualityId).isEqualTo(66L);
        assertThat(insertedQualityRef.get().getStatus()).isEqualTo(ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus());
        assertThat(insertedQualityRef.get().getCurrentRoundNo()).isEqualTo(1);
        assertThat(insertedQualityRef.get().getRecheckRequired()).isFalse();
        assertThat(insertedQualityItemsRef.get()).hasSize(2);
        assertThat(insertedQualityItemsRef.get().get(0).getSampleCount()).isEqualByComparingTo("6");
        assertThat(insertedQualityItemsRef.get().get(1).getSampleCount()).isEqualByComparingTo("4");
    }

    @Test
    void assignChecker_shouldUpdateAssignedChecker() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus()));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));

        service.assignChecker(1L, assignCheckerReq(66L, 188L));

        assertThat(validatedUserIdsRef.get()).containsExactly(188L);
        assertThat(updatedQualityRef.get().getAssignedCheckerUserId()).isEqualTo(188L);
        assertThat(updatedQualityRef.get().getAssignedCheckerTime()).isNotNull();
        assertThat(notifyReqListRef.get()).hasSize(1);
        assertThat(notifyReqListRef.get().get(0).getUserId()).isEqualTo(188L);
        assertThat(notifyReqListRef.get().get(0).getTemplateCode()).isEqualTo("erp_iqc_checker_assigned");
        assertThat(notifyReqListRef.get().get(0).getTemplateParams())
                .containsEntry("qualityNo", "CGZJ-001")
                .containsEntry("purchaseInNo", "CGRK-001");
    }

    @Test
    void submitFirstCheck_shouldRejectWhenCheckerNotAssigned() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())
                .setAssignedCheckerUserId(188L));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "6", "6")));

        assertThatThrownBy(() -> service.submitFirstCheck(99L, firstCheckReq(66L,
                firstCheckItem(101L, "6", "6", "0", null))))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void submitFirstCheck_shouldFinishWhenAllPassed() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus()));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "6", "6"), qualityItem(102L, 12L, "4", "4")));

        service.submitFirstCheck(99L, firstCheckReq(66L,
                firstCheckItem(101L, "6", "6", "0", null),
                firstCheckItem(102L, "4", "4", "0", null)));

        assertThat(insertedRoundsRef.get()).hasSize(2);
        assertThat(insertedDefectsRef.get()).isEmpty();
        assertThat(updatedQualityRef.get().getStatus()).isEqualTo(ErpPurchaseInQualityStatusEnum.DONE.getStatus());
        assertThat(updatedQualityRef.get().getResult()).isEqualTo(ErpPurchaseInQualityResultEnum.PASSED.getStatus());
        assertThat(updatedPurchaseInRef.get().getQaStatus()).isEqualTo(ErpQaStatusEnum.PASSED.getStatus());
        assertThat(updatedPurchaseInRef.get().getStockInStatus()).isEqualTo(ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus());
        assertThat(updatedQualityItemsRef.get()).hasSize(2);
        assertThat(updatedPurchaseInItemsRef.get()).hasSize(2);
    }

    @Test
    void submitFirstCheck_shouldWaitRecheckWhenHasReject() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus()));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "10", "5")));

        service.submitFirstCheck(99L, firstCheckReq(66L,
                firstCheckItem(101L, "5", "4", "1", firstCheckDefect(9001L, "外观不良", "1"))));

        assertThat(insertedRoundsRef.get()).hasSize(1);
        assertThat(insertedDefectsRef.get()).hasSize(1);
        assertThat(updatedQualityRef.get().getStatus()).isEqualTo(ErpPurchaseInQualityStatusEnum.WAIT_RECHECK.getStatus());
        assertThat(updatedQualityRef.get().getRecheckRequired()).isTrue();
        assertThat(updatedQualityItemsRef.get()).isEmpty();
        assertThat(updatedPurchaseInRef.get()).isNull();
    }

    @Test
    void recheckFlow_shouldStartAndSubmitRecheck() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.WAIT_RECHECK.getStatus()));

        service.startRecheck(99L, startRecheckReq(66L, "首检不良需复判"));

        assertThat(updatedQualityRef.get().getStatus()).isEqualTo(ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus());
        assertThat(updatedQualityRef.get().getRecheckReason()).isEqualTo("首检不良需复判");

        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus()));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "10", "10")));
        firstRoundListRef.set(List.of(new ErpPurchaseInQualityRoundDO().setQualityItemId(101L).setRejectCount(new BigDecimal("2"))));
        recheckRoundExistsRef.set(false);

        service.submitRecheck(99L, recheckReq(66L,
                recheckItem(101L, "2", "1", "1", "9", "1", recheckDefect(9002L, "尺寸不良", "1"))));

        assertThat(insertedRoundsRef.get()).hasSize(1);
        assertThat(insertedRoundsRef.get().get(0).getRoundNo()).isEqualTo(2);
        assertThat(insertedDefectsRef.get()).hasSize(1);
        assertThat(updatedQualityRef.get().getStatus()).isEqualTo(ErpPurchaseInQualityStatusEnum.DONE.getStatus());
        assertThat(updatedQualityRef.get().getResult()).isEqualTo(ErpPurchaseInQualityResultEnum.PARTIAL.getStatus());
        assertThat(updatedPurchaseInRef.get().getQaStatus()).isEqualTo(ErpQaStatusEnum.PARTIAL.getStatus());
        assertThat(notifyReqListRef.get())
                .extracting(NotifySendSingleToUserReqDTO::getUserId)
                .containsExactlyInAnyOrder(188L, 199L);
        assertThat(notifyReqListRef.get())
                .extracting(NotifySendSingleToUserReqDTO::getTemplateCode)
                .containsOnly("erp_iqc_stock_in_ready");
    }

    @Test
    void submitRecheck_shouldRejectWhenSampleExceedsFirstRejectCount() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus()));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "10", "10")));
        firstRoundListRef.set(List.of(new ErpPurchaseInQualityRoundDO().setQualityItemId(101L).setRejectCount(BigDecimal.ONE)));

        assertThatThrownBy(() -> service.submitRecheck(99L, recheckReq(66L,
                recheckItem(101L, "2", "1", "1", "9", "1", recheckDefect(1L, "尺寸不良", "1")))))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void submitRecheck_shouldNotifyPurchaseCreatorOnlyWhenRejected() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.RECHECKING.getStatus()));
        ErpPurchaseInDO purchaseIn = purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus());
        purchaseIn.setCreator("188");
        purchaseInRef.set(purchaseIn);
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "10", "10")));
        firstRoundListRef.set(List.of(new ErpPurchaseInQualityRoundDO().setQualityItemId(101L).setRejectCount(new BigDecimal("2"))));
        recheckRoundExistsRef.set(false);

        service.submitRecheck(99L, recheckReq(66L,
                recheckItem(101L, "2", "0", "2", "0", "10", recheckDefect(9002L, "灏哄涓嶈壇", "2"))));

        assertThat(updatedQualityRef.get().getResult()).isEqualTo(ErpPurchaseInQualityResultEnum.REJECTED.getStatus());
        assertThat(updatedPurchaseInRef.get().getQaStatus()).isEqualTo(ErpQaStatusEnum.REJECTED.getStatus());
        assertThat(notifyReqListRef.get())
                .extracting(NotifySendSingleToUserReqDTO::getUserId)
                .containsExactly(188L);
        assertThat(notifyReqListRef.get())
                .extracting(NotifySendSingleToUserReqDTO::getTemplateCode)
                .containsOnly("erp_iqc_recheck_rejected");
    }

    @Test
    void submitFirstCheck_shouldAllowSuperAdminFallback() {
        qualityRef.set(quality(66L, 1L, ErpPurchaseInQualityStatusEnum.FIRST_CHECKING.getStatus())
                .setAssignedCheckerUserId(188L));
        purchaseInRef.set(purchaseIn(1L, ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
        qualityItemsRef.set(List.of(qualityItem(101L, 11L, "6", "6")));
        superAdminRef.set(true);

        service.submitFirstCheck(99L, firstCheckReq(66L, firstCheckItem(101L, "6", "6", "0", null)));

        assertThat(insertedRoundsRef.get()).hasSize(1);
        assertThat(updatedQualityRef.get().getStatus()).isEqualTo(ErpPurchaseInQualityStatusEnum.DONE.getStatus());
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private ErpPurchaseInMapper createPurchaseInMapperProxy() {
        return createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedPurchaseInRef.set((ErpPurchaseInDO) args[0]);
                return 1;
            }
            return null;
        });
    }

    private ErpPurchaseInItemMapper createPurchaseInItemMapperProxy() {
        return createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectListByInId".equals(methodName)) {
                return purchaseInItemsRef.get();
            }
            if ("updateBatch".equals(methodName)) {
                updatedPurchaseInItemsRef.set(new ArrayList<>((Collection<ErpPurchaseInItemDO>) args[0]));
                return true;
            }
            return null;
        });
    }

    private ErpPurchaseInQualityMapper createPurchaseInQualityMapperProxy() {
        return createProxy(ErpPurchaseInQualityMapper.class, (methodName, args) -> {
            if ("selectByPurchaseInId".equals(methodName) || "selectById".equals(methodName)) {
                return qualityRef.get();
            }
            if ("insert".equals(methodName)) {
                ErpPurchaseInQualityDO quality = (ErpPurchaseInQualityDO) args[0];
                if (quality.getId() == null) {
                    quality.setId(66L);
                }
                insertedQualityRef.set(quality);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedQualityRef.set((ErpPurchaseInQualityDO) args[0]);
                return 1;
            }
            return null;
        });
    }

    private ErpPurchaseInQualityItemMapper createPurchaseInQualityItemMapperProxy() {
        return createProxy(ErpPurchaseInQualityItemMapper.class, (methodName, args) -> {
            if ("selectListByQualityId".equals(methodName)) {
                return qualityItemsRef.get();
            }
            if ("insertBatch".equals(methodName)) {
                insertedQualityItemsRef.set(new ArrayList<>((Collection<ErpPurchaseInQualityItemDO>) args[0]));
                return true;
            }
            if ("updateBatch".equals(methodName)) {
                updatedQualityItemsRef.set(new ArrayList<>((Collection<ErpPurchaseInQualityItemDO>) args[0]));
                return true;
            }
            return null;
        });
    }

    private ErpPurchaseInQualityRoundMapper createPurchaseInQualityRoundMapperProxy() {
        return createProxy(ErpPurchaseInQualityRoundMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpPurchaseInQualityRoundDO round = (ErpPurchaseInQualityRoundDO) args[0];
                if (round.getId() == null) {
                    round.setId((long) (insertedRoundsRef.get().size() + 1));
                }
                List<ErpPurchaseInQualityRoundDO> rounds = new ArrayList<>(insertedRoundsRef.get());
                rounds.add(round);
                insertedRoundsRef.set(rounds);
                return 1;
            }
            if ("existsByQualityIdAndRoundNo".equals(methodName)) {
                return (Integer) args[1] == 2 && recheckRoundExistsRef.get();
            }
            if ("selectListByQualityIdAndRoundNo".equals(methodName)) {
                return (Integer) args[1] == 1 ? firstRoundListRef.get() : List.of();
            }
            return List.of();
        });
    }

    private ErpPurchaseInQualityDefectMapper createPurchaseInQualityDefectMapperProxy() {
        return createProxy(ErpPurchaseInQualityDefectMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedDefectsRef.set(new ArrayList<>((Collection<ErpPurchaseInQualityDefectDO>) args[0]));
                return true;
            }
            return List.of();
        });
    }

    private AdminUserApi createAdminUserApiProxy() {
        return createProxy(AdminUserApi.class, (methodName, args) -> {
            if ("validateUser".equals(methodName)) {
                List<Long> userIds = new ArrayList<>(validatedUserIdsRef.get());
                userIds.add((Long) args[0]);
                validatedUserIdsRef.set(userIds);
                return null;
            }
            return null;
        });
    }

    private PermissionApi createPermissionApiProxy() {
        return createProxy(PermissionApi.class, (methodName, args) -> {
            if ("hasAnyRoles".equals(methodName)) {
                return superAdminRef.get();
            }
            return null;
        });
    }

    private MenuService createMenuServiceProxy() {
        return createProxy(MenuService.class, (methodName, args) -> {
            if ("getMenuIdListByPermissionFromCache".equals(methodName)) {
                return stockInMenuIdsRef.get();
            }
            return null;
        });
    }

    private PermissionService createPermissionServiceProxy() {
        return createProxy(PermissionService.class, (methodName, args) -> {
            if ("getMenuRoleIdListByMenuIdFromCache".equals(methodName)) {
                return stockInRoleIdsRef.get();
            }
            if ("getUserRoleIdListByRoleId".equals(methodName)) {
                return stockInUserIdsRef.get();
            }
            return null;
        });
    }

    private NotifyMessageSendApi createNotifyMessageSendApiProxy() {
        return createProxy(NotifyMessageSendApi.class, (methodName, args) -> {
            if ("sendSingleMessageToAdmin".equals(methodName)) {
                List<NotifySendSingleToUserReqDTO> notifyReqList = new ArrayList<>(notifyReqListRef.get());
                notifyReqList.add((NotifySendSingleToUserReqDTO) args[0]);
                notifyReqListRef.set(notifyReqList);
                return 1L;
            }
            return null;
        });
    }

    private ErpPurchaseInDO purchaseIn(Long id, Integer status, Integer qaStatus) {
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO().setId(id).setNo("CGRK-001").setStatus(status).setQaStatus(qaStatus);
        purchaseIn.setCreator("188");
        return purchaseIn;
    }

    private ErpPurchaseInItemDO purchaseInItem(Long id, String count) {
        return new ErpPurchaseInItemDO().setId(id).setInId(1L).setProductId(1000L + id).setWarehouseId(2000L + id)
                .setCount(new BigDecimal(count)).setQaPassCount(BigDecimal.ZERO).setQaRejectCount(BigDecimal.ZERO);
    }

    private ErpPurchaseInQualityDO quality(Long id, Long purchaseInId, Integer status) {
        return new ErpPurchaseInQualityDO().setId(id).setNo("CGZJ-001").setPurchaseInId(purchaseInId)
                .setPurchaseInNo("CGRK-001")
                .setStatus(status).setResult(ErpPurchaseInQualityResultEnum.TO_DECIDE.getStatus())
                .setAssignedCheckerUserId(99L);
    }

    private ErpPurchaseInQualityItemDO qualityItem(Long id, Long purchaseInItemId, String count, String sampleCount) {
        return new ErpPurchaseInQualityItemDO().setId(id).setQualityId(66L).setPurchaseInItemId(purchaseInItemId)
                .setProductId(1000L + id).setWarehouseId(2000L + id).setCount(new BigDecimal(count))
                .setSampleCount(new BigDecimal(sampleCount)).setQaPassCount(BigDecimal.ZERO).setQaRejectCount(BigDecimal.ZERO);
    }

    private ErpPurchaseInQualitySubmitFirstCheckReqVO firstCheckReq(Long id, ErpPurchaseInQualitySubmitFirstCheckReqVO.Item... items) {
        ErpPurchaseInQualitySubmitFirstCheckReqVO reqVO = new ErpPurchaseInQualitySubmitFirstCheckReqVO();
        reqVO.setId(id);
        reqVO.setRemark("首检");
        reqVO.setItems(List.of(items));
        return reqVO;
    }

    private ErpPurchaseInQualityAssignCheckerReqVO assignCheckerReq(Long id, Long assignedCheckerUserId) {
        ErpPurchaseInQualityAssignCheckerReqVO reqVO = new ErpPurchaseInQualityAssignCheckerReqVO();
        reqVO.setId(id);
        reqVO.setAssignedCheckerUserId(assignedCheckerUserId);
        return reqVO;
    }

    private ErpPurchaseInQualitySubmitFirstCheckReqVO.Item firstCheckItem(Long qualityItemId, String sampleCount, String passCount,
                                                                          String rejectCount, ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect defect) {
        ErpPurchaseInQualitySubmitFirstCheckReqVO.Item item = new ErpPurchaseInQualitySubmitFirstCheckReqVO.Item();
        item.setQualityItemId(qualityItemId);
        item.setSampleCount(new BigDecimal(sampleCount));
        item.setRoundPassCount(new BigDecimal(passCount));
        item.setRoundRejectCount(new BigDecimal(rejectCount));
        item.setRoundRemark("首检明细");
        item.setDefects(defect == null ? null : List.of(defect));
        return item;
    }

    private ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect firstCheckDefect(Long reasonId, String reasonName, String defectCount) {
        ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect defect = new ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect();
        defect.setDefectReasonId(reasonId);
        defect.setDefectReasonName(reasonName);
        defect.setDefectCount(new BigDecimal(defectCount));
        return defect;
    }

    private ErpPurchaseInQualityStartRecheckReqVO startRecheckReq(Long id, String reason) {
        ErpPurchaseInQualityStartRecheckReqVO reqVO = new ErpPurchaseInQualityStartRecheckReqVO();
        reqVO.setId(id);
        reqVO.setRecheckReason(reason);
        return reqVO;
    }

    private ErpPurchaseInQualitySubmitRecheckReqVO recheckReq(Long id, ErpPurchaseInQualitySubmitRecheckReqVO.Item... items) {
        ErpPurchaseInQualitySubmitRecheckReqVO reqVO = new ErpPurchaseInQualitySubmitRecheckReqVO();
        reqVO.setId(id);
        reqVO.setRemark("复检");
        reqVO.setItems(List.of(items));
        return reqVO;
    }

    private ErpPurchaseInQualitySubmitRecheckReqVO.Item recheckItem(Long qualityItemId, String sampleCount, String passCount,
                                                                    String rejectCount, String finalPassCount, String finalRejectCount,
                                                                    ErpPurchaseInQualitySubmitRecheckReqVO.Defect defect) {
        ErpPurchaseInQualitySubmitRecheckReqVO.Item item = new ErpPurchaseInQualitySubmitRecheckReqVO.Item();
        item.setQualityItemId(qualityItemId);
        item.setSampleCount(new BigDecimal(sampleCount));
        item.setRoundPassCount(new BigDecimal(passCount));
        item.setRoundRejectCount(new BigDecimal(rejectCount));
        item.setFinalPassCount(new BigDecimal(finalPassCount));
        item.setFinalRejectCount(new BigDecimal(finalRejectCount));
        item.setRoundRemark("复检明细");
        item.setDefects(defect == null ? null : List.of(defect));
        return item;
    }

    private ErpPurchaseInQualitySubmitRecheckReqVO.Defect recheckDefect(Long reasonId, String reasonName, String defectCount) {
        ErpPurchaseInQualitySubmitRecheckReqVO.Defect defect = new ErpPurchaseInQualitySubmitRecheckReqVO.Defect();
        defect.setDefectReasonId(reasonId);
        defect.setDefectReasonName(reasonName);
        defect.setDefectCount(new BigDecimal(defectCount));
        return defect;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args) throws Throwable;
    }

}
