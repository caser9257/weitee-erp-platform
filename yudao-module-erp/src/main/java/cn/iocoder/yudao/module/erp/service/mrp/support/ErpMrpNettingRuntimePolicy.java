package cn.iocoder.yudao.module.erp.service.mrp.support;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpNettingComponentEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpNettingRuntimePolicy {

    private Long policyId;

    private String code;

    private String name;

    private Integer version;

    private boolean defaultPolicy;

    private List<ErpMrpNettingPolicyLineDO> lines;

    public List<ErpMrpNettingPolicyLineDO> getEnabledLines() {
        return lines.stream()
                .filter(line -> Boolean.TRUE.equals(line.getEnableFlag()))
                .sorted(Comparator.comparing(ErpMrpNettingPolicyLineDO::getSequenceNo,
                        Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    public static ErpMrpNettingRuntimePolicy standard() {
        List<ErpMrpNettingPolicyLineDO> lines = ErpMrpNettingComponentEnum.configurableComponents().stream()
                .map(component -> ErpMrpNettingPolicyLineDO.builder()
                        .componentCode(component.getCode())
                        .componentRole(component.getRole())
                        .enableFlag(component.isDefaultEnabled())
                        .sequenceNo(component.getDefaultSequence())
                        .build())
                .collect(Collectors.toList());
        return ErpMrpNettingRuntimePolicy.builder()
                .code("STANDARD_V1")
                .name("标准净需求策略")
                .version(1)
                .defaultPolicy(true)
                .lines(lines)
                .build();
    }
}
