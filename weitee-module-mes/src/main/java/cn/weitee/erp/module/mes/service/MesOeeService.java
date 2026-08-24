package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.module.mes.controller.admin.vo.oee.MesOeeSummaryRespVO;

import java.time.LocalDate;
import java.util.List;

public interface MesOeeService {

    List<MesOeeSummaryRespVO> getOeeSummary(Long workCenterId, LocalDate startDate, LocalDate endDate);

}
