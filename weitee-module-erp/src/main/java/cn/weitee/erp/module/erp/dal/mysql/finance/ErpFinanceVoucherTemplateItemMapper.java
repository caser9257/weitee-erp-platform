package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpFinanceVoucherTemplateItemMapper extends BaseMapperX<ErpFinanceVoucherTemplateItemDO> {

    default List<ErpFinanceVoucherTemplateItemDO> selectListByTemplateId(Long templateId) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherTemplateItemDO>()
                .eq(ErpFinanceVoucherTemplateItemDO::getTemplateId, templateId)
                .orderByAsc(ErpFinanceVoucherTemplateItemDO::getEntryNo)
                .orderByAsc(ErpFinanceVoucherTemplateItemDO::getId));
    }

    default List<ErpFinanceVoucherTemplateItemDO> selectListByTemplateIds(Collection<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherTemplateItemDO>()
                .in(ErpFinanceVoucherTemplateItemDO::getTemplateId, templateIds)
                .orderByAsc(ErpFinanceVoucherTemplateItemDO::getEntryNo)
                .orderByAsc(ErpFinanceVoucherTemplateItemDO::getId));
    }
}
