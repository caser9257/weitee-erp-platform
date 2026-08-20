package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchSaveReqVO;

import jakarta.validation.Valid;
import java.util.List;

public interface MesTaskDispatchService {

    PageResult<MesTaskDispatchRespVO> getWorkbenchPage(MesTaskDispatchPageReqVO pageReqVO);

    List<MesTaskDispatchRespVO> getHistory(Long taskId);

    Long assign(@Valid MesTaskDispatchSaveReqVO reqVO);

    Long reassign(@Valid MesTaskDispatchSaveReqVO reqVO);

    void revoke(Long taskId);
}
