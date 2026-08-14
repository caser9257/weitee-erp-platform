package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportConfirmReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordRespVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopDocumentDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopImportRecordDO;
import cn.weitee.erp.module.mes.dal.mysql.MesSopDocumentMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesSopImportRecordMapper;
import cn.weitee.erp.module.mes.infra.ocr.SopOcrClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MesSopImportServiceImplTest {

    private final AtomicReference<MesSopImportRecordDO> insertedRecordRef = new AtomicReference<>();
    private final AtomicReference<MesSopImportRecordDO> recordRef = new AtomicReference<>();
    private final AtomicReference<MesSopDocumentDO> insertedSopRef = new AtomicReference<>();
    private final AtomicReference<MesSopImportRecordDO> updatedRecordRef = new AtomicReference<>();

    private MesSopImportServiceImpl importService;

    @BeforeEach
    void setUp() throws Exception {
        importService = new MesSopImportServiceImpl();
        insertedRecordRef.set(null);
        recordRef.set(null);
        insertedSopRef.set(null);
        updatedRecordRef.set(null);

        setField(importService, "mesSopImportRecordMapper", createProxy(MesSopImportRecordMapper.class, (m, a) -> {
            if ("insert".equals(m)) {
                insertedRecordRef.set((MesSopImportRecordDO) a[0]);
                return 1;
            }
            if ("selectById".equals(m)) return recordRef.get();
            if ("updateById".equals(m)) {
                updatedRecordRef.set((MesSopImportRecordDO) a[0]);
                return 1;
            }
            return null;
        }));
        setField(importService, "mesSopDocumentMapper", createProxy(MesSopDocumentMapper.class, (m, a) -> {
            if ("insert".equals(m)) {
                insertedSopRef.set((MesSopDocumentDO) a[0]);
                return 1;
            }
            if ("deletePhysicalBySopNo".equals(m)) return 1;
            return null;
        }));
        setField(importService, "sopOcrClient", new SopOcrClient() {
            @Override
            public String recognize(byte[] imageBytes) {
                return "第一步：装夹工件。\n第二步：启动设备。";
            }
        });
    }

    @Test
    void ocrImport_shouldCreatePendingRecord() {
        MesSopImportRecordRespVO resp = importService.ocrImport("sop.png", new byte[]{1, 2, 3});

        assertNotNull(resp);
        assertEquals(0, insertedRecordRef.get().getStatus()); // 待校对
        assertEquals("第一步：装夹工件。\n第二步：启动设备。", insertedRecordRef.get().getOcrText());
    }

    @Test
    void confirmImport_shouldCreateDraftSopAndMarkDone() {
        recordRef.set(new MesSopImportRecordDO().setId(1L).setOcrText("草稿文本").setStatus(0));
        MesSopImportConfirmReqVO reqVO = new MesSopImportConfirmReqVO();
        reqVO.setImportRecordId(1L);
        reqVO.setSopNo("SOP-OCR-001");
        reqVO.setTitle("OCR 导入 SOP");

        Long sopId = importService.confirmImport(reqVO);

        assertEquals(0, insertedSopRef.get().getStatus()); // 草稿态，不直接发布
        assertEquals("SOP-OCR-001", insertedSopRef.get().getSopNo());
        assertEquals(2, updatedRecordRef.get().getStatus()); // 已转正式
    }

    @Test
    void confirmImport_shouldRejectWhenRecordMissing() {
        MesSopImportConfirmReqVO reqVO = new MesSopImportConfirmReqVO();
        reqVO.setImportRecordId(99L);
        reqVO.setSopNo("SOP-X");
        reqVO.setTitle("x");

        assertThrows(ServiceException.class, () -> importService.confirmImport(reqVO));
    }

    @Test
    void confirmImport_shouldRejectWhenAlreadyDone() {
        recordRef.set(new MesSopImportRecordDO().setId(1L).setStatus(2));
        MesSopImportConfirmReqVO reqVO = new MesSopImportConfirmReqVO();
        reqVO.setImportRecordId(1L);
        reqVO.setSopNo("SOP-X");
        reqVO.setTitle("x");

        assertThrows(ServiceException.class, () -> importService.confirmImport(reqVO));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
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
