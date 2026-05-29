package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceReportItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceReportItemSubjectMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceSubjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_DELETE_FAIL_USED_BY_REPORT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_NOT_EXISTS;

@Service
@Validated
public class ErpFinanceSubjectServiceImpl implements ErpFinanceSubjectService {

    @Resource
    private ErpFinanceSubjectMapper erpFinanceSubjectMapper;
    @Resource
    private ErpFinanceReportItemMapper erpFinanceReportItemMapper;
    @Resource
    private ErpFinanceReportItemSubjectMapper erpFinanceReportItemSubjectMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @Override
    public Long createFinanceSubject(ErpFinanceSubjectSaveReqVO createReqVO) {
        financeLedgerService.validateFinanceLedger(createReqVO.getLedgerId());
        validateParentSubject(createReqVO.getLedgerId(), createReqVO.getParentId());
        validateSubjectCodeUnique(null, createReqVO.getLedgerId(), createReqVO.getSubjectCode());
        ErpFinanceSubjectDO subject = BeanUtils.toBean(createReqVO, ErpFinanceSubjectDO.class, item -> {
            item.setLeaf(ObjectUtil.defaultIfNull(createReqVO.getLeaf(), Boolean.TRUE));
            item.setSort(ObjectUtil.defaultIfNull(createReqVO.getSort(), 0));
        });
        erpFinanceSubjectMapper.insert(subject);
        return subject.getId();
    }

    @Override
    public void updateFinanceSubject(ErpFinanceSubjectSaveReqVO updateReqVO) {
        validateFinanceSubjectExists(updateReqVO.getId());
        financeLedgerService.validateFinanceLedger(updateReqVO.getLedgerId());
        validateParentSubject(updateReqVO.getLedgerId(), updateReqVO.getParentId());
        validateSubjectCodeUnique(updateReqVO.getId(), updateReqVO.getLedgerId(), updateReqVO.getSubjectCode());
        ErpFinanceSubjectDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceSubjectDO.class, item -> {
            item.setLeaf(ObjectUtil.defaultIfNull(updateReqVO.getLeaf(), Boolean.TRUE));
            item.setSort(ObjectUtil.defaultIfNull(updateReqVO.getSort(), 0));
        });
        erpFinanceSubjectMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanceSubject(Long id) {
        ErpFinanceSubjectDO subject = validateFinanceSubjectExists(id);
        validateSubjectNotUsedByReportItem(subject);
        erpFinanceSubjectMapper.deleteById(id);
    }

    @Override
    public ErpFinanceSubjectDO getFinanceSubject(Long id) {
        return erpFinanceSubjectMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinanceSubjectDO> getFinanceSubjectPage(ErpFinanceSubjectPageReqVO pageReqVO) {
        return erpFinanceSubjectMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpFinanceSubjectDO> getFinanceSubjectListByLedgerId(Long ledgerId) {
        return erpFinanceSubjectMapper.selectListByLedgerId(ledgerId);
    }

    @Override
    public List<ErpFinanceSubjectDO> getFinanceSubjectListByLedgerIdAndSubjectCodes(Long ledgerId,
                                                                                     Collection<String> subjectCodes) {
        return erpFinanceSubjectMapper.selectListByLedgerIdAndSubjectCodes(ledgerId, subjectCodes);
    }

    private ErpFinanceSubjectDO validateFinanceSubjectExists(Long id) {
        ErpFinanceSubjectDO subject = erpFinanceSubjectMapper.selectById(id);
        if (subject == null) {
            throw exception(FINANCE_SUBJECT_NOT_EXISTS);
        }
        return subject;
    }

    private void validateParentSubject(Long ledgerId, Long parentId) {
        if (parentId == null) {
            return;
        }
        ErpFinanceSubjectDO parent = erpFinanceSubjectMapper.selectById(parentId);
        if (parent == null || !Objects.equals(parent.getLedgerId(), ledgerId)) {
            throw exception(FINANCE_SUBJECT_NOT_EXISTS);
        }
    }

    private void validateSubjectCodeUnique(Long id, Long ledgerId, String subjectCode) {
        ErpFinanceSubjectDO subject = erpFinanceSubjectMapper.selectByLedgerIdAndSubjectCode(ledgerId, subjectCode);
        if (subject == null) {
            return;
        }
        if (id == null || !Objects.equals(subject.getId(), id)) {
            throw exception(FINANCE_SUBJECT_CODE_DUPLICATE, ledgerId, subjectCode);
        }
    }

    private void validateSubjectNotUsedByReportItem(ErpFinanceSubjectDO subject) {
        List<ErpFinanceReportItemSubjectDO> mappings = erpFinanceReportItemSubjectMapper.selectListBySubjectCode(subject.getSubjectCode());
        if (CollUtil.isEmpty(mappings)) {
            return;
        }
        Set<Long> itemIds = convertSet(mappings, ErpFinanceReportItemSubjectDO::getItemId);
        List<ErpFinanceReportItemDO> items = erpFinanceReportItemMapper.selectByIds(itemIds);
        if (items == null) {
            items = Collections.emptyList();
        }
        boolean usedBySameLedger = items.stream().anyMatch(item -> Objects.equals(item.getLedgerId(), subject.getLedgerId()));
        if (usedBySameLedger) {
            throw exception(FINANCE_SUBJECT_DELETE_FAIL_USED_BY_REPORT, subject.getSubjectCode());
        }
    }

}
