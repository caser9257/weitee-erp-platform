package cn.iocoder.yudao.module.project.service.column.impl;

import cn.iocoder.yudao.module.project.controller.admin.vo.column.ProjectColumnSaveReqVO;
import cn.iocoder.yudao.module.project.convert.column.ProjectColumnConvert;
import cn.iocoder.yudao.module.project.dal.dataobject.column.ProjectColumnDO;
import cn.iocoder.yudao.module.project.dal.mysql.column.ProjectColumnMapper;
import cn.iocoder.yudao.module.project.service.column.ProjectColumnService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProjectColumnServiceImpl implements ProjectColumnService {

    @Resource
    private ProjectColumnMapper projectColumnMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createColumn(@Valid ProjectColumnSaveReqVO createReqVO) {
        // 校验数量限制
        Long count = projectColumnMapper.selectCount(ProjectColumnDO::getProjectId, createReqVO.getProjectId());
        if (count >= 30) {
            throw exception(COLUMN_MAX_COUNT_ERROR);
        }

        ProjectColumnDO column = ProjectColumnConvert.INSTANCE.convert(createReqVO);
        if (column.getSort() == null) {
            column.setSort(count.intValue());
        }
        projectColumnMapper.insert(column);
        return column.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateColumn(@Valid ProjectColumnSaveReqVO updateReqVO) {
        ProjectColumnDO column = projectColumnMapper.selectById(updateReqVO.getId());
        if (column == null) {
            throw exception(COLUMN_NOT_FOUND);
        }
        ProjectColumnDO updateObj = ProjectColumnConvert.INSTANCE.convert(updateReqVO);
        projectColumnMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteColumn(Long id) {
        ProjectColumnDO column = projectColumnMapper.selectById(id);
        if (column == null) {
            throw exception(COLUMN_NOT_FOUND);
        }
        projectColumnMapper.deleteById(id);
    }

    @Override
    public List<ProjectColumnDO> getColumnList(Long projectId) {
        return projectColumnMapper.selectList(ProjectColumnDO::getProjectId, projectId,
                ProjectColumnDO::getSort, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortColumns(Long projectId, List<Long> columnIds) {
        for (int i = 0; i < columnIds.size(); i++) {
            ProjectColumnDO column = new ProjectColumnDO();
            column.setId(columnIds.get(i));
            column.setSort(i);
            projectColumnMapper.updateById(column);
        }
    }
}
