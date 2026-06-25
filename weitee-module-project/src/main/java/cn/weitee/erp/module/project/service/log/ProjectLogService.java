package cn.weitee.erp.module.project.service.log;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.log.ProjectLogCreateReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.log.ProjectLogPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.log.ProjectLogRespVO;
import jakarta.validation.Valid;

public interface ProjectLogService {

    /**
     * 创建操作日志
     */
    Long createLog(@Valid ProjectLogCreateReqVO reqVO);

    /**
     * 获得操作日志分页
     */
    PageResult<ProjectLogRespVO> getLogPage(ProjectLogPageReqVO reqVO);

}
