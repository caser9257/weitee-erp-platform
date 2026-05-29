package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceLossDetailDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpOutsourceLossDetailMapper extends BaseMapperX<ErpOutsourceLossDetailDO> {

    default List<ErpOutsourceLossDetailDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapper<ErpOutsourceLossDetailDO>()
                .eq(ErpOutsourceLossDetailDO::getOrderId, orderId)
                .orderByAsc(ErpOutsourceLossDetailDO::getId));
    }

    default List<ErpOutsourceLossDetailDO> selectListByIssueBatchIds(Collection<Long> issueBatchIds) {
        if (CollUtil.isEmpty(issueBatchIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpOutsourceLossDetailDO>()
                .in(ErpOutsourceLossDetailDO::getIssueBatchId, issueBatchIds)
                .orderByAsc(ErpOutsourceLossDetailDO::getId));
    }

}
