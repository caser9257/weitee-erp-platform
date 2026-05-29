package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestConvertReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestConvertReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpMrpSuggestService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP MRP 建议单")
@RestController
@RequestMapping("/erp/mrp-suggest")
@Validated
public class ErpMrpSuggestController {

    @Resource
    private ErpMrpSuggestService mrpSuggestService;
    @Resource
    private ErpProductService productService;

    @GetMapping("/purchase-page")
    @Operation(summary = "获得采购建议分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:query')")
    public CommonResult<PageResult<ErpPurchaseSuggestRespVO>> getPurchaseSuggestPage(@Valid ErpPurchaseSuggestPageReqVO pageReqVO) {
        PageResult<ErpPurchaseSuggestDO> pageResult = mrpSuggestService.getPurchaseSuggestPage(pageReqVO);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(pageResult.getList(), ErpPurchaseSuggestDO::getMaterialId));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpPurchaseSuggestRespVO.class, item -> {
            ErpProductRespVO product = productMap.get(item.getMaterialId());
            if (product != null) {
                item.setMaterialName(product.getName());
            }
        }), pageResult.getTotal()));
    }

    @GetMapping("/production-page")
    @Operation(summary = "获得生产建议分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:query')")
    public CommonResult<PageResult<ErpProductionSuggestRespVO>> getProductionSuggestPage(@Valid ErpProductionSuggestPageReqVO pageReqVO) {
        PageResult<ErpProductionSuggestDO> pageResult = mrpSuggestService.getProductionSuggestPage(pageReqVO);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(pageResult.getList(), ErpProductionSuggestDO::getProductId));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpProductionSuggestRespVO.class, item -> {
            ErpProductRespVO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
        }), pageResult.getTotal()));
    }

    @PostMapping("/purchase-confirm")
    @Operation(summary = "确认采购建议")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:approve')")
    public CommonResult<Boolean> confirmPurchaseSuggest(@RequestBody List<Long> ids) {
        mrpSuggestService.confirmPurchaseSuggest(ids);
        return success(true);
    }

    @PostMapping("/production-confirm")
    @Operation(summary = "确认生产建议")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:approve')")
    public CommonResult<Boolean> confirmProductionSuggest(@RequestBody List<Long> ids) {
        mrpSuggestService.confirmProductionSuggest(ids);
        return success(true);
    }

    @PostMapping("/purchase-reject")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:reject')")
    public CommonResult<Boolean> rejectPurchaseSuggest(@RequestBody List<Long> ids) {
        mrpSuggestService.rejectPurchaseSuggest(ids);
        return success(true);
    }

    @PostMapping("/production-reject")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:reject')")
    public CommonResult<Boolean> rejectProductionSuggest(@RequestBody List<Long> ids) {
        mrpSuggestService.rejectProductionSuggest(ids);
        return success(true);
    }

    @PostMapping("/purchase-convert")
    @Operation(summary = "采购建议转采购订单")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:convert-purchase')")
    public CommonResult<Long> convertPurchaseSuggest(@Valid @RequestBody ErpPurchaseSuggestConvertReqVO reqVO) {
        return success(mrpSuggestService.convertPurchaseSuggest(reqVO));
    }

    @PostMapping("/production-convert")
    @Operation(summary = "生产建议转工单")
    @PreAuthorize("@ss.hasPermission('erp:mrp-suggest:convert-production')")
    public CommonResult<List<Long>> convertProductionSuggest(@Valid @RequestBody ErpProductionSuggestConvertReqVO reqVO) {
        return success(mrpSuggestService.convertProductionSuggest(reqVO));
    }

}
