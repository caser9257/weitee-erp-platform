package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerRoleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ERP 账簿角色关联 Mapper
 */
@Mapper
public interface ErpFinanceLedgerRoleMapper extends BaseMapperX<ErpFinanceLedgerRoleDO> {

    /**
     * 查询指定角色关联的账簿ID列表
     */
    default List<Long> selectLedgerIdsByRoleId(Long roleId) {
        List<ErpFinanceLedgerRoleDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceLedgerRoleDO>()
                .eq(ErpFinanceLedgerRoleDO::getRoleId, roleId)
                .eq(ErpFinanceLedgerRoleDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .select(ErpFinanceLedgerRoleDO::getLedgerId));
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(ErpFinanceLedgerRoleDO::getLedgerId).collect(Collectors.toList());
    }

    /**
     * 查询指定角色集合关联的账簿ID列表
     */
    default List<Long> selectLedgerIdsByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ErpFinanceLedgerRoleDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceLedgerRoleDO>()
                .in(ErpFinanceLedgerRoleDO::getRoleId, roleIds)
                .eq(ErpFinanceLedgerRoleDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .select(ErpFinanceLedgerRoleDO::getLedgerId));
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(ErpFinanceLedgerRoleDO::getLedgerId).distinct().collect(Collectors.toList());
    }

    /**
     * 查询指定账簿关联的角色ID列表
     */
    default List<Long> selectRoleIdsByLedgerId(Long ledgerId) {
        List<ErpFinanceLedgerRoleDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceLedgerRoleDO>()
                .eq(ErpFinanceLedgerRoleDO::getLedgerId, ledgerId)
                .eq(ErpFinanceLedgerRoleDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .select(ErpFinanceLedgerRoleDO::getRoleId));
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(ErpFinanceLedgerRoleDO::getRoleId).collect(Collectors.toList());
    }

}
