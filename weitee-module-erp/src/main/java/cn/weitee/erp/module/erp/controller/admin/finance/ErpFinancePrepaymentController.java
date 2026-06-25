package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentRollbackReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentTraceRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.weitee.erp.module.erp.enums.ErpFinancePrepaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePrepaymentService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.weitee.erp.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 预付款")
@RestController
@RequestMapping("/erp/finance-prepayment")
@Validated
public class ErpFinancePrepaymentController {

    @Resource
    private ErpFinancePrepaymentService prepaymentService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建预付款")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:create')")
    public CommonResult<Long> createFinancePrepayment(@Valid @RequestBody ErpFinancePrepaymentSaveReqVO createReqVO) {
        return success(prepaymentService.createFinancePrepayment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新预付款")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:update')")
    public CommonResult<Boolean> updateFinancePrepayment(@Valid @RequestBody ErpFinancePrepaymentSaveReqVO updateReqVO) {
        prepaymentService.updateFinancePrepayment(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新预付款状态")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:update-status')")
    public CommonResult<Boolean> updateFinancePrepaymentStatus(@RequestParam("id") Long id,
                                                                @RequestParam("status") Integer status) {
        prepaymentService.updateFinancePrepaymentStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除预付款")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:delete')")
    public CommonResult<Boolean> deleteFinancePrepayment(@RequestParam("ids") List<Long> ids) {
        prepaymentService.deleteFinancePrepayment(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得预付款")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:query')")
    public CommonResult<ErpFinancePrepaymentRespVO> getFinancePrepayment(@RequestParam("id") Long id) {
        ErpFinancePrepaymentDO prepayment = prepaymentService.getFinancePrepayment(id);
        if (prepayment == null) {
            return success(null);
        }
        List<ErpFinancePrepaymentAllocateDO> allocateList = prepaymentService.getFinancePrepaymentAllocateListByPrepaymentId(id);
        ErpFinancePrepaymentRespVO respVO = buildPrepaymentRespVO(prepayment, allocateList);
        return success(respVO);
    }

    @GetMapping("/get-trace")
    @Operation(summary = "获得预付款追溯信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:query')")
    public CommonResult<ErpFinancePrepaymentTraceRespVO> getFinancePrepaymentTrace(@RequestParam("id") Long id) {
        ErpFinancePrepaymentDO prepayment = prepaymentService.getFinancePrepayment(id);
        if (prepayment == null) {
            return success(null);
        }
        List<ErpFinancePrepaymentAllocateDO> allocateList = prepaymentService.getFinancePrepaymentAllocateListByPrepaymentId(id);
        List<Long> statementIds = allocateList.stream()
                .map(ErpFinancePrepaymentAllocateDO::getApStatementId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        List<ErpApStatementDO> statementList = statementIds.isEmpty()
                ? Collections.emptyList()
                : apStatementService.getApStatementListByIds(statementIds);

        ErpFinancePrepaymentTraceRespVO traceRespVO = new ErpFinancePrepaymentTraceRespVO();
        traceRespVO.setPrepayment(buildPrepaymentRespVO(prepayment, allocateList));
        traceRespVO.setStatements(buildApStatementRespVOList(statementList));
        traceRespVO.setAllocates(BeanUtils.toBean(allocateList, ErpFinancePrepaymentTraceRespVO.AllocateItem.class, item ->
                item.setStatusName(resolveAllocateStatusName(item.getStatus()))));
        return success(traceRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得预付款分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:query')")
    public CommonResult<PageResult<ErpFinancePrepaymentRespVO>> getFinancePrepaymentPage(@Valid ErpFinancePrepaymentPageReqVO pageReqVO) {
        PageResult<ErpFinancePrepaymentDO> pageResult = prepaymentService.getFinancePrepaymentPage(pageReqVO);
        return success(buildPrepaymentPageResult(pageResult));
    }

    @PostMapping("/allocate")
    @Operation(summary = "预付款核销")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:update')")
    public CommonResult<Boolean> allocateFinancePrepayment(@Valid @RequestBody ErpFinancePrepaymentAllocateReqVO reqVO) {
        prepaymentService.allocateFinancePrepayment(reqVO);
        return success(true);
    }

    @PostMapping("/rollback-allocate")
    @Operation(summary = "预付款核销回滚")
    @PreAuthorize("@ss.hasPermission('erp:finance-prepayment:update')")
    public CommonResult<Boolean> rollbackFinancePrepaymentAllocate(@Valid @RequestBody ErpFinancePrepaymentRollbackReqVO reqVO) {
        prepaymentService.rollbackFinancePrepaymentAllocate(reqVO);
        return success(true);
    }

    private PageResult<ErpFinancePrepaymentRespVO> buildPrepaymentPageResult(PageResult<ErpFinancePrepaymentDO> pageResult) {
        if (pageResult == null || pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult == null ? 0 : pageResult.getTotal());
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpFinancePrepaymentDO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(
                convertSet(pageResult.getList(), ErpFinancePrepaymentDO::getAccountId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(),
                        item -> Stream.of(parseUserId(item.getCreator()), item.getFinanceUserId())));
        return BeanUtils.toBean(pageResult, ErpFinancePrepaymentRespVO.class, respVO -> {
            MapUtils.findAndThen(supplierMap, respVO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, respVO.getAccountId(), account -> respVO.setAccountName(account.getName()));
            MapUtils.findAndThen(userMap, parseUserId(respVO.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, respVO.getFinanceUserId(), user -> respVO.setFinanceUserName(user.getNickname()));
        });
    }

    private ErpFinancePrepaymentRespVO buildPrepaymentRespVO(ErpFinancePrepaymentDO prepayment,
                                                             List<ErpFinancePrepaymentAllocateDO> allocateList) {
        ErpFinancePrepaymentRespVO respVO = BeanUtils.toBean(prepayment, ErpFinancePrepaymentRespVO.class);
        Map<Long, ErpSupplierDO> supplierMap = prepayment.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(List.of(prepayment.getSupplierId()));
        MapUtils.findAndThen(supplierMap, prepayment.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
        Map<Long, ErpAccountDO> accountMap = prepayment.getAccountId() == null
                ? Collections.emptyMap()
                : accountService.getAccountMap(List.of(prepayment.getAccountId()));
        MapUtils.findAndThen(accountMap, prepayment.getAccountId(), account -> respVO.setAccountName(account.getName()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(List.of(prepayment),
                        item -> Stream.of(parseUserId(item.getCreator()), item.getFinanceUserId())));
        MapUtils.findAndThen(userMap, parseUserId(prepayment.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
        MapUtils.findAndThen(userMap, prepayment.getFinanceUserId(), user -> respVO.setFinanceUserName(user.getNickname()));
        respVO.setAllocates(BeanUtils.toBean(allocateList, ErpFinancePrepaymentRespVO.AllocateItem.class, item ->
                item.setStatusName(resolveAllocateStatusName(item.getStatus()))));
        return respVO;
    }

    private List<ErpApStatementRespVO> buildApStatementRespVOList(List<ErpApStatementDO> statementList) {
        if (statementList == null || statementList.isEmpty()) {
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
        return java.util.Arrays.stream(ErpFinancePrepaymentAllocateStatusEnum.values())
                .filter(item -> item.getStatus().equals(status))
                .map(ErpFinancePrepaymentAllocateStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

}
