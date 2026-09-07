package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductionMaterialMapper extends BaseMapperX<ErpProductionMaterialDO> {

    default List<ErpProductionMaterialDO> selectListByProductionOrderId(Long productionOrderId) {
        return selectList(ErpProductionMaterialDO::getProductionOrderId, productionOrderId);
    }

    default List<ErpProductionMaterialDO> selectListByIds(Collection<Long> ids) {
        return selectByIds(ids);
    }

    default int updateIssuedQtyIncrement(Long id, BigDecimal qty) {
        // 净额口径守卫：与 calculateRemainingIssueQty（required - (issued - returned)）一致，
        // 允许退料后重新领料，同时拒绝真实超量并发
        return update(null, new LambdaUpdateWrapper<ErpProductionMaterialDO>()
                .eq(ErpProductionMaterialDO::getId, id)
                .apply("issued_qty - returned_qty + {0} <= required_qty", qty)
                .setSql("issued_qty = issued_qty + " + qty.toPlainString()));
    }

    default int updateReturnedQtyIncrement(Long id, BigDecimal qty) {
        return update(null, new LambdaUpdateWrapper<ErpProductionMaterialDO>()
                .eq(ErpProductionMaterialDO::getId, id)
                .apply("returned_qty + {0} <= issued_qty", qty)
                .setSql("returned_qty = returned_qty + " + qty.toPlainString()));
    }

}
