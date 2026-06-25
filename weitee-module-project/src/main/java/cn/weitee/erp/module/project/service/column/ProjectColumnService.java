package cn.weitee.erp.module.project.service.column;

import cn.weitee.erp.module.project.controller.admin.vo.column.ProjectColumnSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.column.ProjectColumnDO;
import jakarta.validation.Valid;
import java.util.List;

public interface ProjectColumnService {
    Long createColumn(@Valid ProjectColumnSaveReqVO createReqVO);
    void updateColumn(@Valid ProjectColumnSaveReqVO updateReqVO);
    void deleteColumn(Long id);
    List<ProjectColumnDO> getColumnList(Long projectId);
    void sortColumns(Long projectId, List<Long> columnIds);
}
