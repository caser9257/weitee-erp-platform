package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentSaveReqVO;
import cn.weitee.erp.module.mes.service.MesSopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES SOP 管理")
@RestController
@RequestMapping("/mes/sop")
@Validated
public class MesSopController {

    @Resource
    private MesSopService mesSopService;

    @GetMapping("/page")
    @Operation(summary = "获得 SOP 分页")
    @PreAuthorize("@ss.hasPermission('mes:sop:query')")
    public CommonResult<PageResult<MesSopDocumentRespVO>> getSopPage(@Valid MesSopDocumentPageReqVO pageReqVO) {
        return success(mesSopService.getSopPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:query')")
    public CommonResult<MesSopDocumentRespVO> getSop(@RequestParam("id") Long id) {
        return success(mesSopService.getSop(id));
    }

    @GetMapping("/by-step")
    @Operation(summary = "按工序查询已发布 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:query')")
    public CommonResult<List<MesSopDocumentRespVO>> getPublishedSopsByStepId(@RequestParam("routeStepId") Long routeStepId) {
        return success(mesSopService.getPublishedSopsByStepId(routeStepId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:create')")
    public CommonResult<Long> createSop(@Valid @RequestBody MesSopDocumentSaveReqVO reqVO) {
        return success(mesSopService.createSop(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:update')")
    public CommonResult<Boolean> updateSop(@Valid @RequestBody MesSopDocumentSaveReqVO reqVO) {
        mesSopService.updateSop(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "发布/停用 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:update')")
    public CommonResult<Boolean> updateStatus(@RequestParam("id") Long id,
                                              @RequestParam("status") Integer status) {
        mesSopService.updateStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:delete')")
    public CommonResult<Boolean> deleteSop(@RequestParam("id") Long id) {
        mesSopService.deleteSop(id);
        return success(true);
    }

}
