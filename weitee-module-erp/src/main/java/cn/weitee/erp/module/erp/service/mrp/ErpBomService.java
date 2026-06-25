package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpBomService {

    Long createBom(@Valid ErpBomSaveReqVO createReqVO);

    void updateBom(@Valid ErpBomSaveReqVO updateReqVO);

    void updateBomStatus(Long id, Integer status);

    void deleteBom(Long id);

    ErpBomDO getBom(Long id);

    PageResult<ErpBomDO> getBomPage(ErpBomPageReqVO pageReqVO);

    List<ErpBomItemDO> getBomItemList(Long bomId);

    List<ErpBomDO> getEffectiveBomList(Collection<Long> productIds);

    List<ErpBomItemDO> getBomItemListByBomIds(Collection<Long> bomIds);

    List<ErpBomItemSubstituteDO> getBomItemSubstituteList(Collection<Long> bomItemIds);

    ErpBomDO getEffectiveBom(Long productId);

}
