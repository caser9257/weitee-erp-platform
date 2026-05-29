package cn.iocoder.yudao.module.erp.service.rd;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpRdBomService {

    Long createRdBom(@Valid ErpRdBomSaveReqVO createReqVO);

    void updateRdBom(@Valid ErpRdBomSaveReqVO updateReqVO);

    void deleteRdBom(Long id);

    ErpRdBomDO getRdBom(Long id);

    PageResult<ErpRdBomDO> getRdBomPage(ErpRdBomPageReqVO pageReqVO);

    List<ErpRdBomItemDO> getRdBomItemList(Long bomId);

    List<ErpRdBomItemSubstituteDO> getRdBomItemSubstituteList(Collection<Long> bomItemIds);

    void publishRdBom(Long id);

}
