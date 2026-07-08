package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityPrintDataRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualitySubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionFinishQualityService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 成品质检")
@RestController
@RequestMapping("/erp/production-finish-quality")
@Validated
public class ErpProductionFinishQualityController {

    @Resource
    private ErpProductionFinishQualityService productionFinishQualityService;
    @Resource
    private ErpProductService productService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/get")
    @Operation(summary = "获得成品质检单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionFinishQualityRespVO> getProductionFinishQuality(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(productionFinishQualityService.getProductionFinishQuality(id),
                ErpProductionFinishQualityRespVO.class));
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获得成品质检打印数据")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionFinishQualityPrintDataRespVO> getProductionFinishQualityPrintData(
            @RequestParam("id") Long id) {
        ErpProductionFinishQualityDO quality = productionFinishQualityService.getProductionFinishQuality(id);
        ErpProductionFinishQualityPrintDataRespVO printData = new ErpProductionFinishQualityPrintDataRespVO();
        printData.setProductionFinishQuality(BeanUtils.toBean(quality, ErpProductionFinishQualityRespVO.class));
        if (quality.getProductId() != null) {
            ErpProductDO product = productService.getProduct(quality.getProductId());
            printData.setProductName(product != null ? product.getName() : null);
        }
        if (quality.getCheckerUserId() != null) {
            AdminUserRespDTO checker = adminUserApi.getUser(quality.getCheckerUserId());
            printData.setCheckerName(checker != null ? checker.getNickname() : null);
        }
        return success(printData);
    }

    @GetMapping("/page")
    @Operation(summary = "获得成品质检分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpProductionFinishQualityRespVO>> getProductionFinishQualityPage(
            @Valid ErpProductionFinishQualityPageReqVO pageReqVO) {
        PageResult<ErpProductionFinishQualityDO> pageResult = productionFinishQualityService.getProductionFinishQualityPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpProductionFinishQualityRespVO.class));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交成品质检结果")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> submitProductionFinishQuality(@Valid @RequestBody ErpProductionFinishQualitySubmitReqVO reqVO) {
        productionFinishQualityService.submitQuality(reqVO.getId(), getLoginUserId(),
                reqVO.getQualifiedQty(), reqVO.getUnqualifiedQty(), reqVO.getRemark());
        return success(true);
    }

}
