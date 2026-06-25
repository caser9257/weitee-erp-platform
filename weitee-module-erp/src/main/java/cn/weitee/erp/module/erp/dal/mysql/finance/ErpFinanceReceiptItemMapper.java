package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptItemDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 收款单项 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpFinanceReceiptItemMapper extends BaseMapperX<ErpFinanceReceiptItemDO> {

    default List<ErpFinanceReceiptItemDO> selectListByReceiptId(Long receiptId) {
        return selectList(ErpFinanceReceiptItemDO::getReceiptId, receiptId);
    }

    default List<ErpFinanceReceiptItemDO> selectListByReceiptIds(Collection<Long> receiptIds) {
        return selectList(ErpFinanceReceiptItemDO::getReceiptId, receiptIds);
    }

    default BigDecimal selectReceiptPriceSumByBizIdAndBizType(Long bizId, Integer bizType) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpFinanceReceiptItemDO>()
                .select("SUM(receipt_price) AS receipt_price_sum")
                .eq("biz_id", bizId)
                .eq("biz_type", bizType)
                .inSql("receipt_id", "SELECT id FROM erp_finance_receipt WHERE status = "
                        + ErpAuditStatus.APPROVE.getStatus() + " AND deleted = 0"));
        // 获得数量
        if (CollUtil.isEmpty(result)) {
            return BigDecimal.ZERO;
        }
        Object sum = result.get(0).get("receipt_price_sum");
        if (sum == null) {
            return BigDecimal.ZERO;
        }
        if (sum instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(sum.toString());
    }

}
