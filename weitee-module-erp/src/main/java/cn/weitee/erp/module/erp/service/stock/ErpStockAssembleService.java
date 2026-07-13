package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssemblePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssembleSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleItemDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpStockAssembleService {

    Long createStockAssemble(@Valid ErpStockAssembleSaveReqVO reqVO);

    void updateStockAssemble(@Valid ErpStockAssembleSaveReqVO reqVO);

    void updateStockAssembleStatus(Long id, Integer status);

    void deleteStockAssemble(List<Long> ids);

    ErpStockAssembleDO getStockAssemble(Long id);

    PageResult<ErpStockAssembleDO> getStockAssemblePage(ErpStockAssemblePageReqVO reqVO);

    List<ErpStockAssembleItemDO> getStockAssembleItemListByAssembleId(Long assembleId);

    List<ErpStockAssembleItemDO> getStockAssembleItemListByAssembleIds(Collection<Long> assembleIds);
}
