package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
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
        return update(null, new LambdaUpdateWrapper<ErpProductionMaterialDO>()
                .eq(ErpProductionMaterialDO::getId, id)
                .setSql("issued_qty = issued_qty + " + qty.toPlainString()));
    }

    default int updateReturnedQtyIncrement(Long id, BigDecimal qty) {
        return update(null, new LambdaUpdateWrapper<ErpProductionMaterialDO>()
                .eq(ErpProductionMaterialDO::getId, id)
                .setSql("returned_qty = returned_qty + " + qty.toPlainString()));
    }

}
