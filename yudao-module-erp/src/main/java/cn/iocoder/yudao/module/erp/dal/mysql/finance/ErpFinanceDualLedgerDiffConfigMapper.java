package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpFinanceDualLedgerDiffConfigMapper extends BaseMapperX<ErpFinanceDualLedgerDiffConfigDO> {

    default PageResult<ErpFinanceDualLedgerDiffConfigDO> selectPage(ErpFinanceDualLedgerDiffConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceDualLedgerDiffConfigDO>()
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getBizType, reqVO.getBizType())
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getDiffItemType, reqVO.getDiffItemType())
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getExternalSourceType, reqVO.getExternalSourceType())
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getInternalSourceType, reqVO.getInternalSourceType())
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpFinanceDualLedgerDiffConfigDO::getRemark, reqVO.getRemark())
                .orderByAsc(ErpFinanceDualLedgerDiffConfigDO::getBizType)
                .orderByAsc(ErpFinanceDualLedgerDiffConfigDO::getDiffItemType)
                .orderByDesc(ErpFinanceDualLedgerDiffConfigDO::getId));
    }

    default ErpFinanceDualLedgerDiffConfigDO selectByBizTypeAndDiffItemType(Integer bizType, Integer diffItemType) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceDualLedgerDiffConfigDO>()
                .eq(ErpFinanceDualLedgerDiffConfigDO::getBizType, bizType)
                .eq(ErpFinanceDualLedgerDiffConfigDO::getDiffItemType, diffItemType));
    }

    default List<ErpFinanceDualLedgerDiffConfigDO> selectListByBizTypeAndStatus(Integer bizType, Integer status) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceDualLedgerDiffConfigDO>()
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getBizType, bizType)
                .eqIfPresent(ErpFinanceDualLedgerDiffConfigDO::getStatus, status)
                .orderByAsc(ErpFinanceDualLedgerDiffConfigDO::getDiffItemType)
                .orderByAsc(ErpFinanceDualLedgerDiffConfigDO::getId));
    }

}
