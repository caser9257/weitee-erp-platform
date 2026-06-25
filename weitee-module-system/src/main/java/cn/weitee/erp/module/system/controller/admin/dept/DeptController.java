package cn.weitee.erp.module.system.controller.admin.dept;

import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptImportExcelVO;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptImportRespVO;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import cn.weitee.erp.module.system.dal.dataobject.dept.DeptDO;
import cn.weitee.erp.module.system.service.dept.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 部门")
@RestController
@RequestMapping("/system/dept")
@Validated
public class DeptController {

    @Resource
    private DeptService deptService;

    @PostMapping("create")
    @Operation(summary = "创建部门")
    @PreAuthorize("@ss.hasPermission('system:dept:create')")
    public CommonResult<Long> createDept(@Valid @RequestBody DeptSaveReqVO createReqVO) {
        Long deptId = deptService.createDept(createReqVO);
        return success(deptId);
    }

    @PutMapping("update")
    @Operation(summary = "更新部门")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    public CommonResult<Boolean> updateDept(@Valid @RequestBody DeptSaveReqVO updateReqVO) {
        deptService.updateDept(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除部门")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    public CommonResult<Boolean> deleteDept(@RequestParam("id") Long id) {
        deptService.deleteDept(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除部门")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    public CommonResult<Boolean> deleteDeptList(@RequestParam("ids") List<Long> ids) {
        deptService.deleteDeptList(ids);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取部门列表")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<List<DeptRespVO>> getDeptList(DeptListReqVO reqVO) {
        List<DeptDO> list = deptService.getDeptList(reqVO);
        return success(BeanUtils.toBean(list, DeptRespVO.class));
    }

    @GetMapping(value = {"/list-all-simple", "/simple-list"})
    @Operation(summary = "获取部门精简信息列表", description = "只包含被开启的部门，主要用于前端的下拉选项")
    public CommonResult<List<DeptSimpleRespVO>> getSimpleDeptList() {
        List<DeptDO> list = deptService.getDeptList(
                new DeptListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        return success(BeanUtils.toBean(list, DeptSimpleRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得部门信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<DeptRespVO> getDept(@RequestParam("id") Long id) {
        DeptDO dept = deptService.getDept(id);
        return success(BeanUtils.toBean(dept, DeptRespVO.class));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入部门模板")
    @PreAuthorize("@ss.hasPermission('system:dept:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        List<DeptImportExcelVO> list = Arrays.asList(
                DeptImportExcelVO.builder().name("制造中心").parentName("").sort(10)
                        .leaderUsername("admin").phone("13800138000").email("manufacture@test.com")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                DeptImportExcelVO.builder().name("装配车间").parentName("制造中心").sort(20)
                        .leaderUsername("admin").phone("13800138001").email("assembly@test.com")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()
        );
        ExcelUtils.write(response, "部门导入模板.xls", "部门列表", DeptImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入部门")
    @PreAuthorize("@ss.hasPermission('system:dept:import')")
    public CommonResult<DeptImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<DeptImportExcelVO> list = ExcelUtils.read(file, DeptImportExcelVO.class);
        return success(deptService.importDeptList(list, updateSupport));
    }

}
