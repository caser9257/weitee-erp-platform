package cn.weitee.erp.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPrintDataRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnBpmService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_UPDATE_FAIL_PROCESSING;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.weitee.erp.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 采购退货")
@RestController
@RequestMapping("/erp/purchase-return")
@Validated
public class ErpPurchaseReturnController {

    @Resource
    private ErpPurchaseReturnService purchaseReturnService;
    @Resource
    private ErpPurchaseReturnBpmService purchaseReturnBpmService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建采购退货")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:create')")
    public CommonResult<Long> createPurchaseReturn(@Valid @RequestBody ErpPurchaseReturnSaveReqVO createReqVO) {
        return success(purchaseReturnService.createPurchaseReturn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购退货")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:update')")
    public CommonResult<Boolean> updatePurchaseReturn(@Valid @RequestBody ErpPurchaseReturnSaveReqVO updateReqVO) {
        purchaseReturnService.updatePurchaseReturn(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新采购退货的状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:update-status')")
    public CommonResult<Boolean> updatePurchaseReturnStatus(@RequestParam("id") Long id,
                                                      @RequestParam("status") Integer status) {
        purchaseReturnService.updatePurchaseReturnStatusManually(id, status);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交采购退货审批")
    @PreAuthorize("@ss.hasAnyPermissions('erp:purchase-return:submit', 'erp:purchase-return:update-status')")
    public CommonResult<String> submitPurchaseReturn(@Valid @RequestBody ErpPurchaseReturnSubmitReqVO reqVO) {
        return success(purchaseReturnBpmService.submitPurchaseReturn(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/cancel-approval")
    @Operation(summary = "撤回采购退货审批")
    @PreAuthorize("@ss.hasAnyPermissions('erp:purchase-return:cancel-approval', 'erp:purchase-return:update-status')")
    public CommonResult<Boolean> cancelPurchaseReturnApproval(@Valid @RequestBody ErpPurchaseReturnCancelApprovalReqVO reqVO) {
        purchaseReturnBpmService.cancelPurchaseReturnApproval(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购退货")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:delete')")
    public CommonResult<Boolean> deletePurchaseReturn(@RequestParam("ids") List<Long> ids) {
        purchaseReturnService.deletePurchaseReturn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购退货")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:query')")
    public CommonResult<ErpPurchaseReturnRespVO> getPurchaseReturn(@RequestParam("id") Long id) {
        ErpPurchaseReturnDO purchaseReturn = purchaseReturnService.getPurchaseReturn(id);
        if (purchaseReturn == null) {
            return success(null);
        }
        List<ErpPurchaseReturnItemDO> purchaseReturnItemList = purchaseReturnService.getPurchaseReturnItemListByReturnId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseReturnItemList, ErpPurchaseReturnItemDO::getProductId));
        return success(BeanUtils.toBean(purchaseReturn, ErpPurchaseReturnRespVO.class, purchaseReturnVO ->
                purchaseReturnVO.setItems(BeanUtils.toBean(purchaseReturnItemList, ErpPurchaseReturnRespVO.Item.class, item -> {
                    ErpStockDO stock = stockService.getStock(item.getProductId(), item.getWarehouseId());
                    item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获得采购退货打印数据")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:query')")
    public CommonResult<ErpPurchaseReturnPrintDataRespVO> getPurchaseReturnPrintData(@RequestParam("id") Long id) {
        ErpPurchaseReturnRespVO purchaseReturn = getPurchaseReturn(id).getData();
        if (purchaseReturn == null) {
            return success(null);
        }
        ErpPurchaseReturnPrintDataRespVO printData = new ErpPurchaseReturnPrintDataRespVO();
        printData.setPurchaseReturn(purchaseReturn);
        printData.setFinancialFacts(buildPurchaseReturnFinancialFacts(purchaseReturn));
        printData.setSourceAttachments(buildPurchaseReturnSourceAttachments(purchaseReturn.getFileUrl()));
        return success(printData);
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购退货分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:query')")
    public CommonResult<PageResult<ErpPurchaseReturnRespVO>> getPurchaseReturnPage(@Valid ErpPurchaseReturnPageReqVO pageReqVO) {
        PageResult<ErpPurchaseReturnDO> pageResult = purchaseReturnService.getPurchaseReturnPage(pageReqVO);
        return success(buildPurchaseReturnVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购退货 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseReturnExcel(@Valid ErpPurchaseReturnPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseReturnRespVO> list = buildPurchaseReturnVOPageResult(purchaseReturnService.getPurchaseReturnPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购退货.xls", "数据", ErpPurchaseReturnRespVO.class, list);
    }

    private PageResult<ErpPurchaseReturnRespVO> buildPurchaseReturnVOPageResult(PageResult<ErpPurchaseReturnDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 退货项
        List<ErpPurchaseReturnItemDO> purchaseReturnItemList = purchaseReturnService.getPurchaseReturnItemListByReturnIds(
                convertSet(pageResult.getList(), ErpPurchaseReturnDO::getId));
        Map<Long, List<ErpPurchaseReturnItemDO>> purchaseReturnItemMap = convertMultiMap(purchaseReturnItemList, ErpPurchaseReturnItemDO::getReturnId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseReturnItemList, ErpPurchaseReturnItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpPurchaseReturnDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), purchaseReturn -> parseUserId(purchaseReturn.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpPurchaseReturnRespVO.class, purchaseReturn -> {
            purchaseReturn.setItems(BeanUtils.toBean(purchaseReturnItemMap.get(purchaseReturn.getId()), ErpPurchaseReturnRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
            purchaseReturn.setProductNames(CollUtil.join(purchaseReturn.getItems(), "，", ErpPurchaseReturnRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseReturn.getSupplierId(), supplier -> purchaseReturn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, parseUserId(purchaseReturn.getCreator()), user -> purchaseReturn.setCreatorName(user.getNickname()));
        });
    }

    private List<ErpPurchaseReturnPrintDataRespVO.SourceAttachment> buildPurchaseReturnSourceAttachments(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return Collections.emptyList();
        }
        return Arrays.stream(fileUrl.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(url -> {
                    ErpPurchaseReturnPrintDataRespVO.SourceAttachment attachment = new ErpPurchaseReturnPrintDataRespVO.SourceAttachment();
                    attachment.setUrl(url);
                    attachment.setName(resolveAttachmentName(url));
                    return attachment;
                })
                .toList();
    }

    private ErpPurchaseReturnPrintDataRespVO.FinancialFacts buildPurchaseReturnFinancialFacts(ErpPurchaseReturnRespVO purchaseReturn) {
        ErpPurchaseReturnPrintDataRespVO.FinancialFacts financialFacts = new ErpPurchaseReturnPrintDataRespVO.FinancialFacts();
        financialFacts.setAccountId(purchaseReturn.getAccountId());
        financialFacts.setTotalCount(purchaseReturn.getTotalCount());
        financialFacts.setTotalProductPrice(purchaseReturn.getTotalProductPrice());
        financialFacts.setTotalTaxPrice(purchaseReturn.getTotalTaxPrice());
        financialFacts.setDiscountPercent(purchaseReturn.getDiscountPercent());
        financialFacts.setDiscountPrice(purchaseReturn.getDiscountPrice());
        financialFacts.setOtherPrice(purchaseReturn.getOtherPrice());
        financialFacts.setTotalPrice(purchaseReturn.getTotalPrice());
        financialFacts.setRefundPrice(purchaseReturn.getRefundPrice());
        financialFacts.setRemainingPrice(resolveRemainingPrice(purchaseReturn.getTotalPrice(), purchaseReturn.getRefundPrice()));
        financialFacts.setRefundProgressName(resolveRefundProgressName(purchaseReturn.getTotalPrice(), purchaseReturn.getRefundPrice()));
        if (purchaseReturn.getAccountId() != null) {
            Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(List.of(purchaseReturn.getAccountId()));
            MapUtils.findAndThen(accountMap, purchaseReturn.getAccountId(), account -> financialFacts.setAccountName(account.getName()));
        }
        return financialFacts;
    }

    private BigDecimal resolveRemainingPrice(BigDecimal totalPrice, BigDecimal refundPrice) {
        BigDecimal remainingPrice = defaultBigDecimal(totalPrice).subtract(defaultBigDecimal(refundPrice));
        return remainingPrice.compareTo(BigDecimal.ZERO) > 0 ? remainingPrice : BigDecimal.ZERO;
    }

    private String resolveRefundProgressName(BigDecimal totalPrice, BigDecimal refundPrice) {
        BigDecimal total = defaultBigDecimal(totalPrice);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return "-";
        }
        BigDecimal refunded = defaultBigDecimal(refundPrice);
        if (refunded.compareTo(BigDecimal.ZERO) <= 0) {
            return "待退款";
        }
        if (refunded.compareTo(total) >= 0) {
            return "已退清";
        }
        return "部分退款";
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String resolveAttachmentName(String url) {
        String cleanUrl = url.split("\\?")[0].split("#")[0];
        String[] segments = cleanUrl.split("/");
        return segments.length == 0 ? "附件" : segments[segments.length - 1];
    }

}
