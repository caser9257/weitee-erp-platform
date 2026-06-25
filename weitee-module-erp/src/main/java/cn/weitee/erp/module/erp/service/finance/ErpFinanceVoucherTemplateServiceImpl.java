package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherTemplateItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherTemplateMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.enums.CommonStatusEnum.isDisable;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_NOT_LEAF;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_AMOUNT_SOURCE_VALUE_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_ITEM_EMPTY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_SUBJECT_REQUIRED;

@Service
@Validated
public class ErpFinanceVoucherTemplateServiceImpl implements ErpFinanceVoucherTemplateService {

    @Resource
    private ErpFinanceVoucherTemplateMapper erpFinanceVoucherTemplateMapper;
    @Resource
    private ErpFinanceVoucherTemplateItemMapper erpFinanceVoucherTemplateItemMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinanceSubjectService financeSubjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucherTemplate(ErpFinanceVoucherTemplateSaveReqVO createReqVO) {
        financeLedgerService.validateFinanceLedger(createReqVO.getLedgerId());
        validateTemplateItems(createReqVO.getLedgerId(), createReqVO.getItems());
        ErpFinanceVoucherTemplateDO template = BeanUtils.toBean(createReqVO, ErpFinanceVoucherTemplateDO.class, item ->
                item.setAutoGenerate(Boolean.TRUE.equals(createReqVO.getAutoGenerate())));
        erpFinanceVoucherTemplateMapper.insert(template);
        syncTemplateItems(template.getId(), createReqVO.getItems());
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVoucherTemplate(ErpFinanceVoucherTemplateSaveReqVO updateReqVO) {
        validateVoucherTemplateExists(updateReqVO.getId());
        financeLedgerService.validateFinanceLedger(updateReqVO.getLedgerId());
        validateTemplateItems(updateReqVO.getLedgerId(), updateReqVO.getItems());
        ErpFinanceVoucherTemplateDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceVoucherTemplateDO.class, item ->
                item.setAutoGenerate(Boolean.TRUE.equals(updateReqVO.getAutoGenerate())));
        erpFinanceVoucherTemplateMapper.updateById(updateObj);
        List<ErpFinanceVoucherTemplateItemDO> existedItems = erpFinanceVoucherTemplateItemMapper.selectListByTemplateId(updateReqVO.getId());
        if (CollUtil.isNotEmpty(existedItems)) {
            erpFinanceVoucherTemplateItemMapper.deleteByIds(convertList(existedItems, ErpFinanceVoucherTemplateItemDO::getId));
        }
        syncTemplateItems(updateReqVO.getId(), updateReqVO.getItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVoucherTemplate(Long id) {
        validateVoucherTemplateExists(id);
        List<ErpFinanceVoucherTemplateItemDO> existedItems = erpFinanceVoucherTemplateItemMapper.selectListByTemplateId(id);
        if (CollUtil.isNotEmpty(existedItems)) {
            erpFinanceVoucherTemplateItemMapper.deleteByIds(convertList(existedItems, ErpFinanceVoucherTemplateItemDO::getId));
        }
        erpFinanceVoucherTemplateMapper.deleteById(id);
    }

    @Override
    public ErpFinanceVoucherTemplateDO getVoucherTemplate(Long id) {
        return erpFinanceVoucherTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinanceVoucherTemplateDO> getVoucherTemplatePage(ErpFinanceVoucherTemplatePageReqVO pageReqVO) {
        return erpFinanceVoucherTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpFinanceVoucherTemplateDO> getVoucherTemplateListByLedgerAndBizType(Long ledgerId, Integer bizType) {
        return erpFinanceVoucherTemplateMapper.selectListByLedgerIdAndBizType(ledgerId, bizType);
    }

    @Override
    public List<ErpFinanceVoucherTemplateItemDO> getVoucherTemplateItemListByTemplateId(Long templateId) {
        return erpFinanceVoucherTemplateItemMapper.selectListByTemplateId(templateId);
    }

    @Override
    public List<ErpFinanceVoucherTemplateItemDO> getVoucherTemplateItemListByTemplateIds(Collection<Long> templateIds) {
        return erpFinanceVoucherTemplateItemMapper.selectListByTemplateIds(templateIds);
    }

    @Override
    public ErpFinanceVoucherTemplateDO validateVoucherTemplate(Long id) {
        ErpFinanceVoucherTemplateDO template = validateVoucherTemplateExists(id);
        if (isDisable(template.getStatus())) {
            throw exception(FINANCE_VOUCHER_TEMPLATE_NOT_ENABLE, template.getName());
        }
        return template;
    }

    private ErpFinanceVoucherTemplateDO validateVoucherTemplateExists(Long id) {
        ErpFinanceVoucherTemplateDO template = erpFinanceVoucherTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(FINANCE_VOUCHER_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private void validateTemplateItems(Long ledgerId, List<ErpFinanceVoucherTemplateSaveReqVO.Item> items) {
        if (CollUtil.isEmpty(items)) {
            throw exception(FINANCE_VOUCHER_TEMPLATE_ITEM_EMPTY);
        }
        items.forEach(item -> {
            if (item.getSubjectCode() == null || item.getSubjectCode().isBlank()
                    || item.getSubjectName() == null || item.getSubjectName().isBlank()) {
                throw exception(FINANCE_VOUCHER_TEMPLATE_SUBJECT_REQUIRED);
            }
        });
        Map<String, ErpFinanceSubjectDO> subjectMap = convertMap(
                financeSubjectService.getFinanceSubjectListByLedgerIdAndSubjectCodes(ledgerId,
                        convertSet(items, ErpFinanceVoucherTemplateSaveReqVO.Item::getSubjectCode)),
                ErpFinanceSubjectDO::getSubjectCode);
        for (ErpFinanceVoucherTemplateSaveReqVO.Item item : items) {
            ErpFinanceVoucherAmountSourceEnum amountSource = ErpFinanceVoucherAmountSourceEnum.fromType(item.getAmountSource());
            if (amountSource == null) {
                throw exception(FINANCE_VOUCHER_TEMPLATE_AMOUNT_SOURCE_VALUE_REQUIRED);
            }
            if (amountSource.isNeedValue() && item.getAmountSourceValue() == null) {
                throw exception(FINANCE_VOUCHER_TEMPLATE_AMOUNT_SOURCE_VALUE_REQUIRED);
            }
            if (!amountSource.isNeedValue()) {
                item.setAmountSourceValue(null);
            }
            ErpFinanceSubjectDO subject = subjectMap.get(item.getSubjectCode());
            if (subject == null) {
                throw exception(FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS, item.getSubjectCode());
            }
            if (isDisable(subject.getStatus())) {
                throw exception(FINANCE_SUBJECT_NOT_ENABLE, item.getSubjectCode());
            }
            if (!Boolean.TRUE.equals(subject.getLeaf())) {
                throw exception(FINANCE_SUBJECT_NOT_LEAF, item.getSubjectCode());
            }
            item.setSubjectName(subject.getSubjectName());
        }
    }

    private void syncTemplateItems(Long templateId, List<ErpFinanceVoucherTemplateSaveReqVO.Item> items) {
        List<ErpFinanceVoucherTemplateItemDO> createItems = new java.util.ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            ErpFinanceVoucherTemplateSaveReqVO.Item item = items.get(i);
            Integer entryNo = i + 1;
            createItems.add(BeanUtils.toBean(item, ErpFinanceVoucherTemplateItemDO.class, target -> target
                    .setTemplateId(templateId)
                    .setEntryNo(entryNo)));
        }
        if (CollUtil.isNotEmpty(createItems)) {
            erpFinanceVoucherTemplateItemMapper.insertBatch(createItems);
        }
    }
}
