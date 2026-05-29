package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInAuditLogRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInConfirmStockInReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPrintDataRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInStockExecuteCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInBpmService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseSourceBatchService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants.TASK_VARIABLE_REASON;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants.TASK_VARIABLE_STATUS;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 采购入库")
@RestController
@RequestMapping("/erp/purchase-in")
@Validated
public class ErpPurchaseInController {

    private static final String PRODUCT_NAME_DELIMITER = ", ";

    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpPurchaseInBpmService purchaseInBpmService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpPurchaseSourceBatchService purchaseSourceBatchService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmTaskService bpmTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:create')")
    public CommonResult<Long> createPurchaseIn(@Valid @RequestBody ErpPurchaseInSaveReqVO createReqVO) {
        return success(purchaseInService.createPurchaseIn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update')")
    public CommonResult<Boolean> updatePurchaseIn(@Valid @RequestBody ErpPurchaseInSaveReqVO updateReqVO) {
        purchaseInService.updatePurchaseIn(updateReqVO);
        return success(true);
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量更新采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update')")
    public CommonResult<ErpPurchaseInBatchUpdateResultVO> updatePurchaseInBatch(
            @Valid @RequestBody ErpPurchaseInBatchUpdateReqVO reqVO) {
        return success(purchaseInService.updatePurchaseInBatch(reqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新采购入库状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update-status')")
    public CommonResult<Boolean> updatePurchaseInStatus(@RequestParam("id") Long id,
                                                        @RequestParam("status") Integer status) {
        purchaseInService.updatePurchaseInStatus(id, status);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交采购入库审批")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:submit')")
    public CommonResult<String> submitPurchaseIn(@Valid @RequestBody ErpPurchaseInSubmitReqVO reqVO) {
        return success(purchaseInBpmService.submitPurchaseIn(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/cancel-approval")
    @Operation(summary = "撤回采购入库审批")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:cancel-approval')")
    public CommonResult<Boolean> cancelPurchaseInApproval(@Valid @RequestBody ErpPurchaseInCancelApprovalReqVO reqVO) {
        purchaseInBpmService.cancelPurchaseInApproval(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/quality-check")
    @Operation(summary = "采购入库质检")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update')")
    public CommonResult<Boolean> qualityCheckPurchaseIn(@Valid @RequestBody ErpPurchaseInQualityCheckReqVO reqVO) {
        purchaseInService.qualityCheckPurchaseIn(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/confirm-stock-in")
    @Operation(summary = "采购入库确认入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update-status')")
    public CommonResult<Boolean> confirmPurchaseInStockIn(@Valid @RequestBody ErpPurchaseInConfirmStockInReqVO reqVO) {
        purchaseInService.confirmPurchaseInStockIn(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/stock-execute/create")
    @Operation(summary = "采购入库执行入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update-status')")
    public CommonResult<Boolean> createPurchaseInStockExecute(
            @Valid @RequestBody ErpPurchaseInStockExecuteCreateReqVO reqVO) {
        purchaseInService.createPurchaseInStockExecute(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购入库")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:delete')")
    public CommonResult<Boolean> deletePurchaseIn(@RequestParam("ids") List<Long> ids) {
        purchaseInService.deletePurchaseIn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购入库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<ErpPurchaseInRespVO> getPurchaseIn(@RequestParam("id") Long id) {
        ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(id);
        if (purchaseIn == null) {
            return success(null);
        }
        List<ErpPurchaseInItemDO> purchaseInItemList = purchaseInService.getPurchaseInItemListByInId(id);
        List<ErpPurchaseInStockExecuteDO> stockExecuteList = purchaseInService.getPurchaseInStockExecuteListByPurchaseInId(id);
        List<ErpPurchaseInStockExecuteItemDO> stockExecuteItems = purchaseInService.getPurchaseInStockExecuteItemListByExecuteIds(
                convertSet(stockExecuteList, ErpPurchaseInStockExecuteDO::getId));
        List<ErpPurchaseInStockExecuteItemBatchDO> stockExecuteItemBatchList =
                purchaseInService.getPurchaseInStockExecuteItemBatchListByExecuteItemIds(
                        convertSet(stockExecuteItems, ErpPurchaseInStockExecuteItemDO::getId));
        List<ErpPurchaseInAuditLogRespVO> businessAuditLogs = ErpPurchaseInDisplaySupport.buildBusinessAuditLogRespVOs(purchaseIn);
        List<ErpPurchaseInAuditLogRespVO> bpmAuditLogs = buildBpmAuditLogRespVOs(purchaseIn.getProcessInstanceId());
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItemList, ErpPurchaseInItemDO::getProductId));
        LinkedHashSet<Long> purchaseSourceBatchIds = new LinkedHashSet<>(convertSet(
                purchaseInItemList, ErpPurchaseInItemDO::getPurchaseSourceBatchId));
        purchaseSourceBatchIds.addAll(convertSet(
                stockExecuteItemBatchList, ErpPurchaseInStockExecuteItemBatchDO::getPurchaseSourceBatchId));
        purchaseSourceBatchIds.remove(null);
        Map<Long, ErpPurchaseSourceBatchDO> purchaseSourceBatchMap =
                purchaseSourceBatchService.getPurchaseSourceBatchMap(purchaseSourceBatchIds);
        Map<Long, List<ErpPurchaseInStockExecuteItemDO>> stockExecuteItemMap = convertMultiMap(
                stockExecuteItems, ErpPurchaseInStockExecuteItemDO::getExecuteId);
        Map<Long, List<ErpPurchaseInStockExecuteItemBatchDO>> stockExecuteItemBatchMap = convertMultiMap(
                stockExecuteItemBatchList, ErpPurchaseInStockExecuteItemBatchDO::getExecuteItemId);
        Map<Long, AdminUserRespDTO> userMap = getAuditUserMap(purchaseIn, bpmAuditLogs, stockExecuteList);
        return success(BeanUtils.toBean(purchaseIn, ErpPurchaseInRespVO.class, purchaseInVO -> {
            purchaseInVO.setQaUserNickname(getUserNickname(userMap, purchaseIn.getQaUserId()));
            purchaseInVO.setStockInUserNickname(getUserNickname(userMap, purchaseIn.getStockInUserId()));
            purchaseInVO.setCreatorName(getUserNickname(userMap, parseUserId(purchaseIn.getCreator())));
            purchaseInVO.setRemainingStockInCount(calculateRemainingStockInCount(
                    purchaseIn.getQaPassCount(), purchaseIn.getStockInCount()));
            purchaseInVO.setItems(BeanUtils.toBean(purchaseInItemList, ErpPurchaseInRespVO.Item.class, item -> {
                ErpStockDO stock = stockService.getStock(item.getProductId(), item.getWarehouseId());
                item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                item.setRemainingStockInCount(calculateRemainingStockInCount(item.getQaPassCount(), item.getStockInCount()));
                MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                        .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName())
                        .setBatchControlFlag(product.getBatchControlFlag()));
                MapUtils.findAndThen(purchaseSourceBatchMap, item.getPurchaseSourceBatchId(),
                        sourceBatch -> item.setPurchaseSourceBatchNo(sourceBatch.getBatchNo()));
            }));
            purchaseInVO.setStockExecuteList(BeanUtils.toBean(stockExecuteList, ErpPurchaseInRespVO.StockExecute.class, execute -> {
                execute.setCreatorName(getUserNickname(userMap, parseUserId(execute.getCreator())));
                execute.setItems(BeanUtils.toBean(stockExecuteItemMap.get(execute.getId()),
                        ErpPurchaseInRespVO.StockExecuteItem.class, item -> {
                            MapUtils.findAndThen(productMap, item.getProductId(), product ->
                                    item.setProductName(product.getName())
                                            .setProductBarCode(product.getBarCode())
                                            .setProductUnitName(product.getUnitName()));
                            item.setBatches(BeanUtils.toBean(stockExecuteItemBatchMap.get(item.getId()),
                                    ErpPurchaseInRespVO.StockExecuteItemBatch.class, batch ->
                                            MapUtils.findAndThen(purchaseSourceBatchMap, batch.getPurchaseSourceBatchId(),
                                                    sourceBatch -> batch.setPurchaseSourceBatchNo(sourceBatch.getBatchNo()))));
                        }));
            }));
            purchaseInVO.setProductNames(CollUtil.join(
                    purchaseInVO.getItems(), PRODUCT_NAME_DELIMITER, ErpPurchaseInRespVO.Item::getProductName));
            if (CollUtil.isNotEmpty(businessAuditLogs) || CollUtil.isNotEmpty(bpmAuditLogs)) {
                purchaseInVO.setAuditLogs(ErpPurchaseInDisplaySupport.mergeAuditLogs(
                        fillAuditUserNickname(businessAuditLogs, userMap),
                        fillAuditUserNickname(bpmAuditLogs, userMap)));
            }
        }));
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获得采购入库打印数据")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<ErpPurchaseInPrintDataRespVO> getPurchaseInPrintData(@RequestParam("id") Long id) {
        ErpPurchaseInRespVO purchaseIn = getPurchaseIn(id).getData();
        if (purchaseIn == null) {
            return success(null);
        }
        ErpPurchaseInPrintDataRespVO printData = new ErpPurchaseInPrintDataRespVO();
        printData.setPurchaseIn(purchaseIn);
        printData.setSourceAttachments(buildPurchaseInSourceAttachments(purchaseIn.getFileUrl()));
        return success(printData);
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购入库分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<PageResult<ErpPurchaseInRespVO>> getPurchaseInPage(@Valid ErpPurchaseInPageReqVO pageReqVO) {
        PageResult<ErpPurchaseInDO> pageResult = purchaseInService.getPurchaseInPage(pageReqVO);
        return success(buildPurchaseInVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购入库 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseInExcel(@Valid ErpPurchaseInPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseInRespVO> list = buildPurchaseInVOPageResult(purchaseInService.getPurchaseInPage(pageReqVO)).getList();
        ExcelUtils.write(response, "采购入库.xls", "数据", ErpPurchaseInRespVO.class, list);
    }

    private PageResult<ErpPurchaseInRespVO> buildPurchaseInVOPageResult(PageResult<ErpPurchaseInDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<ErpPurchaseInItemDO> purchaseInItemList = purchaseInService.getPurchaseInItemListByInIds(
                convertSet(pageResult.getList(), ErpPurchaseInDO::getId));
        Map<Long, List<ErpPurchaseInItemDO>> purchaseInItemMap = convertMultiMap(purchaseInItemList, ErpPurchaseInItemDO::getInId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItemList, ErpPurchaseInItemDO::getProductId));
        Map<Long, ErpPurchaseSourceBatchDO> purchaseSourceBatchMap = purchaseSourceBatchService.getPurchaseSourceBatchMap(
                purchaseInItemList.stream()
                        .map(ErpPurchaseInItemDO::getPurchaseSourceBatchId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet()));
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpPurchaseInDO::getSupplierId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), purchaseIn -> parseUserId(purchaseIn.getCreator())));
        return BeanUtils.toBean(pageResult, ErpPurchaseInRespVO.class, purchaseIn -> {
            purchaseIn.setRemainingStockInCount(calculateRemainingStockInCount(
                    purchaseIn.getQaPassCount(), purchaseIn.getStockInCount()));
            purchaseIn.setItems(BeanUtils.toBean(purchaseInItemMap.get(purchaseIn.getId()), ErpPurchaseInRespVO.Item.class,
                    item -> {
                        item.setRemainingStockInCount(calculateRemainingStockInCount(item.getQaPassCount(), item.getStockInCount()));
                        MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                                .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName())
                                .setBatchControlFlag(product.getBatchControlFlag()));
                        MapUtils.findAndThen(purchaseSourceBatchMap, item.getPurchaseSourceBatchId(),
                                sourceBatch -> item.setPurchaseSourceBatchNo(sourceBatch.getBatchNo()));
                    }));
            purchaseIn.setProductNames(CollUtil.join(
                    purchaseIn.getItems(), PRODUCT_NAME_DELIMITER, ErpPurchaseInRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(), supplier -> purchaseIn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, parseUserId(purchaseIn.getCreator()), user -> purchaseIn.setCreatorName(user.getNickname()));
        });
    }

    private List<ErpPurchaseInPrintDataRespVO.SourceAttachment> buildPurchaseInSourceAttachments(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return Collections.emptyList();
        }
        ErpPurchaseInPrintDataRespVO.SourceAttachment attachment = new ErpPurchaseInPrintDataRespVO.SourceAttachment();
        attachment.setUrl(fileUrl);
        String cleanUrl = fileUrl.split("\\?")[0].split("#")[0];
        String[] segments = cleanUrl.split("/");
        attachment.setName(segments.length == 0 ? "附件" : segments[segments.length - 1]);
        return Collections.singletonList(attachment);
    }

    private Map<Long, AdminUserRespDTO> getAuditUserMap(ErpPurchaseInDO purchaseIn,
                                                        List<ErpPurchaseInAuditLogRespVO> bpmAuditLogs,
                                                        List<ErpPurchaseInStockExecuteDO> stockExecuteList) {
        Set<Long> userIds = new LinkedHashSet<>();
        addUserId(userIds, parseUserId(purchaseIn.getCreator()));
        addUserId(userIds, purchaseIn.getQaUserId());
        addUserId(userIds, purchaseIn.getStockInUserId());
        if (CollUtil.isNotEmpty(stockExecuteList)) {
            stockExecuteList.forEach(execute -> addUserId(userIds, parseUserId(execute.getCreator())));
        }
        if (CollUtil.isNotEmpty(bpmAuditLogs)) {
            bpmAuditLogs.forEach(log -> addUserId(userIds, log.getOperatorId()));
        }
        return CollUtil.isEmpty(userIds) ? Map.of() : adminUserApi.getUserMap(userIds);
    }

    private List<ErpPurchaseInAuditLogRespVO> fillAuditUserNickname(List<ErpPurchaseInAuditLogRespVO> auditLogs,
                                                                    Map<Long, AdminUserRespDTO> userMap) {
        if (CollUtil.isEmpty(auditLogs)) {
            return List.of();
        }
        auditLogs.forEach(auditLog -> MapUtils.findAndThen(userMap, auditLog.getOperatorId(), user -> {
            auditLog.setOperatorName(user.getNickname());
            auditLog.setOperatorNickname(user.getNickname());
        }));
        return auditLogs;
    }

    private List<ErpPurchaseInAuditLogRespVO> buildBpmAuditLogRespVOs(String processInstanceId) {
        if (StrUtil.isBlank(processInstanceId)) {
            return List.of();
        }
        return bpmTaskService.getTaskListByProcessInstanceId(processInstanceId, true).stream()
                .map(this::buildBpmAuditLogRespVO)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private ErpPurchaseInAuditLogRespVO buildBpmAuditLogRespVO(HistoricTaskInstance task) {
        if (START_USER_NODE_ID.equals(task.getTaskDefinitionKey()) || task.getEndTime() == null) {
            return null;
        }
        Integer taskStatus = (Integer) task.getTaskLocalVariables().get(TASK_VARIABLE_STATUS);
        String actionType = ErpPurchaseInDisplaySupport.resolveActionTypeByTaskStatus(taskStatus);
        if (actionType == null) {
            return null;
        }
        ErpPurchaseInAuditLogRespVO respVO = new ErpPurchaseInAuditLogRespVO();
        respVO.setActionType(actionType);
        respVO.setReason((String) task.getTaskLocalVariables().get(TASK_VARIABLE_REASON));
        respVO.setOperatorId(parseUserId(task.getAssignee()));
        respVO.setTaskName(task.getName());
        respVO.setCreateTime(task.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return respVO;
    }

    private void addUserId(Set<Long> userIds, Long userId) {
        if (userId != null) {
            userIds.add(userId);
        }
    }

    private String getUserNickname(Map<Long, AdminUserRespDTO> userMap, Long userId) {
        return userId == null || !userMap.containsKey(userId) ? null : userMap.get(userId).getNickname();
    }

    private BigDecimal calculateRemainingStockInCount(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal remainingCount = (qaPassCount == null ? BigDecimal.ZERO : qaPassCount)
                .subtract(stockInCount == null ? BigDecimal.ZERO : stockInCount);
        return remainingCount.compareTo(BigDecimal.ZERO) > 0 ? remainingCount : BigDecimal.ZERO;
    }

}
