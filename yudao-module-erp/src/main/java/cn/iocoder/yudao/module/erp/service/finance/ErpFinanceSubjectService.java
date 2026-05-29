package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpFinanceSubjectService {

    Long createFinanceSubject(ErpFinanceSubjectSaveReqVO createReqVO);

    void updateFinanceSubject(ErpFinanceSubjectSaveReqVO updateReqVO);

    void deleteFinanceSubject(Long id);

    ErpFinanceSubjectDO getFinanceSubject(Long id);

    PageResult<ErpFinanceSubjectDO> getFinanceSubjectPage(ErpFinanceSubjectPageReqVO pageReqVO);

    List<ErpFinanceSubjectDO> getFinanceSubjectListByLedgerId(Long ledgerId);

    List<ErpFinanceSubjectDO> getFinanceSubjectListByLedgerIdAndSubjectCodes(Long ledgerId, Collection<String> subjectCodes);

    default Map<String, ErpFinanceSubjectDO> getFinanceSubjectMapByLedgerIdAndSubjectCodes(Long ledgerId,
                                                                                            Collection<String> subjectCodes) {
        return convertMap(getFinanceSubjectListByLedgerIdAndSubjectCodes(ledgerId, subjectCodes),
                ErpFinanceSubjectDO::getSubjectCode);
    }

}
