package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpQcDefectReasonDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IQC 不良原因主数据 Mapper
 */
@Mapper
public interface ErpQcDefectReasonMapper extends BaseMapperX<ErpQcDefectReasonDO> {

    default List<ErpQcDefectReasonDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ErpQcDefectReasonDO>()
                .eqIfPresent(ErpQcDefectReasonDO::getStatus, status)
                .orderByAsc(ErpQcDefectReasonDO::getSort)
                .orderByAsc(ErpQcDefectReasonDO::getId));
    }

}
