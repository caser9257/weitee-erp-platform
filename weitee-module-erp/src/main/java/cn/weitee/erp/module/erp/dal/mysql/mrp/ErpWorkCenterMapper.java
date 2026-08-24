package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpWorkCenterMapper extends BaseMapperX<ErpWorkCenterDO> {

    default PageResult<ErpWorkCenterDO> selectPage(ErpWorkCenterPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpWorkCenterDO>()
                .likeIfPresent(ErpWorkCenterDO::getCenterCode, reqVO.getCenterCode())
                .likeIfPresent(ErpWorkCenterDO::getCenterName, reqVO.getCenterName())
                .eqIfPresent(ErpWorkCenterDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpWorkCenterDO::getId));
    }

    default ErpWorkCenterDO selectByCenterCode(String centerCode) {
        return selectOne(ErpWorkCenterDO::getCenterCode, centerCode);
    }

    default List<ErpWorkCenterDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<ErpWorkCenterDO>()
                .eq(ErpWorkCenterDO::getStatus, 1)
                .orderByAsc(ErpWorkCenterDO::getCenterCode));
    }
}
