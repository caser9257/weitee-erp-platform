package cn.weitee.erp.module.erp.service.project;

import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectLifecycleTimelineDO;

import java.util.List;

/**
 * 项目生命周期服务接口
 *
 * @author system
 */
public interface ErpProjectLifecycleService {

    /**
     * 更新项目生命周期阶段
     *
     * @param projectId 项目编号
     * @param newStage 新阶段
     * @param reason 变更原因
     * @param operatorId 操作人编号
     * @param operatorName 操作人姓名
     */
    void updateLifecycleStage(Long projectId, String newStage, String reason, 
                               Long operatorId, String operatorName);

    /**
     * 获取项目生命周期时间线
     *
     * @param projectId 项目编号
     * @return 时间线记录
     */
    List<ErpProjectLifecycleTimelineDO> getTimeline(Long projectId);

    /**
     * 刷新项目聚合状态
     * 根据合同、订单、收款、出库、开票等子状态重新计算生命周期
     *
     * @param projectId 项目编号
     */
    void refreshProjectStatus(Long projectId);

}
