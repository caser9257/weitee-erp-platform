package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpBomItemSubstituteServiceImplTest {

    private final AtomicReference<PageResult<ErpBomItemSubstituteDO>> pageResultRef = new AtomicReference<>();
    private final AtomicReference<ErpBomItemSubstitutePageReqVO> capturedReqRef = new AtomicReference<>();
    private ErpBomItemSubstituteServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpBomItemSubstituteServiceImpl();
        pageResultRef.set(new PageResult<>(List.of(), 0L));
        capturedReqRef.set(null);
        setField(service, "substituteMapper", createProxy(ErpBomItemSubstituteMapper.class, (methodName, args) -> {
            if ("selectPage".equals(methodName)) {
                capturedReqRef.set((ErpBomItemSubstitutePageReqVO) args[0]);
                return pageResultRef.get();
            }
            return null;
        }));
        setField(service, "bomMapper", createProxy(ErpBomMapper.class, (methodName, args) -> {
            if ("selectByIds".equals(methodName)) {
                return List.of(new ErpBomDO().setId(101L).setBomCode("BOM-001").setProductId(201L));
            }
            return null;
        }));
        setField(service, "bomItemMapper", createProxy(ErpBomItemMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName) || "selectByIds".equals(methodName)) {
                return List.of(new ErpBomItemDO().setId(11L).setBomId(101L).setMaterialId(301L));
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                return Map.of(
                        201L, new cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO()
                                .setId(201L).setName("成品A"),
                        301L, new cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO()
                                .setId(301L).setName("替代料A"),
                        401L, new cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO()
                                .setId(401L).setName("替代料B")
                );
            }
            return null;
        }));
    }

    @Test
    void getSubstitutePage_shouldFillBomAndProductNames() throws Exception {
        pageResultRef.set(new PageResult<>(List.of(
                new ErpBomItemSubstituteDO()
                        .setId(1L)
                        .setBomItemId(11L)
                        .setSubstituteMaterialId(401L)
                        .setPriority(1)
                        .setReplaceRatio(new BigDecimal("1.0"))
                        .setEnableAutoRecommend(Boolean.TRUE)
                        .setSort(1)
                        .setRemark("优先推荐")
        ), 1L));

        PageResult<?> page = service.getSubstitutePage(new ErpBomItemSubstitutePageReqVO());

        assertThat(page.getList()).hasSize(1);
        Object row = page.getList().get(0);
        assertThat(readField(row, "bomCode")).isEqualTo("BOM-001");
        assertThat(readField(row, "productName")).isEqualTo("成品A");
        assertThat(readField(row, "materialName")).isEqualTo("替代料A");
        assertThat(readField(row, "substituteMaterialName")).isEqualTo("替代料B");
    }

    @Test
    void getSubstitutePage_shouldResolveBomItemIdsFromBomAndMaterialFilters() throws Exception {
        ErpBomItemSubstitutePageReqVO reqVO = new ErpBomItemSubstitutePageReqVO();
        reqVO.setBomId(101L);
        reqVO.setMaterialId(301L);

        service.getSubstitutePage(reqVO);

        assertThat(capturedReqRef.get()).isNotNull();
        assertThat(capturedReqRef.get().getBomItemIds()).containsExactly(11L);
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

    private Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
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
