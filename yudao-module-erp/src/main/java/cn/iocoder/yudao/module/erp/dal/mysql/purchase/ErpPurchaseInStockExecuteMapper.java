package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpPurchaseInStockExecuteMapper extends BaseMapperX<ErpPurchaseInStockExecuteDO> {

    default List<ErpPurchaseInStockExecuteDO> selectListByPurchaseInId(Long purchaseInId) {
        return selectList(new LambdaQueryWrapper<ErpPurchaseInStockExecuteDO>()
                .eq(ErpPurchaseInStockExecuteDO::getPurchaseInId, purchaseInId)
                .orderByDesc(ErpPurchaseInStockExecuteDO::getCreateTime)
                .orderByDesc(ErpPurchaseInStockExecuteDO::getId));
    }

    default int updateStatusByPurchaseInId(Long purchaseInId, Integer fromStatus, Integer toStatus) {
        return update(null, new LambdaUpdateWrapper<ErpPurchaseInStockExecuteDO>()
                .eq(ErpPurchaseInStockExecuteDO::getPurchaseInId, purchaseInId)
                .eq(ErpPurchaseInStockExecuteDO::getStatus, fromStatus)
                .set(ErpPurchaseInStockExecuteDO::getStatus, toStatus));
    }

    default ErpPurchaseInStockExecuteDO selectLatestExecutedByPurchaseInId(Long purchaseInId, Integer executedStatus) {
        return selectOne(new LambdaQueryWrapper<ErpPurchaseInStockExecuteDO>()
                .eq(ErpPurchaseInStockExecuteDO::getPurchaseInId, purchaseInId)
                .eq(ErpPurchaseInStockExecuteDO::getStatus, executedStatus)
                .orderByDesc(ErpPurchaseInStockExecuteDO::getCreateTime)
                .orderByDesc(ErpPurchaseInStockExecuteDO::getId)
                .last("LIMIT 1"));
    }

}
