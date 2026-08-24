package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

public interface ErpWorkCenterService {

    Long create(@Valid ErpWorkCenterSaveReqVO reqVO);

    void update(@Valid ErpWorkCenterSaveReqVO reqVO);

    void delete(Long id);

    ErpWorkCenterDO get(Long id);

    PageResult<ErpWorkCenterDO> getPage(ErpWorkCenterPageReqVO reqVO);

    List<ErpWorkCenterDO> getEnabledList();

    List<ErpWorkCenterDO> getWorkCenterList(Collection<Long> ids);

}
