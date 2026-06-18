package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ErpFinanceAssetDepreciationMapper extends BaseMapperX<ErpFinanceAssetDepreciationDO> {

    default PageResult<ErpFinanceAssetDepreciationDO> selectPage(ErpFinanceAssetDepreciationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceAssetDepreciationDO>()
                .eqIfPresent(ErpFinanceAssetDepreciationDO::getAssetId, reqVO.getAssetId())
                .eqIfPresent(ErpFinanceAssetDepreciationDO::getPeriod, reqVO.getPeriod())
                .orderByDesc(ErpFinanceAssetDepreciationDO::getId));
    }

    default List<ErpFinanceAssetDepreciationDO> selectListByPeriod(String period) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceAssetDepreciationDO>()
                .eq(ErpFinanceAssetDepreciationDO::getPeriod, period));
    }

    default List<ErpFinanceAssetDepreciationDO> selectListByAssetId(Long assetId) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceAssetDepreciationDO>()
                .eq(ErpFinanceAssetDepreciationDO::getAssetId, assetId)
                .orderByDesc(ErpFinanceAssetDepreciationDO::getId));
    }

    /**
     * 批量查询指定资产 ID 和期间的折旧记录
     * 用于替代循环内的逐条查询，避免 N+1 问题
     */
    default Set<Long> selectExistingAssetIdsByPeriod(Collection<Long> assetIds, String period) {
        if (CollUtil.isEmpty(assetIds)) {
            return java.util.Collections.emptySet();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinanceAssetDepreciationDO>()
                .in(ErpFinanceAssetDepreciationDO::getAssetId, assetIds)
                .eq(ErpFinanceAssetDepreciationDO::getPeriod, period))
                .stream()
                .map(ErpFinanceAssetDepreciationDO::getAssetId)
                .collect(Collectors.toSet());
    }
}
