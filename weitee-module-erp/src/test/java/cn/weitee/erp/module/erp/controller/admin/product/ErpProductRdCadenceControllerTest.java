package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpRdCadencePageReqVO;
import cn.weitee.erp.module.erp.service.product.ErpProductRdCadenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductRdCadenceControllerTest {

    private static final long RD_DEPT_ID = 103L;
    private static final long OTHER_DEPT_ID = 999L;

    @Mock
    private ErpProductRdCadenceService rdCadenceService;

    @InjectMocks
    private ErpProductRdCadenceController controller;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRdCadencePage_whenRdDept_shouldCallServiceWithDeptId() {
        setLoginUser(RD_DEPT_ID);
        ErpRdCadencePageReqVO reqVO = new ErpRdCadencePageReqVO();
        when(rdCadenceService.getRdCadencePage(any(), eq(RD_DEPT_ID))).thenReturn(new cn.weitee.erp.framework.common.pojo.PageResult<>());

        controller.getRdCadencePage(reqVO);

        verify(rdCadenceService).getRdCadencePage(reqVO, RD_DEPT_ID);
    }

    @Test
    void getRdCadencePage_whenNotRdDept_forwardsDeptIdToService() {
        // 部门闸门在 Service 层（assertRdDept），Controller 仅透传 deptId
        setLoginUser(OTHER_DEPT_ID);
        ErpRdCadencePageReqVO reqVO = new ErpRdCadencePageReqVO();
        when(rdCadenceService.getRdCadencePage(any(), eq(OTHER_DEPT_ID))).thenReturn(new cn.weitee.erp.framework.common.pojo.PageResult<>());

        controller.getRdCadencePage(reqVO);

        verify(rdCadenceService).getRdCadencePage(reqVO, OTHER_DEPT_ID);
    }

    @Test
    void downloadTemplate_whenRdDept_shouldWriteBytes() throws java.io.IOException {
        setLoginUser(RD_DEPT_ID);
        when(rdCadenceService.downloadTemplate()).thenReturn(new byte[]{1, 2, 3});
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadTemplate(response);

        assertThat(response.getContentAsByteArray()).hasSize(3);
        verify(rdCadenceService).downloadTemplate();
    }

    @Test
    void downloadTemplate_whenNotRdDept_forwardsDeptIdToService() throws java.io.IOException {
        setLoginUser(OTHER_DEPT_ID);
        when(rdCadenceService.downloadTemplate()).thenReturn(new byte[]{1, 2, 3});
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadTemplate(response);

        assertThat(response.getContentAsByteArray()).hasSize(3);
        verify(rdCadenceService).downloadTemplate();
    }

    @Test
    void precheckImport_whenRdDept_shouldCallService() {
        setLoginUser(RD_DEPT_ID);
        MultipartFile file = new MockMultipartFile("file", "cadence.xlsx", "application/vnd.ms-excel", new byte[0]);
        ErpProductImportResultVO result = new ErpProductImportResultVO();
        when(rdCadenceService.precheckImport(eq(file), eq(RD_DEPT_ID), eq(false))).thenReturn(result);

        controller.precheckImport(file, false);

        verify(rdCadenceService).precheckImport(file, RD_DEPT_ID, false);
    }

    @Test
    void importCadence_whenRdDept_shouldCallServiceWithUserIdAndDeptId() {
        setLoginUser(RD_DEPT_ID);
        MultipartFile file = new MockMultipartFile("file", "cadence.xlsx", "application/vnd.ms-excel", new byte[0]);
        ErpProductImportResultVO result = new ErpProductImportResultVO();
        when(rdCadenceService.importCadence(eq(file), eq(1L), eq(RD_DEPT_ID), eq(false))).thenReturn(result);

        controller.importCadence(file, false);

        verify(rdCadenceService).importCadence(file, 1L, RD_DEPT_ID, false);
    }

    @Test
    void importCadence_whenNotRdDept_forwardsDeptIdToService() {
        setLoginUser(OTHER_DEPT_ID);
        MultipartFile file = new MockMultipartFile("file", "cadence.xlsx", "application/vnd.ms-excel", new byte[0]);
        ErpProductImportResultVO result = new ErpProductImportResultVO();
        when(rdCadenceService.importCadence(eq(file), eq(1L), eq(OTHER_DEPT_ID), eq(false))).thenReturn(result);

        controller.importCadence(file, false);

        verify(rdCadenceService).importCadence(file, 1L, OTHER_DEPT_ID, false);
    }

    @Test
    void exportRdCadence_whenRdDept_shouldCallService() throws java.io.IOException {
        setLoginUser(RD_DEPT_ID);
        ErpRdCadencePageReqVO reqVO = new ErpRdCadencePageReqVO();
        when(rdCadenceService.exportRdCadence(any(), eq(RD_DEPT_ID))).thenReturn(List.of());

        controller.exportRdCadence(reqVO, new MockHttpServletResponse());

        verify(rdCadenceService).exportRdCadence(reqVO, RD_DEPT_ID);
    }

    private void setLoginUser(Long deptId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setInfo(java.util.Map.of(LoginUser.INFO_KEY_DEPT_ID, String.valueOf(deptId)));
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

}
