package cn.weitee.erp.module.erp.service.project;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectLifecycleTimelineDO;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectLifecycleTimelineMapper;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(rollbackFor = Exception.class)
    public void updateLifecycleStage(Long projectId, String newStage, String reason,
                                      Long operatorId, String operatorName) {
        // 1. 获取项目信息
        ErpProjectDO project = erpProjectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        String oldStage = project.getLifecycleStage();

        // 2. 前置条件校验
        validatePrecondition(project, newStage);

        // 3. 更新项目生命周期阶段
        ErpProjectDO updateProject = new ErpProjectDO();
        updateProject.setId(projectId);
        updateProject.setLifecycleStage(newStage);
        erpProjectMapper.updateById(updateProject);

        // 4. 记录时间线
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

    /**
     * 阶段流转前置条件校验
     *
     * 前置条件矩阵：
     * | 目标阶段 | 前置条件 |
     * |---------|---------|
     * | ORDER | contractExecStatus = SIGNED |
     * | PAYMENT | contractExecStatus = COMPLETED |
     * | SHIPMENT | receiptExecStatus = COMPLETED |
     * | INVOICE | shipmentExecStatus = COMPLETED |
     * | CLOSED | 所有子状态 = COMPLETED |
     * | BLOCKED / CONTRACT / RETURNED / DISPUTED | 无前置条件 |
     */
    private void validatePrecondition(ErpProjectDO project, String newStage) {
        switch (newStage) {
            case "ORDER":
                // 合同必须已签署
                if (!"SIGNED".equals(project.getContractExecStatus())
                        && !"COMPLETED".equals(project.getContractExecStatus())) {
                    throw new RuntimeException("流转到订单阶段失败：合同尚未签署");
                }
                break;

            case "PAYMENT":
                // 合同必须已完成（订单已审批）
                if (!isCompleted(project.getContractExecStatus())) {
                    throw new RuntimeException("流转到收款阶段失败：订单尚未审批通过");
                }
                break;

            case "SHIPMENT":
            case "OUTBOUND":
                // 收款必须已完成
                if (!isCompleted(project.getReceiptExecStatus())) {
                    throw new RuntimeException("流转到发货阶段失败：收款尚未完成");
                }
                break;

            case "INVOICE":
                // 出库/发货必须已完成
                if (!isCompleted(project.getShipmentExecStatus())) {
                    throw new RuntimeException("流转到开票阶段失败：出库尚未完成");
                }
                break;

            case "CLOSED":
                // 所有子状态必须完成
                if (!isCompleted(project.getContractExecStatus())
                        || !isCompleted(project.getReceiptExecStatus())
                        || !isCompleted(project.getShipmentExecStatus())
                        || !isCompleted(project.getInvoiceExecStatus())) {
                    throw new RuntimeException("流转到关闭阶段失败：存在未完成的子状态");
                }
                break;

            // BLOCKED / CONTRACT / RETURNED / DISPUTED 无前置条件
            default:
                break;
        }
    }

    @Override
    public List<ErpProjectLifecycleTimelineDO> getTimeline(Long projectId) {
        return erpProjectLifecycleTimelineMapper.selectList(
                ErpProjectLifecycleTimelineDO::getProjectId, projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshProjectStatus(Long projectId) {
        // 1. 获取项目信息
        ErpProjectDO project = erpProjectMapper.selectById(projectId);
        if (project == null) {
            log.warn("[refreshProjectStatus] 项目[{}]不存在", projectId);
            return;
        }

        String oldStage = project.getLifecycleStage();

        // 2. 根据子状态计算目标阶段（优先级从高到低）
        String newStage = calculateStage(project);

        // 3. 仅当阶段发生变化时才更新
        if (!newStage.equals(oldStage)) {
            ErpProjectDO updateProject = new ErpProjectDO();
            updateProject.setId(projectId);
            updateProject.setLifecycleStage(newStage);

            // 乐观锁：仅当阶段仍为旧值时才更新，防止并发覆盖
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ErpProjectDO> updateWrapper =
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(ErpProjectDO::getId, projectId)
                    .eq(ErpProjectDO::getLifecycleStage, oldStage)
                    .set(ErpProjectDO::getLifecycleStage, newStage);
            int updateCount = erpProjectMapper.update(updateWrapper);
            if (updateCount == 0) {
                log.warn("[refreshProjectStatus] 并发更新冲突，项目[{}]阶段已变化，跳过本次刷新", projectId);
                return;
            }

            // 记录时间线
            ErpProjectLifecycleTimelineDO timeline = ErpProjectLifecycleTimelineDO.builder()
                    .projectId(projectId)
                    .stageCode(newStage)
                    .stageName(getStageName(newStage))
                    .happenTime(LocalDateTime.now())
                    .remark("系统自动刷新")
                    .build();
            erpProjectLifecycleTimelineMapper.insert(timeline);

            log.info("[refreshProjectStatus] 项目[{}]阶段从[{}]自动更新为[{}]", projectId, oldStage, newStage);
        }
    }

    /**
     * 根据子状态计算生命周期阶段
     *
     * 优先级规则（从高到低）：
     * 1. BLOCKED — 当前有阻塞项
     * 2. CLOSED — 所有子状态均为"完成"
     * 3. INVOICE — 出库完成，开票未完成
     * 4. SHIPMENT — 收款完成，出库/发货未完成
     * 5. PAYMENT — 订单已审批，收款未完成
     * 6. ORDER — 合同已签署，订单未完成
     * 7. CONTRACT — 合同未签署（默认初始阶段）
     */
    private String calculateStage(ErpProjectDO project) {
        // 1. 有阻塞项 → BLOCKED
        if (project.getCurrentBlocker() != null && !project.getCurrentBlocker().isEmpty()) {
            return "BLOCKED";
        }

        String contractStatus = project.getContractExecStatus();
        String receiptStatus = project.getReceiptExecStatus();
        String shipmentStatus = project.getShipmentExecStatus();
        String invoiceStatus = project.getInvoiceExecStatus();

        // 2. 所有子状态完成 → CLOSED
        if (isCompleted(contractStatus) && isCompleted(receiptStatus)
                && isCompleted(shipmentStatus) && isCompleted(invoiceStatus)) {
            return "CLOSED";
        }

        // 3. 出库完成，开票未完成 → INVOICE
        if (isCompleted(shipmentStatus) && !isCompleted(invoiceStatus)) {
            return "INVOICE";
        }

        // 4. 收款完成，出库/发货未完成 → SHIPMENT
        if (isCompleted(receiptStatus) && !isCompleted(shipmentStatus)) {
            return "SHIPMENT";
        }

        // 5. 订单已审批，收款未完成 → PAYMENT
        if (isCompleted(contractStatus) && !isCompleted(receiptStatus)) {
            return "PAYMENT";
        }

        // 6. 合同已签署 → ORDER
        if ("SIGNED".equals(contractStatus) || "COMPLETED".equals(contractStatus)) {
            return "ORDER";
        }

        // 7. 默认：合同阶段
        return "CONTRACT";
    }

    /**
     * 判断子状态是否为"完成"
     */
    private boolean isCompleted(String status) {
        return "COMPLETED".equals(status) || "DONE".equals(status) || "FINISHED".equals(status);
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
