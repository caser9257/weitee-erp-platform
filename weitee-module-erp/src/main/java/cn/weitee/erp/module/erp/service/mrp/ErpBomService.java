package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;

import java.util.Collection;
import java.util.List;

public interface ErpBomService {

    /**
     * 更新制造 BOM 状态。
     * 仅允许生命周期审批回调调用（BomDisableResultHandler），不再暴露直接启停接口；
     * 结构维护唯一来源为研发 BOM 审批发布（publishRdBom）。
     */
    void updateBomStatus(Long id, Integer status);

    /**
     * 清空制造 BOM 停用审批在途流程实例：审批通过（落 DISABLE）/驳回/撤回后调用
     */
    void clearDisableApprovalProcess(Long id);

    ErpBomDO getBom(Long id);

    PageResult<ErpBomDO> getBomPage(ErpBomPageReqVO pageReqVO);

    List<ErpBomItemDO> getBomItemList(Long bomId);

    List<ErpBomDO> getEffectiveBomList(Collection<Long> productIds);

    List<ErpBomItemDO> getBomItemListByBomIds(Collection<Long> bomIds);

    List<ErpBomItemSubstituteDO> getBomItemSubstituteList(Collection<Long> bomItemIds);

    ErpBomDO getEffectiveBom(Long productId);

}
