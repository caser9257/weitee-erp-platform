package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleSubjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceRoleSubjectMapper extends BaseMapperX<ErpFinanceRoleSubjectDO> {

    default List<ErpFinanceRoleSubjectDO> selectListByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinanceRoleSubjectDO>()
                .in(ErpFinanceRoleSubjectDO::getRoleId, roleIds)
                .eq(ErpFinanceRoleSubjectDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
    }
}
