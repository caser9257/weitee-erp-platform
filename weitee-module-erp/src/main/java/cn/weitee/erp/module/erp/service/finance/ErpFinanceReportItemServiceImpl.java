package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemSubjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceSubjectMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportAmountRuleEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportItemCategoryEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceSubjectTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_AMOUNT_SIGN_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_CATEGORY_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_CODE_DUPLICATE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_SUBJECT_EMPTY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_NOT_ENABLE;

@Service
@Validated
public class ErpFinanceReportItemServiceImpl implements ErpFinanceReportItemService {

    @Resource
    private ErpFinanceReportItemMapper erpFinanceReportItemMapper;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;
    @Resource
    private ErpFinanceReportItemSubjectMapper erpFinanceReportItemSubjectMapper;
    @Resource
    private ErpFinanceSubjectMapper erpFinanceSubjectMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinanceReportItem(ErpFinanceReportItemSaveReqVO createReqVO) {
        financeLedgerService.validateFinanceLedger(createReqVO.getLedgerId());
        validateReportItemCategory(createReqVO.getReportType(), createReqVO.getItemCategory());
        validateReportItemCodeUnique(null, createReqVO.getLedgerId(), createReqVO.getReportType(), createReqVO.getItemCode());
        validateReportItemSubjects(createReqVO.getLedgerId(), createReqVO.getSubjects());
        ErpFinanceReportItemDO reportItem = BeanUtils.toBean(createReqVO, ErpFinanceReportItemDO.class, item ->
                item.setSort(ObjectUtil.defaultIfNull(createReqVO.getSort(), 0)));
        erpFinanceReportItemMapper.insert(reportItem);
        syncReportItemSubjects(reportItem.getId(), createReqVO.getSubjects());
        return reportItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceReportItem(ErpFinanceReportItemSaveReqVO updateReqVO) {
        validateFinanceReportItemExists(updateReqVO.getId());
        financeLedgerService.validateFinanceLedger(updateReqVO.getLedgerId());
        validateReportItemCategory(updateReqVO.getReportType(), updateReqVO.getItemCategory());
        validateReportItemCodeUnique(updateReqVO.getId(), updateReqVO.getLedgerId(), updateReqVO.getReportType(), updateReqVO.getItemCode());
        validateReportItemSubjects(updateReqVO.getLedgerId(), updateReqVO.getSubjects());
        ErpFinanceReportItemDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceReportItemDO.class, item ->
                item.setSort(ObjectUtil.defaultIfNull(updateReqVO.getSort(), 0)));
        erpFinanceReportItemMapper.updateById(updateObj);
        erpFinanceReportItemSubjectMapper.deleteByItemId(updateReqVO.getId());
        syncReportItemSubjects(updateReqVO.getId(), updateReqVO.getSubjects());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanceReportItem(Long id) {
        validateFinanceReportItemExists(id);
        erpFinanceReportItemSubjectMapper.deleteByItemId(id);
        erpFinanceReportItemMapper.deleteById(id);
    }

    @Override
    public ErpFinanceReportItemDO getFinanceReportItem(Long id) {
        return erpFinanceReportItemMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinanceReportItemDO> getFinanceReportItemPage(ErpFinanceReportItemPageReqVO pageReqVO) {
        List<Long> visibleLedgerIds = financeDataPermissionService.getVisibleLedgerIds();
        if (visibleLedgerIds == null) {
            return erpFinanceReportItemMapper.selectPage(pageReqVO);
        }
        if (CollUtil.isEmpty(visibleLedgerIds)) {
            return PageResult.empty(0L);
        }
        Map<Long, Set<String>> subjectCodesByLedger = new LinkedHashMap<>();
        FinancePermissionScope permissionScope = financeDataPermissionService.getPermissionScope();
        List<Long> scopedLedgerIds = visibleLedgerIds.stream().filter(ledgerId -> {
            FinancePermissionScope.Scope<String> subjectScope = permissionScope == null ? FinancePermissionScope.Scope.all()
                    : permissionScope.subjectScopesByLedger().getOrDefault(ledgerId, FinancePermissionScope.Scope.all());
            if (subjectScope.mode() == FinancePermissionScope.ScopeMode.LIMITED) {
                subjectCodesByLedger.put(ledgerId, subjectScope.values());
            }
            return subjectScope.mode() != FinancePermissionScope.ScopeMode.NONE;
        }).toList();
        if (CollUtil.isEmpty(scopedLedgerIds)) {
            return PageResult.empty(0L);
        }
        return subjectCodesByLedger.isEmpty()
                ? erpFinanceReportItemMapper.selectPageByVisibleLedgerIds(pageReqVO, scopedLedgerIds)
                : erpFinanceReportItemMapper.selectPageByVisibleLedgerIdsAndSubjectCodes(
                pageReqVO, scopedLedgerIds, subjectCodesByLedger);
    }

    @Override
    public List<ErpFinanceReportItemSubjectDO> getFinanceReportItemSubjectListByItemId(Long itemId) {
        return erpFinanceReportItemSubjectMapper.selectListByItemId(itemId);
    }

    @Override
    public List<ErpFinanceReportItemSubjectDO> getFinanceReportItemSubjectListByItemIds(Collection<Long> itemIds) {
        return erpFinanceReportItemSubjectMapper.selectListByItemIds(itemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpFinanceReportTemplateInitRespVO initStandardTemplate(ErpFinanceReportTemplateInitReqVO reqVO) {
        financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        Map<String, ErpFinanceSubjectDO> existedSubjectMap = convertMap(
                erpFinanceSubjectMapper.selectListByLedgerIdAndSubjectCodes(reqVO.getLedgerId(), STANDARD_SUBJECT_MAP.keySet()),
                ErpFinanceSubjectDO::getSubjectCode);
        int createdSubjectCount = 0;
        for (StandardSubject standardSubject : STANDARD_SUBJECT_MAP.values()) {
            if (existedSubjectMap.containsKey(standardSubject.subjectCode)) {
                continue;
            }
            erpFinanceSubjectMapper.insert(new ErpFinanceSubjectDO()
                    .setLedgerId(reqVO.getLedgerId())
                    .setSubjectCode(standardSubject.subjectCode)
                    .setSubjectName(standardSubject.subjectName)
                    .setSubjectType(standardSubject.subjectType)
                    .setBalanceDirection(standardSubject.balanceDirection)
                    .setLeaf(true)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus())
                    .setSort(standardSubject.sort)
                    .setRemark("系统标准模板初始化"));
            createdSubjectCount++;
        }

        int createdItemCount = 0;
        int skippedItemCount = 0;
        int createdMappingCount = 0;
        List<ErpFinanceReportItemDO> existedItems = erpFinanceReportItemMapper.selectList(
                new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceReportItemDO>()
                        .eq(ErpFinanceReportItemDO::getLedgerId, reqVO.getLedgerId()));
        Map<String, ErpFinanceReportItemDO> existedItemMap = convertMap(existedItems,
                item -> item.getReportType() + ":" + item.getItemCode());
        for (StandardReportItem standardItem : STANDARD_REPORT_ITEMS) {
            ErpFinanceReportItemDO existedItem = existedItemMap.get(standardItem.reportType + ":" + standardItem.itemCode);
            if (existedItem != null && !Boolean.TRUE.equals(reqVO.getOverrideExisting())) {
                skippedItemCount++;
                continue;
            }
            Long itemId;
            if (existedItem == null) {
                ErpFinanceReportItemDO createItem = new ErpFinanceReportItemDO()
                        .setLedgerId(reqVO.getLedgerId())
                        .setReportType(standardItem.reportType)
                        .setItemCategory(standardItem.itemCategory)
                        .setItemCode(standardItem.itemCode)
                        .setItemName(standardItem.itemName)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setSort(standardItem.sort)
                        .setRemark("系统标准模板初始化");
                erpFinanceReportItemMapper.insert(createItem);
                itemId = createItem.getId();
                createdItemCount++;
            } else {
                itemId = existedItem.getId();
                erpFinanceReportItemSubjectMapper.deleteByItemId(itemId);
            }
            List<ErpFinanceReportItemSubjectDO> mappings = convertList(standardItem.mappings, mapping ->
                    new ErpFinanceReportItemSubjectDO()
                            .setItemId(itemId)
                            .setSubjectCode(mapping.subjectCode)
                            .setAmountRule(mapping.amountRule)
                            .setAmountSign(mapping.amountSign));
            erpFinanceReportItemSubjectMapper.insertBatch(mappings);
            createdMappingCount += mappings.size();
        }

        ErpFinanceReportTemplateInitRespVO respVO = new ErpFinanceReportTemplateInitRespVO();
        respVO.setLedgerId(reqVO.getLedgerId());
        respVO.setCreatedSubjectCount(createdSubjectCount);
        respVO.setExistedSubjectCount(existedSubjectMap.size());
        respVO.setCreatedItemCount(createdItemCount);
        respVO.setSkippedItemCount(skippedItemCount);
        respVO.setCreatedMappingCount(createdMappingCount);
        return respVO;
    }

    private ErpFinanceReportItemDO validateFinanceReportItemExists(Long id) {
        ErpFinanceReportItemDO item = erpFinanceReportItemMapper.selectById(id);
        if (item == null) {
            throw exception(FINANCE_REPORT_ITEM_NOT_EXISTS);
        }
        return item;
    }

    private void validateReportItemCodeUnique(Long id, Long ledgerId, Integer reportType, String itemCode) {
        ErpFinanceReportItemDO item = erpFinanceReportItemMapper.selectByLedgerIdAndReportTypeAndItemCode(ledgerId, reportType, itemCode);
        if (item == null) {
            return;
        }
        if (id == null || !Objects.equals(item.getId(), id)) {
            throw exception(FINANCE_REPORT_ITEM_CODE_DUPLICATE, ledgerId, itemCode);
        }
    }

    private void validateReportItemSubjects(Long ledgerId, List<ErpFinanceReportItemSaveReqVO.SubjectMapping> subjects) {
        if (CollUtil.isEmpty(subjects)) {
            throw exception(FINANCE_REPORT_ITEM_SUBJECT_EMPTY);
        }
        for (ErpFinanceReportItemSaveReqVO.SubjectMapping subject : subjects) {
            if (!Integer.valueOf(1).equals(subject.getAmountSign()) && !Integer.valueOf(-1).equals(subject.getAmountSign())) {
                throw exception(FINANCE_REPORT_ITEM_AMOUNT_SIGN_INVALID);
            }
        }
        Set<String> subjectCodes = convertSet(subjects, ErpFinanceReportItemSaveReqVO.SubjectMapping::getSubjectCode);
        Map<String, ErpFinanceSubjectDO> subjectMap = convertMap(
                erpFinanceSubjectMapper.selectListByLedgerIdAndSubjectCodes(ledgerId, subjectCodes),
                ErpFinanceSubjectDO::getSubjectCode);
        for (String subjectCode : subjectCodes) {
            ErpFinanceSubjectDO subject = subjectMap.get(subjectCode);
            if (subject == null) {
                throw exception(FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS, subjectCode);
            }
            if (CommonStatusEnum.isDisable(subject.getStatus())) {
                throw exception(FINANCE_SUBJECT_NOT_ENABLE, subjectCode);
            }
        }
    }

    private void validateReportItemCategory(Integer reportType, Integer itemCategory) {
        boolean matched = false;
        if (ErpFinanceReportTypeEnum.BALANCE_SHEET.getType().equals(reportType)) {
            matched = ErpFinanceReportItemCategoryEnum.ASSET.getType().equals(itemCategory)
                    || ErpFinanceReportItemCategoryEnum.LIABILITY.getType().equals(itemCategory)
                    || ErpFinanceReportItemCategoryEnum.EQUITY.getType().equals(itemCategory);
        } else if (ErpFinanceReportTypeEnum.INCOME_STATEMENT.getType().equals(reportType)) {
            matched = ErpFinanceReportItemCategoryEnum.REVENUE.getType().equals(itemCategory)
                    || ErpFinanceReportItemCategoryEnum.COST_EXPENSE.getType().equals(itemCategory);
        } else if (ErpFinanceReportTypeEnum.CASH_FLOW_STATEMENT.getType().equals(reportType)) {
            matched = ErpFinanceReportItemCategoryEnum.CASH_INFLOW.getType().equals(itemCategory)
                    || ErpFinanceReportItemCategoryEnum.CASH_OUTFLOW.getType().equals(itemCategory);
        }
        if (!matched) {
            throw exception(FINANCE_REPORT_ITEM_CATEGORY_MISMATCH, itemCategory, reportType);
        }
    }

    private void syncReportItemSubjects(Long itemId, List<ErpFinanceReportItemSaveReqVO.SubjectMapping> subjects) {
        List<ErpFinanceReportItemSubjectDO> createList = convertList(subjects, subject ->
                BeanUtils.toBean(subject, ErpFinanceReportItemSubjectDO.class, item -> item.setItemId(itemId)));
        if (CollUtil.isNotEmpty(createList)) {
            erpFinanceReportItemSubjectMapper.insertBatch(createList);
        }
    }

    private static final Map<String, StandardSubject> STANDARD_SUBJECT_MAP = buildStandardSubjectMap();

    private static final List<StandardReportItem> STANDARD_REPORT_ITEMS = List.of(
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.ASSET, "BS-CASH", "货币资金", 10,
                    mapping("1001", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1),
                    mapping("1002", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.ASSET, "BS-AR", "应收账款", 20,
                    mapping("1122", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.ASSET, "BS-INVENTORY", "存货", 30,
                    mapping("1403", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1),
                    mapping("1405", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.ASSET, "BS-LONG-ASSET", "长期资产", 40,
                    mapping("1601", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1),
                    mapping("1701", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.LIABILITY, "BS-LOAN", "短期借款", 50,
                    mapping("2001", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.LIABILITY, "BS-AP", "应付账款", 60,
                    mapping("2202", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.LIABILITY, "BS-PAYROLL", "应付职工薪酬", 70,
                    mapping("2211", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.LIABILITY, "BS-TAX", "应交税费", 80,
                    mapping("2221", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.EQUITY, "BS-CAPITAL", "实收资本", 90,
                    mapping("4001", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.BALANCE_SHEET, ErpFinanceReportItemCategoryEnum.EQUITY, "BS-PROFIT", "本年利润", 100,
                    mapping("4103", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.INCOME_STATEMENT, ErpFinanceReportItemCategoryEnum.REVENUE, "IS-REVENUE", "营业收入", 10,
                    mapping("6001", ErpFinanceReportAmountRuleEnum.CURRENT_CREDIT, 1)),
            item(ErpFinanceReportTypeEnum.INCOME_STATEMENT, ErpFinanceReportItemCategoryEnum.COST_EXPENSE, "IS-COST", "营业成本", 20,
                    mapping("6401", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.INCOME_STATEMENT, ErpFinanceReportItemCategoryEnum.COST_EXPENSE, "IS-SELLING-EXPENSE", "销售费用", 30,
                    mapping("6601", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.INCOME_STATEMENT, ErpFinanceReportItemCategoryEnum.COST_EXPENSE, "IS-MANAGE-FINANCE-EXPENSE", "管理及财务费用", 40,
                    mapping("6602", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT, 1),
                    mapping("6603", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.CASH_FLOW_STATEMENT, ErpFinanceReportItemCategoryEnum.CASH_INFLOW, "CF-INFLOW", "现金流入", 10,
                    mapping("1001", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT, 1),
                    mapping("1002", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT, 1)),
            item(ErpFinanceReportTypeEnum.CASH_FLOW_STATEMENT, ErpFinanceReportItemCategoryEnum.CASH_OUTFLOW, "CF-OUTFLOW", "现金流出", 20,
                    mapping("1001", ErpFinanceReportAmountRuleEnum.CURRENT_CREDIT, 1),
                    mapping("1002", ErpFinanceReportAmountRuleEnum.CURRENT_CREDIT, 1))
    );

    private static Map<String, StandardSubject> buildStandardSubjectMap() {
        Map<String, StandardSubject> map = new LinkedHashMap<>();
        addSubject(map, "1001", "库存现金", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 10);
        addSubject(map, "1002", "银行存款", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 20);
        addSubject(map, "1122", "应收账款", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 30);
        addSubject(map, "1403", "原材料", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 40);
        addSubject(map, "1405", "库存商品", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 50);
        addSubject(map, "1601", "固定资产", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 60);
        addSubject(map, "1701", "无形资产", ErpFinanceSubjectTypeEnum.ASSET, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 70);
        addSubject(map, "2001", "短期借款", ErpFinanceSubjectTypeEnum.LIABILITY, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 80);
        addSubject(map, "2202", "应付账款", ErpFinanceSubjectTypeEnum.LIABILITY, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 90);
        addSubject(map, "2211", "应付职工薪酬", ErpFinanceSubjectTypeEnum.LIABILITY, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 100);
        addSubject(map, "2221", "应交税费", ErpFinanceSubjectTypeEnum.LIABILITY, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 110);
        addSubject(map, "4001", "实收资本", ErpFinanceSubjectTypeEnum.EQUITY, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 120);
        addSubject(map, "4103", "本年利润", ErpFinanceSubjectTypeEnum.EQUITY, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 130);
        addSubject(map, "6001", "主营业务收入", ErpFinanceSubjectTypeEnum.REVENUE, ErpFinanceVoucherEntryDirectionEnum.CREDIT, 140);
        addSubject(map, "6401", "主营业务成本", ErpFinanceSubjectTypeEnum.COST, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 150);
        addSubject(map, "6601", "销售费用", ErpFinanceSubjectTypeEnum.EXPENSE, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 160);
        addSubject(map, "6602", "管理费用", ErpFinanceSubjectTypeEnum.EXPENSE, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 170);
        addSubject(map, "6603", "财务费用", ErpFinanceSubjectTypeEnum.EXPENSE, ErpFinanceVoucherEntryDirectionEnum.DEBIT, 180);
        return map;
    }

    private static void addSubject(Map<String, StandardSubject> map, String subjectCode, String subjectName,
                                   ErpFinanceSubjectTypeEnum subjectType, ErpFinanceVoucherEntryDirectionEnum direction,
                                   Integer sort) {
        map.put(subjectCode, new StandardSubject(subjectCode, subjectName, subjectType.getType(), direction.getType(), sort));
    }

    private static StandardReportItem item(ErpFinanceReportTypeEnum reportType, ErpFinanceReportItemCategoryEnum category,
                                           String itemCode, String itemName, Integer sort, StandardMapping... mappings) {
        return new StandardReportItem(reportType.getType(), category.getType(), itemCode, itemName, sort, List.of(mappings));
    }

    private static StandardMapping mapping(String subjectCode, ErpFinanceReportAmountRuleEnum amountRule, Integer amountSign) {
        return new StandardMapping(subjectCode, amountRule.getType(), amountSign);
    }

    private record StandardSubject(String subjectCode, String subjectName, Integer subjectType,
                                   Integer balanceDirection, Integer sort) {
    }

    private record StandardReportItem(Integer reportType, Integer itemCategory, String itemCode,
                                      String itemName, Integer sort, List<StandardMapping> mappings) {
    }

    private record StandardMapping(String subjectCode, Integer amountRule, Integer amountSign) {
    }

}
