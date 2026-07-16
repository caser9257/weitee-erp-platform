package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.weitee.erp.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherTemplateService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 财务凭证模板")
@RestController
@RequestMapping("/erp/finance-voucher-template")
@Validated
public class ErpFinanceVoucherTemplateController {

    @Resource
    private ErpFinanceVoucherTemplateService voucherTemplateService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @PostMapping("/create")
    @Operation(summary = "创建财务凭证模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:create')")
    public CommonResult<Long> createVoucherTemplate(@Valid @RequestBody ErpFinanceVoucherTemplateSaveReqVO createReqVO) {
        validateLedgerAccess(createReqVO.getLedgerId());
        validateSubjectAccess(createReqVO.getLedgerId(), createReqVO.getItems());
        return success(voucherTemplateService.createVoucherTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务凭证模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:update')")
    public CommonResult<Boolean> updateVoucherTemplate(@Valid @RequestBody ErpFinanceVoucherTemplateSaveReqVO updateReqVO) {
        validateLedgerAccess(updateReqVO.getLedgerId());
        validateExistingTemplateAccess(updateReqVO.getId());
        validateSubjectAccess(updateReqVO.getLedgerId(), updateReqVO.getItems());
        voucherTemplateService.updateVoucherTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务凭证模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:delete')")
    public CommonResult<Boolean> deleteVoucherTemplate(@RequestParam("id") Long id) {
        validateExistingTemplateAccess(id);
        voucherTemplateService.deleteVoucherTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务凭证模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:query')")
    public CommonResult<ErpFinanceVoucherTemplateRespVO> getVoucherTemplate(@RequestParam("id") Long id) {
        ErpFinanceVoucherTemplateDO template = voucherTemplateService.getVoucherTemplate(id);
        if (template == null) {
            return success(null);
        }
        validateLedgerAccess(template.getLedgerId());
        List<ErpFinanceVoucherTemplateItemDO> items = voucherTemplateService.getVoucherTemplateItemListByTemplateId(id);
        validateSubjectAccess(template.getLedgerId(), items);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(Collections.singleton(template.getLedgerId()));
        return success(buildTemplateResp(template, items, ledgerMap));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得财务凭证模板精简列表")
    public CommonResult<List<ErpFinanceVoucherTemplateRespVO>> getVoucherTemplateSimpleList(@RequestParam("ledgerId") Long ledgerId,
                                                                                           @RequestParam("bizType") Integer bizType) {
        validateLedgerAccess(ledgerId);
        List<ErpFinanceVoucherTemplateDO> list = voucherTemplateService.getVoucherTemplateListByLedgerAndBizType(ledgerId, bizType)
                .stream().filter(item -> CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus())).toList();
        Map<Long, List<ErpFinanceVoucherTemplateItemDO>> itemMap = convertMultiMap(
                voucherTemplateService.getVoucherTemplateItemListByTemplateIds(convertSet(list, ErpFinanceVoucherTemplateDO::getId)),
                ErpFinanceVoucherTemplateItemDO::getTemplateId);
        list = list.stream().filter(template -> itemMap.getOrDefault(template.getId(), Collections.emptyList()).stream()
                .allMatch(item -> financeDataPermissionService.canAccessSubject(ledgerId, item.getSubjectCode()))).toList();
        return success(convertList(list, item -> new ErpFinanceVoucherTemplateRespVO()
                .setId(item.getId())
                .setLedgerId(item.getLedgerId())
                .setBizType(item.getBizType())
                .setName(item.getName())
                .setAutoGenerate(item.getAutoGenerate())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务凭证模板分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:query')")
    public CommonResult<PageResult<ErpFinanceVoucherTemplateRespVO>> getVoucherTemplatePage(@Valid ErpFinanceVoucherTemplatePageReqVO pageReqVO) {
        PageResult<ErpFinanceVoucherTemplateDO> pageResult = voucherTemplateService.getVoucherTemplatePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, List<ErpFinanceVoucherTemplateItemDO>> itemMap = convertMultiMap(
                voucherTemplateService.getVoucherTemplateItemListByTemplateIds(convertSet(pageResult.getList(), ErpFinanceVoucherTemplateDO::getId)),
                ErpFinanceVoucherTemplateItemDO::getTemplateId);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(convertSet(pageResult.getList(), ErpFinanceVoucherTemplateDO::getLedgerId));
        List<ErpFinanceVoucherTemplateRespVO> respList = convertList(pageResult.getList(),
                template -> buildTemplateResp(template, itemMap.get(template.getId()), ledgerMap));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ErpFinanceVoucherTemplateRespVO buildTemplateResp(ErpFinanceVoucherTemplateDO template,
                                                              List<ErpFinanceVoucherTemplateItemDO> items,
                                                              Map<Long, ErpFinanceLedgerDO> ledgerMap) {
        ErpFinanceVoucherTemplateRespVO respVO = BeanUtils.toBean(template, ErpFinanceVoucherTemplateRespVO.class);
        ErpFinanceLedgerDO ledger = ledgerMap.get(template.getLedgerId());
        respVO.setLedgerName(ledger == null ? null : ledger.getName());
        respVO.setBizTypeName(resolveBizTypeName(template.getBizType()));
        respVO.setResearchCategoryName(resolveResearchCategoryName(template.getResearchCategory()));
        respVO.setItems(BeanUtils.toBean(items, ErpFinanceVoucherTemplateRespVO.Item.class, item -> {
            item.setEntryDirectionName(resolveDirectionName(item.getEntryDirection()));
            item.setAmountSourceName(resolveAmountSourceName(item.getAmountSource()));
        }));
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

    private String resolveResearchCategoryName(Integer researchCategory) {
        if (researchCategory == null) {
            return null;
        }
        ErpResearchExpenseCategoryEnum categoryEnum = ErpResearchExpenseCategoryEnum.fromType(researchCategory);
        return categoryEnum == null ? null : categoryEnum.getName();
    }

    private String resolveDirectionName(Integer direction) {
        for (ErpFinanceVoucherEntryDirectionEnum value : ErpFinanceVoucherEntryDirectionEnum.values()) {
            if (value.getType().equals(direction)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveAmountSourceName(Integer amountSource) {
        for (ErpFinanceVoucherAmountSourceEnum value : ErpFinanceVoucherAmountSourceEnum.values()) {
            if (value.getType().equals(amountSource)) {
                return value.getName();
            }
        }
        return null;
    }

    private void validateExistingTemplateAccess(Long id) {
        if (id == null) {
            return;
        }
        ErpFinanceVoucherTemplateDO template = voucherTemplateService.getVoucherTemplate(id);
        if (template != null) {
            validateLedgerAccess(template.getLedgerId());
            validateSubjectAccess(template.getLedgerId(), voucherTemplateService.getVoucherTemplateItemListByTemplateId(id));
        }
    }

    private void validateLedgerAccess(Long ledgerId) {
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw exception(FORBIDDEN);
        }
    }

    private void validateSubjectAccess(Long ledgerId, List<ErpFinanceVoucherTemplateSaveReqVO.Item> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (ErpFinanceVoucherTemplateSaveReqVO.Item item : items) {
            if (!financeDataPermissionService.canAccessSubject(ledgerId, item.getSubjectCode())) {
                throw exception(FORBIDDEN);
            }
        }
    }

    private void validateSubjectAccess(Long ledgerId, Collection<ErpFinanceVoucherTemplateItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (ErpFinanceVoucherTemplateItemDO item : items) {
            if (!financeDataPermissionService.canAccessSubject(ledgerId, item.getSubjectCode())) {
                throw exception(FORBIDDEN);
            }
        }
    }
}
