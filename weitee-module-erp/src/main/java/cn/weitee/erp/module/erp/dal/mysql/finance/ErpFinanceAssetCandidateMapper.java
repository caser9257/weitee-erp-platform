package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpFinanceAssetCandidateMapper extends BaseMapperX<ErpFinanceAssetCandidateDO> {

    default PageResult<ErpFinanceAssetCandidateDO> selectPage(ErpFinanceAssetCandidatePageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO));
    }

    default PageResult<ErpFinanceAssetCandidateDO> selectPageByDeptIds(ErpFinanceAssetCandidatePageReqVO reqVO,
                                                                        Collection<Long> deptIds) {
        return selectPage(reqVO, buildPageQuery(reqVO).in(ErpFinanceAssetCandidateDO::getDeptId, deptIds));
    }

    private LambdaQueryWrapperX<ErpFinanceAssetCandidateDO> buildPageQuery(ErpFinanceAssetCandidatePageReqVO reqVO) {
        return new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eqIfPresent(ErpFinanceAssetCandidateDO::getSourceType, reqVO.getSourceType())
                .eqIfPresent(ErpFinanceAssetCandidateDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinanceAssetCandidateDO::getId);
    }

    default ErpFinanceAssetCandidateDO selectBySource(Integer sourceType, Long sourceBizId, Long sourceItemId) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eq(ErpFinanceAssetCandidateDO::getSourceType, sourceType)
                .eq(ErpFinanceAssetCandidateDO::getSourceBizId, sourceBizId)
                .eqIfPresent(ErpFinanceAssetCandidateDO::getSourceItemId, sourceItemId)
                .orderByDesc(ErpFinanceAssetCandidateDO::getId)
                .last("LIMIT 1"));
    }

    default List<ErpFinanceAssetCandidateDO> selectListBySource(Integer sourceType, Long sourceBizId) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eq(ErpFinanceAssetCandidateDO::getSourceType, sourceType)
                .eq(ErpFinanceAssetCandidateDO::getSourceBizId, sourceBizId));
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpFinanceAssetCandidateDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinanceAssetCandidateDO>()
                .eq(ErpFinanceAssetCandidateDO::getId, id)
                .eq(ErpFinanceAssetCandidateDO::getStatus, status));
    }
}
