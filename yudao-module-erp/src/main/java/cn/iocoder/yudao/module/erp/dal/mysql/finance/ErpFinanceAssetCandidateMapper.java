package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpFinanceAssetCandidateMapper extends BaseMapperX<ErpFinanceAssetCandidateDO> {

    default PageResult<ErpFinanceAssetCandidateDO> selectPage(ErpFinanceAssetCandidatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eqIfPresent(ErpFinanceAssetCandidateDO::getSourceType, reqVO.getSourceType())
                .eqIfPresent(ErpFinanceAssetCandidateDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinanceAssetCandidateDO::getId));
    }

    default ErpFinanceAssetCandidateDO selectBySource(Integer sourceType, Long sourceBizId, Long sourceItemId) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eq(ErpFinanceAssetCandidateDO::getSourceType, sourceType)
                .eq(ErpFinanceAssetCandidateDO::getSourceBizId, sourceBizId)
                .eqIfPresent(ErpFinanceAssetCandidateDO::getSourceItemId, sourceItemId)
                .orderByDesc(ErpFinanceAssetCandidateDO::getId)
                .last("LIMIT 1"));
    }
}
