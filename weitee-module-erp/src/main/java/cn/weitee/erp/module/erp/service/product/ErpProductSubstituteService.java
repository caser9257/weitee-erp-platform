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

    /**
     * 批量更新物料的替代料列表（diff 替换语义）
     * <p>
     * 根据传入的完整列表，自动新增、更新、删除替代料行。
     * 已存在的行根据 id 更新，新行插入，不再传入的行被删除。
     *
     * @param productId 主物料编号
     * @param list      替代料完整列表
     */
    void batchUpdateSubstitutes(Long productId, List<ErpProductSubstituteSaveReqVO> list);

}
