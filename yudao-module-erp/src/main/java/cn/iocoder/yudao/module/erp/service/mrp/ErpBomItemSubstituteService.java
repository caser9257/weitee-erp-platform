package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstituteRespVO;

public interface ErpBomItemSubstituteService {

    PageResult<ErpBomItemSubstituteRespVO> getSubstitutePage(ErpBomItemSubstitutePageReqVO pageReqVO);

}
