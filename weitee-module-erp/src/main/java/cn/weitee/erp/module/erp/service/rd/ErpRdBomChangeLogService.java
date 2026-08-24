package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomChangeLogRespVO;

import java.util.List;

public interface ErpRdBomChangeLogService {

    void logChange(Long bomId, String changeType, String changeDetail);

    List<ErpRdBomChangeLogRespVO> getChangeLogList(Long bomId);

}
