package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpApStatementItemMapper extends BaseMapperX<ErpApStatementItemDO> {

    default List<ErpApStatementItemDO> selectListByStatementId(Long statementId) {
        return selectList(ErpApStatementItemDO::getStatementId, statementId);
    }

    default List<ErpApStatementItemDO> selectListByStatementIds(Collection<Long> statementIds) {
        if (statementIds == null || statementIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(ErpApStatementItemDO::getStatementId, statementIds);
    }

}
