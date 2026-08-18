package cn.weitee.erp.module.bpm.service.approval.generic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Map;

/**
 * 通用业务表访问服务
 *
 * 基于配置驱动对任意业务表执行查询 / 状态更新 / 流程实例ID回写。
 * 表名与列名经 GenericApprovalConfig 白名单校验，值一律参数绑定，防止 SQL 注入。
 */
@Service
@Slf4j
public class GenericBizService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 按主键查询业务行
     *
     * @return 行数据（列名小写），不存在返回 null
     */
    public Map<String, Object> selectRow(GenericApprovalConfig config, Long bizId) {
        if (config == null || bizId == null) {
            return null;
        }
        String sql = "SELECT * FROM `" + config.getBizTable() + "` WHERE `" + config.getIdColumn() + "` = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForMap(sql, bizId);
        } catch (Exception e) {
            log.warn("[selectRow] 通用查询业务行失败，table={}, id={}", config.getBizTable(), bizId, e);
            return null;
        }
    }

    /**
     * 更新业务状态
     *
     * @return 受影响行数
     */
    public int updateStatus(GenericApprovalConfig config, Long bizId, Integer status) {
        String sql = "UPDATE `" + config.getBizTable() + "` SET `" + config.getStatusColumn()
                + "` = ? WHERE `" + config.getIdColumn() + "` = ?";
        return jdbcTemplate.update(sql, status, bizId);
    }

    /**
     * 回写流程实例 ID（配置了 processInstanceColumn 时）
     */
    public int updateProcessInstanceId(GenericApprovalConfig config, Long bizId, String processInstanceId) {
        if (config.getProcessInstanceColumn() == null) {
            return 0;
        }
        String sql = "UPDATE `" + config.getBizTable() + "` SET `" + config.getProcessInstanceColumn()
                + "` = ? WHERE `" + config.getIdColumn() + "` = ?";
        return jdbcTemplate.update(sql, processInstanceId, bizId);
    }

    /**
     * 读取业务行字段（用于构建审批上下文）
     *
     * @return 字段名 → 值；行不存在返回空 Map
     */
    public Map<String, Object> readContextFields(GenericApprovalConfig config, Long bizId) {
        Map<String, Object> row = selectRow(config, bizId);
        if (row == null) {
            return Collections.emptyMap();
        }
        return row;
    }

}
