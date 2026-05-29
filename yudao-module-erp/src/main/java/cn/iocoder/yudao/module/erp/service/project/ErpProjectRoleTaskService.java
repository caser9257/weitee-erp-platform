package cn.iocoder.yudao.module.erp.service.project;

import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectRoleTaskDO;

import java.time.LocalDate;
import java.util.List;

/**
 * ERP 项目责任任务 Service
 */
public interface ErpProjectRoleTaskService {

    void createOrRefreshPcTask(Long projectId, Long saleOrderId, LocalDate dueDate);

    void createOrRefreshMcTask(Long projectId, Long sourceId, String summary);

    void completePcTask(Long projectId, String remark);

    void completeMcTask(Long projectId, String remark);

    List<ErpProjectRoleTaskDO> getTodoTasksByProjectId(Long projectId);

}
