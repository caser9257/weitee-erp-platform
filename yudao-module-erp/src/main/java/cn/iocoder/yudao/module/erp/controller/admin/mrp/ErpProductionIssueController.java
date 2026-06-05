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
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePrintDataRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherDO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionCostService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionIssueService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
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
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 生产领料")
@RestController
@RequestMapping("/erp/production-material-issue")
@Validated
public class ErpProductionIssueController {

    @Resource
    private ErpProductionIssueService productionIssueService;
    @Resource
    private ErpProductionCostService productionCostService;
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

    @PostMapping("/recommend")
    @Operation(summary = "自动推荐领料批次")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:recommend')")
    public CommonResult<ErpProductionIssueRecommendRespVO> recommend(@Valid @RequestBody ErpProductionIssueRecommendReqVO reqVO) {
        return success(productionIssueService.recommend(reqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建生产领料")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:create')")
    public CommonResult<Long> createProductionIssue(@Valid @RequestBody ErpProductionIssueCreateReqVO reqVO) {
        return success(productionIssueService.createProductionIssue(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产领料单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<ErpProductionIssueRespVO> getProductionIssue(@RequestParam("id") Long id) {
        ErpProductionIssueDO issue = productionIssueService.getProductionIssue(id);
        if (issue == null) {
            return success(null);
        }
        List<ErpProductionIssueItemDO> itemList = productionIssueService.getProductionIssueItemListByIssueId(id);
        return success(buildIssueVO(issue, itemList));
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获得生产领料打印数据")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<ErpProductionIssuePrintDataRespVO> getProductionIssuePrintData(@RequestParam("id") Long id) {
        ErpProductionIssueRespVO issue = getProductionIssue(id).getData();
        if (issue == null) {
            return success(null);
        }
        ErpProductionIssuePrintDataRespVO printData = new ErpProductionIssuePrintDataRespVO();
        printData.setProductionIssue(issue);
        printData.setFinancialFacts(buildFinancialFacts(issue));
        printData.setSourceAttachments(Collections.emptyList());
        return success(printData);
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产领料单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<PageResult<ErpProductionIssueRespVO>> getProductionIssuePage(@Valid ErpProductionIssuePageReqVO pageReqVO) {
        PageResult<ErpProductionIssueDO> pageResult = productionIssueService.getProductionIssuePage(pageReqVO);
        return success(buildIssueVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产领料单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductionIssueExcel(@Valid ErpProductionIssuePageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpProductionIssueRespVO> list = buildIssueVOPageResult(productionIssueService.getProductionIssuePage(pageReqVO)).getList();
        ExcelUtils.write(response, "生产领料单.xls", "数据", ErpProductionIssueRespVO.class, list);
    }

    private PageResult<ErpProductionIssueRespVO> buildIssueVOPageResult(PageResult<ErpProductionIssueDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<Long> productionOrderIds = new ArrayList<>(convertSet(pageResult.getList(), ErpProductionIssueDO::getProductionOrderId));
        productionOrderIds.removeIf(item -> item == null);
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderIds.isEmpty()
                ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(productionOrderIds).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        List<Long> creatorIds = new ArrayList<>(convertList(pageResult.getList(), issue -> parseUserId(issue.getCreator())));
        creatorIds.removeIf(item -> item == null);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);
        return new PageResult<>(convertList(pageResult.getList(), issue -> buildIssuePageVO(issue, orderMap, userMap)), pageResult.getTotal());
    }

    private ErpProductionIssueRespVO buildIssuePageVO(ErpProductionIssueDO issue,
                                                       Map<Long, ErpProductionOrderDO> orderMap,
                                                       Map<Long, AdminUserRespDTO> userMap) {
        ErpProductionIssueRespVO vo = BeanUtils.toBean(issue, ErpProductionIssueRespVO.class);
        MapUtils.findAndThen(orderMap, issue.getProductionOrderId(), order -> vo.setProductionOrderNo(order.getOrderNo()));
        MapUtils.findAndThen(userMap, parseUserId(issue.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        vo.setStatusName(resolveStatusName(issue.getStatus()));
        return vo;
    }

    private ErpProductionIssueRespVO buildIssueVO(ErpProductionIssueDO issue, List<ErpProductionIssueItemDO> itemList) {
        Map<Long, ErpProductionOrderDO> orderMap = issue.getProductionOrderId() == null
                ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(Collections.singleton(issue.getProductionOrderId())).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Long creatorId = parseUserId(issue.getCreator());
        Map<Long, AdminUserRespDTO> userMap = creatorId == null ? Collections.emptyMap() : adminUserApi.getUserMap(Collections.singleton(creatorId));
        Map<Long, ErpProductRespVO> productMap = CollUtil.isEmpty(itemList) ? Collections.emptyMap()
                : productService.getProductVOMap(convertSet(itemList, ErpProductionIssueItemDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = CollUtil.isEmpty(itemList) ? Collections.emptyMap()
                : warehouseService.getWarehouseMap(convertSet(itemList, ErpProductionIssueItemDO::getWarehouseId));
        Map<Long, List<ErpProductionIssueBatchDO>> issueBatchMap = CollUtil.isEmpty(itemList) ? Collections.emptyMap()
                : productionIssueService.getProductionIssueBatchListByIssueItemIds(convertSet(itemList, ErpProductionIssueItemDO::getId)).stream()
                .collect(Collectors.groupingBy(ErpProductionIssueBatchDO::getIssueItemId));

        ErpProductionIssueRespVO vo = BeanUtils.toBean(issue, ErpProductionIssueRespVO.class);
        MapUtils.findAndThen(orderMap, issue.getProductionOrderId(), order -> vo.setProductionOrderNo(order.getOrderNo()));
        MapUtils.findAndThen(userMap, creatorId, user -> vo.setCreatorName(user.getNickname()));
        vo.setStatusName(resolveStatusName(issue.getStatus()));
        vo.setIssueAmount(resolveIssueAmount(issue, itemList));
        vo.setItems(convertList(itemList, item -> {
            ErpProductionIssueRespVO.Item itemVO = BeanUtils.toBean(item, ErpProductionIssueRespVO.Item.class);
            MapUtils.findAndThen(productMap, item.getMaterialId(), product -> itemVO
                    .setMaterialName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setMaterialBarCode(product.getBarCode())
                    .setProductUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> itemVO.setWarehouseName(warehouse.getName()));
            itemVO.setIssueAmount(resolveIssueAmount(item));
            itemVO.setBatches(convertList(issueBatchMap.getOrDefault(item.getId(), Collections.emptyList()),
                    batch -> BeanUtils.toBean(batch, ErpProductionIssueRespVO.Batch.class)));
            return itemVO;
        }));
        return vo;
    }

    private BigDecimal resolveIssueAmount(ErpProductionIssueDO issue, List<ErpProductionIssueItemDO> itemList) {
        if (issue.getIssueAmount() != null) {
            return issue.getIssueAmount();
        }
        return itemList.stream()
                .map(this::resolveIssueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveIssueAmount(ErpProductionIssueItemDO item) {
        return item.getIssueAmount() == null ? BigDecimal.ZERO : item.getIssueAmount();
    }

    private String resolveStatusName(Integer status) {
        return status != null && status == 20 ? "已完成" : null;
    }

    private ErpProductionIssuePrintDataRespVO.FinancialFacts buildFinancialFacts(ErpProductionIssueRespVO issue) {
        ErpProductionIssuePrintDataRespVO.FinancialFacts financialFacts = new ErpProductionIssuePrintDataRespVO.FinancialFacts();
        financialFacts.setIssueItemCount(issue.getItems() == null ? 0 : issue.getItems().size());
        financialFacts.setIssueBatchCount(issue.getItems() == null ? 0 : issue.getItems().stream()
                .mapToInt(item -> item.getBatches() == null ? 0 : item.getBatches().size())
                .sum());
        ErpProductionIssueVoucherDO voucher = productionIssueVoucherService.getVoucherByIssueId(issue.getId());
        if (voucher != null) {
            financialFacts.setVoucherNo(voucher.getVoucherNo());
            financialFacts.setVoucherStatusName(resolveVoucherStatusName(voucher.getStatus()));
        }
        if (issue.getProductionOrderId() != null) {
            try {
                ErpProductionCostDetailRespVO costDetail = productionCostService.getCostDetail(issue.getProductionOrderId());
                financialFacts.setCostSnapshotTime(costDetail.getCostSnapshotTime());
                financialFacts.setMaterialCost(costDetail.getMaterialCost());
                financialFacts.setTotalCost(costDetail.getTotalCost());
                financialFacts.setUnitCost(costDetail.getUnitCost());
                financialFacts.setCostIssueCount(costDetail.getMaterialDetails() == null ? 0 : costDetail.getMaterialDetails().size());
            } catch (Exception ignored) {
                financialFacts.setCostIssueCount(0);
            }
        }
        return financialFacts;
    }

    private String resolveVoucherStatusName(Integer status) {
        return status != null && status == 10 ? "已生成" : "未生成";
    }

}
