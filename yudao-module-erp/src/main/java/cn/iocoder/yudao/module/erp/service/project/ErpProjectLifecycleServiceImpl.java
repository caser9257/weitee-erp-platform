package cn.iocoder.yudao.module.erp.service.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectLifecycleTimelineDO;
import cn.iocoder.yudao.module.erp.dal.mysql.project.ErpProjectLifecycleTimelineMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.project.ErpProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目生命周期服务实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpProjectLifecycleServiceImpl implements ErpProjectLifecycleService {

    @Resource
    private ErpProjectMapper erpProjectMapper;

    @Resource
    private ErpProjectLifecycleTimelineMapper erpProjectLifecycleTimelineMapper;

    @Override
    public void updateLifecycleStage(Long projectId, String newStage, String reason,
                                      Long operatorId, String operatorName) {
        // 1. 获取项目信息
        ErpProjectDO project = erpProjectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        String oldStage = project.getLifecycleStage();

        // 2. 更新项目生命周期阶段
        ErpProjectDO updateProject = new ErpProjectDO();
        updateProject.setId(projectId);
        updateProject.setLifecycleStage(newStage);
        erpProjectMapper.updateById(updateProject);

        // 3. 记录时间线
        ErpProjectLifecycleTimelineDO timeline = ErpProjectLifecycleTimelineDO.builder()
                .projectId(projectId)
                .stageCode(newStage)
                .stageName(getStageName(newStage))
                .happenTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .remark(reason)
                .build();
        erpProjectLifecycleTimelineMapper.insert(timeline);

        log.info("项目[{}]生命周期阶段从[{}]更新为[{}]，原因：{}", projectId, oldStage, newStage, reason);
    }

    @Override
    public List<ErpProjectLifecycleTimelineDO> getTimeline(Long projectId) {
        return erpProjectLifecycleTimelineMapper.selectList(
                ErpProjectLifecycleTimelineDO::getProjectId, projectId);
    }

    @Override
    public void refreshProjectStatus(Long projectId) {
        // TODO: 根据合同、订单、收款、出库、开票等子状态重新计算生命周期
        // 这个方法需要集成多个服务来获取各子状态
        log.info("刷新项目[{}]聚合状态", projectId);
    }

    /**
     * 获取阶段名称
     */
    private String getStageName(String stageCode) {
        switch (stageCode) {
            case "CONTRACT":
                return "合同";
            case "ORDER":
                return "订单";
            case "PAYMENT":
                return "收款";
            case "SHIPMENT":
                return "发货";
            case "OUTBOUND":
                return "出库";
            case "INVOICE":
                return "开票";
            case "CLOSED":
                return "关闭";
            case "CONTRACT_REJECTED":
                return "合同驳回";
            case "BLOCKED":
                return "阻塞";
            case "RETURNED":
                return "退货";
            case "DISPUTED":
                return "争议";
            default:
                return stageCode;
        }
    }

}
