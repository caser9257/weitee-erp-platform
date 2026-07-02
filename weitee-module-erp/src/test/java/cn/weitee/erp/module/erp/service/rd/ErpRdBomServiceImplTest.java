package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpRdBomServiceImplTest {

    private final AtomicReference<ErpRdBomDO> selectRdBomResult = new AtomicReference<>();
    private final AtomicReference<List<ErpRdBomDO>> selectRdBomListResult = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpRdBomItemDO>> selectRdBomItemsResult = new AtomicReference<>(List.of());
    private final AtomicReference<ErpRdBomDO> insertedRdBomRef = new AtomicReference<>();
    private final AtomicReference<ErpRdBomDO> updatedRdBomRef = new AtomicReference<>();
    private final AtomicReference<ErpBomDO> selectedManufacturingBomRef = new AtomicReference<>();
    private final AtomicReference<ErpBomDO> insertedManufacturingBomRef = new AtomicReference<>();
    private final AtomicReference<ErpBomDO> updatedManufacturingBomRef = new AtomicReference<>();
    private final List<ErpBomItemDO> insertedManufacturingItems = new ArrayList<>();
    private final List<Object> insertedManufacturingItemSubstitutes = new ArrayList<>();
    private final AtomicReference<List<Object>> selectedRdBomItemSubstitutesResult = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpRdBomItemDO>> insertedRdBomItemsRef = new AtomicReference<>(List.of());
    private final List<Object> insertedRdBomItemSubstitutes = new ArrayList<>();
    private final AtomicReference<List<ErpProductDO>> validProductsResult = new AtomicReference<>(List.of());

    private ErpRdBomServiceImpl rdBomService;

    @BeforeEach
    void setUp() throws Exception {
        rdBomService = new ErpRdBomServiceImpl();
        selectRdBomResult.set(null);
        selectRdBomListResult.set(List.of());
        selectRdBomItemsResult.set(List.of());
        insertedRdBomRef.set(null);
        updatedRdBomRef.set(null);
        selectedManufacturingBomRef.set(null);
        insertedManufacturingBomRef.set(null);
        updatedManufacturingBomRef.set(null);
        insertedManufacturingItems.clear();
        insertedManufacturingItemSubstitutes.clear();
        selectedRdBomItemSubstitutesResult.set(List.of());
        insertedRdBomItemsRef.set(List.of());
        insertedRdBomItemSubstitutes.clear();
        validProductsResult.set(List.of());
        setField(rdBomService, "rdBomMapper", createRdBomMapperProxy());
        setField(rdBomService, "rdBomItemMapper", createRdBomItemMapperProxy());
        setField(rdBomService, "bomMapper", createBomMapperProxy());
        setField(rdBomService, "bomItemMapper", createBomItemMapperProxy());
        setField(rdBomService, "rdBomItemSubstituteMapper", createRdBomItemSubstituteMapperProxy());
        setField(rdBomService, "bomItemSubstituteMapper", createBomItemSubstituteMapperProxy());
        setField(rdBomService, "productService", createProductServiceProxy());
    }

    @Test
    void updateRdBom_shouldResetPublishedStatusToDraft() {
        selectRdBomResult.set(new ErpRdBomDO().setId(9L).setStatus(1).setPublishedBomId(101L).setVersion("V1.0"));
        validProductsResult.set(List.of(new ErpProductDO().setId(10L).setUnitId(1000L), new ErpProductDO().setId(20L).setUnitId(2000L)));

        ErpRdBomSaveReqVO reqVO = new ErpRdBomSaveReqVO();
        reqVO.setId(9L);
        reqVO.setBomCode("RD-BOM-001");
        reqVO.setProductId(10L);
        reqVO.setVersion("A.2");
        reqVO.setRemark("edit");
        reqVO.setItems(List.of(newItem(20L, "2.5")));

        rdBomService.updateRdBom(reqVO);

        assertEquals(9L, updatedRdBomRef.get().getId());
        assertEquals(0, updatedRdBomRef.get().getStatus());
        assertEquals("V1.0", updatedRdBomRef.get().getVersion());
        assertEquals(101L, updatedRdBomRef.get().getPublishedBomId());
    }

    @Test
    void createRdBom_shouldIgnoreManualVersionAndKeepDraftWithoutVersion() {
        validProductsResult.set(List.of(new ErpProductDO().setId(10L).setUnitId(1000L), new ErpProductDO().setId(20L).setUnitId(2000L)));

        ErpRdBomSaveReqVO reqVO = new ErpRdBomSaveReqVO();
        reqVO.setBomCode("RD-BOM-NEW");
        reqVO.setProductId(10L);
        reqVO.setVersion("V9.0");
        reqVO.setRemark("create");
        reqVO.setItems(List.of(newItem(20L, "1.5")));

        Long id = rdBomService.createRdBom(reqVO);

        assertEquals(901L, id);
        assertNotNull(insertedRdBomRef.get());
        assertEquals(0, insertedRdBomRef.get().getStatus());
        assertNull(insertedRdBomRef.get().getVersion());
        assertEquals(1, insertedRdBomItemsRef.get().size());
        assertEquals(901L, insertedRdBomItemsRef.get().get(0).getBomId());
    }

    @Test
    void createRdBom_shouldPersistItemSubstitutes() throws Exception {
        validProductsResult.set(List.of(
                new ErpProductDO().setId(10L).setUnitId(1000L),
                new ErpProductDO().setId(20L).setUnitId(2000L),
                new ErpProductDO().setId(30L).setUnitId(3000L)));
        setField(rdBomService, "rdBomItemSubstituteMapper", createRdBomItemSubstituteMapperProxy());

        ErpRdBomSaveReqVO reqVO = new ErpRdBomSaveReqVO();
        reqVO.setBomCode("RD-BOM-NEW");
        reqVO.setProductId(10L);
        reqVO.setRemark("create");
        ErpRdBomSaveReqVO.Item item = newItem(20L, "1.5");
        Object substitute = newRdBomItemSubstitute(30L, 1, "1.000000");
        setField(substitute, "id", 888L);
        setItemSubstitutes(item, List.of(substitute));
        reqVO.setItems(List.of(item));

        rdBomService.createRdBom(reqVO);

        assertEquals(1, insertedRdBomItemSubstitutes.size());
        assertNull(readLongField(insertedRdBomItemSubstitutes.get(0), "id"));
        assertEquals(1001L, readLongField(insertedRdBomItemSubstitutes.get(0), "bomItemId"));
        assertEquals(30L, readLongField(insertedRdBomItemSubstitutes.get(0), "substituteMaterialId"));
        assertEquals(1, readIntegerField(insertedRdBomItemSubstitutes.get(0), "priority"));
        assertEquals(new BigDecimal("1.000000"), readBigDecimalField(insertedRdBomItemSubstitutes.get(0), "replaceRatio"));
    }

    @Test
    void publishRdBom_shouldCreateDisabledManufacturingBomDraftAndGenerateVersion() {
        selectRdBomResult.set(new ErpRdBomDO()
                .setId(5L)
                .setBomCode("RD-BOM-100")
                .setProductId(10L)
                .setStatus(0)
                .setRemark("rd"));
        selectRdBomListResult.set(List.of(
                new ErpRdBomDO().setId(1L).setProductId(10L).setVersion("V1.0"),
                new ErpRdBomDO().setId(2L).setProductId(10L).setVersion("V2.0"),
                new ErpRdBomDO().setId(5L).setProductId(10L).setVersion(null)
        ));
        selectRdBomItemsResult.set(List.of(new ErpRdBomItemDO()
                .setId(51L)
                .setBomId(5L)
                .setMaterialId(20L)
                .setMaterialType(1)
                .setUnitId(2000L)
                .setUsageQty(new BigDecimal("3"))
                .setLossRate(new BigDecimal("0.10"))
                .setLeadTimeDay(2)
                .setSort(1)
                .setRemark("item")));

        rdBomService.publishRdBom(5L);

        assertNotNull(insertedManufacturingBomRef.get());
        assertEquals(0, insertedManufacturingBomRef.get().getStatus());
        assertEquals(5L, insertedManufacturingBomRef.get().getSourceRdBomId());
        assertEquals("V3.0", insertedManufacturingBomRef.get().getVersion());
        assertEquals(1, insertedManufacturingItems.size());
        assertEquals(20L, insertedManufacturingItems.get(0).getMaterialId());
        assertEquals(1, updatedRdBomRef.get().getStatus());
        assertEquals("V3.0", updatedRdBomRef.get().getVersion());
        assertEquals(501L, updatedRdBomRef.get().getPublishedBomId());
        assertNotNull(updatedRdBomRef.get().getLastPublishedTime());
    }

    @SuppressWarnings("unchecked")
    @Test
    void publishRdBom_shouldCopyItemSubstitutesToManufacturingBom() throws Exception {
        selectRdBomResult.set(new ErpRdBomDO()
                .setId(5L)
                .setBomCode("RD-BOM-100")
                .setProductId(10L)
                .setStatus(0)
                .setRemark("rd"));
        selectRdBomListResult.set(List.of(
                new ErpRdBomDO().setId(1L).setProductId(10L).setVersion("V1.0"),
                new ErpRdBomDO().setId(2L).setProductId(10L).setVersion("V2.0"),
                new ErpRdBomDO().setId(5L).setProductId(10L).setVersion(null)
        ));
        selectRdBomItemsResult.set(List.of(new ErpRdBomItemDO()
                .setId(51L)
                .setBomId(5L)
                .setMaterialId(20L)
                .setMaterialType(1)
                .setUnitId(2000L)
                .setUsageQty(new BigDecimal("3"))
                .setLossRate(new BigDecimal("0.10"))
                .setLeadTimeDay(2)
                .setSort(1)
                .setRemark("item")));
        Object sourceSubstitute1 = newRdBomItemSubstituteDO(51L, 30L, 1, "1.000000");
        setField(sourceSubstitute1, "id", 7001L);
        Object sourceSubstitute2 = newRdBomItemSubstituteDO(51L, 31L, 2, "0.500000");
        setField(sourceSubstitute2, "id", 7002L);
        selectedRdBomItemSubstitutesResult.set(List.of(sourceSubstitute1, sourceSubstitute2));

        rdBomService.publishRdBom(5L);

        assertEquals(2, insertedManufacturingItemSubstitutes.size());
        assertNull(readLongField(insertedManufacturingItemSubstitutes.get(0), "id"));
        assertEquals(2001L, readLongField(insertedManufacturingItemSubstitutes.get(0), "bomItemId"));
        assertEquals(30L, readLongField(insertedManufacturingItemSubstitutes.get(0), "substituteMaterialId"));
        assertEquals(new BigDecimal("1.000000"), readBigDecimalField(insertedManufacturingItemSubstitutes.get(0), "replaceRatio"));
        assertNull(readLongField(insertedManufacturingItemSubstitutes.get(1), "id"));
        assertEquals(2001L, readLongField(insertedManufacturingItemSubstitutes.get(1), "bomItemId"));
        assertEquals(31L, readLongField(insertedManufacturingItemSubstitutes.get(1), "substituteMaterialId"));
    }

    @SuppressWarnings("unchecked")
    private ErpRdBomMapper createRdBomMapperProxy() {
        return (ErpRdBomMapper) Proxy.newProxyInstance(ErpRdBomMapper.class.getClassLoader(),
                new Class<?>[]{ErpRdBomMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return selectRdBomResult.get();
                    }
                    if ("selectList".equals(method.getName())) {
                        return selectRdBomListResult.get();
                    }
                    if ("insert".equals(method.getName())) {
                        ErpRdBomDO rdBom = (ErpRdBomDO) args[0];
                        rdBom.setId(901L);
                        insertedRdBomRef.set(rdBom);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        updatedRdBomRef.set((ErpRdBomDO) args[0]);
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private ErpRdBomItemMapper createRdBomItemMapperProxy() {
        return (ErpRdBomItemMapper) Proxy.newProxyInstance(ErpRdBomItemMapper.class.getClassLoader(),
                new Class<?>[]{ErpRdBomItemMapper.class},
                (proxy, method, args) -> {
                    if ("selectListByBomId".equals(method.getName())) {
                        return selectRdBomItemsResult.get();
                    }
                    if ("insertBatch".equals(method.getName())) {
                        List<ErpRdBomItemDO> itemDOs = new ArrayList<>((List<ErpRdBomItemDO>) args[0]);
                        for (int i = 0; i < itemDOs.size(); i++) {
                            itemDOs.get(i).setId(1001L + i);
                        }
                        insertedRdBomItemsRef.set(itemDOs);
                        return true;
                    }
                    if ("deleteByBomId".equals(method.getName())) {
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private ErpBomMapper createBomMapperProxy() {
        return (ErpBomMapper) Proxy.newProxyInstance(ErpBomMapper.class.getClassLoader(),
                new Class<?>[]{ErpBomMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return selectedManufacturingBomRef.get();
                    }
                    if ("insert".equals(method.getName())) {
                        ErpBomDO bom = (ErpBomDO) args[0];
                        bom.setId(501L);
                        insertedManufacturingBomRef.set(bom);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        updatedManufacturingBomRef.set((ErpBomDO) args[0]);
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private ErpBomItemMapper createBomItemMapperProxy() {
        return (ErpBomItemMapper) Proxy.newProxyInstance(ErpBomItemMapper.class.getClassLoader(),
                new Class<?>[]{ErpBomItemMapper.class},
                (proxy, method, args) -> {
                    if ("insertBatch".equals(method.getName())) {
                        List<ErpBomItemDO> itemDOs = new ArrayList<>((List<ErpBomItemDO>) args[0]);
                        for (int i = 0; i < itemDOs.size(); i++) {
                            itemDOs.get(i).setId(2001L + i);
                        }
                        insertedManufacturingItems.addAll(itemDOs);
                        return true;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private Object createRdBomItemSubstituteMapperProxy() throws Exception {
        Class<?> mapperClass = Class.forName("cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemSubstituteMapper");
        return Proxy.newProxyInstance(mapperClass.getClassLoader(), new Class<?>[]{mapperClass},
                (proxy, method, args) -> {
                    if ("insertBatch".equals(method.getName())) {
                        insertedRdBomItemSubstitutes.clear();
                        insertedRdBomItemSubstitutes.addAll((List<Object>) args[0]);
                        return true;
                    }
                    if ("selectListByBomItemIds".equals(method.getName())) {
                        return selectedRdBomItemSubstitutesResult.get();
                    }
                    if ("deleteByBomItemIds".equals(method.getName())) {
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private Object createBomItemSubstituteMapperProxy() throws Exception {
        Class<?> mapperClass = Class.forName("cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper");
        return Proxy.newProxyInstance(mapperClass.getClassLoader(), new Class<?>[]{mapperClass},
                (proxy, method, args) -> {
                    if ("insertBatch".equals(method.getName())) {
                        insertedManufacturingItemSubstitutes.clear();
                        insertedManufacturingItemSubstitutes.addAll((List<Object>) args[0]);
                        return true;
                    }
                    if ("selectListByBomItemIds".equals(method.getName())) {
                        return List.of();
                    }
                    if ("deleteByBomItemIds".equals(method.getName())) {
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private ErpProductService createProductServiceProxy() {
        return (ErpProductService) Proxy.newProxyInstance(ErpProductService.class.getClassLoader(),
                new Class<?>[]{ErpProductService.class},
                (proxy, method, args) -> {
                    if ("validProductList".equals(method.getName())) {
                        return validProductsResult.get();
                    }
                    return null;
        });
    }

    private ErpRdBomSaveReqVO.Item newItem(Long materialId, String usageQty) {
        ErpRdBomSaveReqVO.Item item = new ErpRdBomSaveReqVO.Item();
        item.setMaterialId(materialId);
        item.setUsageQty(new BigDecimal(usageQty));
        return item;
    }

    private Object newRdBomItemSubstitute(Long substituteMaterialId, int priority, String replaceRatio) throws Exception {
        Object substitute = newInstance("cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO$Item$Substitute");
        setField(substitute, "substituteMaterialId", substituteMaterialId);
        setField(substitute, "priority", priority);
        setField(substitute, "replaceRatio", new BigDecimal(replaceRatio));
        return substitute;
    }

    private Object newRdBomItemSubstituteDO(Long bomItemId, Long substituteMaterialId, int priority, String replaceRatio) throws Exception {
        Object substitute = newInstance("cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO");
        setField(substitute, "bomItemId", bomItemId);
        setField(substitute, "substituteMaterialId", substituteMaterialId);
        setField(substitute, "priority", priority);
        setField(substitute, "replaceRatio", new BigDecimal(replaceRatio));
        return substitute;
    }

    private void setItemSubstitutes(Object item, List<Object> substitutes) throws Exception {
        setField(item, "substitutes", substitutes);
    }

    private Object newInstance(String className) throws Exception {
        return Class.forName(className).getDeclaredConstructor().newInstance();
    }

    private Long readLongField(Object target, String fieldName) throws Exception {
        Object value = readField(target, fieldName);
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer readIntegerField(Object target, String fieldName) throws Exception {
        Object value = readField(target, fieldName);
        return value == null ? null : ((Number) value).intValue();
    }

    private BigDecimal readBigDecimalField(Object target, String fieldName) throws Exception {
        return (BigDecimal) readField(target, fieldName);
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(target, fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field getDeclaredField(Object target, String fieldName) throws NoSuchFieldException {
        try {
            return target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return target.getClass().getDeclaredField(mapFieldName(fieldName));
        }
    }

    private String mapFieldName(String fieldName) {
        if (fieldName.endsWith("Mapper")) {
            return "erp" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        return fieldName;
    }

}
