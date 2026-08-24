package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentSaveReqVO;

import java.util.List;

public interface MesSopService {

    Long createSop(MesSopDocumentSaveReqVO reqVO);

    void updateSop(MesSopDocumentSaveReqVO reqVO);

    void updateStatus(Long id, Integer status);

    void deleteSop(Long id);

    MesSopDocumentRespVO getSop(Long id);

    PageResult<MesSopDocumentRespVO> getSopPage(MesSopDocumentPageReqVO pageReqVO);

    /**
     * 按工序查询已发布 SOP 列表（现场查看/后续 PDA 复用）。
     */
    List<MesSopDocumentRespVO> getPublishedSopsByStepId(Long routeStepId);

}
