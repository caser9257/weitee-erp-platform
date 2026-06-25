package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpPurchaseInStockExecuteItemMapper extends BaseMapperX<ErpPurchaseInStockExecuteItemDO> {

    default List<ErpPurchaseInStockExecuteItemDO> selectListByExecuteIds(Collection<Long> executeIds) {
        if (CollUtil.isEmpty(executeIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpPurchaseInStockExecuteItemDO>()
                .in(ErpPurchaseInStockExecuteItemDO::getExecuteId, executeIds));
    }

    default List<ErpPurchaseInStockExecuteItemDO> selectListByPurchaseInId(Long purchaseInId) {
        return selectList(ErpPurchaseInStockExecuteItemDO::getPurchaseInId, purchaseInId);
    }

}
