package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpFinanceVoucherEntryMapper extends BaseMapperX<ErpFinanceVoucherEntryDO> {

    default List<ErpFinanceVoucherEntryDO> selectListByVoucherId(Long voucherId) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherEntryDO>()
                .eq(ErpFinanceVoucherEntryDO::getVoucherId, voucherId)
                .orderByAsc(ErpFinanceVoucherEntryDO::getEntryNo)
                .orderByAsc(ErpFinanceVoucherEntryDO::getId));
    }

    default List<ErpFinanceVoucherEntryDO> selectListByVoucherIds(Collection<Long> voucherIds) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherEntryDO>()
                .in(ErpFinanceVoucherEntryDO::getVoucherId, voucherIds)
                .orderByAsc(ErpFinanceVoucherEntryDO::getEntryNo)
                .orderByAsc(ErpFinanceVoucherEntryDO::getId));
    }

    default List<ErpFinanceVoucherEntryDO> selectListByVoucherIdsAndSubjectCode(Collection<Long> voucherIds, String subjectCode) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherEntryDO>()
                .in(ErpFinanceVoucherEntryDO::getVoucherId, voucherIds)
                .eq(ErpFinanceVoucherEntryDO::getSubjectCode, subjectCode)
                .orderByAsc(ErpFinanceVoucherEntryDO::getVoucherId)
                .orderByAsc(ErpFinanceVoucherEntryDO::getEntryNo)
                .orderByAsc(ErpFinanceVoucherEntryDO::getId));
    }
}
