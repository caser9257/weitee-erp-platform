package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPolicyBindingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpNettingPolicyLineMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpNettingPolicyMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpPolicyBindingMapper;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ErpMrpNettingPolicyResolverImpl implements ErpMrpNettingPolicyResolver {

    @Resource
    private ErpMrpNettingPolicyMapper erpMrpNettingPolicyMapper;
    @Resource
    private ErpMrpNettingPolicyLineMapper erpMrpNettingPolicyLineMapper;
    @Resource
    private ErpMrpPolicyBindingMapper erpMrpPolicyBindingMapper;

    @Override
    public ErpMrpNettingRuntimePolicy resolve(String businessType) {
        if (businessType != null && !businessType.isBlank()) {
            ErpMrpPolicyBindingDO binding = erpMrpPolicyBindingMapper.selectByBusinessType(businessType);
            if (binding != null) {
                ErpMrpNettingPolicyDO policy = erpMrpNettingPolicyMapper.selectById(binding.getPolicyId());
                if (policy != null && Boolean.TRUE.equals(policy.getEnableFlag())) {
                    return buildRuntimePolicy(policy);
                }
            }
        }
        ErpMrpNettingPolicyDO defaultPolicy = erpMrpNettingPolicyMapper.selectDefaultPolicy();
        if (defaultPolicy != null) {
            return buildRuntimePolicy(defaultPolicy);
        }
        return ErpMrpNettingRuntimePolicy.standard();
    }

    private ErpMrpNettingRuntimePolicy buildRuntimePolicy(ErpMrpNettingPolicyDO policy) {
        return ErpMrpNettingRuntimePolicy.builder()
                .policyId(policy.getId())
                .code(policy.getCode())
                .name(policy.getName())
                .version(policy.getVersion())
                .defaultPolicy(Boolean.TRUE.equals(policy.getDefaultFlag()))
                .lines(erpMrpNettingPolicyLineMapper.selectListByPolicyId(policy.getId()))
                .build();
    }
}
