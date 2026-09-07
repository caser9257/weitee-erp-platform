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

    /**
     * 工艺路线精简列表（不过滤状态）：供详情展示做 defaultRouteId → 名称映射，
     * 停用路线也需能回显名称，故不按启用态过滤
     */
    List<ErpProcessRouteDO> getSimpleList();
}
