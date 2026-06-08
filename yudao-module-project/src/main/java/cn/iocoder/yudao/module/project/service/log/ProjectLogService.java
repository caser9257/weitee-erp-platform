package cn.iocoder.yudao.module.project.service.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogRespVO;
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
