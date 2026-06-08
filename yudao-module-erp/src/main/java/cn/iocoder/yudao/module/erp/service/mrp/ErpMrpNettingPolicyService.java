package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicyPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPolicyBindingDO;

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
