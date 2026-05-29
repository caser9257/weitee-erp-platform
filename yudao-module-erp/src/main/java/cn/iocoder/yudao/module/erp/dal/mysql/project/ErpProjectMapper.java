package cn.iocoder.yudao.module.erp.dal.mysql.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 项目 Mapper
 */
@Mapper
public interface ErpProjectMapper extends BaseMapperX<ErpProjectDO> {

    default PageResult<ErpProjectDO> selectPage(ErpProjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProjectDO>()
                .likeIfPresent(ErpProjectDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpProjectDO::getName, reqVO.getName())
                .eqIfPresent(ErpProjectDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ErpProjectDO::getProjectType, reqVO.getProjectType())
                .eqIfPresent(ErpProjectDO::getBusinessType, reqVO.getBusinessType())
                .eqIfPresent(ErpProjectDO::getCurrentStageCode, reqVO.getCurrentStageCode())
                .eqIfPresent(ErpProjectDO::getPlanCoordinatorId, reqVO.getPlanCoordinatorId())
                .eqIfPresent(ErpProjectDO::getMaterialControllerId, reqVO.getMaterialControllerId())
                .eqIfPresent(ErpProjectDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpProjectDO::getId));
    }

    default ErpProjectDO selectByNo(String no) {
        return selectOne(ErpProjectDO::getNo, no);
    }

    default List<ErpProjectDO> selectListByStatus(Integer status) {
        return selectList(ErpProjectDO::getStatus, status);
    }

}
