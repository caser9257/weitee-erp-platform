package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 应收台账明细 Mapper
 *
 * @author system
 */
@Mapper
public interface ErpArStatementItemMapper extends BaseMapperX<ErpArStatementItemDO> {

    /**
     * 根据台账ID查询明细列表
     */
    default List<ErpArStatementItemDO> selectListByStatementId(Long statementId) {
        return selectList(ErpArStatementItemDO::getStatementId, statementId);
    }

    /**
     * 根据台账ID列表查询明细列表
     */
    default List<ErpArStatementItemDO> selectListByStatementIds(List<Long> statementIds) {
        return selectList(ErpArStatementItemDO::getStatementId, statementIds);
    }

    /**
     * 根据台账ID删除明细
     */
    default int deleteByStatementId(Long statementId) {
        return delete(ErpArStatementItemDO::getStatementId, statementId);
    }

}
