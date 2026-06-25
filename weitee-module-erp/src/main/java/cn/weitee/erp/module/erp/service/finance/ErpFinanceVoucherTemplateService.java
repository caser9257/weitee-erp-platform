package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpFinanceVoucherTemplateService {

    Long createVoucherTemplate(@Valid ErpFinanceVoucherTemplateSaveReqVO createReqVO);

    void updateVoucherTemplate(@Valid ErpFinanceVoucherTemplateSaveReqVO updateReqVO);

    void deleteVoucherTemplate(Long id);

    ErpFinanceVoucherTemplateDO getVoucherTemplate(Long id);

    PageResult<ErpFinanceVoucherTemplateDO> getVoucherTemplatePage(ErpFinanceVoucherTemplatePageReqVO pageReqVO);

    List<ErpFinanceVoucherTemplateDO> getVoucherTemplateListByLedgerAndBizType(Long ledgerId, Integer bizType);

    List<ErpFinanceVoucherTemplateItemDO> getVoucherTemplateItemListByTemplateId(Long templateId);

    List<ErpFinanceVoucherTemplateItemDO> getVoucherTemplateItemListByTemplateIds(Collection<Long> templateIds);

    ErpFinanceVoucherTemplateDO validateVoucherTemplate(Long id);
}
