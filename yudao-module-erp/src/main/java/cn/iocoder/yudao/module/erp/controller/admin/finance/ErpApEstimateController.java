package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateActionReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimatePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateScanReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateTraceRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApEstimateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApEstimateItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.enums.ErpApEstimateClosureStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApEstimateReverseTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApEstimateStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.finance.ErpApEstimateService;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 暂估单")
@RestController
@RequestMapping("/erp/ap-estimate")
@Validated
public class ErpApEstimateController {

    @Resource
    private ErpApEstimateService apEstimateService;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/scan-month")
    @Operation(summary = "扫描月末暂估")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:scan')")
    public CommonResult<Integer> scanMonth(@Valid @RequestBody ErpApEstimateScanReqVO reqVO) {
        return success(apEstimateService.generateMonthEstimate(reqVO));
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认暂估")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:update')")
    public CommonResult<Boolean> confirm(@Valid @RequestBody ErpApEstimateActionReqVO reqVO) {
        apEstimateService.confirmApEstimate(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/reverse")
    @Operation(summary = "冲回暂估")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:update')")
    public CommonResult<Boolean> reverse(@Valid @RequestBody ErpApEstimateActionReqVO reqVO) {
        apEstimateService.reverseApEstimate(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得暂估单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:query')")
    public CommonResult<ErpApEstimateRespVO> getApEstimate(@RequestParam("id") Long id) {
        ErpApEstimateDO estimate = apEstimateService.getApEstimate(id);
        if (estimate == null) {
            return success(null);
        }
        List<ErpApEstimateItemDO> itemList = apEstimateService.getApEstimateItemListByEstimateId(id);
        ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(
                estimate.getSourceBizType(), estimate.getSourceBizId());
        return success(buildEstimateVO(estimate, itemList, statement));
    }

    @GetMapping("/get-trace")
    @Operation(summary = "获得暂估闭环追溯")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:query')")
    public CommonResult<ErpApEstimateTraceRespVO> getApEstimateTrace(@RequestParam("id") Long id) {
        ErpApEstimateDO estimate = apEstimateService.getApEstimate(id);
        if (estimate == null) {
            return success(null);
        }
        List<ErpApEstimateItemDO> itemList = apEstimateService.getApEstimateItemListByEstimateId(id);
        ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(
                estimate.getSourceBizType(), estimate.getSourceBizId());
        ErpApEstimateTraceRespVO traceRespVO = new ErpApEstimateTraceRespVO();
        traceRespVO.setEstimate(buildEstimateVO(estimate, itemList, statement));
        traceRespVO.setStatement(buildTraceStatement(statement));
        return success(traceRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得暂估单分页")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:query')")
    public CommonResult<PageResult<ErpApEstimateRespVO>> getApEstimatePage(@Valid ErpApEstimatePageReqVO pageReqVO) {
        PageResult<ErpApEstimateDO> pageResult = apEstimateService.getApEstimatePage(pageReqVO);
        return success(buildEstimateVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出暂估单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:ap-estimate:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportApEstimateExcel(@Valid ErpApEstimatePageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpApEstimateRespVO> list = buildEstimateVOPageResult(apEstimateService.getApEstimatePage(pageReqVO)).getList();
        ExcelUtils.write(response, "暂估单.xls", "数据", ErpApEstimateRespVO.class, list);
    }

    private PageResult<ErpApEstimateRespVO> buildEstimateVOPageResult(PageResult<ErpApEstimateDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpApEstimateDO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(pageResult.getList(), ErpApEstimateDO::getAccountId));
        Map<Long, ErpApStatementDO> statementMap = buildStatementMap(pageResult.getList());
        List<Long> creatorIds = new ArrayList<>(convertList(pageResult.getList(),
                estimate -> parseCreatorId(estimate.getCreator())));
        creatorIds.removeIf(item -> item == null);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);
        return new PageResult<>(pageResult.getList().stream().map(estimate -> {
            ErpApEstimateRespVO vo = BeanUtils.toBean(estimate, ErpApEstimateRespVO.class);
            vo.setStatusName(getEstimateStatusName(estimate.getStatus()));
            MapUtils.findAndThen(supplierMap, estimate.getSupplierId(), supplier -> vo.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, estimate.getAccountId(), account -> vo.setAccountName(account.getName()));
            Long creatorId = parseCreatorId(estimate.getCreator());
            MapUtils.findAndThen(userMap, creatorId, user -> vo.setCreatorName(user.getNickname()));
            fillEstimateStatementSummary(vo, estimate, statementMap.get(estimate.getSourceBizId()));
            return vo;
        }).collect(Collectors.toList()), pageResult.getTotal());
    }

    private ErpApEstimateRespVO buildEstimateVO(ErpApEstimateDO estimate, List<ErpApEstimateItemDO> itemList,
                                                ErpApStatementDO statement) {
        Map<Long, ErpSupplierDO> supplierMap = estimate.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(Collections.singleton(estimate.getSupplierId()));
        Map<Long, ErpAccountDO> accountMap = estimate.getAccountId() == null
                ? Collections.emptyMap()
                : accountService.getAccountMap(Collections.singleton(estimate.getAccountId()));
        Long creatorId = parseCreatorId(estimate.getCreator());
        Map<Long, AdminUserRespDTO> userMap = creatorId == null ? Collections.emptyMap() : adminUserApi.getUserMap(Collections.singleton(creatorId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(itemList, ErpApEstimateItemDO::getProductId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(itemList, ErpApEstimateItemDO::getWarehouseId));
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(convertSet(itemList, ErpApEstimateItemDO::getProjectId));

        ErpApEstimateRespVO vo = BeanUtils.toBean(estimate, ErpApEstimateRespVO.class);
        vo.setStatusName(getEstimateStatusName(estimate.getStatus()));
        MapUtils.findAndThen(supplierMap, estimate.getSupplierId(), supplier -> vo.setSupplierName(supplier.getName()));
        MapUtils.findAndThen(accountMap, estimate.getAccountId(), account -> vo.setAccountName(account.getName()));
        MapUtils.findAndThen(userMap, creatorId, user -> vo.setCreatorName(user.getNickname()));
        fillEstimateStatementSummary(vo, estimate, statement);
        vo.setItems(BeanUtils.toBean(itemList, ErpApEstimateRespVO.Item.class, item -> {
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setProductBarCode(product.getBarCode())
                    .setProductUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> item.setWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(projectMap, item.getProjectId(), project -> item.setProjectName(project.getName()));
        }));
        return vo;
    }

    private Map<Long, ErpApStatementDO> buildStatementMap(List<ErpApEstimateDO> estimates) {
        List<Long> bizIds = estimates.stream()
                .filter(item -> item.getSourceBizType() != null && item.getSourceBizId() != null)
                .map(ErpApEstimateDO::getSourceBizId)
                .distinct()
                .collect(Collectors.toList());
        if (bizIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return apStatementService.getApStatementListByBizTypeAndBizIds(
                cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum.PURCHASE_IN.getType(), bizIds)
                .stream()
                .collect(Collectors.toMap(ErpApStatementDO::getBizId, item -> item, (left, right) -> left));
    }

    private void fillEstimateStatementSummary(ErpApEstimateRespVO vo, ErpApEstimateDO estimate, ErpApStatementDO statement) {
        vo.setReverseTypeName(getEstimateReverseTypeName(estimate.getReverseType()));
        vo.setClosureStatus(resolveClosureStatus(estimate, statement));
        vo.setClosureStatusName(getEstimateClosureStatusName(vo.getClosureStatus()));
        if (statement == null) {
            return;
        }
        vo.setSourceStatementId(statement.getId());
        vo.setSourceStatementNo(statement.getStatementNo());
        vo.setSourceStatementStatus(statement.getStatus());
        vo.setSourceStatementStatusName(getStatementStatusName(statement.getStatus()));
        vo.setSourceStatementInvoiceStatus(statement.getInvoiceStatus());
        vo.setSourceStatementInvoiceStatusName(getInvoiceStatusName(statement.getInvoiceStatus()));
        vo.setSourceStatementInvoiceNo(statement.getInvoiceNo());
        vo.setSourceStatementInvoiceAmount(statement.getInvoiceAmount());
    }

    private Integer resolveClosureStatus(ErpApEstimateDO estimate, ErpApStatementDO statement) {
        if (ErpApEstimateStatusEnum.REVERSED.getStatus().equals(estimate.getStatus())) {
            if (ErpApEstimateReverseTypeEnum.INVOICE.getStatus().equals(estimate.getReverseType())) {
                return ErpApEstimateClosureStatusEnum.REVERSED_BY_INVOICE.getStatus();
            }
            if (ErpApEstimateReverseTypeEnum.STATEMENT_CLOSED.getStatus().equals(estimate.getReverseType())) {
                return ErpApEstimateClosureStatusEnum.REVERSED_BY_STATEMENT_CLOSED.getStatus();
            }
            return ErpApEstimateClosureStatusEnum.REVERSED_MANUAL.getStatus();
        }
        if (statement != null && ErpApStatementStatusEnum.CLOSED.getStatus().equals(statement.getStatus())) {
            return ErpApEstimateClosureStatusEnum.EXCEPTION_CLOSED_NOT_REVERSED.getStatus();
        }
        if (statement != null && !ErpApInvoiceStatusEnum.NONE.getStatus().equals(statement.getInvoiceStatus())) {
            return ErpApEstimateClosureStatusEnum.EXCEPTION_BILLED_NOT_REVERSED.getStatus();
        }
        if (ErpApEstimateStatusEnum.CONFIRMED.getStatus().equals(estimate.getStatus())) {
            return ErpApEstimateClosureStatusEnum.CONFIRMED_OPEN.getStatus();
        }
        return ErpApEstimateClosureStatusEnum.GENERATED_OPEN.getStatus();
    }

    private ErpApEstimateTraceRespVO.Statement buildTraceStatement(ErpApStatementDO statement) {
        if (statement == null) {
            return null;
        }
        ErpApEstimateTraceRespVO.Statement traceStatement = BeanUtils.toBean(statement, ErpApEstimateTraceRespVO.Statement.class);
        traceStatement.setStatusName(getStatementStatusName(statement.getStatus()));
        traceStatement.setInvoiceStatusName(getInvoiceStatusName(statement.getInvoiceStatus()));
        return traceStatement;
    }

    private String getEstimateStatusName(Integer status) {
        ErpApEstimateStatusEnum statusEnum = ErpApEstimateStatusEnum.fromStatus(status);
        return statusEnum != null ? statusEnum.getName() : null;
    }

    private String getEstimateReverseTypeName(Integer reverseType) {
        ErpApEstimateReverseTypeEnum reverseTypeEnum = ErpApEstimateReverseTypeEnum.fromStatus(reverseType);
        return reverseTypeEnum != null ? reverseTypeEnum.getName() : null;
    }

    private String getEstimateClosureStatusName(Integer closureStatus) {
        if (closureStatus == null) {
            return null;
        }
        return java.util.Arrays.stream(ErpApEstimateClosureStatusEnum.values())
                .filter(item -> item.getStatus().equals(closureStatus))
                .map(ErpApEstimateClosureStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

    private Long parseCreatorId(String creator) {
        return StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

    private String getInvoiceStatusName(Integer invoiceStatus) {
        if (invoiceStatus == null) {
            return null;
        }
        return java.util.Arrays.stream(ErpApInvoiceStatusEnum.values())
                .filter(item -> item.getStatus().equals(invoiceStatus))
                .map(ErpApInvoiceStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

    private String getStatementStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return java.util.Arrays.stream(ErpApStatementStatusEnum.values())
                .filter(item -> item.getStatus().equals(status))
                .map(ErpApStatementStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

}
