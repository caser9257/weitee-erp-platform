package cn.weitee.erp.module.project.service.flow.impl;

import cn.weitee.erp.module.project.controller.admin.vo.flow.ProjectFlowSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.flow.ProjectFlowDO;
import cn.weitee.erp.module.project.dal.dataobject.flow.ProjectFlowItemDO;
import cn.weitee.erp.module.project.dal.mysql.flow.ProjectFlowItemMapper;
import cn.weitee.erp.module.project.dal.mysql.flow.ProjectFlowMapper;
import cn.weitee.erp.module.project.service.flow.ProjectFlowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.project.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProjectFlowServiceImpl implements ProjectFlowService {

    @Resource
    private ProjectFlowMapper projectFlowMapper;
    @Resource
    private ProjectFlowItemMapper projectFlowItemMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFlow(ProjectFlowSaveReqVO saveReqVO) {
        // 1. 查找或创建工作流
        LambdaQueryWrapper<ProjectFlowDO> flowQuery = new LambdaQueryWrapper<ProjectFlowDO>()
                .eq(ProjectFlowDO::getProjectId, saveReqVO.getProjectId());
        ProjectFlowDO flow = projectFlowMapper.selectOne(flowQuery);
        if (flow == null) {
            flow = ProjectFlowDO.builder()
                    .projectId(saveReqVO.getProjectId())
                    .name(saveReqVO.getName() != null ? saveReqVO.getName() : "Default")
                    .build();
            projectFlowMapper.insert(flow);
        } else if (saveReqVO.getName() != null) {
            flow.setName(saveReqVO.getName());
            projectFlowMapper.updateById(flow);
        }

        // 2. 保存状态节点
        if (saveReqVO.getItems() != null) {
            boolean hasStart = false;
            boolean hasEnd = false;

            for (ProjectFlowSaveReqVO.FlowItemVO itemVO : saveReqVO.getItems()) {
                if ("start".equals(itemVO.getStatus())) hasStart = true;
                if ("end".equals(itemVO.getStatus())) hasEnd = true;

                ProjectFlowItemDO item;
                if (itemVO.getId() != null && itemVO.getId() > 0) {
                    item = projectFlowItemMapper.selectById(itemVO.getId());
                    if (item == null) {
                        throw exception(FLOW_ITEM_NOT_FOUND);
                    }
                } else {
                    item = new ProjectFlowItemDO();
                    item.setFlowId(flow.getId());
                    item.setProjectId(saveReqVO.getProjectId());
                }

                item.setName(itemVO.getName());
                item.setStatus(itemVO.getStatus());
                item.setColor(itemVO.getColor());
                item.setSort(itemVO.getSort());
                item.setUserType(itemVO.getUserType() != null ? itemVO.getUserType() : "add");
                item.setUserLimit(itemVO.getUserLimit() != null ? itemVO.getUserLimit() : false);
                item.setColumnId(itemVO.getColumnId());

                try {
                    item.setTurns(objectMapper.writeValueAsString(itemVO.getTurns() != null ? itemVO.getTurns() : List.of()));
                    item.setUserIds(objectMapper.writeValueAsString(itemVO.getUserIds() != null ? itemVO.getUserIds() : List.of()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                if (itemVO.getId() != null && itemVO.getId() > 0) {
                    projectFlowItemMapper.updateById(item);
                } else {
                    projectFlowItemMapper.insert(item);
                }
            }

            if (!hasStart) {
                throw exception(FLOW_MUST_HAVE_START);
            }
            if (!hasEnd) {
                throw exception(FLOW_MUST_HAVE_END);
            }
        }

        return flow.getId();
    }

    @Override
    public ProjectFlowDO getFlow(Long projectId) {
        LambdaQueryWrapper<ProjectFlowDO> query = new LambdaQueryWrapper<ProjectFlowDO>()
                .eq(ProjectFlowDO::getProjectId, projectId);
        return projectFlowMapper.selectOne(query);
    }

    @Override
    public List<ProjectFlowItemDO> getFlowItems(Long flowId) {
        LambdaQueryWrapper<ProjectFlowItemDO> query = new LambdaQueryWrapper<ProjectFlowItemDO>()
                .eq(ProjectFlowItemDO::getFlowId, flowId)
                .orderByAsc(ProjectFlowItemDO::getSort);
        return projectFlowItemMapper.selectList(query);
    }
}
