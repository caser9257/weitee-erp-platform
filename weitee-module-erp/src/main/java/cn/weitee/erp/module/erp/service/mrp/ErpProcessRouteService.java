package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRoutePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRouteSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteStepDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpProcessRouteService {
    Long create(@Valid ErpProcessRouteSaveReqVO reqVO);
    void update(@Valid ErpProcessRouteSaveReqVO reqVO);
    void updateStatus(Long id, Integer status);
    void delete(Long id);
    ErpProcessRouteDO get(Long id);
    PageResult<ErpProcessRouteDO> getPage(ErpProcessRoutePageReqVO reqVO);
    List<ErpProcessRouteStepDO> getStepList(Long routeId);
}
