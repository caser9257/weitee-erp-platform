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
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentTraceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentBpmService;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 付款单")
@RestController
@RequestMapping("/erp/finance-payment")
@Validated
public class ErpFinancePaymentController {

    @Resource
    private ErpFinancePaymentService financePaymentService;
    @Resource
    private ErpFinancePaymentBpmService financePaymentBpmService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpApStatementService apStatementService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建付款单")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:create')")
    public CommonResult<Long> createFinancePayment(@Valid @RequestBody ErpFinancePaymentSaveReqVO createReqVO) {
        return success(financePaymentService.createFinancePayment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新付款单")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:update')")
    public CommonResult<Boolean> updateFinancePayment(@Valid @RequestBody ErpFinancePaymentSaveReqVO updateReqVO) {
        financePaymentService.updateFinancePayment(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新付款单的状态")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:update-status')")
    public CommonResult<Boolean> updateFinancePaymentStatus(@RequestParam("id") Long id,
                                                           @RequestParam("status") Integer status) {
        financePaymentService.updateFinancePaymentStatus(id, status);
        return success(true);
    }

    @PutMapping("/void")
    @Operation(summary = "作废付款单")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:void')")
    public CommonResult<Boolean> voidFinancePayment(@RequestParam("id") Long id,
                                                    @RequestParam("reason") String reason) {
        financePaymentService.voidFinancePayment(id, reason);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交付款单审批")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:submit')")
    public CommonResult<String> submitFinancePayment(@Valid @RequestBody ErpFinancePaymentSubmitReqVO reqVO) {
        return success(financePaymentBpmService.submitFinancePayment(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/cancel-approval")
    @Operation(summary = "撤回付款单审批")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:cancel-approval')")
    public CommonResult<Boolean> cancelFinancePaymentApproval(@Valid @RequestBody ErpFinancePaymentCancelApprovalReqVO reqVO) {
        financePaymentBpmService.cancelFinancePaymentApproval(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除付款单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:delete')")
    public CommonResult<Boolean> deleteFinancePayment(@RequestParam("ids") List<Long> ids) {
        financePaymentService.deleteFinancePayment(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得付款单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:query')")
    public CommonResult<ErpFinancePaymentRespVO> getFinancePayment(@RequestParam("id") Long id) {
        ErpFinancePaymentDO payment = financePaymentService.getFinancePayment(id);
        if (payment == null) {
            return success(null);
        }
        List<ErpFinancePaymentItemDO> paymentItemList = financePaymentService.getFinancePaymentItemListByPaymentId(id);
        return success(buildFinancePaymentRespVO(payment, paymentItemList));
    }

    @GetMapping("/get-trace")
    @Operation(summary = "获得付款单追溯信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:query')")
    public CommonResult<ErpFinancePaymentTraceRespVO> getFinancePaymentTrace(@RequestParam("id") Long id) {
        ErpFinancePaymentDO payment = financePaymentService.getFinancePayment(id);
        if (payment == null) {
            return success(null);
        }
        List<ErpFinancePaymentItemDO> paymentItemList = financePaymentService.getFinancePaymentItemListByPaymentId(id);
        List<ErpFinancePaymentAllocateDO> allocateList = financePaymentService.getFinancePaymentAllocateListByPaymentId(id);
        List<Long> statementIds = paymentItemList.stream()
                .map(ErpFinancePaymentItemDO::getApStatementId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        List<ErpApStatementDO> statementList = statementIds.isEmpty()
                ? Collections.emptyList()
                : apStatementService.getApStatementListByIds(statementIds);

        ErpFinancePaymentTraceRespVO traceRespVO = new ErpFinancePaymentTraceRespVO();
        traceRespVO.setPayment(buildFinancePaymentRespVO(payment, paymentItemList));
        traceRespVO.setStatements(buildApStatementRespVOList(statementList));
        traceRespVO.setAllocates(BeanUtils.toBean(allocateList, ErpFinancePaymentTraceRespVO.AllocateItem.class, item ->
                item.setStatusName(resolveAllocateStatusName(item.getStatus()))));
        return success(traceRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得付款单分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:query')")
    public CommonResult<PageResult<ErpFinancePaymentRespVO>> getFinancePaymentPage(@Valid ErpFinancePaymentPageReqVO pageReqVO) {
        PageResult<ErpFinancePaymentDO> pageResult = financePaymentService.getFinancePaymentPage(pageReqVO);
        return success(buildFinancePaymentVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出付款单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-payment:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinancePaymentExcel(@Valid ErpFinancePaymentPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpFinancePaymentRespVO> list = buildFinancePaymentVOPageResult(financePaymentService.getFinancePaymentPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "付款单.xls", "数据", ErpFinancePaymentRespVO.class, list);
    }

    private PageResult<ErpFinancePaymentRespVO> buildFinancePaymentVOPageResult(PageResult<ErpFinancePaymentDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 付款项
        List<ErpFinancePaymentItemDO> paymentItemList = financePaymentService.getFinancePaymentItemListByPaymentIds(
                convertSet(pageResult.getList(), ErpFinancePaymentDO::getId));
        Map<Long, List<ErpFinancePaymentItemDO>> financePaymentItemMap = convertMultiMap(paymentItemList, ErpFinancePaymentItemDO::getPaymentId);
        // 1.2 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpFinancePaymentDO::getSupplierId));
        // 1.3 结算账户信息
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(pageResult.getList(), ErpFinancePaymentDO::getAccountId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getList(),
                contact -> Stream.of(parseCreatorId(contact.getCreator()), contact.getFinanceUserId(), contact.getVoidBy())
                        .filter(Objects::nonNull)));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpFinancePaymentRespVO.class, payment -> {
            payment.setItems(BeanUtils.toBean(financePaymentItemMap.get(payment.getId()), ErpFinancePaymentRespVO.Item.class));
            MapUtils.findAndThen(supplierMap, payment.getSupplierId(), supplier -> payment.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, payment.getAccountId(), account -> payment.setAccountName(account.getName()));
            MapUtils.findAndThen(userMap, parseCreatorId(payment.getCreator()), user -> payment.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, payment.getFinanceUserId(), user -> payment.setFinanceUserName(user.getNickname()));
            MapUtils.findAndThen(userMap, payment.getVoidBy(), user -> payment.setVoidByName(user.getNickname()));
        });
    }

    private ErpFinancePaymentRespVO buildFinancePaymentRespVO(ErpFinancePaymentDO payment,
                                                              List<ErpFinancePaymentItemDO> paymentItemList) {
        ErpFinancePaymentRespVO respVO = BeanUtils.toBean(payment, ErpFinancePaymentRespVO.class);
        respVO.setItems(BeanUtils.toBean(paymentItemList, ErpFinancePaymentRespVO.Item.class));
        Map<Long, ErpSupplierDO> supplierMap = payment.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(List.of(payment.getSupplierId()));
        MapUtils.findAndThen(supplierMap, payment.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
        Map<Long, ErpAccountDO> accountMap = payment.getAccountId() == null
                ? Collections.emptyMap()
                : accountService.getAccountMap(List.of(payment.getAccountId()));
        MapUtils.findAndThen(accountMap, payment.getAccountId(), account -> respVO.setAccountName(account.getName()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(List.of(payment),
                item -> Stream.of(parseCreatorId(item.getCreator()), item.getFinanceUserId(), item.getVoidBy())
                        .filter(Objects::nonNull)));
        MapUtils.findAndThen(userMap, parseCreatorId(payment.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
        MapUtils.findAndThen(userMap, payment.getFinanceUserId(), user -> respVO.setFinanceUserName(user.getNickname()));
        MapUtils.findAndThen(userMap, payment.getVoidBy(), user -> respVO.setVoidByName(user.getNickname()));
        return respVO;
    }

    private List<ErpApStatementRespVO> buildApStatementRespVOList(List<ErpApStatementDO> statementList) {
        if (CollUtil.isEmpty(statementList)) {
            return Collections.emptyList();
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(statementList, ErpApStatementDO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(statementList, ErpApStatementDO::getAccountId));
        return BeanUtils.toBean(statementList, ErpApStatementRespVO.class, statement -> {
            MapUtils.findAndThen(supplierMap, statement.getSupplierId(), supplier -> statement.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, statement.getAccountId(), account -> statement.setAccountName(account.getName()));
        });
    }

    private String resolveAllocateStatusName(Integer status) {
        return java.util.Arrays.stream(ErpFinancePaymentAllocateStatusEnum.values())
                .filter(item -> item.getStatus().equals(status))
                .map(ErpFinancePaymentAllocateStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

    private Long parseCreatorId(String creator) {
        return StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
