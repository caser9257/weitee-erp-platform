package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDevicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDeviceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpDeviceDO;
import jakarta.validation.Valid;

public interface ErpDeviceService {

    Long create(@Valid ErpDeviceSaveReqVO reqVO);

    void update(@Valid ErpDeviceSaveReqVO reqVO);

    void delete(Long id);

    ErpDeviceDO get(Long id);

    PageResult<ErpDeviceDO> getPage(ErpDevicePageReqVO reqVO);

}
