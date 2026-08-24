package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionReportReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionRespVO;

public interface MesTaskExecutionService {

    MesTaskExecutionRespVO getTask(String taskNo);

    void start(String taskNo);

    void pause(String taskNo);

    void resume(String taskNo);

    Long report(MesTaskExecutionReportReqVO reqVO);

    void finish(String taskNo);
}
