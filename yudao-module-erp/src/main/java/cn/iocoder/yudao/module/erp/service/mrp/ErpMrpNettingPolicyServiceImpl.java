package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicyPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPolicyBindingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpNettingPolicyLineMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpNettingPolicyMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpPolicyBindingMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpNettingComponentEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.MRP_NETTING_POLICY_DEFAULT_DELETE_FORBIDDEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.MRP_NETTING_POLICY_NOT_EXISTS;

@Service
@Validated
public class ErpMrpNettingPolicyServiceImpl implements ErpMrpNettingPolicyService {

    @Resource
    private ErpMrpNettingPolicyMapper erpMrpNettingPolicyMapper;
    @Resource
    private ErpMrpNettingPolicyLineMapper erpMrpNettingPolicyLineMapper;
    @Resource
    private ErpMrpPolicyBindingMapper erpMrpPolicyBindingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPolicy(ErpMrpNettingPolicySaveReqVO createReqVO) {
        NormalizedPolicyRequest normalized = normalizeRequest(createReqVO);
        validateRequest(null, normalized);
        ErpMrpNettingPolicyDO policy = BeanUtils.toBean(createReqVO, ErpMrpNettingPolicyDO.class, item -> item
                .setCode(normalized.code)
                .setName(normalized.name)
                .setVersion(1)
                .setEnableFlag(normalized.enableFlag)
                .setDefaultFlag(normalized.defaultFlag));
        erpMrpNettingPolicyMapper.insert(policy);
        syncPolicyLines(policy.getId(), normalized.lines);
        syncPolicyBindings(policy.getId(), normalized.businessTypes);
        if (normalized.defaultFlag) {
            clearOtherDefaultPolicies(policy.getId());
        }
        return policy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicy(ErpMrpNettingPolicySaveReqVO updateReqVO) {
        ErpMrpNettingPolicyDO existed = validatePolicyExists(updateReqVO.getId());
        NormalizedPolicyRequest normalized = normalizeRequest(updateReqVO);
        validateRequest(existed, normalized);
        ErpMrpNettingPolicyDO updateObj = BeanUtils.toBean(updateReqVO, ErpMrpNettingPolicyDO.class, item -> item
                .setId(updateReqVO.getId())
                .setCode(normalized.code)
                .setName(normalized.name)
                .setVersion((existed.getVersion() == null ? 0 : existed.getVersion()) + 1)
                .setEnableFlag(normalized.enableFlag)
                .setDefaultFlag(normalized.defaultFlag));
        erpMrpNettingPolicyMapper.updateById(updateObj);
        syncPolicyLines(updateReqVO.getId(), normalized.lines);
        syncPolicyBindings(updateReqVO.getId(), normalized.businessTypes);
        if (normalized.defaultFlag) {
            clearOtherDefaultPolicies(updateReqVO.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(Long id) {
        ErpMrpNettingPolicyDO policy = validatePolicyExists(id);
        if (Boolean.TRUE.equals(policy.getDefaultFlag())) {
            throw exception(MRP_NETTING_POLICY_DEFAULT_DELETE_FORBIDDEN);
        }
        erpMrpNettingPolicyLineMapper.deleteByPolicyId(id);
        erpMrpPolicyBindingMapper.deleteByPolicyId(id);
        erpMrpNettingPolicyMapper.deleteById(id);
    }

    @Override
    public ErpMrpNettingPolicyDO getPolicy(Long id) {
        return erpMrpNettingPolicyMapper.selectById(id);
    }

    @Override
    public PageResult<ErpMrpNettingPolicyDO> getPolicyPage(ErpMrpNettingPolicyPageReqVO pageReqVO) {
        Collection<Long> policyIds = null;
        if (pageReqVO.getBusinessType() != null && !pageReqVO.getBusinessType().isBlank()) {
            List<ErpMrpPolicyBindingDO> bindings = erpMrpPolicyBindingMapper.selectListByBusinessTypes(List.of(pageReqVO.getBusinessType()));
            policyIds = bindings.stream().map(ErpMrpPolicyBindingDO::getPolicyId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (policyIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }
        return erpMrpNettingPolicyMapper.selectPage(pageReqVO, policyIds);
    }

    @Override
    public List<ErpMrpNettingPolicyLineDO> getPolicyLineList(Set<Long> policyIds) {
        if (CollUtil.isEmpty(policyIds)) {
            return Collections.emptyList();
        }
        return erpMrpNettingPolicyLineMapper.selectListByPolicyIds(policyIds);
    }

    @Override
    public List<ErpMrpPolicyBindingDO> getPolicyBindingList(Set<Long> policyIds) {
        if (CollUtil.isEmpty(policyIds)) {
            return Collections.emptyList();
        }
        return erpMrpPolicyBindingMapper.selectListByPolicyIds(policyIds);
    }

    private void validateRequest(ErpMrpNettingPolicyDO existed, NormalizedPolicyRequest normalized) {
        ErpMrpNettingPolicyDO sameCodePolicy = erpMrpNettingPolicyMapper.selectByCode(normalized.code);
        if (sameCodePolicy != null && (existed == null || !Objects.equals(sameCodePolicy.getId(), existed.getId()))) {
            throw invalidParamException("策略编码已存在");
        }
        validateLines(normalized.lines);
        validateBusinessTypes(existed == null ? null : existed.getId(), normalized.businessTypes);
    }

    private void validateLines(List<ErpMrpNettingPolicyLineDO> lines) {
        Map<String, ErpMrpNettingPolicyLineDO> lineMap = new LinkedHashMap<>();
        Set<Integer> usedSequence = new LinkedHashSet<>();
        for (ErpMrpNettingPolicyLineDO line : lines) {
            ErpMrpNettingComponentEnum component = ErpMrpNettingComponentEnum.valueOfCode(line.getComponentCode());
            if (component == null) {
                throw invalidParamException("存在不支持的组件编码");
            }
            if (lineMap.putIfAbsent(line.getComponentCode(), line) != null) {
                throw invalidParamException("组件配置存在重复项");
            }
            if (!Boolean.TRUE.equals(line.getEnableFlag())) {
                continue;
            }
            if ("SUPPLY_CONSUME".equals(component.getRole())) {
                if (line.getSequenceNo() == null || line.getSequenceNo() <= 0) {
                    throw invalidParamException(component.getLabel() + "的抵扣顺序必须大于 0");
                }
                if (!usedSequence.add(line.getSequenceNo())) {
                    throw invalidParamException("抵扣顺序不能重复");
                }
            }
        }
    }

    private void validateBusinessTypes(Long currentPolicyId, List<String> businessTypes) {
        if (CollUtil.isEmpty(businessTypes)) {
            return;
        }
        List<ErpMrpPolicyBindingDO> bindings = erpMrpPolicyBindingMapper.selectListByBusinessTypes(businessTypes);
        for (ErpMrpPolicyBindingDO binding : bindings) {
            if (Objects.equals(binding.getPolicyId(), currentPolicyId)) {
                continue;
            }
            throw invalidParamException("业务类型 " + binding.getBusinessType() + " 已绑定其他策略");
        }
    }

    private void syncPolicyLines(Long policyId, List<ErpMrpNettingPolicyLineDO> lines) {
        erpMrpNettingPolicyLineMapper.deleteByPolicyId(policyId);
        for (ErpMrpNettingPolicyLineDO line : lines) {
            erpMrpNettingPolicyLineMapper.insert(line.setId(null).setPolicyId(policyId));
        }
    }

    private void syncPolicyBindings(Long policyId, List<String> businessTypes) {
        erpMrpPolicyBindingMapper.deleteByPolicyId(policyId);
        if (CollUtil.isEmpty(businessTypes)) {
            return;
        }
        for (String businessType : businessTypes) {
            erpMrpPolicyBindingMapper.insert(ErpMrpPolicyBindingDO.builder()
                    .policyId(policyId)
                    .businessType(businessType)
                    .enableFlag(Boolean.TRUE)
                    .build());
        }
    }

    private void clearOtherDefaultPolicies(Long currentPolicyId) {
        List<ErpMrpNettingPolicyDO> defaultPolicies = erpMrpNettingPolicyMapper.selectList(ErpMrpNettingPolicyDO::getDefaultFlag, Boolean.TRUE);
        for (ErpMrpNettingPolicyDO policy : defaultPolicies) {
            if (Objects.equals(policy.getId(), currentPolicyId)) {
                continue;
            }
            erpMrpNettingPolicyMapper.updateById(new ErpMrpNettingPolicyDO()
                    .setId(policy.getId())
                    .setDefaultFlag(Boolean.FALSE));
        }
    }

    private NormalizedPolicyRequest normalizeRequest(ErpMrpNettingPolicySaveReqVO reqVO) {
        String code = reqVO.getCode().trim().toUpperCase(Locale.ROOT);
        String name = reqVO.getName().trim();
        List<String> businessTypes = reqVO.getBusinessTypes() == null ? Collections.emptyList()
                : reqVO.getBusinessTypes().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .collect(Collectors.toList());
        List<ErpMrpNettingPolicyLineDO> lines = normalizeLines(reqVO.getLines());
        return new NormalizedPolicyRequest(code, name,
                reqVO.getEnableFlag() == null ? Boolean.TRUE : reqVO.getEnableFlag(),
                reqVO.getDefaultFlag() == null ? Boolean.FALSE : reqVO.getDefaultFlag(),
                businessTypes, lines);
    }

    private List<ErpMrpNettingPolicyLineDO> normalizeLines(List<ErpMrpNettingPolicySaveReqVO.Line> reqLines) {
        Map<String, ErpMrpNettingPolicySaveReqVO.Line> reqLineMap = new LinkedHashMap<>();
        for (ErpMrpNettingPolicySaveReqVO.Line line : reqLines) {
            reqLineMap.put(line.getComponentCode(), line);
        }
        List<ErpMrpNettingPolicyLineDO> lines = new ArrayList<>();
        for (ErpMrpNettingComponentEnum component : ErpMrpNettingComponentEnum.configurableComponents()) {
            ErpMrpNettingPolicySaveReqVO.Line reqLine = reqLineMap.get(component.getCode());
            lines.add(ErpMrpNettingPolicyLineDO.builder()
                    .componentCode(component.getCode())
                    .componentRole(component.getRole())
                    .enableFlag(reqLine == null || reqLine.getEnableFlag() == null
                            ? component.isDefaultEnabled() : reqLine.getEnableFlag())
                    .sequenceNo(reqLine == null || reqLine.getSequenceNo() == null
                            ? component.getDefaultSequence() : reqLine.getSequenceNo())
                    .build());
        }
        return lines;
    }

    private ErpMrpNettingPolicyDO validatePolicyExists(Long id) {
        ErpMrpNettingPolicyDO policy = erpMrpNettingPolicyMapper.selectById(id);
        if (policy == null) {
            throw exception(MRP_NETTING_POLICY_NOT_EXISTS);
        }
        return policy;
    }

    private record NormalizedPolicyRequest(String code, String name, Boolean enableFlag, Boolean defaultFlag,
                                           List<String> businessTypes, List<ErpMrpNettingPolicyLineDO> lines) {
    }
}
