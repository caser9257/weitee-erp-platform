package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
}
