package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstituteRespVO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpBomItemSubstituteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 替代料管理")
@RestController
@RequestMapping("/erp/mrp-substitute")
@Validated
public class ErpBomItemSubstituteController {

    @Resource
    private ErpBomItemSubstituteService substituteService;

    @GetMapping("/page")
    @Operation(summary = "获得替代料管理分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-substitute:query')")
    public CommonResult<PageResult<ErpBomItemSubstituteRespVO>> getSubstitutePage(
            @Valid ErpBomItemSubstitutePageReqVO pageReqVO) {
        return success(substituteService.getSubstitutePage(pageReqVO));
    }

}
