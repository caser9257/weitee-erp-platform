package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceReportItemSubjectMapper extends BaseMapperX<ErpFinanceReportItemSubjectDO> {

    default List<ErpFinanceReportItemSubjectDO> selectListByItemId(Long itemId) {
        List<ErpFinanceReportItemSubjectDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceReportItemSubjectDO>()
                .eq(ErpFinanceReportItemSubjectDO::getItemId, itemId)
                .orderByAsc(ErpFinanceReportItemSubjectDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpFinanceReportItemSubjectDO> selectListByItemIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ErpFinanceReportItemSubjectDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceReportItemSubjectDO>()
                .in(ErpFinanceReportItemSubjectDO::getItemId, itemIds)
                .orderByAsc(ErpFinanceReportItemSubjectDO::getItemId)
                .orderByAsc(ErpFinanceReportItemSubjectDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpFinanceReportItemSubjectDO> selectListBySubjectCode(String subjectCode) {
        List<ErpFinanceReportItemSubjectDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceReportItemSubjectDO>()
                .eq(ErpFinanceReportItemSubjectDO::getSubjectCode, subjectCode)
                .orderByAsc(ErpFinanceReportItemSubjectDO::getItemId)
                .orderByAsc(ErpFinanceReportItemSubjectDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default Long selectCountBySubjectCode(String subjectCode) {
        return selectCount(new LambdaQueryWrapperX<ErpFinanceReportItemSubjectDO>()
                .eq(ErpFinanceReportItemSubjectDO::getSubjectCode, subjectCode));
    }

    default void deleteByItemId(Long itemId) {
        delete(new LambdaQueryWrapper<ErpFinanceReportItemSubjectDO>()
                .eq(ErpFinanceReportItemSubjectDO::getItemId, itemId));
    }

}
