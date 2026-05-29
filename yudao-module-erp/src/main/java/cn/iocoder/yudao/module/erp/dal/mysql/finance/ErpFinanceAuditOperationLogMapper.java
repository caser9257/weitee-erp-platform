package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAuditOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 审计操作日志 Mapper
 */
@Mapper
public interface ErpFinanceAuditOperationLogMapper extends BaseMapperX<ErpFinanceAuditOperationLogDO> {

}
