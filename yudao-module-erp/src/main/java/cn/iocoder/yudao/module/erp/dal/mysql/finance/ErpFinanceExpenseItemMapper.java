package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceExpenseItemMapper extends BaseMapperX<ErpFinanceExpenseItemDO> {

    default List<ErpFinanceExpenseItemDO> selectListByExpenseId(Long expenseId) {
        return selectList(ErpFinanceExpenseItemDO::getExpenseId, expenseId);
    }

    default List<ErpFinanceExpenseItemDO> selectListByExpenseIds(Collection<Long> expenseIds) {
        if (expenseIds == null || expenseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinanceExpenseItemDO>()
                .in(ErpFinanceExpenseItemDO::getExpenseId, expenseIds)
                .orderByAsc(ErpFinanceExpenseItemDO::getId));
    }

}
