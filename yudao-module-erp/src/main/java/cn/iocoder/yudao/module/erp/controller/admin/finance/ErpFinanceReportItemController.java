package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceReportAmountRuleEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceReportItemCategoryEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceReportTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceLedgerService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceReportItemService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 财务报表项目")
@RestController
@RequestMapping("/erp/finance-report-item")
@Validated
public class ErpFinanceReportItemController {

    @Resource
    private ErpFinanceReportItemService financeReportItemService;
    @Resource
    private ErpFinanceSubjectService financeSubjectService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @PostMapping("/create")
    @Operation(summary = "创建财务报表项目")
    @PreAuthorize("@ss.hasPermission('erp:finance-report-item:create')")
    public CommonResult<Long> createFinanceReportItem(@Valid @RequestBody ErpFinanceReportItemSaveReqVO createReqVO) {
        return success(financeReportItemService.createFinanceReportItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务报表项目")
    @PreAuthorize("@ss.hasPermission('erp:finance-report-item:update')")
    public CommonResult<Boolean> updateFinanceReportItem(@Valid @RequestBody ErpFinanceReportItemSaveReqVO updateReqVO) {
        financeReportItemService.updateFinanceReportItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务报表项目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-report-item:delete')")
    public CommonResult<Boolean> deleteFinanceReportItem(@RequestParam("id") Long id) {
        financeReportItemService.deleteFinanceReportItem(id);
        return success(true);
    }

    @PostMapping("/init-standard-template")
    @Operation(summary = "初始化财务报表标准模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-report-item:create')")
    public CommonResult<ErpFinanceReportTemplateInitRespVO> initStandardTemplate(
            @Valid @RequestBody ErpFinanceReportTemplateInitReqVO reqVO) {
        return success(financeReportItemService.initStandardTemplate(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务报表项目")
    @PreAuthorize("@ss.hasPermission('erp:finance-report-item:query')")
    public CommonResult<ErpFinanceReportItemRespVO> getFinanceReportItem(@RequestParam("id") Long id) {
        ErpFinanceReportItemDO item = financeReportItemService.getFinanceReportItem(id);
        if (item == null) {
            return success(null);
        }
        List<ErpFinanceReportItemSubjectDO> subjects = financeReportItemService.getFinanceReportItemSubjectListByItemId(id);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(Collections.singleton(item.getLedgerId()));
        Map<String, ErpFinanceSubjectDO> subjectMap = financeSubjectService.getFinanceSubjectMapByLedgerIdAndSubjectCodes(
                item.getLedgerId(), convertSet(subjects, ErpFinanceReportItemSubjectDO::getSubjectCode));
        return success(buildReportItemResp(item, subjects, ledgerMap, subjectMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务报表项目分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-report-item:query')")
    public CommonResult<PageResult<ErpFinanceReportItemRespVO>> getFinanceReportItemPage(@Valid ErpFinanceReportItemPageReqVO pageReqVO) {
        PageResult<ErpFinanceReportItemDO> pageResult = financeReportItemService.getFinanceReportItemPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, ErpFinanceReportItemDO> itemMap = convertMap(pageResult.getList(), ErpFinanceReportItemDO::getId);
        List<ErpFinanceReportItemSubjectDO> subjects = financeReportItemService.getFinanceReportItemSubjectListByItemIds(itemMap.keySet());
        Map<Long, List<ErpFinanceReportItemSubjectDO>> subjectMapByItem = convertMultiMap(subjects, ErpFinanceReportItemSubjectDO::getItemId);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(
                convertSet(pageResult.getList(), ErpFinanceReportItemDO::getLedgerId));
        Map<String, ErpFinanceSubjectDO> subjectMap = buildSubjectMap(pageResult.getList(), subjects, itemMap);
        return success(new PageResult<>(convertList(pageResult.getList(),
                item -> buildReportItemResp(item, subjectMapByItem.get(item.getId()), ledgerMap, subjectMap)),
                pageResult.getTotal()));
    }

    private Map<String, ErpFinanceSubjectDO> buildSubjectMap(List<ErpFinanceReportItemDO> items,
                                                             List<ErpFinanceReportItemSubjectDO> subjects,
                                                             Map<Long, ErpFinanceReportItemDO> itemMap) {
        if (CollUtil.isEmpty(subjects)) {
            return Collections.emptyMap();
        }
        Set<Long> ledgerIds = convertSet(items, ErpFinanceReportItemDO::getLedgerId);
        Map<String, ErpFinanceSubjectDO> result = new HashMap<>();
        for (Long ledgerId : ledgerIds) {
            Set<String> subjectCodes = subjects.stream()
                    .filter(subject -> itemMap.containsKey(subject.getItemId()))
                    .filter(subject -> ledgerId.equals(itemMap.get(subject.getItemId()).getLedgerId()))
                    .map(ErpFinanceReportItemSubjectDO::getSubjectCode)
                    .collect(Collectors.toSet());
            for (ErpFinanceSubjectDO subject : financeSubjectService.getFinanceSubjectListByLedgerIdAndSubjectCodes(ledgerId, subjectCodes)) {
                result.put(buildSubjectKey(ledgerId, subject.getSubjectCode()), subject);
            }
        }
        return result;
    }

    private ErpFinanceReportItemRespVO buildReportItemResp(ErpFinanceReportItemDO item,
                                                            List<ErpFinanceReportItemSubjectDO> subjects,
                                                            Map<Long, ErpFinanceLedgerDO> ledgerMap,
                                                            Map<String, ErpFinanceSubjectDO> subjectMap) {
        ErpFinanceReportItemRespVO respVO = BeanUtils.toBean(item, ErpFinanceReportItemRespVO.class);
        ErpFinanceLedgerDO ledger = ledgerMap.get(item.getLedgerId());
        respVO.setLedgerName(ledger == null ? null : ledger.getName());
        respVO.setReportTypeName(resolveReportTypeName(item.getReportType()));
        respVO.setItemCategoryName(resolveItemCategoryName(item.getItemCategory()));
        respVO.setSubjects(convertList(subjects, subject -> {
            ErpFinanceReportItemRespVO.SubjectMapping mapping = BeanUtils.toBean(subject,
                    ErpFinanceReportItemRespVO.SubjectMapping.class);
            ErpFinanceSubjectDO financeSubject = subjectMap.get(buildSubjectKey(item.getLedgerId(), subject.getSubjectCode()));
            mapping.setSubjectName(financeSubject == null ? null : financeSubject.getSubjectName());
            mapping.setAmountRuleName(resolveAmountRuleName(subject.getAmountRule()));
            return mapping;
        }));
        return respVO;
    }

    private String buildSubjectKey(Long ledgerId, String subjectCode) {
        return ledgerId + ":" + subjectCode;
    }

    private String resolveReportTypeName(Integer reportType) {
        for (ErpFinanceReportTypeEnum value : ErpFinanceReportTypeEnum.values()) {
            if (value.getType().equals(reportType)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveItemCategoryName(Integer itemCategory) {
        for (ErpFinanceReportItemCategoryEnum value : ErpFinanceReportItemCategoryEnum.values()) {
            if (value.getType().equals(itemCategory)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveAmountRuleName(Integer amountRule) {
        for (ErpFinanceReportAmountRuleEnum value : ErpFinanceReportAmountRuleEnum.values()) {
            if (value.getType().equals(amountRule)) {
                return value.getName();
            }
        }
        return null;
    }
}
