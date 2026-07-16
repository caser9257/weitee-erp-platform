package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherActionReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherRecomputeReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherReverseReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePeriodService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherIntegrityService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherTemplateService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 财务凭证")
@RestController
@RequestMapping("/erp/finance-voucher")
@Validated
public class ErpFinanceVoucherController {

    @Resource
    private ErpFinanceVoucherService financeVoucherService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;
    @Resource
    private ErpFinanceVoucherTemplateService voucherTemplateService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinancePeriodService financePeriodService;
    @Resource
    private ErpFinanceVoucherIntegrityService financeVoucherIntegrityService;

    @PostMapping("/generate")
    @Operation(summary = "生成财务凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:create')")
    public CommonResult<Long> generateVoucher(@Valid @RequestBody ErpFinanceVoucherGenerateReqVO reqVO) {
        validateLedgerAccess(reqVO.getLedgerId());
        return success(financeVoucherService.generateVoucher(reqVO));
    }

    @PostMapping("/approve")
    @Operation(summary = "审核财务凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Boolean> approveVoucher(@Valid @RequestBody ErpFinanceVoucherActionReqVO reqVO) {
        validateVoucherIdsAccess(reqVO.getIds());
        financeVoucherService.approveVoucher(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/cancel-approve")
    @Operation(summary = "反审核财务凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Boolean> cancelApproveVoucher(@Valid @RequestBody ErpFinanceVoucherActionReqVO reqVO) {
        validateVoucherIdsAccess(reqVO.getIds());
        financeVoucherService.cancelApproveVoucher(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/post")
    @Operation(summary = "过账财务凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Boolean> postVoucher(@Valid @RequestBody ErpFinanceVoucherActionReqVO reqVO) {
        validateVoucherIdsAccess(reqVO.getIds());
        financeVoucherService.postVoucher(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/cancel-post")
    @Operation(summary = "反过账财务凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Boolean> cancelPostVoucher(@Valid @RequestBody ErpFinanceVoucherActionReqVO reqVO) {
        validateVoucherIdsAccess(reqVO.getIds());
        financeVoucherService.cancelPostVoucher(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/reverse")
    @Operation(summary = "冲销财务凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Long> reverseVoucher(@Valid @RequestBody ErpFinanceVoucherReverseReqVO reqVO) {
        validateVoucherAccess(reqVO.getId());
        return success(financeVoucherService.reverseVoucher(getLoginUserId(), reqVO));
    }

    @PostMapping("/recompute")
    @Operation(summary = "重算自动凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Long> recomputeAutoVoucher(@Valid @RequestBody ErpFinanceVoucherRecomputeReqVO reqVO) {
        return success(financeVoucherService.recomputeAutoGeneratedVoucher(
                reqVO.getBizType(), reqVO.getBizId(), getLoginUserId(), reqVO.getRemark()));
    }

    @PostMapping("/check-integrity")
    @Operation(summary = "检查凭证完整性")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:query')")
    public CommonResult<ErpFinanceVoucherIntegrityCheckRespVO> checkVoucherIntegrity(
            @Valid @RequestBody ErpFinanceVoucherIntegrityCheckReqVO reqVO) {
        validateLedgerAccess(reqVO.getLedgerId());
        return success(financeVoucherIntegrityService.checkIntegrity(
                reqVO.getBizType(), reqVO.getLedgerId(),
                reqVO.getStartDate(), reqVO.getEndDate()));
    }

    @PostMapping("/batch-recompute")
    @Operation(summary = "批量重算凭证")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:update')")
    public CommonResult<Integer> batchRecomputeVouchers(
            @Valid @RequestBody ErpFinanceVoucherIntegrityCheckReqVO reqVO) {
        validateLedgerAccess(reqVO.getLedgerId());
        return success(financeVoucherIntegrityService.batchRecomputeVouchers(
                reqVO.getBizType(), reqVO.getLedgerId(),
                reqVO.getStartDate(), reqVO.getEndDate(),
                getLoginUserId(), "批量重算补生成凭证"));
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务凭证")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:query')")
    public CommonResult<ErpFinanceVoucherRespVO> getVoucher(@RequestParam("id") Long id) {
        ErpFinanceVoucherDO voucher = financeVoucherService.getVoucher(id);
        if (voucher == null) {
            return success(null);
        }
        validateLedgerAccess(voucher.getLedgerId());
        validateDeptAccess(voucher.getDeptId());
        List<ErpFinanceVoucherEntryDO> entries = financeVoucherService.getVoucherEntryListByVoucherId(id);
        validateSubjectAccess(voucher.getLedgerId(), entries);
        return success(buildVoucherResp(voucher, entries,
                financeLedgerService.getFinanceLedgerMap(Collections.singleton(voucher.getLedgerId())),
                Collections.singletonMap(voucher.getPeriodId(), financePeriodService.getFinancePeriod(voucher.getPeriodId())),
                Collections.singletonMap(voucher.getTemplateId(), voucherTemplateService.getVoucherTemplate(voucher.getTemplateId())),
                buildRelatedVoucherMap(Collections.singletonList(voucher))));
    }

    @GetMapping("/get-by-biz")
    @Operation(summary = "按业务单据获得财务凭证")
    @Parameters({
            @Parameter(name = "ledgerId", description = "账簿编号", required = true),
            @Parameter(name = "bizType", description = "业务类型", required = true),
            @Parameter(name = "bizId", description = "业务单据编号", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:query')")
    public CommonResult<ErpFinanceVoucherRespVO> getVoucherByBiz(@RequestParam("ledgerId") Long ledgerId,
                                                                 @RequestParam("bizType") Integer bizType,
                                                                 @RequestParam("bizId") Long bizId) {
        validateLedgerAccess(ledgerId);
        ErpFinanceVoucherDO voucher = financeVoucherService.getVoucherByLedgerAndBiz(ledgerId, bizType, bizId);
        if (voucher == null) {
            return success(null);
        }
        List<ErpFinanceVoucherEntryDO> entries = financeVoucherService.getVoucherEntryListByVoucherId(voucher.getId());
        validateSubjectAccess(voucher.getLedgerId(), entries);
        return success(buildVoucherResp(voucher, entries,
                financeLedgerService.getFinanceLedgerMap(Collections.singleton(voucher.getLedgerId())),
                Collections.singletonMap(voucher.getPeriodId(), financePeriodService.getFinancePeriod(voucher.getPeriodId())),
                Collections.singletonMap(voucher.getTemplateId(), voucherTemplateService.getVoucherTemplate(voucher.getTemplateId())),
                buildRelatedVoucherMap(Collections.singletonList(voucher))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务凭证分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher:query')")
    public CommonResult<PageResult<ErpFinanceVoucherRespVO>> getVoucherPage(@Valid ErpFinanceVoucherPageReqVO pageReqVO) {
        PageResult<ErpFinanceVoucherDO> pageResult = financeVoucherService.getVoucherPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, List<ErpFinanceVoucherEntryDO>> entryMap = convertMapToEntries(
                financeVoucherService.getVoucherEntryListByVoucherIds(convertSet(pageResult.getList(), ErpFinanceVoucherDO::getId)));
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(convertSet(pageResult.getList(), ErpFinanceVoucherDO::getLedgerId));
        Map<Long, ErpFinancePeriodDO> periodMap = convertMap(
                convertList(pageResult.getList(), item -> financePeriodService.getFinancePeriod(item.getPeriodId())),
                ErpFinancePeriodDO::getId);
        Map<Long, ErpFinanceVoucherTemplateDO> templateMap = convertMap(
                convertList(pageResult.getList(), item -> voucherTemplateService.getVoucherTemplate(item.getTemplateId())),
                ErpFinanceVoucherTemplateDO::getId);
        Map<Long, ErpFinanceVoucherDO> relatedVoucherMap = buildRelatedVoucherMap(pageResult.getList());
        List<ErpFinanceVoucherRespVO> respList = convertList(pageResult.getList(),
                voucher -> buildVoucherResp(voucher, entryMap.get(voucher.getId()), ledgerMap, periodMap, templateMap, relatedVoucherMap));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private void validateLedgerAccess(Long ledgerId) {
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw exception(FORBIDDEN);
        }
    }

    private void validateVoucherAccess(Long voucherId) {
        if (voucherId == null) {
            return;
        }
        validateVoucherListAccess(financeVoucherService.getVoucherListByIds(Collections.singleton(voucherId)));
    }

    private void validateVoucherIdsAccess(List<Long> voucherIds) {
        if (CollUtil.isEmpty(voucherIds)) {
            return;
        }
        validateVoucherListAccess(financeVoucherService.getVoucherListByIds(voucherIds));
    }

    private void validateVoucherListAccess(List<ErpFinanceVoucherDO> vouchers) {
        if (CollUtil.isEmpty(vouchers)) {
            return;
        }
        vouchers.forEach(voucher -> {
            validateLedgerAccess(voucher.getLedgerId());
            validateDeptAccess(voucher.getDeptId());
            validateSubjectAccess(voucher.getLedgerId(),
                    financeVoucherService.getVoucherEntryListByVoucherId(voucher.getId()));
        });
    }

    private void validateSubjectAccess(Long ledgerId, Collection<ErpFinanceVoucherEntryDO> entries) {
        if (CollUtil.isEmpty(entries)) {
            return;
        }
        for (ErpFinanceVoucherEntryDO entry : entries) {
            if (!financeDataPermissionService.canAccessSubject(ledgerId, entry.getSubjectCode())) {
                throw exception(FORBIDDEN);
            }
        }
    }

    private void validateDeptAccess(Long deptId) {
        if (deptId != null && !financeDataPermissionService.canAccessDept(deptId)) {
            throw exception(FORBIDDEN);
        }
    }

    private Map<Long, List<ErpFinanceVoucherEntryDO>> convertMapToEntries(List<ErpFinanceVoucherEntryDO> entries) {
        return entries.stream().collect(Collectors.groupingBy(ErpFinanceVoucherEntryDO::getVoucherId));
    }

    private Map<Long, ErpFinanceVoucherDO> buildRelatedVoucherMap(List<ErpFinanceVoucherDO> voucherList) {
        Set<Long> relatedIds = voucherList.stream()
                .flatMap(item -> Stream.of(item.getReverseVoucherId(), item.getReverseFromVoucherId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(relatedIds)) {
            return Collections.emptyMap();
        }
        List<ErpFinanceVoucherDO> relatedVouchers = financeVoucherService.getVoucherListByIds(relatedIds);
        if (CollUtil.isEmpty(relatedVouchers)) {
            return Collections.emptyMap();
        }
        return convertMap(relatedVouchers.stream()
                .filter(voucher -> financeDataPermissionService.canAccessLedger(voucher.getLedgerId()))
                .collect(Collectors.toList()), ErpFinanceVoucherDO::getId);
    }

    private ErpFinanceVoucherRespVO buildVoucherResp(ErpFinanceVoucherDO voucher,
                                                     List<ErpFinanceVoucherEntryDO> entries,
                                                     Map<Long, ErpFinanceLedgerDO> ledgerMap,
                                                     Map<Long, ErpFinancePeriodDO> periodMap,
                                                     Map<Long, ErpFinanceVoucherTemplateDO> templateMap,
                                                     Map<Long, ErpFinanceVoucherDO> relatedVoucherMap) {
        ErpFinanceVoucherRespVO respVO = BeanUtils.toBean(voucher, ErpFinanceVoucherRespVO.class);
        ErpFinanceLedgerDO ledger = ledgerMap.get(voucher.getLedgerId());
        ErpFinancePeriodDO period = periodMap.get(voucher.getPeriodId());
        ErpFinanceVoucherTemplateDO template = templateMap.get(voucher.getTemplateId());
        ErpFinanceVoucherDO reverseVoucher = relatedVoucherMap.get(voucher.getReverseVoucherId());
        ErpFinanceVoucherDO reverseFromVoucher = relatedVoucherMap.get(voucher.getReverseFromVoucherId());
        respVO.setLedgerName(ledger == null ? null : ledger.getName());
        respVO.setPeriodCode(period == null ? null : period.getPeriodCode());
        respVO.setTemplateName(template == null ? null : template.getName());
        respVO.setBizTypeName(resolveBizTypeName(voucher.getBizType()));
        respVO.setStatusName(resolveStatusName(voucher.getStatus()));
        respVO.setReverseVoucherNo(reverseVoucher == null ? null : reverseVoucher.getVoucherNo());
        respVO.setReverseFromVoucherNo(reverseFromVoucher == null ? null : reverseFromVoucher.getVoucherNo());
        respVO.setEntries(BeanUtils.toBean(entries, ErpFinanceVoucherRespVO.Item.class));
        return respVO;
    }

    private String resolveBizTypeName(Integer bizType) {
        for (ErpBizTypeEnum value : ErpBizTypeEnum.values()) {
            if (value.getType().equals(bizType)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveStatusName(Integer status) {
        for (ErpFinanceVoucherStatusEnum value : ErpFinanceVoucherStatusEnum.values()) {
            if (value.getStatus().equals(status)) {
                return value.getName();
            }
        }
        return null;
    }
}
