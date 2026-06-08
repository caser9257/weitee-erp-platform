package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementExportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPaymentEnablePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPaymentEnableRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPrintDataRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementUpdateInvoiceReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseReturnService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 应付台账")
@RestController
@RequestMapping("/erp/ap-statement")
@Validated
public class ErpApStatementController {

    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpPurchaseReturnService purchaseReturnService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得应付台账分页")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<PageResult<ErpApStatementRespVO>> getApStatementPage(@Valid ErpApStatementPageReqVO reqVO) {
        return success(buildApStatementPageResult(apStatementService.getApStatementPage(reqVO)));
    }

    @GetMapping("/get")
    @Operation(summary = "获得应付台账详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<ErpApStatementRespVO> getApStatement(@RequestParam("id") Long id) {
        ErpApStatementDO statement = apStatementService.getApStatement(id);
        if (statement == null) {
            return success(null);
        }
        List<ErpApStatementItemDO> itemList = apStatementService.getApStatementItemListByStatementId(id);
        return success(buildApStatementRespVO(statement, itemList));
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获得应付台账打印数据")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<ErpApStatementPrintDataRespVO> getApStatementPrintData(@RequestParam("id") Long id) {
        ErpApStatementDO statement = apStatementService.getApStatement(id);
        if (statement == null) {
            return success(null);
        }
        List<ErpApStatementItemDO> itemList = apStatementService.getApStatementItemListByStatementId(id);
        ErpApStatementRespVO statementVO = buildApStatementRespVO(statement, itemList);
        ErpApStatementPrintDataRespVO printData = new ErpApStatementPrintDataRespVO();
        printData.setStatement(statementVO);
        printData.setSourceAttachments(buildSourceAttachmentLinks(statement.getBizType(), statement.getBizId()));
        return success(printData);
    }

    @PutMapping("/update-invoice")
    @Operation(summary = "更新应付台账收票信息")
    @PreAuthorize("@ss.hasAnyPermissions('erp:ap-statement:update', 'erp:ap-statement:query')")
    public CommonResult<Boolean> updateInvoice(@Valid @RequestBody ErpApStatementUpdateInvoiceReqVO updateReqVO) {
        apStatementService.updateInvoice(updateReqVO);
        return success(true);
    }

    @GetMapping("/summary")
    @Operation(summary = "获得应付台账汇总")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<List<ErpApStatementSummaryRespVO>> getSummary(
            @RequestParam(value = "supplierId", required = false) Long supplierId) {
        List<ErpApStatementSummaryRespVO> list = apStatementService.getSummaryList(supplierId);
        fillSummarySupplierNames(list);
        return success(list);
    }

    @GetMapping("/payment-enable-page")
    @Operation(summary = "获得可核销应付分页")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<PageResult<ErpApStatementPaymentEnableRespVO>> getPaymentEnablePage(
            @Valid ErpApStatementPaymentEnablePageReqVO reqVO) {
        PageResult<ErpApStatementDO> pageResult = apStatementService.getPaymentEnablePage(reqVO);
        return success(buildPaymentEnablePageResult(pageResult));
    }

    @GetMapping("/aging")
    @Operation(summary = "获得应付账龄")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<List<ErpApStatementAgingRespVO>> getAgingList(@Valid ErpApStatementAgingReqVO reqVO) {
        List<ErpApStatementAgingRespVO> list = apStatementService.getAgingList(reqVO);
        fillAgingSupplierNames(list);
        return success(list);
    }

    @GetMapping("/reconciliation")
    @Operation(summary = "获得供应商对账分页")
    @PreAuthorize("@ss.hasPermission('erp:ap-statement:query')")
    public CommonResult<PageResult<ErpApStatementReconciliationRespVO>> getReconciliationPage(
            @Valid ErpApStatementReconciliationReqVO reqVO) {
        PageResult<ErpApStatementReconciliationRespVO> pageResult = apStatementService.getReconciliationPage(reqVO);
        fillReconciliationNames(pageResult.getList());
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出应付台账 Excel")
    @PreAuthorize("@ss.hasAnyPermissions('erp:ap-statement:export', 'erp:ap-statement:query')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportApStatementExcel(@Valid ErpApStatementPageReqVO reqVO,
                                       HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpApStatementRespVO> list = buildApStatementPageResult(apStatementService.getApStatementPage(reqVO)).getList();
        ExcelUtils.write(response, "应付台账.xls", "数据", ErpApStatementExportRespVO.class, buildApStatementExportList(list));
    }

    private PageResult<ErpApStatementRespVO> buildApStatementPageResult(PageResult<ErpApStatementDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpApStatementDO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(pageResult.getList(), ErpApStatementDO::getAccountId));
        return BeanUtils.toBean(pageResult, ErpApStatementRespVO.class, respVO -> {
            MapUtils.findAndThen(supplierMap, respVO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, respVO.getAccountId(), account -> respVO.setAccountName(account.getName()));
        });
    }

    private ErpApStatementRespVO buildApStatementRespVO(ErpApStatementDO statement, List<ErpApStatementItemDO> itemList) {
        ErpApStatementRespVO respVO = BeanUtils.toBean(statement, ErpApStatementRespVO.class);
        Map<Long, ErpSupplierDO> supplierMap = statement.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(List.of(statement.getSupplierId()));
        MapUtils.findAndThen(supplierMap, statement.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
        Map<Long, ErpAccountDO> accountMap = statement.getAccountId() == null
                ? Collections.emptyMap()
                : accountService.getAccountMap(List.of(statement.getAccountId()));
        MapUtils.findAndThen(accountMap, statement.getAccountId(), account -> respVO.setAccountName(account.getName()));
        List<Long> operatorIds = itemList.stream()
                .map(ErpApStatementItemDO::getCreator)
                .map(ErpApStatementController::parseCreatorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, AdminUserRespDTO> operatorMap = operatorIds.isEmpty()
                ? Collections.emptyMap()
                : adminUserApi.getUserMap(operatorIds);
        respVO.setItems(itemList.stream().map(sourceItem -> {
            ErpApStatementRespVO.Item item = BeanUtils.toBean(sourceItem, ErpApStatementRespVO.Item.class);
            MapUtils.findAndThen(operatorMap, parseCreatorId(sourceItem.getCreator()),
                    user -> item.setOperatorName(user.getNickname()));
            return item;
        }).toList());
        return respVO;
    }

    private List<ErpApStatementExportRespVO> buildApStatementExportList(List<ErpApStatementRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return BeanUtils.toBean(list, ErpApStatementExportRespVO.class, item -> {
            item.setBizTypeName(resolveBizTypeName(item.getBizType()));
            item.setInvoiceStatusName(resolveInvoiceStatusName(item.getInvoiceStatus()));
            item.setStatusName(resolveStatementStatusName(item.getStatus()));
        });
    }

    private List<ErpApStatementPrintDataRespVO.AttachmentLink> buildSourceAttachmentLinks(Integer bizType, Long bizId) {
        if (bizType == null || bizId == null) {
            return Collections.emptyList();
        }
        String fileUrl = null;
        if (ErpBizTypeEnum.PURCHASE_IN.getType().equals(bizType)) {
            ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(bizId);
            fileUrl = purchaseIn == null ? null : purchaseIn.getFileUrl();
        } else if (ErpBizTypeEnum.PURCHASE_RETURN.getType().equals(bizType)) {
            ErpPurchaseReturnDO purchaseReturn = purchaseReturnService.getPurchaseReturn(bizId);
            fileUrl = purchaseReturn == null ? null : purchaseReturn.getFileUrl();
        }
        return buildAttachmentLinks(fileUrl);
    }

    private List<ErpApStatementPrintDataRespVO.AttachmentLink> buildAttachmentLinks(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return Collections.emptyList();
        }
        return Arrays.stream(fileUrl.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(url -> {
                    ErpApStatementPrintDataRespVO.AttachmentLink link = new ErpApStatementPrintDataRespVO.AttachmentLink();
                    link.setUrl(url);
                    link.setName(resolveAttachmentName(url));
                    return link;
                })
                .toList();
    }

    private String resolveAttachmentName(String url) {
        String cleanUrl = url.split("\\?")[0].split("#")[0];
        String fileName = cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1);
        if (StrUtil.isBlank(fileName)) {
            return "附件";
        }
        try {
            return java.net.URLDecoder.decode(fileName, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception ignored) {
            return fileName;
        }
    }

    private PageResult<ErpApStatementPaymentEnableRespVO> buildPaymentEnablePageResult(PageResult<ErpApStatementDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpApStatementDO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(pageResult.getList(), ErpApStatementDO::getAccountId));
        return BeanUtils.toBean(pageResult, ErpApStatementPaymentEnableRespVO.class, respVO -> {
            MapUtils.findAndThen(supplierMap, respVO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, respVO.getAccountId(), account -> respVO.setAccountName(account.getName()));
        });
    }

    private void fillSummarySupplierNames(List<ErpApStatementSummaryRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(list, ErpApStatementSummaryRespVO::getSupplierId));
        list.forEach(item ->
                MapUtils.findAndThen(supplierMap, item.getSupplierId(), supplier -> item.setSupplierName(supplier.getName())));
    }

    private void fillAgingSupplierNames(List<ErpApStatementAgingRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(list, ErpApStatementAgingRespVO::getSupplierId));
        list.forEach(item ->
                MapUtils.findAndThen(supplierMap, item.getSupplierId(), supplier -> item.setSupplierName(supplier.getName())));
    }

    private void fillReconciliationNames(List<ErpApStatementReconciliationRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(list, ErpApStatementReconciliationRespVO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(list, ErpApStatementReconciliationRespVO::getAccountId));
        list.forEach(item -> {
            MapUtils.findAndThen(supplierMap, item.getSupplierId(), supplier -> item.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, item.getAccountId(), account -> item.setAccountName(account.getName()));
        });
    }

    private String resolveBizTypeName(Integer bizType) {
        return Arrays.stream(ErpBizTypeEnum.values())
                .filter(item -> ObjectUtil.equal(item.getType(), bizType))
                .map(ErpBizTypeEnum::getName)
                .findFirst()
                .orElse("");
    }

    private String resolveInvoiceStatusName(Integer invoiceStatus) {
        return Arrays.stream(ErpApInvoiceStatusEnum.values())
                .filter(item -> ObjectUtil.equal(item.getStatus(), invoiceStatus))
                .map(ErpApInvoiceStatusEnum::getName)
                .findFirst()
                .orElse("");
    }

    private String resolveStatementStatusName(Integer status) {
        return Arrays.stream(ErpApStatementStatusEnum.values())
                .filter(item -> ObjectUtil.equal(item.getStatus(), status))
                .map(ErpApStatementStatusEnum::getName)
                .findFirst()
                .orElse("");
    }

    private static Long parseCreatorId(String creator) {
        return parseUserId(creator);
    }

}
