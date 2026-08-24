package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.service.mrp.ErpWorkCenterService;
import cn.weitee.erp.module.mes.controller.admin.vo.oee.MesOeeSummaryRespVO;
import cn.weitee.erp.module.mes.dal.mysql.MesOeeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MesOeeServiceImplTest {

    private MesOeeServiceImpl oeeService;

    @BeforeEach
    void setUp() throws Exception {
        oeeService = new MesOeeServiceImpl();
        MesOeeMapper mapper = (MesOeeMapper) Proxy.newProxyInstance(
                MesOeeMapper.class.getClassLoader(), new Class<?>[]{MesOeeMapper.class},
                (proxy, method, args) -> {
                    if ("selectOeeSummary".equals(method.getName())) {
                        MesOeeSummaryRespVO vo = new MesOeeSummaryRespVO();
                        vo.setWorkCenterId(4L);
                        vo.setStatDate("2026-08-14");
                        vo.setTaskCount(2L);
                        vo.setPlanQty(new BigDecimal("100"));
                        vo.setReportedQty(new BigDecimal("80"));
                        vo.setQualifiedQty(new BigDecimal("76"));
                        vo.setPlanMinutes(960L);
                        vo.setActualMinutes(900L);
                        return List.of(vo);
                    }
                    return null;
                });
        setField(oeeService, "mesOeeMapper", mapper);
        ErpWorkCenterService wcService = (ErpWorkCenterService) Proxy.newProxyInstance(
                ErpWorkCenterService.class.getClassLoader(), new Class<?>[]{ErpWorkCenterService.class},
                (proxy, method, args) -> {
                    if ("getWorkCenterList".equals(method.getName())) {
                        return List.of(new ErpWorkCenterDO().setId(4L).setCenterName("Cutting-Center"));
                    }
                    return null;
                });
        setField(oeeService, "workCenterService", wcService);
    }

    @Test
    void getOeeSummary_shouldCalculateRates() {
        List<MesOeeSummaryRespVO> list = oeeService.getOeeSummary(4L, null, null);

        assertEquals(1, list.size());
        MesOeeSummaryRespVO vo = list.get(0);
        assertEquals("Cutting-Center", vo.getWorkCenterName());
        // 可用率 = 900/960 = 93.75
        assertEquals(new BigDecimal("93.75"), vo.getAvailabilityRate());
        // 达成率 = 80/100 = 80.00
        assertEquals(new BigDecimal("80.00"), vo.getAchievementRate());
        // 良品率 = 76/80 = 95.00
        assertEquals(new BigDecimal("95.00"), vo.getQualityRate());
        // OEE = 0.9375 * 0.8 * 0.95 = 0.7125 → 71.25
        assertEquals(new BigDecimal("71.25"), vo.getOee());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
