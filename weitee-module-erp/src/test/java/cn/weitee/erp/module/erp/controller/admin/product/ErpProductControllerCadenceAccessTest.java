package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.service.product.ErpProductBpmService;
import cn.weitee.erp.module.erp.service.product.ErpProductImportService;
import cn.weitee.erp.module.erp.service.product.ErpProductPendingChangeService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductControllerCadenceAccessTest {

    @Mock
    private ErpProductService productService;
    @Mock
    private ErpProductImportService productImportService;
    @Mock
    private ErpProductBpmService productBpmService;
    @Mock
    private ErpProductPendingChangeService productPendingChangeService;
    @InjectMocks
    private ErpProductController controller;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProduct_whenUserIsInRdDepartment_shouldAllowCadenceFields() {
        setLoginUserDeptId(103L);
        ProductSaveReqVO reqVO = new ProductSaveReqVO();
        reqVO.setPcbComponent(true);
        when(productService.createProduct(reqVO)).thenReturn(1L);

        Long productId = controller.createProduct(reqVO).getData();

        assertThat(productId).isEqualTo(1L);
        verify(productService).createProduct(reqVO);
    }

    @Test
    void createProduct_whenUserIsOutsideRdDepartment_shouldRejectCadenceFields() {
        setLoginUserDeptId(999L);
        ProductSaveReqVO reqVO = new ProductSaveReqVO();
        reqVO.setSchematicPart("R");

        ServiceException exception = org.junit.jupiter.api.Assertions.assertThrows(ServiceException.class,
                () -> controller.createProduct(reqVO));

        assertThat(exception.getCode()).isEqualTo(1_030_500_002);
        assertThat(exception.getMessage()).isEqualTo("无权限");
        verify(productService, never()).createProduct(any());
    }

    @Test
    void getProduct_whenUserIsOutsideRdDepartment_shouldMaskCadenceFields() {
        setLoginUserDeptId(999L);
        ErpProductRespVO product = new ErpProductRespVO();
        product.setPcbComponent(true);
        product.setSchematicPart("R");
        product.setPcbFootprint("0603");
        when(productService.getProductVOList(List.of(1L))).thenReturn(List.of(product));

        ErpProductRespVO result = controller.getProduct(1L).getData();

        assertThat(result.getPcbComponent()).isNull();
        assertThat(result.getSchematicPart()).isNull();
        assertThat(result.getPcbFootprint()).isNull();
    }

    private void setLoginUserDeptId(Long deptId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setInfo(Map.of(LoginUser.INFO_KEY_DEPT_ID, String.valueOf(deptId)));
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

}
