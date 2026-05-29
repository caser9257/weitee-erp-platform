package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpBomItemSubstituteMapper extends BaseMapperX<ErpBomItemSubstituteDO> {

    default PageResult<ErpBomItemSubstituteDO> selectPage(ErpBomItemSubstitutePageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO));
    }

    static LambdaQueryWrapperX<ErpBomItemSubstituteDO> buildPageQuery(ErpBomItemSubstitutePageReqVO reqVO) {
        return new LambdaQueryWrapperX<ErpBomItemSubstituteDO>()
                .inIfPresent(ErpBomItemSubstituteDO::getBomItemId, reqVO.getBomItemIds())
                .eqIfPresent(ErpBomItemSubstituteDO::getSubstituteMaterialId, reqVO.getSubstituteMaterialId())
                .eqIfPresent(ErpBomItemSubstituteDO::getEnableAutoRecommend, reqVO.getEnableAutoRecommend())
                .orderByDesc(ErpBomItemSubstituteDO::getId);
    }

    default List<ErpBomItemSubstituteDO> selectListByBomItemIds(Collection<Long> bomItemIds) {
        return selectList(ErpBomItemSubstituteDO::getBomItemId, bomItemIds);
    }

    default int deleteByBomItemIds(Collection<Long> bomItemIds) {
        return deleteBatch(ErpBomItemSubstituteDO::getBomItemId, bomItemIds);
    }

}
