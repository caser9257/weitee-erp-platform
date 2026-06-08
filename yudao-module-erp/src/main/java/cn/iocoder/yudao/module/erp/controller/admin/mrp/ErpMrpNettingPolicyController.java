package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicyPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicyRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPolicyBindingDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpNettingComponentEnum;
import cn.iocoder.yudao.module.erp.service.mrp.ErpMrpNettingPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 净需求策略")
@RestController
@RequestMapping("/erp/mrp-netting-policy")
@Validated
public class ErpMrpNettingPolicyController {

    @Resource
    private ErpMrpNettingPolicyService nettingPolicyService;

    @PostMapping("/create")
    @Operation(summary = "创建净需求策略")
    @PreAuthorize("@ss.hasPermission('erp:mrp-netting-policy:create')")
    public CommonResult<Long> createPolicy(@Valid @RequestBody ErpMrpNettingPolicySaveReqVO createReqVO) {
        return success(nettingPolicyService.createPolicy(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新净需求策略")
    @PreAuthorize("@ss.hasPermission('erp:mrp-netting-policy:update')")
    public CommonResult<Boolean> updatePolicy(@Valid @RequestBody ErpMrpNettingPolicySaveReqVO updateReqVO) {
        nettingPolicyService.updatePolicy(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除净需求策略")
    @PreAuthorize("@ss.hasPermission('erp:mrp-netting-policy:delete')")
    public CommonResult<Boolean> deletePolicy(@RequestParam("id") Long id) {
        nettingPolicyService.deletePolicy(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得净需求策略")
    @PreAuthorize("@ss.hasPermission('erp:mrp-netting-policy:query')")
    public CommonResult<ErpMrpNettingPolicyRespVO> getPolicy(@RequestParam("id") Long id) {
        return success(buildRespVO(nettingPolicyService.getPolicy(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得净需求策略分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-netting-policy:query')")
    public CommonResult<PageResult<ErpMrpNettingPolicyRespVO>> getPolicyPage(@Valid ErpMrpNettingPolicyPageReqVO pageReqVO) {
        PageResult<ErpMrpNettingPolicyDO> pageResult = nettingPolicyService.getPolicyPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<ErpMrpNettingPolicyRespVO> buildRespVOList(List<ErpMrpNettingPolicyDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> policyIds = convertSet(list, ErpMrpNettingPolicyDO::getId);
        Map<Long, List<ErpMrpNettingPolicyLineDO>> lineMap = convertMultiMap(
                nettingPolicyService.getPolicyLineList(policyIds), ErpMrpNettingPolicyLineDO::getPolicyId);
        Map<Long, List<ErpMrpPolicyBindingDO>> bindingMap = convertMultiMap(
                nettingPolicyService.getPolicyBindingList(policyIds), ErpMrpPolicyBindingDO::getPolicyId);
        return BeanUtils.toBean(list, ErpMrpNettingPolicyRespVO.class, item -> {
            List<ErpMrpPolicyBindingDO> bindings = bindingMap.getOrDefault(item.getId(), Collections.emptyList());
            item.setBusinessTypes(bindings.stream().map(ErpMrpPolicyBindingDO::getBusinessType).distinct().toList());
            List<ErpMrpNettingPolicyLineDO> lines = lineMap.getOrDefault(item.getId(), Collections.emptyList());
            item.setLines(BeanUtils.toBean(lines, ErpMrpNettingPolicyRespVO.Line.class, line -> {
                ErpMrpNettingComponentEnum component = ErpMrpNettingComponentEnum.valueOfCode(line.getComponentCode());
                if (component != null) {
                    line.setComponentName(component.getLabel());
                    line.setComponentRole(component.getRole());
                }
            }).stream()
                    .sorted((left, right) -> Integer.compare(
                            left.getSequenceNo() == null ? Integer.MAX_VALUE : left.getSequenceNo(),
                            right.getSequenceNo() == null ? Integer.MAX_VALUE : right.getSequenceNo()))
                    .collect(Collectors.toList()));
        });
    }

    private ErpMrpNettingPolicyRespVO buildRespVO(ErpMrpNettingPolicyDO policy) {
        if (policy == null) {
            return null;
        }
        return buildRespVOList(List.of(policy)).getFirst();
    }
}
