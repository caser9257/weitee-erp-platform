package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentRollbackReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpFinancePrepaymentService {

    Long createFinancePrepayment(@Valid ErpFinancePrepaymentSaveReqVO createReqVO);

    void updateFinancePrepayment(@Valid ErpFinancePrepaymentSaveReqVO updateReqVO);

    void updateFinancePrepaymentStatus(Long id, Integer status);

    void deleteFinancePrepayment(List<Long> ids);

    ErpFinancePrepaymentDO getFinancePrepayment(Long id);

    PageResult<ErpFinancePrepaymentDO> getFinancePrepaymentPage(ErpFinancePrepaymentPageReqVO pageReqVO);

    void allocateFinancePrepayment(@Valid ErpFinancePrepaymentAllocateReqVO reqVO);

    void rollbackFinancePrepaymentAllocate(@Valid ErpFinancePrepaymentRollbackReqVO reqVO);

    List<ErpFinancePrepaymentAllocateDO> getFinancePrepaymentAllocateListByPrepaymentId(Long prepaymentId);

    List<ErpFinancePrepaymentAllocateDO> getFinancePrepaymentAllocateListByPrepaymentIds(Collection<Long> prepaymentIds);

    List<ErpFinancePrepaymentAllocateDO> getApprovedFinancePrepaymentAllocateListByStatementIds(Collection<Long> statementIds);

}
