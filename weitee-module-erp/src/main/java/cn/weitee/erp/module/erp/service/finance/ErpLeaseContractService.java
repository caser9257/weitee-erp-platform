package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpLeaseContractDO;

import java.util.List;

/**
 * 租赁合同 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface ErpLeaseContractService {

    Long createLeaseContract(ErpLeaseContractSaveReqVO reqVO);

    void updateLeaseContract(ErpLeaseContractSaveReqVO reqVO);

    void deleteLeaseContract(Long id);

    ErpLeaseContractDO getLeaseContract(Long id);

    PageResult<ErpLeaseContractDO> getLeaseContractPage(ErpLeaseContractPageReqVO reqVO);

    List<ErpLeaseContractDO> getLeaseContractList();

    /**
     * 提交审批
     */
    void submitApproval(Long id);

    /**
     * 审批通过
     */
    void approve(Long id, String remark);

    /**
     * 审批驳回
     */
    void reject(Long id, String remark);

}
