package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstituteRespVO;

public interface ErpBomItemSubstituteService {

    PageResult<ErpBomItemSubstituteRespVO> getSubstitutePage(ErpBomItemSubstitutePageReqVO pageReqVO);

}
