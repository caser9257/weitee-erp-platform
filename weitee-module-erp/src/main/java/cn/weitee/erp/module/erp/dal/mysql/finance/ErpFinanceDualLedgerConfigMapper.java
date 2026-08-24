package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpFinanceDualLedgerConfigMapper extends BaseMapperX<ErpFinanceDualLedgerConfigDO> {

    default PageResult<ErpFinanceDualLedgerConfigDO> selectPage(ErpFinanceDualLedgerConfigPageReqVO reqVO) {
        return selectPage(reqVO, null);
    }

    default PageResult<ErpFinanceDualLedgerConfigDO> selectPageByVisibleLedgerIds(
            ErpFinanceDualLedgerConfigPageReqVO reqVO, Collection<Long> visibleLedgerIds) {
        if (visibleLedgerIds != null && visibleLedgerIds.isEmpty()) {
            return PageResult.empty(0L);
        }
        LambdaQueryWrapperX<ErpFinanceDualLedgerConfigDO> query = new LambdaQueryWrapperX<>();
        query.eqIfPresent(ErpFinanceDualLedgerConfigDO::getBizType, reqVO.getBizType());
        query.eqIfPresent(ErpFinanceDualLedgerConfigDO::getExternalLedgerId, reqVO.getExternalLedgerId());
        query.eqIfPresent(ErpFinanceDualLedgerConfigDO::getInternalLedgerId, reqVO.getInternalLedgerId());
        query.eqIfPresent(ErpFinanceDualLedgerConfigDO::getStatus, reqVO.getStatus());
        query.likeIfPresent(ErpFinanceDualLedgerConfigDO::getRemark, reqVO.getRemark());
        query.orderByAsc(ErpFinanceDualLedgerConfigDO::getBizType);
        query.orderByDesc(ErpFinanceDualLedgerConfigDO::getId);
        if (visibleLedgerIds != null) {
            query.in(ErpFinanceDualLedgerConfigDO::getExternalLedgerId, visibleLedgerIds)
                    .in(ErpFinanceDualLedgerConfigDO::getInternalLedgerId, visibleLedgerIds);
        }
        return selectPage(reqVO, query);
    }

    default ErpFinanceDualLedgerConfigDO selectByBizType(Integer bizType) {
        return selectOne(ErpFinanceDualLedgerConfigDO::getBizType, bizType);
    }

    default List<ErpFinanceDualLedgerConfigDO> selectListByStatus(Integer status) {
        return selectList(ErpFinanceDualLedgerConfigDO::getStatus, status);
    }
}
