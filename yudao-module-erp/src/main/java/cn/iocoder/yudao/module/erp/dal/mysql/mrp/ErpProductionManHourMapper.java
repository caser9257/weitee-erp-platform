package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProductionManHourMapper extends BaseMapperX<ErpProductionManHourDO> {

    default PageResult<ErpProductionManHourDO> selectPage(ErpProductionManHourPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionManHourDO>()
                .eqIfPresent(ErpProductionManHourDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionManHourDO::getAccountingMonth, reqVO.getAccountingMonth())
                .betweenIfPresent(ErpProductionManHourDO::getWorkDate, reqVO.getWorkDate())
                .orderByDesc(ErpProductionManHourDO::getId));
    }

    default List<ErpProductionManHourDO> selectListByAccountingMonth(String accountingMonth) {
        return selectList(new LambdaQueryWrapperX<ErpProductionManHourDO>()
                .eq(ErpProductionManHourDO::getAccountingMonth, accountingMonth)
                .orderByAsc(ErpProductionManHourDO::getProductionOrderId, ErpProductionManHourDO::getId));
    }

}
