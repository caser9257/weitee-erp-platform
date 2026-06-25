package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicyPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicySaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPolicyBindingDO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

public interface ErpMrpNettingPolicyService {

    Long createPolicy(@Valid ErpMrpNettingPolicySaveReqVO createReqVO);

    void updatePolicy(@Valid ErpMrpNettingPolicySaveReqVO updateReqVO);

    void deletePolicy(Long id);

    ErpMrpNettingPolicyDO getPolicy(Long id);

    PageResult<ErpMrpNettingPolicyDO> getPolicyPage(ErpMrpNettingPolicyPageReqVO pageReqVO);

    List<ErpMrpNettingPolicyLineDO> getPolicyLineList(Set<Long> policyIds);

    List<ErpMrpPolicyBindingDO> getPolicyBindingList(Set<Long> policyIds);
}
