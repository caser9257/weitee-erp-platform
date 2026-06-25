package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerDiffConfigMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffSourceTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_BIZ_ITEM_DUPLICATE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_DEPRECIATION_SOURCE_ITEM_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceDualLedgerDiffConfigServiceImplTest {

    @Test
    void createDualLedgerDiffConfig_shouldInsertWhenValid() throws Exception {
        ErpFinanceDualLedgerDiffConfigServiceImpl service = new ErpFinanceDualLedgerDiffConfigServiceImpl();
        AtomicReference<ErpFinanceDualLedgerDiffConfigDO> insertedRef = new AtomicReference<>();

        setField(service, "erpFinanceDualLedgerDiffConfigMapper",
                createProxy(ErpFinanceDualLedgerDiffConfigMapper.class, (methodName, args) -> {
                    if ("selectByBizTypeAndDiffItemType".equals(methodName)) {
                        return null;
                    }
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualLedgerDiffConfigDO config = (ErpFinanceDualLedgerDiffConfigDO) args[0];
                        config.setId(201L);
                        insertedRef.set(config);
                        return 1;
                    }
                    return null;
                }));

        ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO = new ErpFinanceDualLedgerDiffConfigSaveReqVO();
        reqVO.setBizType(11);
        reqVO.setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setExternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setExternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setInternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setInternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("人工成本双账套差异口径");

        Long id = service.createDualLedgerDiffConfig(reqVO);

        assertEquals(201L, id);
        assertEquals(11, insertedRef.get().getBizType().intValue());
        assertEquals(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType(), insertedRef.get().getDiffItemType());
        assertEquals(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType(), insertedRef.get().getExternalSourceType());
        assertEquals(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType(),
                insertedRef.get().getInternalSourceValue());
    }

    @Test
    void createDualLedgerDiffConfig_shouldRejectDuplicateBizItem() throws Exception {
        ErpFinanceDualLedgerDiffConfigServiceImpl service = new ErpFinanceDualLedgerDiffConfigServiceImpl();

        setField(service, "erpFinanceDualLedgerDiffConfigMapper",
                createProxy(ErpFinanceDualLedgerDiffConfigMapper.class, (methodName, args) -> {
                    if ("selectByBizTypeAndDiffItemType".equals(methodName)) {
                        return new ErpFinanceDualLedgerDiffConfigDO().setId(9L).setBizType(11)
                                .setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
                    }
                    return null;
                }));

        ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO = buildBaseReqVO();
        reqVO.setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setExternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setExternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setInternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setInternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createDualLedgerDiffConfig(reqVO));

        assertEquals(FINANCE_DUAL_LEDGER_DIFF_CONFIG_BIZ_ITEM_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    void createDualLedgerDiffConfig_shouldAllowSameSourcePair() throws Exception {
        ErpFinanceDualLedgerDiffConfigServiceImpl service = new ErpFinanceDualLedgerDiffConfigServiceImpl();
        AtomicReference<ErpFinanceDualLedgerDiffConfigDO> insertedRef = new AtomicReference<>();
        setField(service, "erpFinanceDualLedgerDiffConfigMapper",
                createProxy(ErpFinanceDualLedgerDiffConfigMapper.class, (methodName, args) -> {
                    if ("selectByBizTypeAndDiffItemType".equals(methodName)) {
                        return null;
                    }
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualLedgerDiffConfigDO config = (ErpFinanceDualLedgerDiffConfigDO) args[0];
                        config.setId(301L);
                        insertedRef.set(config);
                        return 1;
                    }
                    return null;
                }));

        ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO = buildBaseReqVO();
        reqVO.setBizType(32);
        reqVO.setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType());
        reqVO.setExternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setExternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType());
        reqVO.setInternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setInternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType());
        reqVO.setCalculationType(1);
        reqVO.setRatio(new BigDecimal("1.5000"));

        Long id = service.createDualLedgerDiffConfig(reqVO);

        assertEquals(301L, id);
        assertEquals(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType(),
                insertedRef.get().getExternalSourceValue());
        assertEquals(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType(),
                insertedRef.get().getInternalSourceValue());
    }

    @Test
    void createDualLedgerDiffConfig_shouldAllowSameSourcePairWhenRatioDiffers() throws Exception {
        ErpFinanceDualLedgerDiffConfigServiceImpl service = new ErpFinanceDualLedgerDiffConfigServiceImpl();
        AtomicReference<ErpFinanceDualLedgerDiffConfigDO> insertedRef = new AtomicReference<>();
        setField(service, "erpFinanceDualLedgerDiffConfigMapper",
                createProxy(ErpFinanceDualLedgerDiffConfigMapper.class, (methodName, args) -> {
                    if ("selectByBizTypeAndDiffItemType".equals(methodName)) {
                        return null;
                    }
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualLedgerDiffConfigDO config = (ErpFinanceDualLedgerDiffConfigDO) args[0];
                        config.setId(302L);
                        insertedRef.set(config);
                        return 1;
                    }
                    return null;
                }));

        ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO = buildBaseReqVO();
        reqVO.setBizType(32);
        reqVO.setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setExternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setExternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setInternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setInternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setCalculationType(1);
        reqVO.setRatio(new BigDecimal("1.1500"));

        Long id = service.createDualLedgerDiffConfig(reqVO);

        assertEquals(302L, id);
        assertEquals(new BigDecimal("1.1500"), insertedRef.get().getRatio());
    }

    @Test
    void createDualLedgerDiffConfig_shouldRejectAssetDepreciationSourceForNonDepreciationItem() throws Exception {
        ErpFinanceDualLedgerDiffConfigServiceImpl service = new ErpFinanceDualLedgerDiffConfigServiceImpl();
        setField(service, "erpFinanceDualLedgerDiffConfigMapper",
                createProxy(ErpFinanceDualLedgerDiffConfigMapper.class, (methodName, args) -> null));

        ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO = buildBaseReqVO();
        reqVO.setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType());
        reqVO.setExternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.ASSET_DEPRECIATION.getType());
        reqVO.setExternalSourceValue(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createDualLedgerDiffConfig(reqVO));

        assertEquals(FINANCE_DUAL_LEDGER_DIFF_CONFIG_DEPRECIATION_SOURCE_ITEM_INVALID.getCode(), ex.getCode());
    }

    private ErpFinanceDualLedgerDiffConfigSaveReqVO buildBaseReqVO() {
        ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO = new ErpFinanceDualLedgerDiffConfigSaveReqVO();
        reqVO.setBizType(11);
        reqVO.setDiffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType());
        reqVO.setExternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType());
        reqVO.setExternalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType());
        reqVO.setInternalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.ASSET_DEPRECIATION.getType());
        reqVO.setInternalSourceValue(null);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("折旧双账套差异口径");
        return reqVO;
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

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
