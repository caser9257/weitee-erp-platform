package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ErpFinanceAssetMapper extends BaseMapperX<ErpFinanceAssetDO> {

    default PageResult<ErpFinanceAssetDO> selectPage(ErpFinanceAssetPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO));
    }

    default PageResult<ErpFinanceAssetDO> selectPageByDeptIds(ErpFinanceAssetPageReqVO reqVO, Collection<Long> deptIds) {
        return selectPage(reqVO, buildPageQuery(reqVO).in(ErpFinanceAssetDO::getDeptId, deptIds));
    }

    private LambdaQueryWrapperX<ErpFinanceAssetDO> buildPageQuery(ErpFinanceAssetPageReqVO reqVO) {
        return new LambdaQueryWrapperX<ErpFinanceAssetDO>()
                .likeIfPresent(ErpFinanceAssetDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpFinanceAssetDO::getName, reqVO.getName())
                .likeIfPresent(ErpFinanceAssetDO::getCategoryName, reqVO.getCategoryName())
                .eqIfPresent(ErpFinanceAssetDO::getAssetType, reqVO.getAssetType())
                .eqIfPresent(ErpFinanceAssetDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinanceAssetDO::getId);
    }

    default ErpFinanceAssetDO selectByNo(String no) {
        return selectOne(ErpFinanceAssetDO::getNo, no);
    }

    default ErpFinanceAssetDO selectByCandidateId(Long candidateId) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceAssetDO>()
                .eq(ErpFinanceAssetDO::getCandidateId, candidateId)
                .orderByDesc(ErpFinanceAssetDO::getId)
                .last("LIMIT 1"));
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpFinanceAssetDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinanceAssetDO>()
                .eq(ErpFinanceAssetDO::getId, id)
                .eq(ErpFinanceAssetDO::getStatus, status));
    }
}
