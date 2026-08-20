package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteSaveReqVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpProductSubstituteService {

    List<ErpProductSubstituteRespVO> getListByProductId(Long productId);

    Map<Long, List<ErpProductSubstituteRespVO>> getListMapByProductIds(Collection<Long> productIds);

    Map<Long, Boolean> getHasSubstituteMap(Collection<Long> productIds);

    Long createSubstitute(ErpProductSubstituteSaveReqVO reqVO);

    void deleteSubstitute(Long productId, Long substituteProductId);

}
