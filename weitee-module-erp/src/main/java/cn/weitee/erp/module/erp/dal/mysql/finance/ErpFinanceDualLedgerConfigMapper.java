package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpFinanceDualLedgerConfigMapper extends BaseMapperX<ErpFinanceDualLedgerConfigDO> {

    default PageResult<ErpFinanceDualLedgerConfigDO> selectPage(ErpFinanceDualLedgerConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceDualLedgerConfigDO>()
                .eqIfPresent(ErpFinanceDualLedgerConfigDO::getBizType, reqVO.getBizType())
                .eqIfPresent(ErpFinanceDualLedgerConfigDO::getExternalLedgerId, reqVO.getExternalLedgerId())
                .eqIfPresent(ErpFinanceDualLedgerConfigDO::getInternalLedgerId, reqVO.getInternalLedgerId())
                .eqIfPresent(ErpFinanceDualLedgerConfigDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpFinanceDualLedgerConfigDO::getRemark, reqVO.getRemark())
                .orderByAsc(ErpFinanceDualLedgerConfigDO::getBizType)
                .orderByDesc(ErpFinanceDualLedgerConfigDO::getId));
    }

    default ErpFinanceDualLedgerConfigDO selectByBizType(Integer bizType) {
        return selectOne(ErpFinanceDualLedgerConfigDO::getBizType, bizType);
    }

    default List<ErpFinanceDualLedgerConfigDO> selectListByStatus(Integer status) {
        return selectList(ErpFinanceDualLedgerConfigDO::getStatus, status);
    }
}
