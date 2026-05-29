package cn.iocoder.yudao.module.erp.dal.mysql.project;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectRoleTaskDO;
import cn.iocoder.yudao.module.erp.enums.ErpProjectRoleTaskStatusConstants;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProjectRoleTaskMapper extends BaseMapperX<ErpProjectRoleTaskDO> {

    default ErpProjectRoleTaskDO selectTodoTask(Long projectId, String roleCode, String taskType) {
        return selectOne(new LambdaQueryWrapperX<ErpProjectRoleTaskDO>()
                .eq(ErpProjectRoleTaskDO::getProjectId, projectId)
                .eq(ErpProjectRoleTaskDO::getRoleCode, roleCode)
                .eq(ErpProjectRoleTaskDO::getTaskType, taskType)
                .eq(ErpProjectRoleTaskDO::getTaskStatus, ErpProjectRoleTaskStatusConstants.TODO)
                .last("LIMIT 1"));
    }

    default List<ErpProjectRoleTaskDO> selectTodoListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<ErpProjectRoleTaskDO>()
                .eq(ErpProjectRoleTaskDO::getProjectId, projectId)
                .eq(ErpProjectRoleTaskDO::getTaskStatus, ErpProjectRoleTaskStatusConstants.TODO)
                .orderByDesc(ErpProjectRoleTaskDO::getId));
    }

}
