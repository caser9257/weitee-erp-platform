package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.ErpMrpPlanPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpMrpPlanMapper extends BaseMapperX<ErpMrpPlanDO> {

    default PageResult<ErpMrpPlanDO> selectPage(ErpMrpPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpMrpPlanDO>()
                .likeIfPresent(ErpMrpPlanDO::getPlanNo, reqVO.getPlanNo())
                .likeIfPresent(ErpMrpPlanDO::getPlanName, reqVO.getPlanName())
                .betweenIfPresent(ErpMrpPlanDO::getPlanStartDate, reqVO.getPlanDate())
                .eqIfPresent(ErpMrpPlanDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpMrpPlanDO::getId));
    }

    default ErpMrpPlanDO selectByPlanNo(String planNo) {
        return selectOne(ErpMrpPlanDO::getPlanNo, planNo);
    }

}
