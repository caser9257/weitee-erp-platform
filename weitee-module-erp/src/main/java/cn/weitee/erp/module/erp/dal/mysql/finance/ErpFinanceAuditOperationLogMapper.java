package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAuditOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 审计操作日志 Mapper
 */
@Mapper
public interface ErpFinanceAuditOperationLogMapper extends BaseMapperX<ErpFinanceAuditOperationLogDO> {

}
