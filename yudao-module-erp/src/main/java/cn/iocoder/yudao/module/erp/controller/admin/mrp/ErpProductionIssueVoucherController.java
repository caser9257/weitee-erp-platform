package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issuevoucher.ErpProductionIssueVoucherPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issuevoucher.ErpProductionIssueVoucherRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionIssueVoucherService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionOrderService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 生产领料出库凭证")
@RestController
@RequestMapping("/erp/production-material-issue-voucher")
@Validated
public class ErpProductionIssueVoucherController {

    @Resource
    private ErpProductionIssueVoucherService productionIssueVoucherService;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/get")
    @Operation(summary = "获得生产领料出库凭证")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<ErpProductionIssueVoucherRespVO> getVoucher(@RequestParam("id") Long id) {
        ErpProductionIssueVoucherDO voucher = productionIssueVoucherService.getVoucher(id);
        if (voucher == null) {
            return success(null);
        }
        List<ErpProductionIssueVoucherItemDO> itemList = productionIssueVoucherService.getVoucherItemListByVoucherId(id);
        return success(buildVoucherVO(voucher, itemList));
    }

    @GetMapping("/get-by-issue-id")
    @Operation(summary = "按领料单获得生产领料出库凭证")
    @Parameter(name = "issueId", description = "领料单编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<ErpProductionIssueVoucherRespVO> getVoucherByIssueId(@RequestParam("issueId") Long issueId) {
        ErpProductionIssueVoucherDO voucher = productionIssueVoucherService.getVoucherByIssueId(issueId);
        if (voucher == null) {
            return success(null);
        }
        List<ErpProductionIssueVoucherItemDO> itemList = productionIssueVoucherService.getVoucherItemListByVoucherId(voucher.getId());
        return success(buildVoucherVO(voucher, itemList));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产领料出库凭证分页")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<PageResult<ErpProductionIssueVoucherRespVO>> getVoucherPage(@Valid ErpProductionIssueVoucherPageReqVO pageReqVO) {
        PageResult<ErpProductionIssueVoucherDO> pageResult = productionIssueVoucherService.getVoucherPage(pageReqVO);
        return success(buildVoucherVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产领料出库凭证 Excel")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVoucherExcel(@Valid ErpProductionIssueVoucherPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpProductionIssueVoucherRespVO> list = buildVoucherVOPageResult(
                productionIssueVoucherService.getVoucherPage(pageReqVO)).getList();
        ExcelUtils.write(response, "生产领料出库凭证.xls", "数据", ErpProductionIssueVoucherRespVO.class, list);
    }

    private PageResult<ErpProductionIssueVoucherRespVO> buildVoucherVOPageResult(PageResult<ErpProductionIssueVoucherDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderService.getProductionOrderList(
                        convertSet(pageResult.getList(), ErpProductionIssueVoucherDO::getProductionOrderId)).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertList(pageResult.getList(),
                item -> parseUserId(item.getCreator())));
        return new PageResult<>(convertList(pageResult.getList(), voucher -> {
            ErpProductionIssueVoucherRespVO vo = BeanUtils.toBean(voucher, ErpProductionIssueVoucherRespVO.class);
            MapUtils.findAndThen(orderMap, voucher.getProductionOrderId(), order -> vo.setProductionOrderNo(order.getOrderNo()));
            MapUtils.findAndThen(userMap, parseUserId(voucher.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            vo.setStatusName(resolveStatusName(voucher.getStatus()));
            return vo;
        }), pageResult.getTotal());
    }

    private ErpProductionIssueVoucherRespVO buildVoucherVO(ErpProductionIssueVoucherDO voucher,
                                                           List<ErpProductionIssueVoucherItemDO> itemList) {
        Map<Long, ErpProductionOrderDO> orderMap = voucher.getProductionOrderId() == null
                ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(List.of(voucher.getProductionOrderId())).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, AdminUserRespDTO> userMap = voucher.getCreator() == null
                ? Collections.emptyMap()
                : adminUserApi.getUserMap(List.of(parseUserId(voucher.getCreator())));
        Map<Long, ErpProductRespVO> productMap = CollUtil.isEmpty(itemList) ? Collections.emptyMap()
                : productService.getProductVOMap(convertSet(itemList, ErpProductionIssueVoucherItemDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = CollUtil.isEmpty(itemList) ? Collections.emptyMap()
                : warehouseService.getWarehouseMap(convertSet(itemList, ErpProductionIssueVoucherItemDO::getWarehouseId));

        ErpProductionIssueVoucherRespVO vo = BeanUtils.toBean(voucher, ErpProductionIssueVoucherRespVO.class);
        MapUtils.findAndThen(orderMap, voucher.getProductionOrderId(), order -> vo.setProductionOrderNo(order.getOrderNo()));
        MapUtils.findAndThen(userMap, parseUserId(voucher.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        vo.setStatusName(resolveStatusName(voucher.getStatus()));
        vo.setItems(convertList(itemList, item -> {
            ErpProductionIssueVoucherRespVO.Item itemVO = BeanUtils.toBean(item, ErpProductionIssueVoucherRespVO.Item.class);
            MapUtils.findAndThen(productMap, item.getMaterialId(), product -> itemVO
                    .setMaterialName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setMaterialBarCode(product.getBarCode())
                    .setProductUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> itemVO.setWarehouseName(warehouse.getName()));
            return itemVO;
        }));
        return vo;
    }

    private String resolveStatusName(Integer status) {
        return status != null && status == 10 ? "已生成" : null;
    }

}
