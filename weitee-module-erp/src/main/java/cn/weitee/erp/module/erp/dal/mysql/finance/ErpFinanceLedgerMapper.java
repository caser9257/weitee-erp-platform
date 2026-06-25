package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpFinanceLedgerMapper extends BaseMapperX<ErpFinanceLedgerDO> {

    default PageResult<ErpFinanceLedgerDO> selectPage(ErpFinanceLedgerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceLedgerDO>()
                .likeIfPresent(ErpFinanceLedgerDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpFinanceLedgerDO::getName, reqVO.getName())
                .eqIfPresent(ErpFinanceLedgerDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpFinanceLedgerDO::getDefaultStatus, reqVO.getDefaultStatus())
                .likeIfPresent(ErpFinanceLedgerDO::getRemark, reqVO.getRemark())
                .orderByDesc(ErpFinanceLedgerDO::getDefaultStatus)
                .orderByDesc(ErpFinanceLedgerDO::getSort)
                .orderByDesc(ErpFinanceLedgerDO::getId));
    }

    default ErpFinanceLedgerDO selectByNo(String no) {
        return selectOne(ErpFinanceLedgerDO::getNo, no);
    }

    default ErpFinanceLedgerDO selectByDefaultStatus() {
        return selectOne(ErpFinanceLedgerDO::getDefaultStatus, true);
    }

    default List<ErpFinanceLedgerDO> selectListByStatus(Integer status) {
        return selectList(ErpFinanceLedgerDO::getStatus, status);
    }
}
