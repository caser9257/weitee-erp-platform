package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProductionReportMapper extends BaseMapperX<ErpProductionReportDO> {

    default PageResult<ErpProductionReportDO> selectPage(ErpProductionReportPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionReportDO>()
                .likeIfPresent(ErpProductionReportDO::getReportNo, reqVO.getReportNo())
                .eqIfPresent(ErpProductionReportDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionReportDO::getReportType, reqVO.getReportType())
                .betweenIfPresent(ErpProductionReportDO::getReportDate, reqVO.getReportDate())
                .orderByDesc(ErpProductionReportDO::getId));
    }

    default List<ErpProductionReportDO> selectListByProductionOrderId(Long productionOrderId) {
        return selectList(Wrappers.<ErpProductionReportDO>lambdaQuery()
                .eq(ErpProductionReportDO::getProductionOrderId, productionOrderId)
                .orderByDesc(ErpProductionReportDO::getReportDate));
    }
}
